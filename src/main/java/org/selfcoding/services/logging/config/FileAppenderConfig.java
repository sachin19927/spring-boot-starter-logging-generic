package org.selfcoding.services.logging.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties("selfcoding.log.fileappender")
public class FileAppenderConfig {

	private boolean enabled= false;
	private String logPath;
	private String fileName;
	private boolean rollBySizeAndDate= false;
	
}
