
package com.krishagni.catissueplus.core.administrative.repository.impl;

import java.util.List;

import org.hibernate.Query;

import com.krishagni.catissueplus.core.administrative.domain.Biohazard;
import com.krishagni.catissueplus.core.administrative.repository.BiohazardDao;
import com.krishagni.catissueplus.core.common.repository.AbstractDao;

public class BiohazardDaoImpl extends AbstractDao<Biohazard> implements BiohazardDao {
	
	@SuppressWarnings("unchecked")
	@Override
	public Biohazard getBiohazard(String name) {
		Query query = sessionFactory.getCurrentSession().getNamedQuery(GET_BIOHAZARD_BY_NAME);
		query.setString("biohazardName", name);
		List<Biohazard> biohazardList = query.list();
		return !biohazardList.isEmpty() ? biohazardList.get(0) : null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Boolean isUniqueBiohazardName(String biohazardName) {
		Query query = getSessionFactory().getCurrentSession().getNamedQuery(GET_BIOHAZARD_BY_NAME);
		query.setString("biohazardName", biohazardName);

		List<Biohazard> list = query.list();
		return list.isEmpty() ? true : false;
	}

	@Override
	public Biohazard getBiohazard(long id) {
		return (Biohazard) sessionFactory.getCurrentSession().get(Biohazard.class, id);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Biohazard> getAllBiohazards(int maxResults) {
		Query query = sessionFactory.getCurrentSession().getNamedQuery(GET_ALL_BIOHAZARDS);
		query.setMaxResults(maxResults);
		return query.list();
	}
	
	private static final String FQN = Biohazard.class.getName();

	private static final String GET_BIOHAZARD_BY_NAME = FQN + ".getBiohazardByName";

	private static final String GET_ALL_BIOHAZARDS = FQN + ".getAllBiohazards";

}
