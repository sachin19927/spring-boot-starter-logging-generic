package org.selfcoding.services.logging.resttemplate;

import static org.selfcoding.services.logging.config.LoggingConstants.CORRELATION_ID_HEADER_KEY;
import static org.selfcoding.services.logging.config.LoggingConstants.CORRELATION_ID_MDC_KEY;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;

@RestClientTest
@RequiredArgsConstructor
@ContextConfiguration(classes = {TestConfig3.class,RestTemplateCorrelationIdInterceptor.class})
public class RestTemplateCorrelationIdInterceptorIntegrationTest {
	
	private final MockRestServiceServer mockRestServiceServer;
	private final RestTemplate restTemplate;

	@Test
	void putsCorrelationIdFromMdcIntoRequestHeader_whenMaskingRestTemplateCall() {
		MDC.put(CORRELATION_ID_MDC_KEY, "FOO");
		mockRestServiceServer.expect(requestTo("/foo"))
		.andExpect(header(CORRELATION_ID_HEADER_KEY, "FOO"))
		.andRespond(withSuccess("{\"bar\":\"moo\"}", MediaType.APPLICATION_JSON));
		
		restTemplate.getForObject("/foo", String.class);
	}
	
	@Test
	void generateAndPutsCorrelationIdIntoRequestHeader_whenCorrelationIdNotInMdc() {
		MDC.put(CORRELATION_ID_MDC_KEY, null);
		mockRestServiceServer.expect(requestTo("/foo"))
		.andExpect(header(CORRELATION_ID_HEADER_KEY, Matchers.any(String.class)))
		.andRespond(withSuccess("{\"bar\":\"moo\"}", MediaType.APPLICATION_JSON));
		
		restTemplate.getForObject("/foo", String.class);
	}
}


class TestConfig3{
	
	@Bean
	RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder, RestTemplateCorrelationIdInterceptor restTemplateCorrelationIdInterceptor) {
		return restTemplateBuilder.interceptors(restTemplateCorrelationIdInterceptor).build();
	}
}