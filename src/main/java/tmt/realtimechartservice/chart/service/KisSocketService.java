package tmt.realtimechartservice.chart.service;

import reactor.core.publisher.Mono;

public interface KisSocketService {
	Mono<Void> sendMessageToWebSocketServerToRealTimePrice();

	Mono<Void> sendMessageToWebSocketServerToAskingPrice();
}
