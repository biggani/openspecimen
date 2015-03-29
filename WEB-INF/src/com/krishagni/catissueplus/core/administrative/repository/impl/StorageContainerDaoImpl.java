
package com.krishagni.catissueplus.core.administrative.repository.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;

import com.krishagni.catissueplus.core.administrative.domain.StorageContainer;
import com.krishagni.catissueplus.core.administrative.domain.StorageContainerPosition;
import com.krishagni.catissueplus.core.administrative.repository.StorageContainerDao;
import com.krishagni.catissueplus.core.administrative.repository.StorageContainerListCriteria;
import com.krishagni.catissueplus.core.common.repository.AbstractDao;
import com.krishagni.catissueplus.core.common.util.Status;

import edu.wustl.catissuecore.util.global.Constants;

public class StorageContainerDaoImpl extends AbstractDao<StorageContainer> implements StorageContainerDao {

	@Override
	public Class<?> getType() {
		return StorageContainer.class;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<StorageContainer> getStorageContainers(StorageContainerListCriteria listCrit) {
		return new ListQueryBuilder(listCrit).query().list();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public StorageContainer getByName(String name) {
		List<StorageContainer> result = sessionFactory.getCurrentSession()
				.createCriteria(StorageContainer.class)
				.add(Restrictions.eq("name", name))
				.add(Restrictions.eq("activityStatus", Constants.ACTIVITY_STATUS_ACTIVE))
				.list();

		return result.isEmpty() ? null : result.iterator().next();
	}
	
	@SuppressWarnings("unchecked")
	public StorageContainer getByBarcode(String barcode) {		
		List<StorageContainer> result = sessionFactory.getCurrentSession()
				.createCriteria(StorageContainer.class)
				.add(Restrictions.eq("barcode", barcode))
				.add(Restrictions.eq("activityStatus", Status.ACTIVITY_STATUS_ACTIVE.getStatus()))
				.list();

		return result.isEmpty() ? null : result.iterator().next();		
	}

	@Override
	public void delete(StorageContainerPosition position) {
		sessionFactory.getCurrentSession().delete(position);		
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<Long> getContainerIdsBySiteIds(List<Long> siteIds) {
		return sessionFactory.getCurrentSession()
				.getNamedQuery(GET_CONTAINER_IDS_BY_SITE_IDS)
				.setParameterList("siteIds", siteIds)
				.list();
	}
	
	private static final String FQN = StorageContainer.class.getName();
	
	private static final String GET_CONTAINER_IDS_BY_SITE_IDS = FQN + ".getContainerIdsBySiteIds";
	
	private class ListQueryBuilder {
		private StorageContainerListCriteria crit;
		
		private StringBuilder from = new StringBuilder();
		
		private StringBuilder where = new StringBuilder();
		
		private Map<String, Object> params = new HashMap<String, Object>();
		
		public ListQueryBuilder(StorageContainerListCriteria crit) {
			this.crit = crit;
				
			
			if (crit.hierarchical()) {
				from = new StringBuilder("select distinct c from ").append(getType().getName()).append(" c")
						.append(" join c.descendentContainers dc");
				where = new StringBuilder("where dc.activityStatus = :activityStatus");
			} else {
				from = new StringBuilder("select c from ").append(getType().getName()).append(" c");
				where = new StringBuilder("where c.activityStatus = :activityStatus");						
			}
			
			params.put("activityStatus", Status.ACTIVITY_STATUS_ACTIVE.getStatus());
		}
		
		public Query query() {						
			addNameRestriction();		
			addSiteRestriction();
					
			addFreeContainersRestriction();
			addSpecimenRestriction();
			addCpRestriction();
			addStoreSpecimenRestriction();
			
			addParentRestriction();
			
			addIdsRestriction();
			
			String hql = new StringBuilder(from).append(" ").append(where)
					.append(" order by c.name asc")
					.toString();
			
			Query query = sessionFactory.getCurrentSession().createQuery(hql)
					.setFirstResult(crit.startAt())
					.setMaxResults(crit.maxResults());
			
			for (Map.Entry<String, Object> param : params.entrySet()) {
				query.setParameter(param.getKey(), param.getValue());
			}
			
			return query;
		}
		
		private void addAnd() {
			if (where.length() != 0) {
				where.append(" and ");
			}
		}
		
		private void addNameRestriction() {			
			if (StringUtils.isBlank(crit.query())) {
				return;
			}
			
			addAnd();
			where.append("upper(c.name) like :name");
			params.put("name", "%" + crit.query().toUpperCase() + "%");
		}

		private void addFreeContainersRestriction() {			
			if (!crit.onlyFreeContainers()) {
				return;
			}
			
			if (crit.hierarchical()) {
				from.append(" join dc.stats stats");
			} else {
				from.append(" join c.stats stats");
			}
			
			addAnd();
			where.append("stats.freePositions > 0");
		}

		private void addSiteRestriction() {			
			if (StringUtils.isBlank(crit.siteName())) {
				return;
			}
			
			from.append(" join c.site site");

			addAnd();
			where.append("site.name = :siteName");
			params.put("siteName", crit.siteName());
		}

		private void addParentRestriction() {			
			if (!crit.topLevelContainers() && crit.parentContainerId() == null) {
				return;
			}
			
			if (crit.topLevelContainers()) {
				from.append(" left join c.parentContainer pc");			
			} else if (crit.parentContainerId() != null) {
				from.append(" join c.parentContainer pc");
			} 

			addAnd();
			Long parentId = crit.parentContainerId();
			if (parentId == null) {
				where.append("pc is null");
			} else {
				where.append("pc.id = :parentId");
				params.put("parentId", parentId);						
			}
		}
		
		public void addIdsRestriction() {
			if (crit.ids().isEmpty()) {
				return;
			}
			
			addAnd();
			/*
			 * This is to avoid oracle 1000 limit for IN clause
			 */
			String inClause = new InClauseBuilder("c.id", 1000, crit.ids()).buildInClause(); 
			where.append(inClause);
		}

		private void addSpecimenRestriction() {			
			String specimenClass = crit.specimenClass(), specimenType = crit.specimenType();			
			if (StringUtils.isBlank(specimenClass) && StringUtils.isBlank(specimenType)) {
				return;
			}
			
			addAnd();
			if (crit.hierarchical()) {
				where.append("(:specimenClass in elements(dc.compAllowedSpecimenClasses)")
				.append(" or ")
				.append(":specimenType in elements(dc.compAllowedSpecimenTypes)")
				.append(")");				
			} else {
				where.append("(:specimenClass in elements(c.compAllowedSpecimenClasses)")
				.append(" or ")
				.append(":specimenType in elements(c.compAllowedSpecimenTypes)")
				.append(")");				
			}
			
			params.put("specimenClass", specimenClass);
			params.put("specimenType", specimenType);
		}
		
		private void addCpRestriction() {			
			Long cpId = crit.cpId();
			if (cpId == null || cpId == -1) {
				return;
			}
			
			if (crit.hierarchical()) {
				from.append(" left join dc.compAllowedCps cp");
			} else {
				from.append(" left join c.compAllowedCps cp");
			}
			
			addAnd();
			where.append("(cp is null or cp.id = :cpId)");
			params.put("cpId", cpId);
		}	
		
		private void addStoreSpecimenRestriction() {			
			if (crit.storeSpecimensEnabled() == null) {
				return;
			}

			addAnd();
			if (crit.hierarchical()) {
				where.append("dc.storeSpecimenEnabled = :storeSpecimenEnabled");
			} else {
				where.append("c.storeSpecimenEnabled = :storeSpecimenEnabled");
			}
			
			params.put("storeSpecimenEnabled", crit.storeSpecimensEnabled());
		}
	}
	
	private class InClauseBuilder {

		private String propertyName;
		
		private Set<Long> values = new HashSet<Long>();
		
		private int limit;
		
		public InClauseBuilder(String propertyName, int limit, Set<Long> values) {
			this.propertyName = propertyName;
			this.limit = limit;
			this.values = values;
		}
		
		public String buildInClause() {
			if (values.isEmpty()) {
				return "";
			}
			
			StringBuilder sb = new StringBuilder();
			List<Long> ids = new ArrayList<Long>(values);

			sb.append("(");
			while (!ids.isEmpty()) {
				int size = ids.size() > limit ? limit : ids.size();
				List<Long> subList = ids.subList(0, size);
				
				sb.append(propertyName)
						.append(" in ")
						.append(buildIdString(subList))
						.append(" or ");
				
				ids.subList(0, size).clear();
			}
			
			if (sb.length() > 4) {
				sb.setLength(sb.length() - 4);
			}
			
			sb.append(")");
			
			return sb.toString();
		}
		
		private String buildIdString(List<Long> ids) {
			StringBuilder sb = new StringBuilder();
			sb.append("(");
			
			for (Long id : ids) {
				sb.append(id)
						.append(",");
			}
			sb.setLength(sb.length() -1);
			sb.append(")");
			
			return sb.toString();
		}
	}
}
