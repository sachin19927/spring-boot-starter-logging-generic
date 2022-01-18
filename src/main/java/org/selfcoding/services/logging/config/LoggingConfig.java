package org.selfcoding.services.logging.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(LoggingConfigProperties.class)
public class LoggingConfig {

	private final Environment sprinEnvironment;
	private final LoggingConfigProperties loggingConfigProperties;
	
	@Bean
	boolean logPayloads(){
		
		return !prodProfilesActive() || loggingConfigProperties.isLogInProd();
	}
	
	private boolean prodProfilesActive() {
		List<String> activeProfiles=Arrays.asList(sprinEnvironment.getActiveProfiles());
		return activeProfiles.contains("prod") || activeProfiles.contains("production");
	}
	
	@Bean
	ApplicationRunner addCustomFields(BuildProperties buildProperties,FileAppenderConfig fileAppenderConfig)
	{
		String[] logStashserver=loggingConfigProperties.getLogstashServers();
		
		boolean logStashEnabled=logStashserver !=null && logStashserver.length>0;
		return new LoggingCustomizer(buildProperties,sprinEnvironment,logStashEnabled,fileAppenderConfig.isEnabled(),loggingConfigProperties);
		
	}
}
