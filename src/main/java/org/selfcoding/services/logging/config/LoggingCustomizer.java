package org.selfcoding.services.logging.config;

import static org.selfcoding.services.logging.config.LoggingConstants.COMPONENT_ID_KEY;
import static org.selfcoding.services.logging.config.LoggingConstants.COMPONENT_VERSION_KEY;
import static org.selfcoding.services.logging.config.LoggingConstants.ENVIRONMENT_KEY;
import static org.selfcoding.services.logging.config.LoggingConstants.EVENT_TYPE_KEY;
import static org.selfcoding.services.logging.config.LoggingConstants.EVENT_TYPE_USR;
import static org.selfcoding.services.logging.config.LoggingConstants.HOST_KEY;
import static org.selfcoding.services.logging.config.LoggingConstants.SPRING_PROFILES_ACTIVE_KEY;
import static org.selfcoding.services.logging.config.LoggingConstants.SUBSYSTEM_ID_KEY;
import static org.slf4j.Logger.ROOT_LOGGER_NAME;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.info.BuildProperties;
import org.springframework.core.env.Environment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.appender.LogstashTcpSocketAppender;
import net.logstash.logback.composite.JsonProviders;
import net.logstash.logback.composite.loggingevent.LoggingEventPatternJsonProvider;
import net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder;

@Slf4j
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class LoggingCustomizer implements ApplicationRunner {

	private final BuildProperties buildProperties;
	private final Environment environment;
	private final boolean logStashEnabled;
	private final boolean isFileAppenderEnabled;
	private final LoggingConfigProperties loggingConfigProperties;
	
	@Override
	public void run(ApplicationArguments args) throws Exception {
		Logger root= (Logger)org.slf4j.LoggerFactory.getLogger(ROOT_LOGGER_NAME);
		ConsoleAppender consoleAppender = (ConsoleAppender) root.getAppender("Console");
		
		LoggingEventCompositeJsonEncoder encoder=(LoggingEventCompositeJsonEncoder) consoleAppender.getEncoder();
		
		customzieEncoder(buildProperties, encoder);
		
		if(logStashEnabled)
		{
			LogstashTcpSocketAppender logstashTcpSocketAppender=(LogstashTcpSocketAppender) root.getAppender("logstash_appender");
			LoggingEventCompositeJsonEncoder logstashEncoder= (LoggingEventCompositeJsonEncoder) logstashTcpSocketAppender.getEncoder();
			customzieEncoder(buildProperties, logstashEncoder);
		}
		if(isFileAppenderEnabled)
		{
			RollingFileAppender rollingFileAppender= (RollingFileAppender) root.getAppender("file_appender");
			LoggingEventCompositeJsonEncoder logstashEncoder= (LoggingEventCompositeJsonEncoder) rollingFileAppender.getEncoder();
			customzieEncoder(buildProperties, logstashEncoder);
		}
		
	}

	private void customzieEncoder(BuildProperties buildProperties,LoggingEventCompositeJsonEncoder encoder)
	{
		JsonProviders<ILoggingEvent> providers= encoder.getProviders();
		
		providers.getProviders().stream().filter(LoggingEventPatternJsonProvider.class:: isInstance).map(LoggingEventPatternJsonProvider.class::cast).findFirst().ifPresent(provider1->customziePattern(buildProperties,provider1));
	}
	
	private void customziePattern(BuildProperties buildProperties,LoggingEventPatternJsonProvider provider) 
	{
		String pattern=provider.getPattern();
		
		ObjectMapper objectMapper= new ObjectMapper();
		Map map;
		
		try {
			map=objectMapper.readValue(pattern, Map.class);
			Optional.ofNullable(loggingConfigProperties.getSubSystemId()).ifPresent(subSystemId-> map.put(SUBSYSTEM_ID_KEY, subSystemId));
			Optional.ofNullable(loggingConfigProperties.getComponentId()).ifPresent(componentId-> map.put(COMPONENT_ID_KEY, componentId));
			Optional.ofNullable(loggingConfigProperties.getComponentVersion()).ifPresent(componentVersion-> map.put(COMPONENT_VERSION_KEY, componentVersion));
			Optional.ofNullable(loggingConfigProperties.getEnvironment()).ifPresent(environment-> map.put(ENVIRONMENT_KEY, environment));
			
			addHostName(map);
			addActiveSpringProfiles(map);
			map.put(EVENT_TYPE_KEY, EVENT_TYPE_USR);
			String modifiedPattern=objectMapper.writeValueAsString(map);
			provider.setPattern(modifiedPattern);
		}
		catch (JsonProcessingException e) {
			log.error("Unable to manipulate loggin provider's pattern to add custom fields", e);
		}
	}
	public void addActiveSpringProfiles(Map map)
	{
		List<String> activeProfilesList=Arrays.asList(environment.getActiveProfiles());
		if(!activeProfilesList.isEmpty() && !activeProfilesList.contains("local"))
		{
			map.put(SPRING_PROFILES_ACTIVE_KEY, String.join(",", activeProfilesList));
		}
	}
	
	public void addHostName(Map map)
	{
		try {
			String hostName=InetAddress.getLocalHost().getHostName();
			map.put(HOST_KEY, hostName);
		}catch (Exception e) {
		log.warn("Unable to obtain hostname", e);
		}
		
	}

}
