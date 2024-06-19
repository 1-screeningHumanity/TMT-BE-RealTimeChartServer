package tmt.realtimechartservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class RealTimeChartServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(RealTimeChartServiceApplication.class, args);
	}

}
