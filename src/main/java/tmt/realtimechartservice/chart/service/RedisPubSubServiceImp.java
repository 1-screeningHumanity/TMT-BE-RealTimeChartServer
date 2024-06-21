package tmt.realtimechartservice.chart.service;

import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.ReactiveRedisMessageListenerContainer;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@Slf4j
@Service
public class RedisPubSubServiceImp implements RedisPubSubService {

	private final Sinks.Many<String> sink;

	@Autowired
	private ReactiveRedisService reactiveRedisService;

	public RedisPubSubServiceImp(ReactiveRedisMessageListenerContainer listenerContainer) {
		this.sink = Sinks.many().multicast().directAllOrNothing();

		ChannelTopic reactiveStock = new ChannelTopic("reactive_stock");

		listenerContainer.receive(reactiveStock)
				.map(message -> (String) message.getMessage())
				.subscribe(sink::tryEmitNext);
	}

	@Override
	public Flux<String> getRealTimePrice(String stockCode, ServerWebExchange exchange) {
		return reactiveRedisService.getPrice(stockCode)
				.concatWith(sink.asFlux()
						.filter(message -> message.startsWith("stock:" + stockCode))
						.mergeWith(Flux.interval(Duration.ofSeconds(10))
								.map(tick -> "keep-alive")
						)
						.doOnError(error -> log.warn("Error in getRealTimePrice stream: {}",
								error.getMessage(), error))
						.doOnCancel(() -> log.info("Price Client disconnected2"))
				)
				.doOnCancel(() -> log.info("Price Client disconnected1"))
				.doFinally(signalType -> log.info("Stream finally closed with signal type: {}", signalType))
	}

	@Override
	public Flux<String> getAskPrice(String stockCode, ServerWebExchange exchange) {
		return reactiveRedisService.getAskPrice(stockCode)
				.concatWith(sink.asFlux()
						.filter(message -> message.startsWith("stock:askPrice-" + stockCode))
						.mergeWith(Flux.interval(Duration.ofSeconds(10))
								.map(tick -> "keep-alive"))
						.doOnError(error -> log.warn("Error in getAskPrice stream: {}",
								error.getMessage(), error))  // 에러 발생 시 로그 출력
						.doOnCancel(() -> log.info(
								"Ask Price Client disconnected"))); // 클라이언트 연결 해제 시 로그 출력)
	}
}
