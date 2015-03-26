package com.krishagni.catissueplus.core.common.events;

public enum Resource {
	CP("collection-protocol"),
	
	CPR("collection-protocol-registration"),
	
	VISIT("visit"),
	
	SPECIMEN("specimen");
	
	private final String text;
	
	private Resource(String text) {
		this.text = text;
	}
	
	@Override
	public String toString() {
		return text;
	}
}
