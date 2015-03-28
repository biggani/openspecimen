package com.krishagni.catissueplus.core.common.events;

public enum Resource {
	CP("CollectionProtocol"),
	
	PARTICIPANT_PHI("ParticipantPhi"),
	
	VISIT("Visit"),
	
	SPECIMEN("Specimen"),
	
	ORDER("Order"),
	
	STORAGE_CONTAINER("StorageContainer"),
	
	USER("User"),
	
	DISTRIBUTION_ORDER("DistributionOrder"),
	
	DP("DistributionProtocol"),
	
	SITE("Site");
	
	private final String name;
	
	private Resource(String name) {
		this.name = name;
	}
	
	public String getName() { 
		return name;
	}
}
