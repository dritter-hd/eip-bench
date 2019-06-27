package com.eipbench.camel;

import java.util.UUID;

public class IdGenerator {
	
	private Boolean generateRandomIds = false;
	
	public IdGenerator() {
	
	}

	public IdGenerator(Boolean generateRandomIds) {
		this.generateRandomIds = generateRandomIds;
	}
	
	public String randomUUID(){
		if (generateRandomIds) {
			return Long.toString(UUID.randomUUID().getMostSignificantBits());
		}
		
		return "1";
	}

}
