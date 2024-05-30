package tmt.realtimechartservice.chart.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tmt.realtimechartservice.common.KisUrls;

@Service
public class KisSocketServiceImp implements KisSocketService {

	private final ReactorNettyWebSocketClient client;
	@Autowired
	private ReactiveRedisService reactiveRedisService;

	@Value("${kis.key.socketKey}")
	private String socketKey;

	public KisSocketServiceImp() {
		this.client = new ReactorNettyWebSocketClient();
	}

	@Override
	public Mono<Void> sendMessageToWebSocketServer() {
		URI uri = UriComponentsBuilder.fromUriString(
				KisUrls.REAL_TIME_EXECUTION_PRICE_PATH.getFullUrl()).build().toUri();

		return client.execute(uri, session -> {
			// JSON 메시지 생성
			List<String> messages = createMessages();

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
								// 주식 현자가 : 전일 대비율 : 주식 시가 : 주식 고가 : 주식 저가
								String stockInfo = String.format("%s:%s:%s:%s:%s",
										parseReceivedMessage[2],
										parseReceivedMessage[5],
										parseReceivedMessage[7],
										parseReceivedMessage[8],
										parseReceivedMessage[9]);

								reactiveRedisService.save(stockCode, stockInfo).subscribe();

							}))
					.then();
		});
	}

	private List<String> createMessages() {
		// 실시간 주식 리스트 20개
		List<String> stockCodes = List.of(
				"005930", "000660", "373220", "207940", "005380",
				"005935", "000270", "068270", "005490", "105560",
				"035420", "006400", "051910", "028260", "055550",
				"012330", "003670", "035720", "247540", "009830",
				"086790", "000810", "010950", "011170", "034730");
		List<String> messages = new ArrayList<>();

		for (String stockCode : stockCodes) {
			messages.add(getJsonMessage(stockCode));
		}

		return messages;
	}

	private String getJsonMessage(String stockCode) {
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
