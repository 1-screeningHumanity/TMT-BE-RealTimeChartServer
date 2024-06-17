package tmt.realtimechartservice.chart.service;

import reactor.core.publisher.Mono;

public interface KisSocketService {
	void startPrice();

	void stopPrice();

	void startAskPrice();

	void stopAskPrice();

	Mono<Void> sendMessageToWebSocketServerToRealTimePrice();

	Mono<Void> sendMessageToWebSocketServerToAskingPrice();
}
