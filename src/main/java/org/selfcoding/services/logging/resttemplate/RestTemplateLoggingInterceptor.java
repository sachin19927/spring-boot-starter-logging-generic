package org.selfcoding.services.logging.resttemplate;

import static net.logstash.logback.argument.StructuredArguments.entries;
import static org.selfcoding.services.logging.config.LoggingConstants.BODY_UNKNOWN;
import static org.selfcoding.services.logging.config.LoggingConstants.EVENT_NAME_KEY;
import static org.selfcoding.services.logging.config.LoggingConstants.EVENT_NAME_REQUEST_SENT;
import static org.selfcoding.services.logging.config.LoggingConstants.EVENT_NAME_RESPONSE_RECEIVED;
import static org.selfcoding.services.logging.config.LoggingConstants.REQUEST_BODY_KEY;
import static org.selfcoding.services.logging.config.LoggingConstants.REQUEST_HTTP_METHOD_KEY;
import static org.selfcoding.services.logging.config.LoggingConstants.RESPONSE_BODY_KEY;
import static org.selfcoding.services.logging.config.LoggingConstants.RESPONSE_HTTP_STATUS_KEY;
import static org.selfcoding.services.logging.config.LoggingConstants.URL_KEY;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.selfcoding.services.logging.mask.FieldMasker;
import org.selfcoding.services.logging.mask.JsonFieldMaskingException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class RestTemplateLoggingInterceptor implements ClientHttpRequestInterceptor{

	private final FieldMasker fieldMasker;
	private final boolean logPayloads;
	private final List<String> queryParamMaskingList;
	
	@Override
	@SuppressWarnings("PlaceholderCountMatchesArgumentCount")
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
			throws IOException {
		Map<String, String> requestLoggingMap=new HashMap<>();
		HttpMethod httpMethod=request.getMethod();
		
		if(logPayloads && !HttpMethod.GET.equals(httpMethod))
		{
			String requestBody=new String(body);
			requestBody=maskRequestBody(requestBody);
			requestLoggingMap.put(REQUEST_BODY_KEY, requestBody);
			
		}
		URI uri =request.getURI();
		if(queryParamMaskingList!=null)
		{
			uri=maskQueryParam(uri);
		}
		requestLoggingMap.put(REQUEST_HTTP_METHOD_KEY, httpMethod.toString());
		requestLoggingMap.put(EVENT_NAME_KEY, EVENT_NAME_REQUEST_SENT);
		requestLoggingMap.put(URL_KEY, uri.toString());
		
		log.info("Sending request", entries(requestLoggingMap));
		
		ClientHttpResponse response=execution.execute(request, body);
		
		Map<String, String> responseLoggingMap=new HashMap<>();
		if(logPayloads) {
			String responseBody=getResponseBody(response);
			responseBody=maskResponseBody(responseBody);
			responseLoggingMap.put(RESPONSE_BODY_KEY, responseBody);
		}
		
		responseLoggingMap.put(RESPONSE_HTTP_STATUS_KEY, String.valueOf(response.getRawStatusCode()));
		responseLoggingMap.put(EVENT_NAME_KEY,EVENT_NAME_RESPONSE_RECEIVED);
		responseLoggingMap.put(URL_KEY, uri.toString());
		
		log.info("Received response", entries(responseLoggingMap));
		
		return response;
	}
	
	private URI maskQueryParam(URI uri)
	{
		MultiValueMap<String, String> queryParam=UriComponentsBuilder.fromUri(uri).build(true).getQueryParams();
		MultiValueMap<String, String> replacementParams=new LinkedMultiValueMap<>();
		
		queryParam.forEach((key,value)-> {
			if(queryParamMaskingList.contains(key)) {
				replacementParams.put(key, Collections.singletonList(FieldMasker.MASK));
			}else
			{
				replacementParams.put(key, value);
			}
		});
		
		uri=UriComponentsBuilder.newInstance().scheme(uri.getScheme()).host(uri.getHost()).path(uri.getPath()).queryParams(replacementParams).build(true).toUri();
		return uri;
	}
	
	private String getResponseBody(ClientHttpResponse response)
	{
		String responseBody;
		try(BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(response.getBody()))) {
			return bufferedReader.lines().collect(Collectors.joining("\n"));
		}catch (Exception e) {
			responseBody= "UNKNOWN";
		}
		return responseBody;
	}	
	
	private String maskResponseBody(String responseBody)
	{
		try {
			return fieldMasker.maskJsonForLogging(responseBody);
		}catch (JsonFieldMaskingException e) {
			return BODY_UNKNOWN;
		}
	}
	
	private String maskRequestBody(String requestBody)
	{
		try {
			return fieldMasker.maskJsonForLogging(requestBody);
		}catch (JsonFieldMaskingException e) {
			return BODY_UNKNOWN;
		}
	}
}
