package com.krishagni.catissueplus.core.common;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.krishagni.catissueplus.core.administrative.domain.Site;
import com.krishagni.catissueplus.core.administrative.domain.User;
import com.krishagni.catissueplus.core.biospecimen.domain.CollectionProtocol;
import com.krishagni.catissueplus.core.biospecimen.repository.DaoFactory;
import com.krishagni.catissueplus.core.common.events.OpenSpecimenObjectAccessDetail;
import com.krishagni.catissueplus.core.common.events.OpenSpecimenOperation;
import com.krishagni.catissueplus.core.common.events.OpenSpecimenResource;
import com.krishagni.catissueplus.core.common.util.AuthUtil;
import com.krishagni.rbac.events.CpSiteInfo;
import com.krishagni.rbac.service.RbacService;

@Configurable
public class AccessCtrlManager {

	@Autowired
	private RbacService rbacService;
	
	@Autowired
	private DaoFactory daoFactory;
	
	private static AccessCtrlManager instance;
	
	private AccessCtrlManager() {
		
	}
	
	public static AccessCtrlManager getInstance() {
		if (instance == null) {
			instance = new AccessCtrlManager();
		}
		
		return instance;
	}
	
	public boolean isAdmin() {
		return AuthUtil.getCurrentUser().isAdmin();
	}
	
	public boolean hasReadPermissions(OpenSpecimenResource resource, CollectionProtocol cp, Set<Site> sites) {
		User currentUser = AuthUtil.getCurrentUser();
		if (currentUser.isAdmin()) {
			return true;
		}
		
		return rbacService.checkAccess(currentUser.getId(), resource.toString(), OpenSpecimenOperation.READ.toString(), 
				cp.getId(), getSiteIds(sites));
	}
	
	public boolean hasCreatePermissions(OpenSpecimenResource resource, CollectionProtocol cp, Set<Site> sites) {
		User currentUser = AuthUtil.getCurrentUser();
		if (currentUser.isAdmin()) {
			return true;
		}
		
		return rbacService.checkAccess(currentUser.getId(), resource.toString(), OpenSpecimenOperation.CREATE.toString(), 
				cp.getId(), getSiteIds(sites));
	}
	
	public boolean hasUpdatePermissions(OpenSpecimenResource resource, CollectionProtocol cp, Set<Site> sites) {
		User currentUser = AuthUtil.getCurrentUser();
		if (currentUser.isAdmin()) {
			return true;
		}
		
		return rbacService.checkAccess(currentUser.getId(), resource.toString(), OpenSpecimenOperation.UPDATE.toString(), 
				cp.getId(), getSiteIds(sites));
	}
	
	public boolean hasDeletePermissions(OpenSpecimenResource resource, CollectionProtocol cp, Set<Site> sites) {
		User currentUser = AuthUtil.getCurrentUser();
		if (currentUser.isAdmin()) {
			return true;
		}
		
		return rbacService.checkAccess(currentUser.getId(), resource.toString(), OpenSpecimenOperation.DELETE.toString(), 
				cp.getId(), getSiteIds(sites));
	}
	
	public OpenSpecimenObjectAccessDetail getReadableCpIds() {
		User currentUser = AuthUtil.getCurrentUser();
		if (currentUser.isAdmin()) {
			return OpenSpecimenObjectAccessDetail.canAccessAll();
		}
		
		List<CpSiteInfo> cpSites = rbacService.getAccessibleCpSites(currentUser.getId(), OpenSpecimenResource.CP.toString(), 
				OpenSpecimenOperation.READ.toString());
		Set<Long> readableCps = new HashSet<Long>();
		Set<Long> sitesToBeFetched = new HashSet<Long>();
		OpenSpecimenObjectAccessDetail objAccess = new OpenSpecimenObjectAccessDetail(false);
		
		for (CpSiteInfo cpSite : cpSites) {
			if (cpSite.getCpId() == null && cpSite.getSiteId() == null) {
				objAccess.setCanAccessAll(true);
				break;
			} else if (cpSite.getCpId() != null) {
				readableCps.add(cpSite.getCpId());
			} else if (cpSite.getSiteId() != null) { 
				sitesToBeFetched.add(cpSite.getSiteId());
			}
		}
		
		if (sitesToBeFetched.size() > 0 && !objAccess.getCanAccessAll()) {
			readableCps.addAll(daoFactory.getCollectionProtocolDao().getCpIdsBySiteIds(sitesToBeFetched));
		}
		
		objAccess.setIds(readableCps);
		return objAccess;
	}
	
	private Set<Long> getSiteIds(Set<Site> sites) {
		Set<Long> siteIds = new HashSet<Long>();
		
		for (Site site : sites) {
			siteIds.add(site.getId());
		}
		
		return siteIds;
	}
}
