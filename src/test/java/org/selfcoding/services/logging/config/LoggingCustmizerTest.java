package org.selfcoding.services.logging.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.selfcoding.services.logging.config.LoggingConstants.COMPONENT_ID_KEY;
import static org.selfcoding.services.logging.config.LoggingConstants.COMPONENT_VERSION_KEY;
import static org.selfcoding.services.logging.config.LoggingConstants.EVENT_TYPE_KEY;
import static org.selfcoding.services.logging.config.LoggingConstants.EVENT_TYPE_USR;
import static org.selfcoding.services.logging.config.LoggingConstants.HOST_KEY;
import static org.selfcoding.services.logging.config.LoggingConstants.SPRING_PROFILES_ACTIVE_KEY;
import static org.selfcoding.services.logging.config.LoggingConstants.SUBCOMPONENT_VERSION_KEY;
import static org.selfcoding.services.logging.config.LoggingConstants.SUBSYSTEM_ID_KEY;
import static org.slf4j.Logger.ROOT_LOGGER_NAME;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import org.selfcoding.services.logging.BuildPropertiesConfig;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.composite.JsonProviders;
import net.logstash.logback.composite.loggingevent.LoggingEventPatternJsonProvider;
import net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder;
@Slf4j
@ActiveProfiles("foo")
@RequiredArgsConstructor
@SpringBootTest(classes = {LoggingConfig.class,LoggingConfigProperties.class,FileAppenderConfig.class,BuildPropertiesConfig.class},
properties = {"selfcoding.log.componentId=ComponentId","selfcoding.log.componentVersion=ComponentVersion",
		"selfcoding.log.subSystemId=SubSystemId","selfcoding.log.logstash-servers="
		} )
public class LoggingCustmizerTest {

	private final LoggingConfigProperties loggingConfigProperties;
	private final BuildProperties buildProperties;
	
	void addCustomFieldsToPatternJsonProvider_whenApplicationStarts() throws JsonProcessingException,UnknownHostException
	{
		
		Logger root= (Logger) org.slf4j.LoggerFactory.getLogger(ROOT_LOGGER_NAME);
		ConsoleAppender consoleAppender= (ConsoleAppender) root.getAppender("Console");
		
		LoggingEventCompositeJsonEncoder encoder= (LoggingEventCompositeJsonEncoder) consoleAppender.getEncoder();
		
		JsonProviders<ILoggingEvent> providers =encoder.getProviders();
	
		String pattern= providers.getProviders().stream()
				.filter(LoggingEventPatternJsonProvider.class::isInstance)
				.map(LoggingEventPatternJsonProvider.class::cast)
				.findFirst().get().getPattern();
		
		ObjectMapper objectMapper=new ObjectMapper();
		
		Map<String, String> map = objectMapper.readValue(pattern, Map.class);
		
		String eventType=map.get(EVENT_TYPE_KEY);
		String subSystemId=map.get(SUBSYSTEM_ID_KEY);
		String host=map.get(HOST_KEY);
		String componentId=map.get(COMPONENT_ID_KEY);
		String subComponenetVersion=map.get(SUBCOMPONENT_VERSION_KEY);
		String componentVersion=map.get(COMPONENT_VERSION_KEY);
		String springProfilesActive=map.get(SPRING_PROFILES_ACTIVE_KEY);
		
		String hostName=InetAddress.getLocalHost().getHostName();
		
		assertThat(eventType).isNotNull().isEqualTo(EVENT_TYPE_USR);
		assertThat(subSystemId).isNotNull().isEqualTo(loggingConfigProperties.getSubSystemId());
		assertThat(host).isNotNull().isEqualTo(hostName);
		assertThat(componentId).isNotNull().isEqualTo(loggingConfigProperties.getComponentId());
		assertThat(subComponenetVersion).isNotNull().isEqualTo(buildProperties.getVersion());
		assertThat(componentVersion).isNotNull().isEqualTo(loggingConfigProperties.getComponentVersion());
		assertThat(springProfilesActive).isNotNull().isEqualTo("foo");
		
	}
}
