
package com.krishagni.catissueplus.core.administrative.repository.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.krishagni.catissueplus.core.administrative.domain.Site;
import com.krishagni.catissueplus.core.administrative.repository.SiteDao;
import com.krishagni.catissueplus.core.administrative.repository.SiteListCriteria;
import com.krishagni.catissueplus.core.common.repository.AbstractDao;
import com.krishagni.catissueplus.core.common.util.Status;

public class SiteDaoImpl extends AbstractDao<Site> implements SiteDao {
	
	@Override
	public Class<?> getType() {
		return Site.class;
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Site> getSites(SiteListCriteria listCrit) {
		Criteria query = sessionFactory.getCurrentSession().createCriteria(Site.class)
				.add(Restrictions.eq("activityStatus", Status.ACTIVITY_STATUS_ACTIVE.getStatus()))
				.addOrder(Order.asc("name"))
				.setFirstResult(listCrit.startAt())
				.setMaxResults(listCrit.maxResults());
				
		if (StringUtils.isNotBlank(listCrit.query())) {
			MatchMode mathMode = listCrit.exactMatch() ? MatchMode.EXACT : MatchMode.ANYWHERE;
			query.add(Restrictions.ilike("name", listCrit.query(), mathMode));
		}
		
		applyIdsFilter(query, "id", listCrit.ids());
		
		return query.list();
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Site> getSitesByNames(List<String> siteNames) {
		return sessionFactory.getCurrentSession()
				.getNamedQuery(GET_SITES_BY_NAMES)
				.setParameterList("siteNames", siteNames)
				.list();
	}
	
	@Override
	public Site getSiteByName(String siteName) {
		List<Site> result = getSitesByNames(Collections.singletonList(siteName));
		
		return result.isEmpty() ? null : result.get(0);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public Site getSiteByCode(String siteCode) {
		List<Site> result = getSessionFactory().getCurrentSession()
				.getNamedQuery(GET_SITE_BY_CODE)
				.setString("siteCode", siteCode)
				.list();
		
		return result.isEmpty() ? null : result.get(0);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Long> getSiteIdsByCpIds(List<Long> cpIds) {
		return sessionFactory.getCurrentSession()
				.getNamedQuery(GET_SITE_IDS_BY_CP_IDS)
				.setParameterList("cpIds", cpIds)
				.list();
	}

	private static final String FQN = Site.class.getName();

	private static final String GET_SITES_BY_NAMES = FQN + ".getSitesByNames";
	
	private static final String GET_SITE_BY_CODE = FQN + ".getSiteByCode";

	private static final String GET_SITE_IDS_BY_CP_IDS = FQN + ".getSiteIdsByCpIds";
}
