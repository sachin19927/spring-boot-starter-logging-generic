package org.selfcoding.services.logging.mask;

import static org.selfcoding.services.logging.mask.FieldMasker.MaskingStrategy.FULL;
import static org.selfcoding.services.logging.mask.FieldMasker.MaskingStrategy.LAST_4;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.xml.messaging.saaj.soap.impl.ElementImpl;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FieldMaskerImpl implements FieldMasker {

	private final ObjectMapper objectMapper;
	
	private static final String KEY_AS_KEY="key";
	private static final String VALUE_AS_KEY="value";
	private static final Map<String, MaskingStrategy> LOGGING_MASKED_FIELDS=new HashMap<>();
	private static final Map<String, MaskingStrategy> PCI_LOGGING_MASKED_FIELDS=new HashMap<>();
	private static final Map<String, MaskingStrategy> PII_LOGGING_MASKED_FIELDS=new HashMap<>();
	
	static {
		LOGGING_MASKED_FIELDS.put("password", FULL);
		LOGGING_MASKED_FIELDS.put("creditcard", LAST_4);
		LOGGING_MASKED_FIELDS.put("creditcardnumber", LAST_4);
		LOGGING_MASKED_FIELDS.put("cardnumber", LAST_4);
		LOGGING_MASKED_FIELDS.put("cardexpirydate", FULL);
		LOGGING_MASKED_FIELDS.put("dob", FULL);
		LOGGING_MASKED_FIELDS.put("ssn", FULL);
		LOGGING_MASKED_FIELDS.put("birthdate", FULL);
		LOGGING_MASKED_FIELDS.put("accountnumber", FULL);
		LOGGING_MASKED_FIELDS.put("cardholdername", FULL);
		LOGGING_MASKED_FIELDS.put("cardexpirationdate", FULL);
		LOGGING_MASKED_FIELDS.put("expirationdate", FULL);
		LOGGING_MASKED_FIELDS.put("billingaccountnumber", FULL);

		PCI_LOGGING_MASKED_FIELDS.put("ssn", LAST_4);
		PCI_LOGGING_MASKED_FIELDS.put("CVV", FULL);
		PCI_LOGGING_MASKED_FIELDS.put("creditcardnumber", LAST_4);
		PCI_LOGGING_MASKED_FIELDS.put("debitcardnumber", LAST_4);
		PCI_LOGGING_MASKED_FIELDS.put("pannumber", LAST_4);
		PCI_LOGGING_MASKED_FIELDS.put("alternatecardnumber", LAST_4);
		PCI_LOGGING_MASKED_FIELDS.put("billingaccountnumber", LAST_4);
		PCI_LOGGING_MASKED_FIELDS.put("visa", LAST_4);
		PCI_LOGGING_MASKED_FIELDS.put("mastercard", LAST_4);
		PCI_LOGGING_MASKED_FIELDS.put("cardnumber", LAST_4);
		PCI_LOGGING_MASKED_FIELDS.put("cardexpirydate", FULL);
		PCI_LOGGING_MASKED_FIELDS.put("dob", FULL);
		PCI_LOGGING_MASKED_FIELDS.put("birthdate", FULL);
		PCI_LOGGING_MASKED_FIELDS.put("accountnumber", LAST_4);
		PCI_LOGGING_MASKED_FIELDS.put("cardholdername", LAST_4);
		PCI_LOGGING_MASKED_FIELDS.put("cardexpirationdate", FULL);
		PCI_LOGGING_MASKED_FIELDS.put("expirationdate", FULL);
		PCI_LOGGING_MASKED_FIELDS.put("billingaccountnumber", FULL);
	
	}
	
	@Override
	public String maskJsonForLogging(String json) {
		
		return maskJson(json, false, false,true);
	}

	@Override
	public String maskJson(String json, boolean maskPci, boolean maskPii) {
		return maskJson(json, maskPci, maskPii,false);	
	}

	@Override
	public String maskSoap(String requestBody) {

		try {
			SOAPMessage soapMessage=stringToMessage(requestBody);
			soapMessage.getSOAPPart().getEnvelope();
			breadthFirstTraversal(soapMessage.getSOAPPart());
			return messageToString(soapMessage);
			
		}
		catch (SOAPException | IOException e) {
			return "[UNKNOWN]";
		}
	}

	@Override
	public FieldMasker addLoggingMaskKey(String string, MaskingStrategy strategy) {
		LOGGING_MASKED_FIELDS.put(string.toLowerCase(), strategy);
		return this;
	}

	@Override
	public FieldMasker addPciMaskKey(String string, MaskingStrategy strategy) {
		PCI_LOGGING_MASKED_FIELDS.put(string.toLowerCase(), strategy);
		return this;
	}

	@Override
	public FieldMasker addPiiMaskKey(String string, MaskingStrategy strategy) {
		PII_LOGGING_MASKED_FIELDS.put(string.toLowerCase(), strategy);
		return this;
	}

	
	private String maskJson(String json,boolean maskPci,boolean maskPii,boolean logging)
	{
		try {
			Object map=objectMapper.readValue(json, Object.class);
			maskFields(map, maskPci, maskPii, logging);
			return objectMapper.writeValueAsString(map);
		}
		catch (JsonProcessingException e) {
			throw new JsonFieldMaskingException(e);
		}
	}
	
	private void maskFields(Object jsonMap,boolean maskPci, boolean maskPii, boolean logging) {
		if(jsonMap instanceof Map)
		{
			handleMap((Map<String,Object>) jsonMap, maskPci, maskPii, logging);
		}
		if(jsonMap instanceof List)
		{
			handleList((List<Object>) jsonMap, maskPci, maskPii, logging);
		}
	}
	
	private void handleList(List<Object> list,boolean maskPci, boolean maskPii, boolean logging) {
		for(Object value: list) {
			if(value instanceof Map || value instanceof List)
			{
				maskFields(value, maskPci, maskPii, logging);
			}
		}
	}
	
	private void handleMap(Map<String, Object>map,boolean maskPci, boolean maskPii, boolean logging)
	{
		for(Map.Entry<String, Object> entry : map.entrySet()){
			Object value=entry.getValue();
			if(value instanceof String)
			{
				String lowercasekey=entry.getKey().toLowerCase();
				String lowercasevalue=((String) value).toLowerCase();
				checkForFields(logging, entry, lowercasekey, lowercasevalue, map, LOGGING_MASKED_FIELDS);
				checkForFields(maskPci, entry, lowercasekey, lowercasevalue, map, PCI_LOGGING_MASKED_FIELDS);
				checkForFields(maskPii, entry, lowercasekey, lowercasevalue, map, PII_LOGGING_MASKED_FIELDS);
			}else if(value instanceof Map || value instanceof List) {
				maskFields(value, maskPci, maskPii, logging);
			}
		}
	}
	
	private void checkForFields(boolean logginTypeEnabled,Map.Entry<String, Object> entry,String lowercasekey,String lowercasevalue, Map<String, Object> map,Map<String, MaskingStrategy> fieldsToMaskMap) {
		if(logginTypeEnabled && fieldsToMaskMap.containsKey(lowercasekey))
		{
			MaskingStrategy maskingStrategy=fieldsToMaskMap.get(lowercasekey);
			if(FULL.equals(maskingStrategy))
			{
				entry.setValue(MASK);
			}
			else if(LAST_4.equals(maskingStrategy)&& entry.getValue() instanceof String) {
				String masked=maskLast4((String)entry.getValue());
				entry.setValue(masked);
			}
		}
		else if(logginTypeEnabled && map.size()==2 && lowercasekey.equalsIgnoreCase(KEY_AS_KEY) && fieldsToMaskMap.containsKey(lowercasevalue) && map.containsKey(VALUE_AS_KEY))
		{
			MaskingStrategy maskingStrategy=fieldsToMaskMap.get(lowercasevalue);
			if(FULL.equals(maskingStrategy))
			{
				map.replace(VALUE_AS_KEY, MASK);
			}
			else if(LAST_4.equals(maskingStrategy)&& entry.getValue() instanceof String) {
				String masked=maskLast4((String)map.get(VALUE_AS_KEY));
				map.replace(VALUE_AS_KEY, masked);
			}

		}
	}
	
	private String messageToString(SOAPMessage soap)throws SOAPException,IOException
	{
		ByteArrayOutputStream stream= new ByteArrayOutputStream();
		soap.writeTo(stream);
		return new String(stream.toByteArray(),StandardCharsets.UTF_8);
	}
	
	private SOAPMessage stringToMessage(String soap) throws SOAPException,IOException
	{
		InputStream inputStream=new ByteArrayInputStream(soap.getBytes());
		return MessageFactory.newInstance().createMessage(null, inputStream);
	}
	
	private void breadthFirstTraversal(Node node)
	{
		if(node instanceof Element)
		{
			String elementLocalName=((ElementImpl)node).getElementName().getLocalName();
			Node firstChild=node.getFirstChild();
			if(firstChild!=null && LOGGING_MASKED_FIELDS.containsKey(elementLocalName.toLowerCase())) {
				firstChild.setTextContent(MASK);
			}
		}
	}
	
	
	private String maskLast4(String string)
	{
		if(string==null)
		return null;
		
		int length=string.length();
		
		if("".equals(string) || string.length()<=4)
		{
			return string; 
		}
		
		String stars= IntStream.range(0, length-4).mapToObj(i->"*").collect(Collectors.joining());
		
		return stars+ string.substring(length-4,length);
	}
	
}
