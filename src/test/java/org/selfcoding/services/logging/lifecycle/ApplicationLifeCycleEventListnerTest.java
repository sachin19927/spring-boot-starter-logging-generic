package org.selfcoding.services.logging.lifecycle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.selfcoding.services.logging.config.LoggingConstants.EVENT_NAME_COMPONENT_STARTED;
import static org.selfcoding.services.logging.config.LoggingConstants.EVENT_NAME_COMPONENT_STOPPED;
import static org.selfcoding.services.logging.config.LoggingConstants.EVENT_NAME_KEY;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.selfcoding.services.logging.MemoryAppender;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.ContextClosedEvent;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import lombok.RequiredArgsConstructor;
import net.logstash.logback.marker.ObjectAppendingMarker;

@RequiredArgsConstructor
@SpringBootTest(classes = {LifeCycleLogginAutoconfig.class})
public class ApplicationLifeCycleEventListnerTest {

	private final ApplicationEventPublisher applicationEventPublisher;
	private final MemoryAppender memoryAppender=new MemoryAppender();
	
	@BeforeEach
	void setUp() {
		
	Logger logger=	(Logger) LoggerFactory.getLogger(ApplicationLifeCycleEventListner.class);
	memoryAppender.setContext(  (LoggerContext) LoggerFactory.getILoggerFactory() );
	logger.setLevel(Level.DEBUG);
	logger.addAppender(memoryAppender);
	memoryAppender.start();
	
	}
	
	@SuppressWarnings("deprecation")
	@Test
	void logsComponentStartedEvent_whenApplicationStartedEventOccurs() {
		applicationEventPublisher.publishEvent(new ApplicationStartedEvent(mock(SpringApplication.class), null, null));
	
		List<ILoggingEvent> loggingEvents=memoryAppender.search("The Component has started.");
		assertThat(loggingEvents).isNotEmpty();
		
		ObjectAppendingMarker argument=memoryAppender.getArgument(EVENT_NAME_KEY, loggingEvents.get(0).getArgumentArray());
		assertThat(argument).isNotNull();
		assertThat(argument.getFieldValue()).isEqualTo(EVENT_NAME_COMPONENT_STARTED);
		
	}
	
	@Test
	void logsComponentStoppedEvent_whenApplicationClosedEventOccurs() {
		applicationEventPublisher.publishEvent(new ContextClosedEvent(mock(org.springframework.context.ConfigurableApplicationContext.class)));
		
		List<ILoggingEvent> loggingEvents=memoryAppender.search("The Component has stooped.");
		assertThat(loggingEvents).isNotEmpty();
		
		ObjectAppendingMarker argument=memoryAppender.getArgument(EVENT_NAME_KEY, loggingEvents.get(0).getArgumentArray());
		assertThat(argument).isNotNull();
		assertThat(argument.getFieldValue()).isEqualTo(EVENT_NAME_COMPONENT_STOPPED);
	}
}
