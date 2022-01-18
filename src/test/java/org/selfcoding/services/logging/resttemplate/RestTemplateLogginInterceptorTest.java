package org.selfcoding.services.logging.resttemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.selfcoding.services.logging.mask.FieldMasker;
import org.selfcoding.services.logging.mask.JsonFieldMaskingException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;


public class RestTemplateLogginInterceptorTest {

	@Mock
	private FieldMasker fieldMasker;
	
	private RestTemplateLoggingInterceptor restTemplateLoggingInterceptor;
	
	@BeforeEach
	void setUp() {
		openMocks(this);
		restTemplateLoggingInterceptor= new RestTemplateLoggingInterceptor(fieldMasker, true, null); 
	}
	
	@Test
	void happyPath() throws IOException, URISyntaxException {
		HttpRequest httpRequest=mock(HttpRequest.class);
		String bodyText="foo";
		byte[] bytes=bodyText.getBytes();
		ClientHttpRequestExecution clientHttpRequestExecution=mock(ClientHttpRequestExecution.class);
		ClientHttpResponse clientHttpResponse=mock(ClientHttpResponse.class);
		JsonFieldMaskingException jsonFieldMaskingException=new JsonFieldMaskingException(new Throwable());
		when(fieldMasker.maskJsonForLogging(bodyText)).thenReturn("masked", "masked");
		when(httpRequest.getHeaders()).thenReturn(new HttpHeaders());
		when(httpRequest.getMethod()).thenReturn(HttpMethod.POST);
		when(httpRequest.getURI()).thenReturn(new URI("/foo"));
		when(clientHttpRequestExecution.execute(httpRequest, bytes)).thenReturn(clientHttpResponse);
		when(clientHttpResponse.getBody()).thenReturn(new ByteArrayInputStream(bytes));
		
		ClientHttpResponse response = restTemplateLoggingInterceptor.intercept(httpRequest, bytes, clientHttpRequestExecution);
		
		assertThat(response).isEqualTo(clientHttpResponse);
		
		verify(fieldMasker,times(2)).maskJsonForLogging(bodyText);
	}
	
	@Test
	void intercept_works_whenMaskJsonFieldMaskingException() throws IOException, URISyntaxException {
		HttpRequest httpRequest=mock(HttpRequest.class);
		String bodyText="foo";
		byte[] bytes=bodyText.getBytes();
		ClientHttpRequestExecution clientHttpRequestExecution=mock(ClientHttpRequestExecution.class);
		ClientHttpResponse clientHttpResponse=mock(ClientHttpResponse.class);
		JsonFieldMaskingException jsonFieldMaskingException=new JsonFieldMaskingException(new Throwable());
		when(fieldMasker.maskJsonForLogging(bodyText)).thenThrow(jsonFieldMaskingException,jsonFieldMaskingException);
		when(httpRequest.getHeaders()).thenReturn(new HttpHeaders());
		when(httpRequest.getMethod()).thenReturn(HttpMethod.POST);
		when(httpRequest.getURI()).thenReturn(new URI("/foo"));
		when(clientHttpRequestExecution.execute(httpRequest, bytes)).thenReturn(clientHttpResponse);
		when(clientHttpResponse.getBody()).thenReturn(new ByteArrayInputStream(bytes));
		
		ClientHttpResponse response = restTemplateLoggingInterceptor.intercept(httpRequest, bytes, clientHttpRequestExecution);
		
		assertThat(response).isEqualTo(clientHttpResponse);
		
		verify(fieldMasker,times(2)).maskJsonForLogging(bodyText);
	}
	
}
