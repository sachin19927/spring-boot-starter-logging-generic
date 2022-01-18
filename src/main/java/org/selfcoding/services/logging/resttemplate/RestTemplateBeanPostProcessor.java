package org.selfcoding.services.logging.resttemplate;

import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RestTemplateBeanPostProcessor implements BeanPostProcessor {
private final RestTemplateLoggingInterceptor restTemplateLoggingInterceptor;
private final RestTemplateCorrelationIdInterceptor restTemplateCorrelationIdInterceptor;
@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
	if(bean instanceof RestTemplate)
	{
		RestTemplate restTemplate=(RestTemplate) bean;
		ClientHttpRequestFactory factory=new BufferingClientHttpRequestFactory(new HttpComponentsClientHttpRequestFactory());
		restTemplate.setRequestFactory(factory);
		List<ClientHttpRequestInterceptor> interceptors= restTemplate.getInterceptors();
		interceptors.add(restTemplateLoggingInterceptor);
		interceptors.add(restTemplateCorrelationIdInterceptor);
		
		AnnotationAwareOrderComparator.sort(interceptors);
	}
	return bean;
	}
}
