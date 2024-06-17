package tmt.realtimechartservice.chart.service;

import reactor.core.publisher.Mono;

public interface ReactiveRedisService {
	Mono<Void> save(String key, String value);
	Mono<String> getPrice(String key);
	Mono<String> getAskPrice(String key);
}
