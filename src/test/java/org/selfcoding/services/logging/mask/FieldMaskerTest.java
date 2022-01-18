package org.selfcoding.services.logging.mask;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.selfcoding.services.logging.mask.FieldMasker.MASK;
import static org.selfcoding.services.logging.mask.FieldMasker.MaskingStrategy.FULL;
import static org.selfcoding.services.logging.mask.FieldMasker.MaskingStrategy.LAST_4;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@SpringBootTest(classes = {FieldMaskerImpl.class,ObjectMapper.class})
class FieldMaskerTest {

	private final FieldMasker fieldMasker;
	private final ObjectMapper objectMapper;
	
	@Nested
	class MaskJsonForLogging{
		
		@Test
		void maskDefaultKey_whenGivenJson() throws  JsonProcessingException {
			String maskedJson=fieldMasker.maskJsonForLogging("{\"password\": \"password\"}");
			
			Map<String, Object> map= objectMapper.readValue(maskedJson, new TypeReference<Map<String, Object>>() {
			});
			
			Object value=map.get("password");
			assertThat(value).isEqualTo(MASK); 
		}
		
		
		@Test
		void maskDefaultKey_whenKeyContainsInNestedObject() throws  JsonProcessingException {
			String maskedJson=fieldMasker.maskJsonForLogging("{\n"+
					 "	\"name\": \"sachin\",\n" +
					 "	\"object\":{\n " +
					 "	\"password\": \"password\"\n" +
					 "	}\n"+
					"}");
			
			Map<String, Object> map= objectMapper.readValue(maskedJson, new TypeReference<Map<String, Object>>() {
			});
			Map<String, Object> nestedObject= (Map) map.get("object");
			String value=(String) nestedObject.get("password");
			assertThat(value).isEqualTo(MASK); 
		}
		
		@Test
		void works_whenJsonHasNestedListOfObjects() throws  JsonProcessingException {
			String jsonWithNesteList="{\n"+
					 "	\"list\": [\n" +
					 "{\n"+
					 "  \t \"foo\" : \"bar\",\n" +
					 "	\"password\": \"moo\"\n" +
					 "	}\n"+
					 "	]\n"+
					"}";
			String maskedJson =fieldMasker.maskJsonForLogging(jsonWithNesteList);
			Map<String, Object> map= objectMapper.readValue(maskedJson, new TypeReference<Map<String, Object>>() {
			});
			
			List<Object> list=(List<Object>) map.get("list");
			Map<String, Object> object=(Map<String, Object>) list.get(0);
			assertThat(object).containsEntry("password", MASK); 
		}
		
		@Test
		void works_whenJsonisListOfObjects() throws  JsonProcessingException {
			String jsonWithNesteList="[\n" +
					 "	{\n"+
					 "  \t \"foo\" : \"bar\",\n" +
					 "	\t \"password\": \"moo\"\n" +
					 "	},\n"+
					 "	{\n"+
					 "  \t \"foo\" : \"bar\",\n" +
					 "	\t \"password\": \"moo\"\n" +
					 "	}\n"+
					 "	]\n";
			String maskedJson =fieldMasker.maskJsonForLogging(jsonWithNesteList);
			List<Object> list= objectMapper.readValue(maskedJson, new TypeReference<List<Object>>() {
			});
			
			Map<String, Object> object1=(Map<String, Object>) list.get(0);
			Map<String, Object> object2=(Map<String, Object>) list.get(1);
			assertThat(object1).containsEntry("password", MASK); 
			assertThat(object2).containsEntry("password", MASK); 
		}
		
		@Test
		void throwsJsonFieldMaskingException_whenGivenNonJsonString() {
			assertThrows(JsonFieldMaskingException.class, ()-> fieldMasker.maskJsonForLogging("foo"));
		}
		
		
		@Nested
		class Last4
		{
			@Test 
			void works() throws JsonProcessingException{
				String json="{\"cardNumber\" : \"1234567890123456\" }";
				fieldMasker.addLoggingMaskKey("cardNumber",LAST_4);
				
				String maskedJson=fieldMasker.maskJsonForLogging(json);
				
				Map<String, Object> map=objectMapper.readValue(maskedJson, Map.class);
				
				assertThat(map).containsEntry("cardNumber", "************3456");
			}
			
			@Test 
			void returnsNull_whenGivenNull() throws JsonProcessingException{
				String json="{\"cardNumber\" : null }";
				fieldMasker.addLoggingMaskKey("cardNumber",LAST_4);
				
				String maskedJson=fieldMasker.maskJsonForLogging(json);
				
				Map<String, Object> map=objectMapper.readValue(maskedJson, Map.class);
				
				assertThat(map).containsEntry("cardNumber", null);
			}
			
			@Test 
			void returnsEmptyString_whenGivenEmptytring() throws JsonProcessingException{
				String json="{\"cardNumber\" : \"\" }";
				fieldMasker.addLoggingMaskKey("cardNumber",LAST_4);
				
				String maskedJson=fieldMasker.maskJsonForLogging(json);
				
				Map<String, Object> map=objectMapper.readValue(maskedJson, Map.class);
				
				assertThat(map).containsEntry("cardNumber", "");
			}
			
			@Test 
			void mask_whenGivenSgtringLessThenFourCharsInLength() throws JsonProcessingException{
				String json="{\"cardNumber\" : \"123\" }";
				fieldMasker.addLoggingMaskKey("cardNumber",LAST_4);
				
				String maskedJson=fieldMasker.maskJsonForLogging(json);
				
				Map<String, Object> map=objectMapper.readValue(maskedJson, Map.class);
				
				assertThat(map).containsEntry("cardNumber", "123");
			}
			
			@Test 
			void maskPciData_whenPciDataKeyConfigured() throws JsonProcessingException{
				fieldMasker.addPciMaskKey("pciDataKey", FULL);
				String maskedJson=fieldMasker.maskJson(
						"{\n"+
						 "	\"pciDataKey\" : \"blahaaa\" ,\n" +
						 "	\"piiDataKey\" : \"foooooo\" \n" +
						  "}",true,false);
				fieldMasker.addLoggingMaskKey("cardNumber",LAST_4);
				
				
				Map<String, Object> map=objectMapper.readValue(maskedJson, new TypeReference<Map<String, Object>>() {
				});
				
				Object value=map.get("pciDataKey");
				assertThat(value).isEqualTo(MASK);
			}
			
			@Test 
			void maskPiiData_whenPiiDataKeyConfigured() throws JsonProcessingException{
				fieldMasker.addPciMaskKey("piiDataKey", FULL);
				String maskedJson=fieldMasker.maskJson(
						"{\n"+
						 "	\"pciDataKey\" : \"blahaaa\" ,\n" +
						 "	\"piiDataKey\" : \"foooooo\" \n" +
						  "}",true,false);
				fieldMasker.addLoggingMaskKey("cardNumber",LAST_4);
				
				
				Map<String, Object> map=objectMapper.readValue(maskedJson, new TypeReference<Map<String, Object>>() {
				});
				
				Object value=map.get("piiDataKey");
				assertThat(value).isEqualTo(MASK);
			}
		}
		
	}
	
	
	@Nested
	class KeyValuePair
	{
		@Test
		void works_whenKeyValuePairPassed() throws JsonProcessingException{
		String json="{\n"
				+ "	\"metadata\": \n"
				+ "		[\n"
				+ "			{\n"
				+ "				\"key\": \"cardNumber\",\n"
				+ "				\"value\": \"456278941234\"	\n"
				+ "			},\n"
				+ "			{\n"
				+ "				\"key\": \"cardStatus\",\n"
				+ "				\"value\": \"A\"	\n"
				+ "			},	\n"
				+ "			{\n"
				+ "				\"key\": \"cardExpiryDate\",\n"
				+ "				\"value\": \"2023-09\"	\n"
				+ "			}				\n"
				+ "		]\n"
				+ "}";
		String expectedJson="{\n"
				+ "	\"metadata\": \n"
				+ "		[\n"
				+ "			{\n"
				+ "				\"key\": \"cardNumber\",\n"
				+ "				\"value\": \"********1234\"	\n"
				+ "			},\n"
				+ "			{\n"
				+ "				\"key\": \"cardStatus\",\n"
				+ "				\"value\": \"A\"	\n"
				+ "			},	\n"
				+ "			{\n"
				+ "				\"key\": \"cardExpiryDate\",\n"
				+ "				\"value\": \"******\"	\n"
				+ "			}				\n"
				+ "		]\n"
				+ "}";
		
		String maskedJson=fieldMasker.maskJsonForLogging(json);
		assertThat(objectMapper.readTree(maskedJson)).isEqualTo(objectMapper.readTree(expectedJson));
		}
		
		@Test
		void works_whenKeyValueInReverseorderPassed() throws JsonProcessingException{
		String json="{\n"
				+ "\"metadata\": \n"
				+ "[\n"
				+ "{\n"
				+ "\"value\": \"456278941234\",\n"
				+ "\"key\": \"cardNumber\"\n"
				+ "},\n"
				+ "{\n"
				+ "\"value\": \"A\",\n"
				+ "\"key\": \"cardStatus\"\n"
				+ "},\n"
				+ "{\n"
				+ "\"value\": \"2023-09\",\n"
				+ "\"key\": \"cardExpiryDate\"\n"
				+ "}\n"
				+ "]\n"
				+ "}";
		String expectedJson="{\n"
				+ "\"metadata\": \n"
				+ "[\n"
				+ "{\n"
				+ "\"value\": \"********1234\",\n"
				+ "\"key\": \"cardNumber\"\n"
				+ "},\n"
				+ "{\n"
				+ "\"value\": \"A\",\n"
				+ "\"key\": \"cardStatus\"\n"
				+ "},\n"
				+ "{\n"
				+ "\"value\": \"******\",\n"
				+ "\"key\": \"cardExpiryDate\"\n"
				+ "}\n"
				+ "]\n"
				+ "}";
		
		String maskedJson=fieldMasker.maskJsonForLogging(json);
		assertThat(objectMapper.readTree(maskedJson)).isEqualTo(objectMapper.readTree(expectedJson));
		}
		
		@Test
		void works_whenKeyValuePairAndNormalFieldPassed() throws JsonProcessingException{
		String json="{\n"
				+ "	\"ssn\": \"456346\", \n"
				+ "	\"metadata\": \n"
				+ "		[\n"
				+ "			{\n"
				+ "				\"key\": \"cardNumber\",\n"
				+ "				\"value\": \"456278941234\"	\n"
				+ "			},\n"
				+ "			{\n"
				+ "				\"key\": \"cardStatus\",\n"
				+ "				\"value\": \"A\"	\n"
				+ "			},	\n"
				+ "			{\n"
				+ "				\"key\": \"cardExpiryDate\",\n"
				+ "				\"value\": \"2023-09\"	\n"
				+ "			}				\n"
				+ "		]\n"
				+ "}";
		String expectedJson="{\n"
				+ "	\"ssn\": \"******\", \n"
				+ "	\"metadata\": \n"
				+ "		[\n"
				+ "			{\n"
				+ "				\"key\": \"cardNumber\",\n"
				+ "				\"value\": \"********1234\"	\n"
				+ "			},\n"
				+ "			{\n"
				+ "				\"key\": \"cardStatus\",\n"
				+ "				\"value\": \"A\"	\n"
				+ "			},	\n"
				+ "			{\n"
				+ "				\"key\": \"cardExpiryDate\",\n"
				+ "				\"value\": \"******\"	\n"
				+ "			}				\n"
				+ "		]\n"
				+ "}";
		
		String maskedJson=fieldMasker.maskJsonForLogging(json);
		assertThat(objectMapper.readTree(maskedJson)).isEqualTo(objectMapper.readTree(expectedJson));
		}
		
		@Test
		void works_whenKeyWithOutValuePassed() throws JsonProcessingException{
		String json="{\n"
				+ "\"metadata\": \n"
				+ "[\n"
				+ "{\n"
				+ "\"key\": \"cardNumber\"\n"
				+ "},\n"
				+ "{\n"
				+ "\"key\": \"cardStatus\"\n"
				+ "},\n"
				+ "{\n"
				+ "\"key\": \"cardExpiryDate\"\n"
				+ "}\n"
				+ "]\n"
				+ "}";		
		String maskedJson=fieldMasker.maskJsonForLogging(json);
		assertThat(objectMapper.readTree(maskedJson)).isEqualTo(objectMapper.readTree(json));
		}
		
		@Test
		void works_whenFieldsPassedWithOutKeyValuePair() throws JsonProcessingException{
			String json="{\n"
					+ "	\"ssn\":\"123232322\",\n"
					+ "	\"firstName\":\"Sachin\",\n"
					+ "	\"lastName\":\"HS\",\n"
					+ "	\"age\":29,\n"
					+ "	\"cardDetails\":\n"
					+ "		[\n"
					+ "			{\n"
					+ "				\"type\":\"VISA\",\n"
					+ "				\"cardNumber\":\"123445681234\",\n"
					+ "				\"cardExpiryDate\":\"2026-09\"				\n"
					+ "			}\n"
					+ "		]\n"
					+ "}";
			String expectedJson="{\n"
					+ "	\"ssn\":\"******\",\n"
					+ "	\"firstName\":\"Sachin\",\n"
					+ "	\"lastName\":\"HS\",\n"
					+ "	\"age\":29,\n"
					+ "	\"cardDetails\":\n"
					+ "		[\n"
					+ "			{\n"
					+ "				\"type\":\"VISA\",\n"
					+ "				\"cardNumber\":\"********1234\",\n"
					+ "				\"cardExpiryDate\":\"******\"				\n"
					+ "			}\n"
					+ "		]\n"
					+ "}";
			String maskedJson=fieldMasker.maskJsonForLogging(json);
			assertThat(objectMapper.readTree(maskedJson)).isEqualTo(objectMapper.readTree(expectedJson));
		}
	}

	@Nested
	class MaskSoap{
		
	}
	
}
