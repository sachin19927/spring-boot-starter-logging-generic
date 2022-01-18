package org.selfcoding.services.logging.resttemplate;

import static org.selfcoding.services.logging.config.LoggingConstants.CORRELATION_ID_HEADER_KEY;
import static org.selfcoding.services.logging.config.LoggingConstants.CORRELATION_ID_MDC_KEY;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RestTemplateCorrelationIdInterceptor implements ClientHttpRequestInterceptor {

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
			throws IOException {
		String correlationId=Optional.ofNullable(MDC.get(CORRELATION_ID_MDC_KEY)).orElseGet(this::generateCorrelationId);
		
		HttpHeaders headers=request.getHeaders();
		headers.add(CORRELATION_ID_HEADER_KEY, correlationId);
		return execution.execute(request, body) ;
	}

	private String generateCorrelationId() {
		String newCorrelationId= UUID.randomUUID().toString();
		MDC.put(CORRELATION_ID_MDC_KEY, newCorrelationId);
		return newCorrelationId;
	}
}
