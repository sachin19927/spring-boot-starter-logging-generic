package org.selfcoding.services.logging;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class TestContoller {

	@PostMapping("/test")
	public Map<String, Object> post(@RequestBody Map<String, Object> map)
	{
		log.info("Test Contrller hit");
		HashMap<String, Object> responseMap=new HashMap<>();
		responseMap.put("bar","foo");
		return responseMap;
	}
	
	@GetMapping("/mask")
	public Map<String, Object> mask()
	{
		log.info("Test mask Contrller hit");
		HashMap<String, Object> responseMap=new HashMap<>();
		responseMap.put("cardnumber","1234567891");
		return responseMap;
	}
	
	@GetMapping("/actuator")
	public Map<String, Object> actuator()
	{
		log.info("Test actuator Contrller hit");
		HashMap<String, Object> responseMap=new HashMap<>();
		responseMap.put("status","UP");
		return responseMap;
	}
	
	@PostMapping("/testinvalidjson")
	public String postInvalidJson(@RequestBody String string)
	{
		return "bar";
	}
	
	@PostMapping("/blacklisted")
	public String blacklisted(@RequestBody String string)
	{
		return "bar";
	}
}
