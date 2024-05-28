package tmt.realtimechartservice.global.config;


import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.ArrayList;
import java.util.List;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

@Configuration
public class SwaggerConfig {

    private static final String BEARER_TOKEN = "Bearer ";
    private static final String BEARER_SCHEME = "bearer";
    private static final String BEARER_FORMAT = "JWT";
    private int serverPort;

    @Value("${spring.application.name}")
    private String applicationName;

    @EventListener
    public void onApplicationEvent(WebServerInitializedEvent event) {
        this.serverPort = event.getWebServer().getPort();
    }

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .components(new Components())
                .addSecurityItem(new SecurityRequirement().addList(BEARER_TOKEN))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes(BEARER_TOKEN, new SecurityScheme()
                                .name(BEARER_TOKEN)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme(BEARER_SCHEME)
                                .bearerFormat(BEARER_FORMAT)))
                .info(apiInfo());
    }

    private Info apiInfo() {
        return new Info()
                .title("default Service 테스트")
                .description("Springdoc을 사용한 Swagger UI 테스트")
                .version("1.0.0");
    }

    @Bean
    public OpenApiCustomizer customOpenAPI() {
        return openApi -> openApi.servers(servers());
    }

    private List<Server> servers() {
        List<Server> servers = new ArrayList<>();
        Server localServer = new Server();
        localServer.setUrl("http://localhost:" + serverPort);
        localServer.setDescription("Local Test Server URL");

        Server dynamicServer = new Server();
        dynamicServer.setUrl("https://screeninghumanity.shop/api/v1/realtimestock");
        dynamicServer.setDescription("AWS 배포 서버");

        servers.add(localServer);
        servers.add(dynamicServer);
        return servers;
    }
}
