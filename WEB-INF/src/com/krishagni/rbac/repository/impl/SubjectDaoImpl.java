package com.krishagni.rbac.repository.impl;

import org.hibernate.Criteria;
import org.hibernate.criterion.Junction;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import com.krishagni.catissueplus.core.common.repository.AbstractDao;
import com.krishagni.rbac.domain.Subject;
import com.krishagni.rbac.domain.SubjectRole;
import com.krishagni.rbac.events.UserAccessInformation;
import com.krishagni.rbac.repository.SubjectDao;

public class SubjectDaoImpl extends AbstractDao<Subject> implements SubjectDao {

	@Override
	public Subject getSubject(Long subjectId) {
		return (Subject) sessionFactory.getCurrentSession()
				.get(Subject.class, subjectId);
	}

	@Override
	public boolean canUserAccess(UserAccessInformation info) {
		/*
		 * mandatory resource, operation, userid
		 */
		Criteria query = sessionFactory.getCurrentSession()
				.createCriteria(SubjectRole.class)
				.createAlias("role", "rl")
				.createAlias("rl.acl", "acl")
				.createAlias("acl.resource", "res")
				.createAlias("acl.operations", "ops")
				.createAlias("ops.operation", "op")
				.createAlias("subject", "sub")
				.add(Restrictions.eq("res.name", info.resource()))
				.add(Restrictions.eq("op.name", info.operation()))
				.add(Restrictions.eq("sub.id", info.subjectId()));
		
		query.setProjection(Projections.projectionList()
				.add(Projections.property("sub.id")));
		
		addResourceObjIdRestriction(query, info);
		addCpRestriction(query, info);
		addSiteRestrictions(query, info);
		return !query.list().isEmpty();
	}
	
	private void addResourceObjIdRestriction(Criteria query, UserAccessInformation info) {
		Junction condition = Restrictions.disjunction()
				.add(Restrictions.isNull("ops.resourceInstanceId"));		
		if (info.objectId() != null) {
			condition.add(Restrictions.eq("ops.resourceInstanceId", info.objectId()));
		}
		
		query.add(condition);
	}
	
	private void addCpRestriction(Criteria query, UserAccessInformation info) {
		query.createAlias("collectionProtocol", "cp");
		
		Junction condition = Restrictions.disjunction()
				.add(Restrictions.isNull("collectionProtocol"));
		if (info.cpId() != null) {
			condition.add(Restrictions.eq("cp.id", info.cpId()));
		}
		
		query.add(condition);
	}
	
	private void addSiteRestrictions(Criteria query, UserAccessInformation info) {
		query.createAlias("site", "site_");		

		Junction condition = Restrictions.disjunction()
				.add(Restrictions.isNull("site"));
		if (info.sites().size() > 0) {
			condition.add(Restrictions.in("site_.id", info.sites()));
		}
		
		query.add(condition);
	}
}
