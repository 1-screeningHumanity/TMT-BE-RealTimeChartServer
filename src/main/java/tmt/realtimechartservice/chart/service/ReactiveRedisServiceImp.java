package tmt.realtimechartservice.chart.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ReactiveRedisServiceImp implements ReactiveRedisService {

	private static final String PRICE_PREFIX = "stock:";
	private static final String ASKING_PRICE_PREFIX = "stock:askPrice-";
	private static final String CHANNEL = "reactive_stock";

	private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

	@Override
	public Mono<Void> save(String key, String value) {
		return reactiveRedisTemplate.opsForValue().set(PRICE_PREFIX + key, value)
				.then(reactiveRedisTemplate.convertAndSend(CHANNEL, PRICE_PREFIX + key + ":" + value).then());
	}

	@Override
	public Mono<String> getPrice(String key) {
		return reactiveRedisTemplate.opsForValue().get(PRICE_PREFIX + key)
				.map(value -> PRICE_PREFIX + key + ":" + value);
	}

	@Override
	public Mono<String> getAskPrice(String key) {
		return reactiveRedisTemplate.opsForValue().get(ASKING_PRICE_PREFIX + key)
				.map(value -> ASKING_PRICE_PREFIX + key + ":" + value);
	}
}
