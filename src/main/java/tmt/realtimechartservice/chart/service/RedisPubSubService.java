package tmt.realtimechartservice.chart.service;

import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;

public interface RedisPubSubService {
//	Flux<String> getRealTimePrice(String stockCode);

	Flux<String> getRealTimePrice(String stockCode, ServerWebExchange exchange);

//	Flux<String> getAskPrice(String stockCode);

	Flux<String> getAskPrice(String stockCode, ServerWebExchange exchange);
}
