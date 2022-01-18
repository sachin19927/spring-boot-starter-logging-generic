package org.selfcoding.services.logging.mask;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class MaskingConfig {

	@Bean
	@ConditionalOnMissingBean
	public FieldMasker jsonFieldMasker(ObjectMapper objectMapper) {
		return new FieldMaskerImpl(objectMapper);
	}
}
