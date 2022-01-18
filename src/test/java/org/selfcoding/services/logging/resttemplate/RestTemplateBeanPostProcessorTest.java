package org.selfcoding.services.logging.resttemplate;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.selfcoding.services.logging.BuildPropertiesConfig;
import org.selfcoding.services.logging.config.FileAppenderConfig;
import org.selfcoding.services.logging.config.LoggingConfig;
import org.selfcoding.services.logging.config.LoggingConfigProperties;
import org.selfcoding.services.logging.lifecycle.LifeCycleLogginAutoconfig;
import org.selfcoding.services.logging.mask.MaskingConfig;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@SpringBootTest(classes = {MaskingConfig.class,TestConfig.class,RestTemplateConfig.class,LifeCycleLogginAutoconfig.class,
		LoggingConfig.class,LoggingConfigProperties.class,FileAppenderConfig.class,ObjectMapper.class,BuildPropertiesConfig.class})
public class RestTemplateBeanPostProcessorTest {

	private final ObjectProvider<RestTemplate> objectProvider;
	
	@Nested
	class RestTemplateBeanPostProcessor{
		
		@Test
		void addClientLoggingInterceptorToAllRestTemplate_whenContextStarts() {
			
			List<RestTemplate> restTemplates= objectProvider.orderedStream().collect(Collectors.toList());
			
			assertThat(restTemplates).hasSize(2);
			
			for (RestTemplate restTemplate : restTemplates) {
				boolean containgsLogginClientInterceptor=false;
				for (ClientHttpRequestInterceptor interceptor : restTemplate.getInterceptors()) {
					if(interceptor instanceof RestTemplateLoggingInterceptor) {
					containgsLogginClientInterceptor=true;
					break;
					}
				}
				assertThat(containgsLogginClientInterceptor).isTrue();
			}
		}
		
		@Test
		void addCorrelationIdInterceptorToAllRestTemplate_whenContextStarts() {
			
			List<RestTemplate> restTemplates= objectProvider.orderedStream().collect(Collectors.toList());
			
			assertThat(restTemplates).hasSize(2);
			
			for (RestTemplate restTemplate : restTemplates) {
				boolean containgsCorrelationIdInterceptor=false;
				for (ClientHttpRequestInterceptor interceptor : restTemplate.getInterceptors()) {
					if(interceptor instanceof RestTemplateLoggingInterceptor) {
						containgsCorrelationIdInterceptor=true;
					break;
					}
				}
				assertThat(containgsCorrelationIdInterceptor).isTrue();
			}
		}
		
		@Test
		void putCorrelationIdFirstInListOfInterceptor_whenAddingInterceptor() {
			List<RestTemplate> restTemplate=objectProvider.orderedStream().collect(Collectors.toList());
			
			ClientHttpRequestInterceptor firstInterceptor=restTemplate.get(0).getInterceptors().get(0);
			
			assertThat(firstInterceptor).isNotNull().isInstanceOf(RestTemplateCorrelationIdInterceptor.class);
		}
	}
}


@Configuration
class TestConfig{
	
	@Bean
	RestTemplate restTemplate1() {
		return new RestTemplate(); 
	}
	
	@Bean
	RestTemplate restTemplate2() {
		return new RestTemplate(); 
	}
}