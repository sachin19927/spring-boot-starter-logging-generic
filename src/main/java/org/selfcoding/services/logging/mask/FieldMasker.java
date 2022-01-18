package org.selfcoding.services.logging.mask;

public interface FieldMasker {

	String maskJsonForLogging(String json);
	
	String maskJson(String json,boolean maskPci,boolean maskPii);
	
	String maskSoap(String soap);
	
	FieldMasker addLoggingMaskKey(String string,MaskingStrategy strategy);

	FieldMasker addPciMaskKey(String string,MaskingStrategy strategy);
	
	FieldMasker addPiiMaskKey(String string,MaskingStrategy strategy);

	String MASK="******";
	enum MaskingStrategy{
		FULL,LAST_4
	}

}
