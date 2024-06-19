package tmt.realtimechartservice.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.listener.ReactiveRedisMessageListenerContainer;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class ReactiveRedisConfig {
	@Value("${spring.data.redis.host}")
	private String host;

	@Value("${spring.data.redis.port}")
	private int port;

	@Value("${spring.data.redis.password}")
	private String password;

	@Bean
	@Primary
	public ReactiveRedisConnectionFactory ReactiveRedisConnectionFactory() {

		RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
		redisConfig.setHostName(host);
		redisConfig.setPort(port);
		redisConfig.setPassword(password);

		return new LettuceConnectionFactory(redisConfig);
//		return new LettuceConnectionFactory(host, port);
	}

	@Bean
	public ReactiveRedisTemplate<String, Object> reactiveRedisTemplate(ReactiveRedisConnectionFactory customReactiveRedisConnectionFactory) {
		RedisSerializationContext<String, Object> serializationContext = RedisSerializationContext
				.<String, Object>newSerializationContext(new StringRedisSerializer())
				.value(new GenericJackson2JsonRedisSerializer())
				.hashKey(new StringRedisSerializer())
				.hashValue(new GenericJackson2JsonRedisSerializer())
				.build();

		return new ReactiveRedisTemplate<>(customReactiveRedisConnectionFactory, serializationContext);
	}

	@Bean
	public ReactiveRedisMessageListenerContainer reactiveRedisMessageListenerContainer(ReactiveRedisConnectionFactory customReactiveRedisConnectionFactory) {
		return new ReactiveRedisMessageListenerContainer(customReactiveRedisConnectionFactory);
	}
}
