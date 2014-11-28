
package com.krishagni.catissueplus.core.biospecimen.repository.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import com.krishagni.catissueplus.core.biospecimen.domain.Specimen;
import com.krishagni.catissueplus.core.biospecimen.repository.SpecimenDao;
import com.krishagni.catissueplus.core.common.repository.AbstractDao;
import com.krishagni.catissueplus.core.common.util.Status;

public class SpecimenDaoImpl extends AbstractDao<Specimen> implements SpecimenDao {
	private static final String FQN = Specimen.class.getName();
	
	private static final String GET_SPECIMEN_BY_LABEL = FQN + ".getSpecimenByLabel";
	
	private static final String GET_SPECIMEN_BY_BARCODE = FQN + ".getSpecimenByBarcode";
	
	@Override
	public List<Specimen> getAllSpecimens(int startAt, int maxRecords, String... searchString) {
		Criteria criteria = sessionFactory.getCurrentSession()
				.createCriteria(Specimen.class, "s")
				.add(Restrictions.or(
						Restrictions.eq("s.activityStatus", Status.ACTIVITY_STATUS_ACTIVE.getStatus()),
						Restrictions.eq("s.activityStatus", Status.ACTIVITY_STATUS_CLOSED.getStatus())));
		
		addSearchConditions(criteria, searchString);
		criteria.addOrder(Order.asc("s.label"));
		addLimits(criteria, startAt, maxRecords);
		return getSpecimens(criteria);
	}
	
	@Override
	public Long getSpecimensCount(String... searchString) {
		Criteria criteria = sessionFactory.getCurrentSession()
				.createCriteria(Specimen.class, "s")
				.add(Restrictions.or(
						Restrictions.eq("s.activityStatus", Status.ACTIVITY_STATUS_ACTIVE.getStatus()),
						Restrictions.eq("s.activityStatus", Status.ACTIVITY_STATUS_CLOSED.getStatus())))
				.setProjection(Projections.countDistinct("s.id"));
		
		addSearchConditions(criteria, searchString);
		return ((Number)criteria.uniqueResult()).longValue();
	}
	
	@Override
	public Long getScgId(Long specimenId) {
		Specimen specimen = (Specimen) sessionFactory.getCurrentSession().get(Specimen.class, specimenId);
		
		if (specimen != null) {
			if (specimen.getSpecimenCollectionGroup() != null) {
				return specimen.getSpecimenCollectionGroup().getId();
			}
		}
		
		return null;
	}

	@Override
	public Specimen getSpecimen(Long id) {
		return (Specimen)sessionFactory.getCurrentSession().get(Specimen.class, id);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Specimen getSpecimenByLabel(String label) {
		List<Specimen> specimens  = sessionFactory.getCurrentSession()
			.getNamedQuery(GET_SPECIMEN_BY_LABEL)
			.setString("label", label)
			.list();
		
		return specimens.isEmpty() ? null : specimens.iterator().next();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Specimen getSpecimenByBarcode(String barcode) {
		List<Specimen> specimens = sessionFactory.getCurrentSession()
				.getNamedQuery(GET_SPECIMEN_BY_BARCODE)
				.setString("barcode", barcode)
				.list();
		
		return specimens.isEmpty() ? null : specimens.iterator().next();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Specimen> getSpecimensByLabel(List<String> labels) {
		List<Specimen> specimens = new ArrayList<Specimen>();
		
		int i = 0;
		int numLabels = labels.size();
		while (i < numLabels) {
			List<String> params = labels.subList(i, i + 500 > numLabels ? numLabels : i + 500);
			i += 500;
			
			specimens.addAll(
				sessionFactory.getCurrentSession()
				.createCriteria(Specimen.class)
				.add(Restrictions.in("label", params))
				.list());
		}
		
		return specimens;
	}
	
	private void addSearchConditions(Criteria criteria, String[] searchString) {
		if (searchString == null || searchString.length == 0 || StringUtils.isBlank(searchString[0])) {
			return;
		}
		
		Disjunction srchCond = Restrictions.disjunction();
		srchCond.add(Restrictions.or(
				Restrictions.ilike("s.label", searchString[0], MatchMode.ANYWHERE),
				Restrictions.ilike("s.barcode", searchString[0], MatchMode.ANYWHERE)
				));
		criteria.add(srchCond);
	}
	
	
	private void addLimits(Criteria criteria, int start, int maxRecords) {
		criteria.setFirstResult(start <= 0 ? 0 : start);
		if (maxRecords > 0) {
			criteria.setMaxResults(maxRecords);
		}
	}
	
	@SuppressWarnings("unchecked")
	private List<Specimen> getSpecimens(Criteria criteria) {
		List<Specimen> result = criteria.list();
		return result;		
	}
	
}
