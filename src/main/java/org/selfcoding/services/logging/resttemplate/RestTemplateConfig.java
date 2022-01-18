package org.selfcoding.services.logging.resttemplate;

import org.selfcoding.services.logging.config.LoggingConfigProperties;
import org.selfcoding.services.logging.mask.FieldMasker;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestOperations;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
@ConditionalOnBean(RestOperations.class)
public class RestTemplateConfig {

	private final LoggingConfigProperties loggingConfigProperties;
	
	@Bean
	public RestTemplateLoggingInterceptor restTemplateLoggingInterceptor(FieldMasker fieldMasker, boolean logPayload) {
		return new RestTemplateLoggingInterceptor(fieldMasker, logPayload,loggingConfigProperties.getRestTemplatequeryMaskingList());
	}
	
	@Bean
	public RestTemplateCorrelationIdInterceptor correlationIdInterceptor()
	{
		return new RestTemplateCorrelationIdInterceptor();
	}
	
	@Bean
	public RestTemplateBeanPostProcessor restTemplateBeanPostProcessor(RestTemplateLoggingInterceptor restTemplateLoggingInterceptor,RestTemplateCorrelationIdInterceptor restTemplateCorrelationIdInterceptor) {
		return new RestTemplateBeanPostProcessor(restTemplateLoggingInterceptor, restTemplateCorrelationIdInterceptor);
	}
}
