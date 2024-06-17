package tmt.realtimechartservice.chart.service;

public interface KafkaProducerService {
	void sendTrade(String message);
}
