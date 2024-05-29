package tmt.realtimechartservice.chart.presentation;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tmt.realtimechartservice.chart.service.KisSocketService;
import tmt.realtimechartservice.chart.service.RedisPubSubServiceImp;

@RestController
@RequiredArgsConstructor
public class KisController {
	private final KisSocketService kisSocketService;
	private final RedisPubSubServiceImp redisPubSubServiceImp;
	@GetMapping("/real-time")
	public Mono<Void> connectKisWebSocketStockPrice() {
		return kisSocketService.sendMessageToWebSocketServer();
	}

	@GetMapping(value = "/stream/{stockCode}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<String> stream(@PathVariable("stockCode") String stockCode){
		return redisPubSubServiceImp.getMessage(stockCode);
	}
}
