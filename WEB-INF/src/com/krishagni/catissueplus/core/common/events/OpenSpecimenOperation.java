package com.krishagni.catissueplus.core.common.events;

public enum OpenSpecimenOperation {
	CREATE("create"),
	
	READ("read"),
	
	UPDATE("update"),
	
	DELETE("delete");
	
	private final String text;
	
	private OpenSpecimenOperation(String text) {
		this.text = text;
	}
	
	@Override
	public String toString() {
		return text;
	}
}
