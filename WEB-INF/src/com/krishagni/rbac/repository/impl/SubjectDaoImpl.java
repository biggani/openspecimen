package com.krishagni.rbac.repository.impl;

import java.util.ArrayList;
import java.util.List;

import com.krishagni.catissueplus.core.common.repository.AbstractDao;
import com.krishagni.rbac.domain.Subject;
import com.krishagni.rbac.events.CpSiteInfo;
import com.krishagni.rbac.events.UserAccessCriteria;
import com.krishagni.rbac.repository.SubjectDao;


public class SubjectDaoImpl extends AbstractDao<Subject> implements SubjectDao {
	@Override
	public Subject getSubject(Long subjectId) {
		return (Subject) sessionFactory.getCurrentSession()
				.get(Subject.class, subjectId);
	}
	

	@SuppressWarnings("unchecked")
	@Override
	public List<CpSiteInfo> getAccessibleCpSites(UserAccessCriteria accessInfo) {
		List<Object[]> rows = sessionFactory.getCurrentSession()
				.getNamedQuery(GET_CP_SITE_ACCESS_INFO)
				.setString("resource", accessInfo.resource())
				.setString("operation", accessInfo.operation())
				.setLong("userId", accessInfo.subjectId())
				.list();
		
		return getCpSites(rows);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public boolean canUserAccess(UserAccessCriteria info) {
		if (info.sites().isEmpty()) {
			info.sites().add(-1L);
		}
		
		List<Object[]> result = sessionFactory.getCurrentSession()
				.getNamedQuery(CAN_USER_ACCESS)
				.setString("resource", info.resource())
				.setString("operation", info.operation())
				.setLong("subjectId", info.subjectId())
				.setLong("cpId", info.cpId())
				.setParameterList("siteIds", info.sites())
				.list();
		
		return !result.isEmpty();
	}
	
	/*
	 * Private methods
	 */
	
	private List<CpSiteInfo> getCpSites(List<Object[]> rows) {
		List<CpSiteInfo> list = new ArrayList<CpSiteInfo>();
		for (Object[] row : rows) {
			list.add(new CpSiteInfo((Long)row[0], (Long)row[1]));
		}
		
		return list;
	}
	
	private static final String FQN = Subject.class.getName();

	private static final String GET_CP_SITE_ACCESS_INFO = FQN + ".getCpSiteAccessInfo";

	private static final String CAN_USER_ACCESS = FQN + ".canUserAccess";
	
}
