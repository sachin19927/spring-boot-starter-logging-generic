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
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@SpringBootTest(classes = {MaskingConfig.class,TestConfigRestOperations.class,RestTemplateConfig.class,LifeCycleLogginAutoconfig.class,
		LoggingConfig.class,LoggingConfigProperties.class,FileAppenderConfig.class,ObjectMapper.class,BuildPropertiesConfig.class})
public class RestOperationBeanPostProcessorTest {

	private final ObjectProvider<RestOperations> objectProvider;
	
	@Nested
	class RestTemplateBeanPostProcessor{
		
		@Test
		void addsClientLoggingInterceptorToAllRestOperations_whenContextStarts() {
			List<RestOperations> restTemplate=objectProvider.orderedStream().collect(Collectors.toList());
			
			assertThat(restTemplate).hasSize(2);
			
			for (RestOperations restOperations : restTemplate) {
				boolean containgsLogginClientInterceptor=false;
				RestTemplate restTemplate1= (RestTemplate) restOperations;
				for (ClientHttpRequestInterceptor interceptor : restTemplate1.getInterceptors()) {
					if(interceptor instanceof RestTemplateLoggingInterceptor) {
					containgsLogginClientInterceptor=true;
					break;
					}
				}
				assertThat(containgsLogginClientInterceptor).isTrue();
			}
		}
		
		@Test
		void addsCorrelationInterceptorToAllRestOperations_whenContextStarts() {
			List<RestOperations> restTemplate=objectProvider.orderedStream().collect(Collectors.toList());
			
			assertThat(restTemplate).hasSize(2);
			
			for (RestOperations restOperations : restTemplate) {
				boolean containgsCorrelationIdInterceptor=false;
				RestTemplate restTemplate1= (RestTemplate) restOperations;
				for (ClientHttpRequestInterceptor interceptor : restTemplate1.getInterceptors()) {
					if(interceptor instanceof RestTemplateCorrelationIdInterceptor) {
						containgsCorrelationIdInterceptor=true;
					break;
					}
				}
				assertThat(containgsCorrelationIdInterceptor).isTrue();
			}
		}

	}
}


@Configuration
class TestConfigRestOperations{
	
	@Bean
	RestOperations restOpertions1() {
		return new RestTemplate(); 
	}
	
	@Bean
	RestOperations restOpertions2() {
		return new RestTemplate(); 
	}
}