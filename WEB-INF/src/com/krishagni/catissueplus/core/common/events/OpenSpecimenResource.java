package com.krishagni.catissueplus.core.common.events;

public enum OpenSpecimenResource {
	CP("collection-protocol"),
	
	CPR("collection-protocol-registration"),
	
	VISIT("visit"),
	
	SPECIMEN("specimen");
	
	private final String text;
	
	private OpenSpecimenResource(String text) {
		this.text = text;
	}
	
	@Override
	public String toString() {
		return text;
	}
}
