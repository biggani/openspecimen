
package com.krishagni.catissueplus.core.administrative.services.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.krishagni.catissueplus.core.administrative.domain.Site;
import com.krishagni.catissueplus.core.administrative.domain.factory.SiteErrorCode;
import com.krishagni.catissueplus.core.administrative.domain.factory.SiteFactory;
import com.krishagni.catissueplus.core.administrative.events.SiteDetail;
import com.krishagni.catissueplus.core.administrative.events.SiteQueryCriteria;
import com.krishagni.catissueplus.core.administrative.repository.SiteListCriteria;
import com.krishagni.catissueplus.core.administrative.services.SiteService;
import com.krishagni.catissueplus.core.biospecimen.repository.DaoFactory;
import com.krishagni.catissueplus.core.common.PlusTransactional;
import com.krishagni.catissueplus.core.common.access.AccessCtrlMgr;
import com.krishagni.catissueplus.core.common.errors.ErrorType;
import com.krishagni.catissueplus.core.common.errors.OpenSpecimenException;
import com.krishagni.catissueplus.core.common.events.AccessDetail;
import com.krishagni.catissueplus.core.common.events.DeleteEntityOp;
import com.krishagni.catissueplus.core.common.events.Operation;
import com.krishagni.catissueplus.core.common.events.RequestEvent;
import com.krishagni.catissueplus.core.common.events.Resource;
import com.krishagni.catissueplus.core.common.events.ResponseEvent;

public class SiteServiceImpl implements SiteService {
	private SiteFactory siteFactory;

	private DaoFactory daoFactory;
	
	public void setDaoFactory(DaoFactory daoFactory) {
		this.daoFactory = daoFactory;
	}

	public void setSiteFactory(SiteFactory siteFactory) {
		this.siteFactory = siteFactory;
	}
	
	@Override
	@PlusTransactional	
	public ResponseEvent<List<SiteDetail>> getSites(RequestEvent<SiteListCriteria> req) {
		try {
			SiteListCriteria crit = req.getPayload();
			crit.ids(getReadableSiteIds());
			
			List<Site> sites = daoFactory.getSiteDao().getSites(crit);
			return ResponseEvent.response(SiteDetail.from(sites));
		} catch(OpenSpecimenException ose) {
			return ResponseEvent.error(ose);
		} catch(Exception ex) {
			return ResponseEvent.serverError(ex);
		}
	}

	@Override
	@PlusTransactional		
	public ResponseEvent<SiteDetail> getSite(RequestEvent<SiteQueryCriteria> req) {
		try { 
			SiteQueryCriteria crit = req.getPayload();
		
			Site site = null;
			
			if (crit.getId() != null) {
				site = daoFactory.getSiteDao().getById(crit.getId());
			} else if (crit.getName() != null) {
				site = daoFactory.getSiteDao().getSiteByName(crit.getName());
			}
			
			if (site == null) {
				return ResponseEvent.userError(SiteErrorCode.NOT_FOUND);
			}
			
			ensureUserHasReadPermission(site);
			return ResponseEvent.response(SiteDetail.from(site));
		} catch(OpenSpecimenException ose) {
			return ResponseEvent.error(ose);
		} catch(Exception ex) {
			return ResponseEvent.serverError(ex);
		}
	}

	@Override
	@PlusTransactional	
	public ResponseEvent<SiteDetail> createSite(RequestEvent<SiteDetail> req) {
		try {	
			AccessCtrlMgr.getInstance().ensureUserIsAdmin();
			Site site = siteFactory.createSite(req.getPayload());
			
			OpenSpecimenException ose = new OpenSpecimenException(ErrorType.USER_ERROR);
			ensureUniqueConstraint(site, null, ose);
			ose.checkAndThrow();
			
			daoFactory.getSiteDao().saveOrUpdate(site, true);
			
			return ResponseEvent.response(SiteDetail.from(site));
		} catch (OpenSpecimenException ose) {
			return ResponseEvent.error(ose);
		} catch (Exception e) {
			return ResponseEvent.serverError(e);
		}
	}

	@Override
	@PlusTransactional		
	public ResponseEvent<SiteDetail> updateSite(RequestEvent<SiteDetail> req) {
		try {
			AccessCtrlMgr.getInstance().ensureUserIsAdmin();
			SiteDetail detail = req.getPayload();

			Site existing = daoFactory.getSiteDao().getById(detail.getId());
			if (existing == null) {
				return ResponseEvent.userError(SiteErrorCode.NOT_FOUND);
			}

			Site site = siteFactory.createSite(detail);
			
			OpenSpecimenException ose = new OpenSpecimenException(ErrorType.USER_ERROR);
			ensureUniqueConstraint(site, existing, ose);
			ose.checkAndThrow();
			
			existing.update(site);
			daoFactory.getSiteDao().saveOrUpdate(existing);
			
			return ResponseEvent.response(SiteDetail.from(existing));
		} catch (OpenSpecimenException ose) {
			return ResponseEvent.error(ose);
		} catch (Exception e) {
			return ResponseEvent.serverError(e);
		}		
	}

	@Override
	@PlusTransactional	
	public ResponseEvent<Map<String, List>> deleteSite(RequestEvent<DeleteEntityOp> req) {
		try {
			AccessCtrlMgr.getInstance().ensureUserIsAdmin();
			DeleteEntityOp deleteOp = req.getPayload();

			Site site = daoFactory.getSiteDao().getById(deleteOp.getId());
			if (site == null) {
				return ResponseEvent.userError(SiteErrorCode.NOT_FOUND);
			}
			
			Map<String, List> dependencies = site.delete(deleteOp.isClose());
			if (!dependencies.isEmpty()) {
				return ResponseEvent.response(dependencies);
			}
			
			daoFactory.getSiteDao().saveOrUpdate(site);
			return ResponseEvent.response(Collections.<String, List>emptyMap());
		} catch (OpenSpecimenException ose) {
			return ResponseEvent.error(ose);
		} catch (Exception e) {
			return ResponseEvent.serverError(e);
		}
	}

	private void ensureUniqueConstraint(Site newSite, Site existingSite, OpenSpecimenException ose) {
		if (!isUniqueName(newSite, existingSite)) {
			ose.addError(SiteErrorCode.DUP_NAME);
		}
		
		if(!isUniqueCode(newSite, existingSite)) {
			ose.addError(SiteErrorCode.DUP_CODE);
		}
	}

	private boolean isUniqueName(Site newSite, Site existingSite) {
		if (existingSite != null && newSite.getName().equals(existingSite.getName())) {
			return true;
		}
		
		Site site = daoFactory.getSiteDao().getSiteByName(newSite.getName());
		if (site != null) {
			return false; 
		} 
		
		return true;
	}
	
	private boolean isUniqueCode(Site newSite, Site existingSite) {
		if (StringUtils.isBlank(newSite.getCode()) ||
	              existingSite != null && newSite.getCode().equals(existingSite.getCode())) {
			return true;
		}
		
		Site site = daoFactory.getSiteDao().getSiteByCode(newSite.getCode());
		if (site != null) {
			return false; 
		}
		
		return true;
	}
	
	/***************************************************************
	 * Permission Checker                                          *
	 ***************************************************************/
	
	private Set<Long> getReadableSiteIds() {
		AccessDetail accessDetail = AccessCtrlMgr.getInstance().getAccessibleSites(Resource.SITE, Operation.READ);
		
		if (!accessDetail.canAccessAll()) {
			return accessDetail.getIds();
		} else {
			return Collections.<Long>emptySet();
		}
	}
	
	private void ensureUserHasReadPermission(Site site) {
		AccessCtrlMgr.getInstance().ensureReadPermission(Resource.SITE, Collections.singleton(site));
	}
}
