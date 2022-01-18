package org.selfcoding.services.logging.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties("selfcoding.log")
public class LoggingConfigProperties {

	private String environment;
	private String subSystemId;
	private String componentId;
	private String componentVersion;
	private boolean maskResponses=true;
	private String[] logstashServers;
	private String[] endPointLoggingBlackList;
	private List<String> restTemplatequeryMaskingList;
	private boolean logInProd=false;
}
