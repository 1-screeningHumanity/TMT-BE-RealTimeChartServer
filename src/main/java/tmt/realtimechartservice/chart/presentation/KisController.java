package tmt.realtimechartservice.chart.presentation;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tmt.realtimechartservice.chart.service.KisSocketService;
import tmt.realtimechartservice.chart.service.RedisPubSubServiceImp;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class KisController {
	private final KisSocketService kisSocketService;
	private final RedisPubSubServiceImp redisPubSubServiceImp;
	@GetMapping("/real-time")
	public Mono<Void> connectKisWebSocketStockPrice() {
		return kisSocketService.sendMessageToWebSocketServerToRealTimePrice();
	}

	@GetMapping("/real-time/ask-price")
	public Mono<Void> connectKisWebSocketAskPrice() {
		return kisSocketService.sendMessageToWebSocketServerToAskingPrice();
	}

	@GetMapping(value = "/stream/{stockCode}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<String> streamRealTime(@PathVariable("stockCode") String stockCode){
		return redisPubSubServiceImp.getRealTimePrice(stockCode);
	}

	@GetMapping(value = "/stream/{stockCode}/asking-price", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<String> streamAskBid(@PathVariable("stockCode") String stockCode){
		return redisPubSubServiceImp.getAskPrice(stockCode);
	}
}
