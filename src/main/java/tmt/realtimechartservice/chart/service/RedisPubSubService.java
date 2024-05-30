package tmt.realtimechartservice.chart.service;

import reactor.core.publisher.Flux;

public interface RedisPubSubService {
	Flux<String> getMessage(String stockCode);

	Flux<String> getAskPrice(String stockCode);
}
