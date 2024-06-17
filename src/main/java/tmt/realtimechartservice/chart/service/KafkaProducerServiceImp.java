package tmt.realtimechartservice.chart.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerServiceImp implements KafkaProducerService{

	private final KafkaTemplate<String, String> kafkaTemplate;

	public KafkaProducerServiceImp(KafkaTemplate<String, String> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}

	@Override
	public void sendTrade(String message) {
		kafkaTemplate.send("realchart-trade-stockinfo", message);
	}
}
