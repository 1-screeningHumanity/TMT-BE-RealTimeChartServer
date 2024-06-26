package tmt.realtimechartservice.chart.service;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tmt.realtimechartservice.common.KisUrls;

@Slf4j
@Service
public class KisSocketServiceImp implements KisSocketService {

	private final ReactorNettyWebSocketClient client;
	@Autowired
	private ReactiveRedisService reactiveRedisService;

	@Autowired
	private KafkaProducerService kafkaProducerService;
	private final AtomicReference<WebSocketSession> webSocketSessionRefPrice = new AtomicReference<>();
	private final AtomicReference<WebSocketSession> webSocketSessionRefAskPrice = new AtomicReference<>();

	@Value("${kis.key.socketKey}")
	private String socketKey;

	// 실시간 주식에 사용하는 상위 20개 코드 리스트
	private final List<String> stockCodes = List.of(
			"005930", "000660", "373220", "207940", "005380",
			"005935", "000270", "068270", "005490", "105560",
			"035420", "006400", "051910", "028260", "055550",
			"012330", "003670", "035720", "247540", "009830");

	public KisSocketServiceImp() {
		this.client = new ReactorNettyWebSocketClient();
	}

	@Override
	@Scheduled(cron = "0 0 9 * * MON-FRI")
	public void startPrice() {
		log.info("Start sendMessageToWebSocketServerToRealTimePrice");
		if (webSocketSessionRefPrice.get() == null) {
			sendMessageToWebSocketServerToRealTimePrice().subscribe();
		}
	}

	@Override
	@Scheduled(cron = "0 35 15 * * MON-FRI")
	public void stopPrice() {
		log.info("Stop sendMessageToWebSocketServerToRealTimePrice");
		WebSocketSession session = webSocketSessionRefPrice.getAndSet(null);
		if (session != null && session.isOpen()) {
			session.close().subscribe();
		}
	}

	@Override
	@Scheduled(cron = "0 55 8 * * MON-FRI")
	public void startAskPrice() {
		log.info("Start sendMessageToWebSocketServerToAskingPrice");
		if (webSocketSessionRefAskPrice.get() == null) {
			sendMessageToWebSocketServerToAskingPrice().subscribe();
		}
	}

	@Override
	@Scheduled(cron = "0 35 15 * * MON-FRI")
	public void stopAskPrice() {
		log.info("Stop sendMessageToWebSocketServerToAskingPrice");
		WebSocketSession session = webSocketSessionRefAskPrice.getAndSet(null);
		if (session != null && session.isOpen()) {
			session.close().subscribe();
		}
	}

	@Override
	public Mono<Void> sendMessageToWebSocketServerToRealTimePrice() {
		URI uri = UriComponentsBuilder.fromUriString(
				KisUrls.REAL_TIME_EXECUTION_PRICE_PATH.getFullUrl()).build().toUri();

		return client.execute(uri, session -> {
			webSocketSessionRefPrice.set(session);  // 세션 저장

			// JSON 메시지 생성
			List<String> messages = createRealPriceMessages();

			Flux<WebSocketMessage> message = Flux.fromIterable(messages)
					.map(session::textMessage);

			return session.send(message)
					.thenMany(session.receive()
							.doOnNext(data -> {
								String receivedMessage = data.getPayloadAsText();
								String[] parseReceivedMessage = receivedMessage.split("\\^");

								// 데이터가 없는 경우
								if (parseReceivedMessage.length < 2) {
									return;
								}

								String stockCode = parseReceivedMessage[0].split("\\|")[3];

								// stck_prpr + ":" + prdy_ctrt + ":" + stck_oprc + ":" + stck_hgpr + ":" + stck_lwpr
								// 주식 현재가 : 전일 대비율 : 주식 시가 : 주식 고가 : 주식 저가
								String stockInfo = String.format("%s:%s:%s:%s:%s",
										parseReceivedMessage[2],
										parseReceivedMessage[5],
										parseReceivedMessage[7],
										parseReceivedMessage[8],
										parseReceivedMessage[9]);

                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS");
                                String formattedDateTime = LocalDateTime.now().format(formatter);

								// Trade 카프카 전송
								kafkaProducerService.sendTrade("{\n"
										+ "  \"stockCode\":\"" + stockCode + "\",\n"
										+ "  \"price\":\"" + parseReceivedMessage[2] + "\",\n"
										+ "  \"date\":\"" + formattedDateTime + "\"\n"
										+ "}");

								reactiveRedisService.save(stockCode, stockInfo).subscribe();
							}))
					.then()
					.doFinally(signalType -> webSocketSessionRefPrice.set(null));  // 종료 시 세션 null로 설정
		});
	}

	@Override
	public Mono<Void> sendMessageToWebSocketServerToAskingPrice() {
		URI uri = UriComponentsBuilder.fromUriString(
				KisUrls.REAL_TIME_ASKING_PRICE_PATH.getRealFullUrl()).build().toUri();

		return client.execute(uri, session -> {
			webSocketSessionRefAskPrice.set(session);  // 세션 저장

			// JSON 메시지 생성
			List<String> messages = createAskPriceJsonMessages();

			Flux<WebSocketMessage> message = Flux.fromIterable(messages)
					.map(session::textMessage);

			return session.send(message)
					.thenMany(session.receive()
							.doOnNext(data -> {
								String receivedMessage = data.getPayloadAsText();
								String[] parseReceivedMessage = receivedMessage.split("\\^");

								// 데이터가 없는 경우
								if (parseReceivedMessage.length < 2) {
									return;
								}

								String stockCode = parseReceivedMessage[0].split("\\|")[3];

								String askPriceInfo = String.format(
										"%s:%s:%s:%s:%s:%s:%s:%s:%s:%s:%s:%s:%s:%s",
										parseReceivedMessage[3],    // 매도 호가 1
										parseReceivedMessage[4],    // 매도 호가 2
										parseReceivedMessage[5],    // 매도 호가 3
										parseReceivedMessage[23],   // 매도 호가 1 잔량
										parseReceivedMessage[24],   // 매도 호가 2 잔량
										parseReceivedMessage[25],   // 매도 호가 3 잔량
										parseReceivedMessage[13],   // 매수 호가 1
										parseReceivedMessage[14],   // 매수 호가 2
										parseReceivedMessage[15],   // 매수 호가 3
										parseReceivedMessage[33],   // 매수 호가 1 잔량
										parseReceivedMessage[34],   // 매수 호가 2 잔량
										parseReceivedMessage[35],   // 매수 호가 3 잔량
										parseReceivedMessage[43],   // 총 매도 호가 잔량
										parseReceivedMessage[44]    // 총 매수 호가 잔량
								);

								reactiveRedisService.save("askPrice-" + stockCode, askPriceInfo).subscribe();
							}))
					.then()
					.doFinally(signalType -> webSocketSessionRefAskPrice.set(null));  // 종료 시 세션 null로 설정
		});
	}

	private List<String> createRealPriceMessages() {
		// 실시간 주식 리스트 20개
		return stockCodes.stream()
				.map(this::createJsonMessageToRealTimePrice)
				.toList();
	}

	private List<String> createAskPriceJsonMessages() {
		// 실시간 주식 리스트 20개
		return stockCodes.stream()
				.map(this::crateJsonMessageToAskingPrice)
				.toList();
	}

	private String crateJsonMessageToAskingPrice(String stockCode) {
		return "{\n"
				+ "         \"header\":\n"
				+ "         {\n"
				+ "                  \"approval_key\": \"" + socketKey + "\",\n"
				+ "                  \"custtype\":\"P\",\n"
				+ "                  \"tr_type\":\"1\",\n"
				+ "                  \"content-type\":\"utf-8\"\n"
				+ "         },\n"
				+ "         \"body\":\n"
				+ "         {\n"
				+ "                  \"input\":\n"
				+ "                  {\n"
				+ "                           \"tr_id\":\"H0STASP0\",\n"
				+ "                           \"tr_key\":\"" + stockCode + "\"\n"
				+ "                  }\n"
				+ "         }\n"
				+ "}";
	}

	private String createJsonMessageToRealTimePrice(String stockCode) {
		return "{\n"
				+ "         \"header\":\n"
				+ "         {\n"
				+ "                  \"approval_key\": \"" + socketKey + "\",\n"
				+ "                  \"custtype\":\"P\",\n"
				+ "                  \"tr_type\":\"1\",\n"
				+ "                  \"content-type\":\"utf-8\"\n"
				+ "         },\n"
				+ "         \"body\":\n"
				+ "         {\n"
				+ "                  \"input\":\n"
				+ "                  {\n"
				+ "                           \"tr_id\":\"H0STCNT0\",\n"
				+ "                           \"tr_key\":\"" + stockCode + "\"\n"
				+ "                  }\n"
				+ "         }\n"
				+ "}";
	}
}
