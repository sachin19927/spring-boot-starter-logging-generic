package org.selfcoding.services.logging.lifecycle;

import static net.logstash.logback.argument.StructuredArguments.keyValue;
import static org.selfcoding.services.logging.config.LoggingConstants.EVENT_NAME_COMPONENT_STARTED;
import static org.selfcoding.services.logging.config.LoggingConstants.EVENT_NAME_COMPONENT_STOPPED;
import static org.selfcoding.services.logging.config.LoggingConstants.EVENT_NAME_KEY;

import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@RequiredArgsConstructor
@SuppressWarnings("PlaceholderCountMatchesArgumentCount")
public class ApplicationLifeCycleEventListner {

	@EventListener
	public void handleApplicationStartedEvent(ApplicationStartedEvent applicationStartedEvent) {
			log.info("The Component has started.",keyValue(EVENT_NAME_KEY, EVENT_NAME_COMPONENT_STARTED));
	}
	
	@EventListener
	public void handleContextClosedEvent(ContextClosedEvent contextClosedEvent) {
		log.info("The Component has stooped.",keyValue(EVENT_NAME_KEY, EVENT_NAME_COMPONENT_STOPPED));
	}
}
