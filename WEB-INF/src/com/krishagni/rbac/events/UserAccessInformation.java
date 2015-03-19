package com.krishagni.rbac.events;

import java.util.HashSet;
import java.util.Set;

public class UserAccessInformation {
	private Long subjectId;
	
	private Long groupId;
	
	private Long cpId;
	
	private Set<Long> sites = new HashSet<Long>();
	
	private String resource;
	
	private String operation;
	
	private Long objectId;
	
	public Long subjectId() {
		return subjectId;
	}

	public UserAccessInformation subjectId(Long subjectId) {
		this.subjectId = subjectId;
		return this;
	}

	public Long groupId() {
		return groupId;
	}

	public UserAccessInformation groupId(Long groupId) {
		this.groupId = groupId;
		return this;
	}

	public Long cpId() {
		return cpId;
	}
	
	public UserAccessInformation cpId(Long cpId) {
		this.cpId = cpId;
		return this;
	}
	
	public Set<Long> sites() {
		return sites;
	}
	
	public UserAccessInformation sites(Set<Long> sites) {
		this.sites = sites;
		return this;
	}

	public String resource() {
		return resource;
	}

	public UserAccessInformation resource(String resourceName) {
		this.resource = resourceName;
		return this;
	}

	public String operation() {
		return operation;
	}

	public UserAccessInformation operation(String operationName) {
		this.operation = operationName;
		return this;
	}

	public Long objectId() {
		return objectId;
	}

	public UserAccessInformation objectId(Long resourceInstanceId) {
		this.objectId = resourceInstanceId;
		return this;
	}
}