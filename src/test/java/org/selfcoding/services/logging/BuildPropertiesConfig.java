package org.selfcoding.services.logging;

import java.util.Properties;

import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BuildPropertiesConfig {

	@Bean
	BuildProperties buildProperties() {
		Properties properties= new Properties();
		properties.setProperty("version", "1.0.0");
		return new BuildProperties(properties);
	}
}
