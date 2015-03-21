package com.krishagni.rbac.repository;

import java.util.List;
import java.util.Set;

import com.krishagni.catissueplus.core.common.repository.Dao;
import com.krishagni.rbac.domain.Subject;
import com.krishagni.rbac.events.CpSiteInfo;
import com.krishagni.rbac.events.UserAccessInformation;

public interface SubjectDao extends Dao<Subject> {
	public Subject getSubject(Long subjectId);
	
	public List<CpSiteInfo> getAccessibleCpSites(UserAccessInformation accessInfo);
	
	public Set<Long> getCpsBySitesIds(Set<Long> siteIds);
	
	public boolean canUserAccess(UserAccessInformation accessInfo);
}
