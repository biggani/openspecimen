package com.krishagni.catissueplus.core.common.events;

public enum Resource {
	CP("CollectionProtocol"),
	
	CPR("Participant (PHI)"),
	
	VISIT("Visit"),
	
	SPECIMEN("Specimen"),
	
	ORDER("Order"),
	
	STORAGE_CONTAINER("StorageContainer"),
	
	USER("User"),
	
	DISTRIBUTION_ORDER("DistributionOrder");
	
	private final String name;
	
	private Resource(String name) {
		this.name = name;
	}
	
	public String getName() { 
		return name;
	}
}
