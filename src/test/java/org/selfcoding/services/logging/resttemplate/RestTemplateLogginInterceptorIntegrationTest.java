package org.selfcoding.services.logging.resttemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.selfcoding.services.logging.config.LoggingConstants.EVENT_NAME_KEY;
import static org.selfcoding.services.logging.config.LoggingConstants.EVENT_NAME_REQUEST_SENT;
import static org.selfcoding.services.logging.config.LoggingConstants.EVENT_NAME_RESPONSE_RECEIVED;
import static org.selfcoding.services.logging.config.LoggingConstants.REQUEST_BODY_KEY;
import static org.selfcoding.services.logging.config.LoggingConstants.REQUEST_HTTP_METHOD_KEY;
import static org.selfcoding.services.logging.config.LoggingConstants.RESPONSE_BODY_KEY;
import static org.selfcoding.services.logging.config.LoggingConstants.RESPONSE_HTTP_STATUS_KEY;
import static org.selfcoding.services.logging.config.LoggingConstants.URL_KEY;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.selfcoding.services.logging.MemoryAppender;
import org.selfcoding.services.logging.mask.FieldMaskerImpl;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriComponentsBuilder;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import lombok.RequiredArgsConstructor;
@RestClientTest
@RequiredArgsConstructor
@ContextConfiguration(classes = {TestConfig2.class,RestTemplateLoggingInterceptor.class,FieldMaskerImpl.class})
public class RestTemplateLogginInterceptorIntegrationTest {

	private final MockRestServiceServer mockRestServiceServer;
	private final RestTemplate restTemplate;
	private final MemoryAppender memoryAppender= new MemoryAppender();
	
	@BeforeEach
	void setUp() {
		Logger logger= (Logger) LoggerFactory.getLogger(RestTemplateLoggingInterceptor.class);
		memoryAppender.setContext((LoggerContext)LoggerFactory.getILoggerFactory());
		logger.setLevel(Level.DEBUG);
		logger.addAppender(memoryAppender);
		memoryAppender.start();
	}
	
	@Test
	void createsLogEntryForRequestAndResponse_whenMakingRestTemplateCall() {
		String body="{\"bar\":\"moo\"}";
		
		HashMap<Object, Object> request=new HashMap<Object, Object>();
		request.put("bar", "moo");
		
		mockRestServiceServer.expect(requestTo("/foo")).andRespond(withSuccess(body,MediaType.APPLICATION_JSON));
		
		restTemplate.postForEntity("/foo", request, String.class);
		
		List<ILoggingEvent> requestLogEntry=memoryAppender.search("Sending request");
		List<ILoggingEvent> responseLogEntry=memoryAppender.search("Received response");
		
		assertThat(requestLogEntry).isNotNull().isNotEmpty().hasSize(1);
		assertThat(responseLogEntry).isNotNull().isNotEmpty().hasSize(1);
		
		Object[] requestArgumentArray=requestLogEntry.get(0).getArgumentArray();
		Object[] responseArgumentArray=responseLogEntry.get(0).getArgumentArray();
		
		Map.Entry<String, String> requestBodyArgument=memoryAppender.getMapEntry(REQUEST_BODY_KEY, requestArgumentArray);
		Map.Entry<String, String> requestHttpMethod=memoryAppender.getMapEntry(REQUEST_HTTP_METHOD_KEY, requestArgumentArray);
		Map.Entry<String, String> requestEventName=memoryAppender.getMapEntry(EVENT_NAME_KEY, requestArgumentArray);
		Map.Entry<String, String> requestUrlKey=memoryAppender.getMapEntry(URL_KEY, requestArgumentArray);
		
		Map.Entry<String, String> responseBodyArgument=memoryAppender.getMapEntry(RESPONSE_BODY_KEY, responseArgumentArray);
		Map.Entry<String, String> responseStatus=memoryAppender.getMapEntry(RESPONSE_HTTP_STATUS_KEY, responseArgumentArray);
		Map.Entry<String, String> responseEventName=memoryAppender.getMapEntry(EVENT_NAME_KEY, responseArgumentArray);
		Map.Entry<String, String> responseUrlKey=memoryAppender.getMapEntry(URL_KEY, responseArgumentArray);
		
		
		assertThat(requestBodyArgument).isNotNull();
		assertThat(requestHttpMethod).isNotNull();
		assertThat(requestEventName).isNotNull();
		assertThat(requestUrlKey).isNotNull();
		
		assertThat(responseBodyArgument).isNotNull();
		assertThat(responseStatus).isNotNull();
		assertThat(responseEventName).isNotNull();
		assertThat(requestUrlKey).isNotNull();
		
		
		assertThat(requestBodyArgument.getValue()).isEqualTo(body);
		assertThat(requestHttpMethod.getValue()).isEqualTo("POST");
		assertThat(requestEventName.getValue()).isEqualTo(EVENT_NAME_REQUEST_SENT);
		assertThat(requestUrlKey.getValue()).isEqualTo("/foo");
		
		assertThat(responseBodyArgument.getValue()).isEqualTo(body);
		assertThat(responseStatus.getValue()).isEqualTo("200");
		assertThat(responseEventName.getValue()).isEqualTo(EVENT_NAME_RESPONSE_RECEIVED);
		assertThat(requestUrlKey.getValue()).isEqualTo("/foo");
	}

	@Test
	void maskQueryParameter_whenMaskingResttemplateCall() {
		String body="{\"bar\":\"moo\"}";
		HashMap<Object, Object> request= new HashMap<Object, Object>();
		request.put("bar", "moo");
		
		mockRestServiceServer.expect(requestTo("https://host.com/moo?foo=bar")).andRespond(withSuccess(body,MediaType.APPLICATION_JSON));
		
		String uri=UriComponentsBuilder.newInstance().scheme("https").host("host.com").path("/moo").queryParam("foo", "bar").build().toString();
		
		restTemplate.postForEntity(uri, request, String.class);
		List<ILoggingEvent> requestLogEntry=memoryAppender.search("Sending request");
		List<ILoggingEvent> responseLogEntry=memoryAppender.search("Received response");
		
		assertThat(requestLogEntry).isNotNull().isNotEmpty().hasSize(1);
		assertThat(responseLogEntry).isNotNull().isNotEmpty().hasSize(1);
		
		Object[] requestArgumentArray=requestLogEntry.get(0).getArgumentArray();
		Object[] responseArgumentArray=responseLogEntry.get(0).getArgumentArray();
		
		
		Map.Entry<String, String> requestUrlKey=memoryAppender.getMapEntry(URL_KEY, requestArgumentArray);
		Map.Entry<String, String> responseUrlKey=memoryAppender.getMapEntry(URL_KEY, responseArgumentArray);
		
		assertThat(requestUrlKey).isNotNull();
		assertThat(responseUrlKey).isNotNull();
		
		assertThat(requestUrlKey.getValue()).isEqualTo("https://host.com/moo?foo=******");
		assertThat(responseUrlKey.getValue()).isEqualTo("https://host.com/moo?foo=******");
	}
	
	@Test
	void disableEncodingOfQueryParams_whenMaksingResttemplateCall() {
		
		String body="{\"bar\":\"moo\"}";
		HashMap<Object, Object> request= new HashMap<Object, Object>();
		request.put("bar", "moo");
		
		mockRestServiceServer.expect(requestTo("https://host.com/moo?test=bar%20bar")).andRespond(withSuccess(body,MediaType.APPLICATION_JSON));
		
		String uri=UriComponentsBuilder.newInstance().scheme("https").host("host.com").path("/moo").queryParam("test", "bar%20bar").build().toString();
		
		DefaultUriBuilderFactory defaultUriBuilderFactory= new DefaultUriBuilderFactory();
		defaultUriBuilderFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);
		
		restTemplate.setUriTemplateHandler(defaultUriBuilderFactory);
		
		restTemplate.postForEntity(uri, request, String.class);
		List<ILoggingEvent> requestLogEntry=memoryAppender.search("Sending request");
		List<ILoggingEvent> responseLogEntry=memoryAppender.search("Received response");
		
		assertThat(requestLogEntry).isNotNull().isNotEmpty().hasSize(1);
		assertThat(responseLogEntry).isNotNull().isNotEmpty().hasSize(1);
		
		Object[] requestArgumentArray=requestLogEntry.get(0).getArgumentArray();
		Object[] responseArgumentArray=responseLogEntry.get(0).getArgumentArray();
		
		
		Map.Entry<String, String> requestUrlKey=memoryAppender.getMapEntry(URL_KEY, requestArgumentArray);
		Map.Entry<String, String> responseUrlKey=memoryAppender.getMapEntry(URL_KEY, responseArgumentArray);
		
		assertThat(requestUrlKey).isNotNull();
		assertThat(responseUrlKey).isNotNull();
		
		assertThat(requestUrlKey.getValue()).isEqualTo("https://host.com/moo?test=bar%20bar");
		assertThat(responseUrlKey.getValue()).isEqualTo("https://host.com/moo?test=bar%20bar");
	}
	
	@Test
	void queryParamWthEncode_whenMaksingResttemplateCall() {
		
		String body="{\"bar\":\"moo\"}";
		HashMap<Object, Object> request= new HashMap<Object, Object>();
		request.put("bar", "moo");
		
		mockRestServiceServer.expect(requestTo("https://host.com/moo?test=bar%2520bar")).andRespond(withSuccess(body,MediaType.APPLICATION_JSON));
		
		String uri=UriComponentsBuilder.newInstance().scheme("https").host("host.com").path("/moo").queryParam("test", "bar%2520bar").build().toString();
		
		DefaultUriBuilderFactory defaultUriBuilderFactory= new DefaultUriBuilderFactory();
		defaultUriBuilderFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);
		
		restTemplate.setUriTemplateHandler(defaultUriBuilderFactory);
		
		restTemplate.postForEntity(uri, request, String.class);
		List<ILoggingEvent> requestLogEntry=memoryAppender.search("Sending request");
		List<ILoggingEvent> responseLogEntry=memoryAppender.search("Received response");
		
		assertThat(requestLogEntry).isNotNull().isNotEmpty().hasSize(1);
		assertThat(responseLogEntry).isNotNull().isNotEmpty().hasSize(1);
		
		Object[] requestArgumentArray=requestLogEntry.get(0).getArgumentArray();
		Object[] responseArgumentArray=responseLogEntry.get(0).getArgumentArray();
		
		
		Map.Entry<String, String> requestUrlKey=memoryAppender.getMapEntry(URL_KEY, requestArgumentArray);
		Map.Entry<String, String> responseUrlKey=memoryAppender.getMapEntry(URL_KEY, responseArgumentArray);
		
		assertThat(requestUrlKey).isNotNull();
		assertThat(responseUrlKey).isNotNull();
		
		assertThat(requestUrlKey.getValue()).isEqualTo("https://host.com/moo?test=bar%2520bar");
		assertThat(responseUrlKey.getValue()).isEqualTo("https://host.com/moo?test=bar%2520bar");

		
	}
	
		
	
}


class TestConfig2{
	
	@Bean
	RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder, RestTemplateLoggingInterceptor loggingInterceptor) {
		return restTemplateBuilder.interceptors(loggingInterceptor).build();
	}
	
	@Bean
	boolean logPayLoad() {
		return true;
	}
	
	@Bean
	List<String> queryParamMaskingList(){
		return Collections.singletonList("foo");
	}
}