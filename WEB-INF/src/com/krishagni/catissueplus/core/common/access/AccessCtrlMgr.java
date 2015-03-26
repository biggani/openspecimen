package com.krishagni.catissueplus.core.common.access;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.krishagni.catissueplus.core.administrative.domain.Site;
import com.krishagni.catissueplus.core.administrative.domain.User;
import com.krishagni.catissueplus.core.biospecimen.domain.CollectionProtocol;
import com.krishagni.catissueplus.core.biospecimen.repository.DaoFactory;
import com.krishagni.catissueplus.core.common.errors.OpenSpecimenException;
import com.krishagni.catissueplus.core.common.events.AccessDetail;
import com.krishagni.catissueplus.core.common.events.Operation;
import com.krishagni.catissueplus.core.common.events.Resource;
import com.krishagni.catissueplus.core.common.util.AuthUtil;
import com.krishagni.rbac.common.errors.RbacErrorCode;
import com.krishagni.rbac.events.CpSiteInfo;
import com.krishagni.rbac.service.RbacService;

@Configurable
public class AccessCtrlMgr {

	@Autowired
	private RbacService rbacService;
	
	@Autowired
	private DaoFactory daoFactory;
	
	private static AccessCtrlMgr instance;
	
	private AccessCtrlMgr() {
		
	}
	
	public static AccessCtrlMgr getInstance() {
		if (instance == null) {
			instance = new AccessCtrlMgr();
		}
		
		return instance;
	}
	
	public void ensureUserIsAdmin() {
		User currentUser = AuthUtil.getCurrentUser();
		
		if (!currentUser.isAdmin()) {
			accessDenied();
		}
	}
	
	public void hasReadPermissions(Resource resource, CollectionProtocol cp, Set<Site> sites) {
		User currentUser = AuthUtil.getCurrentUser();
		if (currentUser.isAdmin()) {
			return;
		}
		
		if (!rbacService.canPerformOperation( 
				currentUser.getId(), 
				resource.toString(), 
				Operation.READ.toString(), 
				cp.getId(), 
				getSiteIds(sites))) {
			accessDenied();
		}
	}
	
	public void hasCreatePermissions(Resource resource, CollectionProtocol cp, Set<Site> sites) {
		User currentUser = AuthUtil.getCurrentUser();
		if (currentUser.isAdmin()) {
			return;
		}
		
		if(!rbacService.canPerformOperation(
				currentUser.getId(), 
				resource.toString(), 
				Operation.CREATE.toString(), 
				cp.getId(), 
				getSiteIds(sites))) {
			accessDenied();
		}
	}
	
	public void hasUpdatePermissions(Resource resource, CollectionProtocol cp, Set<Site> sites) {
		User currentUser = AuthUtil.getCurrentUser();
		if (currentUser.isAdmin()) {
			return;
		}
		
		if(!rbacService.canPerformOperation(
				currentUser.getId(), 
				resource.toString(), 
				Operation.UPDATE.toString(), 
				cp.getId(), 
				getSiteIds(sites))) {
			accessDenied();
		}
	}
	
	public void hasDeletePermissions(Resource resource, CollectionProtocol cp, Set<Site> sites) {
		User currentUser = AuthUtil.getCurrentUser();
		if (currentUser.isAdmin()) {
			return;
		}
		
		if(!rbacService.canPerformOperation(
				currentUser.getId(), 
				resource.toString(), 
				Operation.DELETE.toString(), 
				cp.getId(), 
				getSiteIds(sites))) {
			accessDenied();
		}
	}
	
	public AccessDetail getReadableCpIds() {
		User currentUser = AuthUtil.getCurrentUser();
		if (currentUser.isAdmin()) {
			return AccessDetail.ACCESS_TO_ALL;
		}
		
		List<CpSiteInfo> cpSites = rbacService.getAccessibleCpSites(
				currentUser.getId(), 
				Resource.CP.toString(), 
				Operation.READ.toString());
		
		Set<Long> readableCps = new HashSet<Long>();
		Set<Long> sitesToBeFetched = new HashSet<Long>();
		AccessDetail objAccess = new AccessDetail(false);
		
		for (CpSiteInfo cpSite : cpSites) {
			if (cpSite.getCpId() == null && cpSite.getSiteId() == null) {
				objAccess.setAccessAll(true);
				break;
			} else if (cpSite.getCpId() != null) {
				readableCps.add(cpSite.getCpId());
			} else if (cpSite.getSiteId() != null) { 
				sitesToBeFetched.add(cpSite.getSiteId());
			}
		}
		
		if (sitesToBeFetched.size() > 0 && !objAccess.getAccessAll()) {
			readableCps.addAll(daoFactory.getCollectionProtocolDao().getCpIdsBySiteIds(sitesToBeFetched));
		}
		
		objAccess.setIds(readableCps);
		if (!objAccess.isAccessAllowed()) {
			accessDenied();
		}
		return objAccess;
	}
	
	private void accessDenied() {
		throw OpenSpecimenException.userError(RbacErrorCode.ACCESS_DENIED);
	}
	
	private Set<Long> getSiteIds(Set<Site> sites) {
		Set<Long> siteIds = new HashSet<Long>();
		
		for (Site site : sites) {
			siteIds.add(site.getId());
		}
		
		return siteIds;
	}
}
