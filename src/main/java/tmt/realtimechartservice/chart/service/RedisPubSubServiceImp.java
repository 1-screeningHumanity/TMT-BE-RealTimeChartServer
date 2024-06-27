package tmt.realtimechartservice.chart.service;

import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.ReactiveSubscription;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.ReactiveRedisMessageListenerContainer;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Slf4j
@Service
public class RedisPubSubServiceImp implements RedisPubSubService {

	private final Flux<String> sharedFlux;

	@Autowired
	private ReactiveRedisService reactiveRedisService;

	public RedisPubSubServiceImp(ReactiveRedisMessageListenerContainer listenerContainer) {
		ChannelTopic reactiveStock = new ChannelTopic("reactive_stock");

		this.sharedFlux = listenerContainer.receive(reactiveStock)
				.map(ReactiveSubscription.Message::getMessage)
				.share()
				.doOnSubscribe(test -> log.info("receive start"))
				.doOnCancel(() -> log.info("receive cancel"));
	}

	@Override
	public Flux<String> getRealTimePrice(String stockCode) {
		return reactiveRedisService.getPrice(stockCode)
				.concatWith(sharedFlux.filter(
								message -> message.startsWith("stock:" + stockCode))
						.doOnCancel(() -> log.info("shareFlux sub cancel"))

				)
				.mergeWith(Flux.interval(Duration.ofSeconds(10))
						.map(tick -> "keep-alive"))
				.doOnCancel(() -> log.info("price cancel"));
	}

	@Override
	public Flux<String> getAskPrice(String stockCode) {
		return reactiveRedisService.getAskPrice(stockCode)
				.concatWith(sharedFlux.filter(
						message -> message.startsWith("stock:askPrice-" + stockCode))
				)
				.mergeWith(Flux.interval(Duration.ofSeconds(10))
						.map(tick -> "keep-alive"))
				.doOnCancel(() -> log.info("ask price cancel"));
	}
}
