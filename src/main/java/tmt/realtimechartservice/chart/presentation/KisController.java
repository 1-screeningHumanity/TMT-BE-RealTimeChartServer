package tmt.realtimechartservice.chart.presentation;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import tmt.realtimechartservice.chart.service.KisSocketService;

@RestController
@RequiredArgsConstructor
public class KisController {
	private final KisSocketService kisSocketService;

	@GetMapping("/real-time")
	public Mono<Void> kis() {
		return kisSocketService.sendMessageToWebSocketServer();
	}

}
