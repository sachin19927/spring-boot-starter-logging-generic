package org.selfcoding.services.logging.resttemplate;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.selfcoding.services.logging.config.LoggingConstants.CORRELATION_ID_HEADER_KEY;
import static org.selfcoding.services.logging.config.LoggingConstants.CORRELATION_ID_MDC_KEY;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;

public class RestTemplateCorrelationIdInterceptorTest {
	
	private final RestTemplateCorrelationIdInterceptor restTemplateCorrelationIdInterceptor= new RestTemplateCorrelationIdInterceptor();
	
	@Test
	void addsCorreltationIdToOutBoundRequestHeader() throws IOException{
		
		HttpRequest httpRequest=mock(HttpRequest.class);
		HttpHeaders httpHeaders=mock(HttpHeaders.class);
		ClientHttpRequestExecution clientHttpRequestExecution=mock(ClientHttpRequestExecution.class);
		when(httpRequest.getHeaders()).thenReturn(httpHeaders);
		
		MDC.put(CORRELATION_ID_MDC_KEY, "foo");
		restTemplateCorrelationIdInterceptor.intercept(httpRequest, "body".getBytes(), clientHttpRequestExecution);
		verify(httpHeaders).add(CORRELATION_ID_HEADER_KEY, "foo");
	}

}
