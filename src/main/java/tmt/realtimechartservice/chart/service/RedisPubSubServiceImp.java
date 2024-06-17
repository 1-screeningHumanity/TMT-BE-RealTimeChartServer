package tmt.realtimechartservice.chart.service;

import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.ReactiveRedisMessageListenerContainer;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
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
	public Flux<String> getRealTimePrice(String stockCode) {
		return reactiveRedisService.getPrice(stockCode)
				.concatWith(sink.asFlux()
						.filter(message -> message.startsWith("stock:" + stockCode))
						.mergeWith(Flux.interval(Duration.ofSeconds(10)).map(tick -> "keep-alive"))
						.doOnCancel(() -> log.info("Price Client disconnected"))); // 클라이언트 연결 해제 시 로그 출력)

	}

	@Override
	public Flux<String> getAskPrice(String stockCode) {
		return reactiveRedisService.getAskPrice(stockCode)
				.concatWith(sink.asFlux()
						.filter(message -> message.startsWith("stock:askPrice-" + stockCode))
						.mergeWith(Flux.interval(Duration.ofSeconds(10)).map(tick -> "keep-alive"))
						.doOnCancel(() -> log.info("Ask Price Client disconnected"))); // 클라이언트 연결 해제 시 로그 출력)
	}
}
