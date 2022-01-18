package org.selfcoding.services.logging.lifecycle;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LifeCycleLogginAutoconfig {

	@Bean
	ApplicationLifeCycleEventListner applicationLifeCycleEventListner()
	{
		return new ApplicationLifeCycleEventListner();
	}
}
