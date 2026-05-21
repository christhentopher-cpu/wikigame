package com.mdsg.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	private final MdsgProperties properties;

	public WebSocketConfig(MdsgProperties properties) {
		this.properties = properties;
	}

	@Override
	public void registerStompEndpoints(@NonNull StompEndpointRegistry registry) {
		String[] allowedPatterns = properties.getCors()
			.getAllowedOriginPatterns()
			.toArray(String[]::new);

		registry.addEndpoint(properties.getWebsocket().getEndpoint())
			.setAllowedOriginPatterns(allowedPatterns)
			.withSockJS();
	}

	@Override
	public void configureMessageBroker(@NonNull MessageBrokerRegistry registry) {
		registry.setApplicationDestinationPrefixes(properties.getWebsocket().getApplicationPrefix());
		registry.enableSimpleBroker(properties.getWebsocket().getBrokerPrefix());
	}

}
