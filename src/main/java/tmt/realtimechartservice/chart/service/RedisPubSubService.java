package tmt.realtimechartservice.chart.service;

import reactor.core.publisher.Flux;

public interface RedisPubSubService {
//	Flux<String> getRealTimePrice(String stockCode);

	Flux<String> getRealTimePrice(String stockCode);

//	Flux<String> getAskPrice(String stockCode);

	Flux<String> getAskPrice(String stockCode);
}
