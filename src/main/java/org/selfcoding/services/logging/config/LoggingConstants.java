package org.selfcoding.services.logging.config;

public class LoggingConstants {

	private LoggingConstants() {
	}

	public static final  String CORRELATION_ID_MDC_KEY="CorrelationId";
	public static final  String CORRELATION_ID_HEADER_KEY="v-correlation-id";
	public static final  String ALTERNATE_CORRELATION_ID_HEADER_KEY="V_CorrelationId";

	public static final  String SUBSYSTEM_ID_KEY="SubSystemId";
	public static final  String COMPONENT_ID_KEY="ComponentId";
	public static final  String REQUEST_BODY_KEY="RequestBody";
	public static final  String RESPONSE_BODY_KEY="ResponseBody";
	public static final  String COMPONENT_VERSION_KEY="ComponentVersion";
	public static final  String SUBCOMPONENT_VERSION_KEY="SubComponentVersion";
	public static final  String HOST_KEY="Host";
	public static final  String EVENT_TYPE_KEY="EventType";
	public static final  String EVENT_NAME_KEY="EventName";
	public static final  String URL_KEY="URL";
	public static final  String ENDPOINT="Endpoint";
	public static final  String SPRING_PROFILES_ACTIVE_KEY="SpringProfilesActive";
	public static final  String ENVIRONMENT_KEY="Environment";
	
	
	public static final  String EVENT_NAME_COMPONENT_STARTED="ComponentStarted";
	public static final  String EVENT_NAME_COMPONENT_STOPPED="ComponentStopped";
	public static final  String EVENT_NAME_REQUEST_SENT="RequestSent";
	public static final  String EVENT_NAME_REQUEST_RECEIVED="RequestReceived";
	public static final  String EVENT_NAME_RESPONSE_RECEIVED="ResponseReceived";
	public static final  String EVENT_NAME_RESPONSE_SENT="ResponseSent";
	
	public static final  String REQUEST_HTTP_METHOD_KEY="HttpMethod";
	public static final  String RESPONSE_HTTP_STATUS_KEY="HttpStatus";
	
	public static final  String EVENT_TYPE_USR="USR";
	
	public static final  String BODY_UNKNOWN="[unknown]";
}
