package tmt.realtimechartservice.chart.service;

import reactor.core.publisher.Mono;

public interface KisSocketService {
	Mono<Void> sendMessageToWebSocketServer();

	Mono<Void> sendMessageToWebSocketServerToAskingPrice();
}
