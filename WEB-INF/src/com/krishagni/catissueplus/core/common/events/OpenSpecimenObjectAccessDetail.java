package com.krishagni.catissueplus.core.common.events;

import java.util.HashSet;
import java.util.Set;

public class OpenSpecimenObjectAccessDetail {
	private Boolean canAccessAll;
	
	private Set<Long> ids = new HashSet<Long>();

	public OpenSpecimenObjectAccessDetail() {
		
	}
	
	public OpenSpecimenObjectAccessDetail(boolean canAccessAll) {
		this.canAccessAll = canAccessAll;
	}
	
	public Boolean getCanAccessAll() {
		return canAccessAll;
	}

	public void setCanAccessAll(Boolean canAccessAll) {
		this.canAccessAll = canAccessAll;
	}

	public Set<Long> getIds() {
		return ids;
	}

	public void setIds(Set<Long> ids) {
		this.ids = ids;
	}
	
	public static OpenSpecimenObjectAccessDetail canAccessAll() {
		OpenSpecimenObjectAccessDetail objAccess = new OpenSpecimenObjectAccessDetail();
		objAccess.setCanAccessAll(true);
		return objAccess;
	}
}
