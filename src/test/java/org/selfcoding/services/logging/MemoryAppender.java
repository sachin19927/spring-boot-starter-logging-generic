package org.selfcoding.services.logging;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.util.ReflectionUtils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import net.logstash.logback.marker.MapEntriesAppendingMarker;
import net.logstash.logback.marker.ObjectAppendingMarker;

public class MemoryAppender extends ListAppender<ILoggingEvent> {
	
	public void reset()
	{
		this.list.clear();
	}
	
	public boolean contains(String string, Level level)
	{
		return this.list.stream().anyMatch(event->event.getMessage().contains(string) && event.getLevel().equals(level));
	}
	
	public int countEventsForLogger(String loggerName) {
		return (int) this.list.stream().filter(event-> event.getLoggerName().contains(loggerName)).count();
	}

	public List<ILoggingEvent> search(String string )
	{
		return this.list.stream().filter(event-> event.getMessage().contains(string) ).collect(Collectors.toList());
	}
	
	
	public List<ILoggingEvent> search(String string, Level level )
	{
		return this.list.stream().filter(event-> event.getMessage().contains(string) && event.getLevel().equals(level)).collect(Collectors.toList());
	}
	
	public int getSize() {
		return this.list.size();
	}
	
	public List<ILoggingEvent> getLoggedEvents()
	{
		return Collections.unmodifiableList(this.list);
	}
	
	public ObjectAppendingMarker getArgument(String fieldNameTarget, Object[] requestRecievedArguments)
	{
		for(Object requestRecievedArgument: requestRecievedArguments ) {
			String fieldName=((ObjectAppendingMarker) requestRecievedArgument).getFieldName();
			if(fieldName.equals(fieldNameTarget)) {
				return (ObjectAppendingMarker) requestRecievedArgument;
			}
		}
		return null;
	}
	
	public Map.Entry<String, String> getMapEntry(String fieldNameTarget, Object[] requestRecievedArguments)
	{
		MapEntriesAppendingMarker mapEntries= (MapEntriesAppendingMarker)requestRecievedArguments[0];
		
		Field field=ReflectionUtils.findField(MapEntriesAppendingMarker.class, "map");
		ReflectionUtils.makeAccessible(field);
		Map<String, String> map= (Map) ReflectionUtils.getField(field, mapEntries);
		for(Map.Entry<String, String> entry : map.entrySet())
		{
			if(fieldNameTarget.equals(entry.getKey())) {
				return entry;
			}
		}
		return null;
	}
}
