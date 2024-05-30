package tmt.realtimechartservice.chart.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ReactiveRedisServiceImp implements ReactiveRedisService {

	private static final String REDIS_KEY_PREFIX = "stock:";
	private static final String CHANNEL = "reactive_stock";

	private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

	@Override
	public Mono<Void> save(String key, String value) {
		return reactiveRedisTemplate.opsForValue().set(REDIS_KEY_PREFIX + key, value)
				.then(reactiveRedisTemplate.convertAndSend(CHANNEL, REDIS_KEY_PREFIX + key + ":" + value).then());
	}

	@Override
	public Mono<String> get(String key) {
		return reactiveRedisTemplate.opsForValue().get(REDIS_KEY_PREFIX + key);
	}
}
