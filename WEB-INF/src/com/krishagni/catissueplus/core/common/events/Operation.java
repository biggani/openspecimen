package com.krishagni.catissueplus.core.common.events;

public enum Operation {
	CREATE("create"),
	
	READ("read"),
	
	UPDATE("update"),
	
	DELETE("delete");
	
	private final String text;
	
	private Operation(String text) {
		this.text = text;
	}
	
	@Override
	public String toString() {
		return text;
	}
}
