
package com.krishagni.catissueplus.core.biospecimen.services.impl;

import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import com.krishagni.catissueplus.core.administrative.domain.Site;
import com.krishagni.catissueplus.core.administrative.domain.User;
import com.krishagni.catissueplus.core.biospecimen.domain.AliquotSpecimensRequirement;
import com.krishagni.catissueplus.core.biospecimen.domain.CollectionProtocol;
import com.krishagni.catissueplus.core.biospecimen.domain.CollectionProtocolEvent;
import com.krishagni.catissueplus.core.biospecimen.domain.ConsentTier;
import com.krishagni.catissueplus.core.biospecimen.domain.DerivedSpecimenRequirement;
import com.krishagni.catissueplus.core.biospecimen.domain.Specimen;
import com.krishagni.catissueplus.core.biospecimen.domain.SpecimenRequirement;
import com.krishagni.catissueplus.core.biospecimen.domain.factory.CollectionProtocolFactory;
import com.krishagni.catissueplus.core.biospecimen.domain.factory.CpErrorCode;
import com.krishagni.catissueplus.core.biospecimen.domain.factory.CpeErrorCode;
import com.krishagni.catissueplus.core.biospecimen.domain.factory.CpeFactory;
import com.krishagni.catissueplus.core.biospecimen.domain.factory.SpecimenRequirementFactory;
import com.krishagni.catissueplus.core.biospecimen.domain.factory.SrErrorCode;
import com.krishagni.catissueplus.core.biospecimen.events.CollectionProtocolDetail;
import com.krishagni.catissueplus.core.biospecimen.events.CollectionProtocolEventDetail;
import com.krishagni.catissueplus.core.biospecimen.events.CollectionProtocolSummary;
import com.krishagni.catissueplus.core.biospecimen.events.ConsentTierDetail;
import com.krishagni.catissueplus.core.biospecimen.events.ConsentTierOp;
import com.krishagni.catissueplus.core.biospecimen.events.ConsentTierOp.OP;
import com.krishagni.catissueplus.core.biospecimen.events.CopyCpeOpDetail;
import com.krishagni.catissueplus.core.biospecimen.events.CpQueryCriteria;
import com.krishagni.catissueplus.core.biospecimen.events.CprSummary;
import com.krishagni.catissueplus.core.biospecimen.events.SpecimenRequirementDetail;
import com.krishagni.catissueplus.core.biospecimen.repository.CollectionProtocolDao;
import com.krishagni.catissueplus.core.biospecimen.repository.CpListCriteria;
import com.krishagni.catissueplus.core.biospecimen.repository.CprListCriteria;
import com.krishagni.catissueplus.core.biospecimen.repository.DaoFactory;
import com.krishagni.catissueplus.core.biospecimen.services.CollectionProtocolService;
import com.krishagni.catissueplus.core.common.PlusTransactional;
import com.krishagni.catissueplus.core.common.access.AccessCtrlMgr;
import com.krishagni.catissueplus.core.common.errors.ErrorType;
import com.krishagni.catissueplus.core.common.errors.OpenSpecimenException;
import com.krishagni.catissueplus.core.common.events.AccessDetail;
import com.krishagni.catissueplus.core.common.events.RequestEvent;
import com.krishagni.catissueplus.core.common.events.Resource;
import com.krishagni.catissueplus.core.common.events.ResponseEvent;
import com.krishagni.catissueplus.core.common.util.AuthUtil;

import edu.wustl.common.beans.SessionDataBean;

public class CollectionProtocolServiceImpl implements CollectionProtocolService {

	private CollectionProtocolFactory cpFactory;
	
	private CpeFactory cpeFactory;
	
	private SpecimenRequirementFactory srFactory;

	private DaoFactory daoFactory;

	public CollectionProtocolFactory getCpFactory() {
		return cpFactory;
	}

	public void setCpFactory(CollectionProtocolFactory cpFactory) {
		this.cpFactory = cpFactory;
	}

	public CpeFactory getCpeFactory() {
		return cpeFactory;
	}

	public void setCpeFactory(CpeFactory cpeFactory) {
		this.cpeFactory = cpeFactory;
	}
	
	public SpecimenRequirementFactory getSrFactory() {
		return srFactory;
	}

	public void setSrFactory(SpecimenRequirementFactory srFactory) {
		this.srFactory = srFactory;
	}

	public DaoFactory getDaoFactory() {
		return daoFactory;
	}

	public void setDaoFactory(DaoFactory daoFactory) {
		this.daoFactory = daoFactory;
	}

	@Override
	@PlusTransactional
	public ResponseEvent<List<CollectionProtocolSummary>> getProtocols(RequestEvent<CpListCriteria> req) {
		try {
			AccessDetail accessDetail = AccessCtrlMgr.getInstance().getReadableCpIds();
			
			CpListCriteria crit = req.getPayload();			
			if (!accessDetail.canAccessAll()) {
				crit.ids(accessDetail.getIds());
			}
			
			List<CollectionProtocolSummary> cpList = daoFactory.getCollectionProtocolDao().getCollectionProtocols(crit);			
			return ResponseEvent.response(cpList);
		} catch (OpenSpecimenException oce) {
			return ResponseEvent.error(oce);
		} catch (Exception e) {
			return ResponseEvent.serverError(e);
		}
	}
	
	@Override
	@PlusTransactional
	public ResponseEvent<CollectionProtocolDetail> getCollectionProtocol(RequestEvent<CpQueryCriteria> req) {
		try {
			CpQueryCriteria crit = req.getPayload();
			CollectionProtocol cp = null;
			
			if (crit.getId() != null) {
				cp = daoFactory.getCollectionProtocolDao().getById(crit.getId()); 
			} else if (crit.getTitle() != null) {
				cp = daoFactory.getCollectionProtocolDao().getCollectionProtocol(crit.getTitle());
			} else if (crit.getShortTitle() != null) {
				cp = daoFactory.getCollectionProtocolDao().getCpByShortTitle(crit.getShortTitle());
			}
			
			if (cp == null) {
				return ResponseEvent.userError(CpErrorCode.NOT_FOUND);
			}

			ensureUserHasReadPermission(cp);
			return ResponseEvent.response(CollectionProtocolDetail.from(cp, crit.isFullObject()));
		} catch (OpenSpecimenException oce) {
			return ResponseEvent.error(oce);
		} catch (Exception e) {
			return ResponseEvent.serverError(e);
		}
	}
	
	@Override
	@PlusTransactional
	public ResponseEvent<List<CprSummary>> getRegisteredParticipants(RequestEvent<CprListCriteria> req) {
		try {	
			CprListCriteria listCrit = req.getPayload();

			boolean userHasPhiRead = doesUserHavePhiAccess(listCrit.cpId());
			listCrit.includePhi(listCrit.includePhi() && userHasPhiRead);
			
			return ResponseEvent.response(daoFactory.getCprDao().getCprList(listCrit));
		} catch (OpenSpecimenException oce) {
			return ResponseEvent.error(oce);
		} catch (Exception e) {
			return ResponseEvent.serverError(e);
		}
	}

	@Override
	@PlusTransactional
	public ResponseEvent<CollectionProtocolDetail> createCollectionProtocol(RequestEvent<CollectionProtocolDetail> req) {
		try {
			CollectionProtocol cp = cpFactory.createCollectionProtocol(req.getPayload());
			ensureUserHasCreatePermission(cp);
			ensureUsersBelongtoCpSites(cp);
			
			OpenSpecimenException ose = new OpenSpecimenException(ErrorType.USER_ERROR);
			ensureUniqueTitle(cp.getTitle(), ose);
			ensureUniqueShortTitle(cp.getShortTitle(), ose);
			ose.checkAndThrow();

			daoFactory.getCollectionProtocolDao().saveOrUpdate(cp);
			return ResponseEvent.response(CollectionProtocolDetail.from(cp));
		} catch (OpenSpecimenException ose) {
			return ResponseEvent.error(ose);
		} catch (Exception e) {
			return ResponseEvent.serverError(e);
		}
	}
	
	@Override
	@PlusTransactional
	public ResponseEvent<CollectionProtocolDetail> updateCollectionProtocol(RequestEvent<CollectionProtocolDetail> req) {
		try {
			CollectionProtocolDetail detail = req.getPayload();
			CollectionProtocol existingCp = daoFactory.getCollectionProtocolDao().getById(detail.getId());
			if (existingCp == null) {
				return ResponseEvent.userError(CpErrorCode.NOT_FOUND);
			}

			ensureUserHasUpdatePermission(existingCp);
			CollectionProtocol cp = cpFactory.createCollectionProtocol(detail);
			ensureUsersBelongtoCpSites(cp);
			
			OpenSpecimenException ose = new OpenSpecimenException(ErrorType.USER_ERROR);
			ensureUniqueTitle(existingCp, cp, ose);
			ose.checkAndThrow();
			
			existingCp.update(cp);
			return ResponseEvent.response(CollectionProtocolDetail.from(existingCp));
		} catch (OpenSpecimenException ose) {
			return ResponseEvent.error(ose);
		} catch (Exception e) {
			return ResponseEvent.serverError(e);
		}
	}
	
	@Override
	@PlusTransactional
	public ResponseEvent<CollectionProtocolDetail> importCollectionProtocol(RequestEvent<CollectionProtocolDetail> req) {
		try {
			SessionDataBean sdb = req.getSessionDataBean();
			CollectionProtocolDetail cpDetail = req.getPayload();
			
			ResponseEvent<CollectionProtocolDetail> resp = createCollectionProtocol(req);
			resp.throwErrorIfUnsuccessful();
						
			importConsents(resp.getPayload().getId(), sdb, cpDetail.getConsents());
			importEvents(cpDetail.getTitle(), sdb, cpDetail.getEvents());		
			
			return resp;
		} catch (OpenSpecimenException ose) {
			return ResponseEvent.error(ose);
		} catch (Exception e) {
			return ResponseEvent.serverError(e);
		}
	}
	
	@Override
	@PlusTransactional
	public ResponseEvent<List<ConsentTierDetail>> getConsentTiers(RequestEvent<Long> req) {
		Long cpId = req.getPayload();

		try {
			CollectionProtocol cp = daoFactory.getCollectionProtocolDao().getById(cpId);
			if (cp == null) {
				return ResponseEvent.userError(CpErrorCode.NOT_FOUND);
			}
			
			ensureUserHasReadPermission(cp);
			return ResponseEvent.response(ConsentTierDetail.from(cp.getConsentTier()));
		} catch (OpenSpecimenException ose) {
			return ResponseEvent.error(ose);
		} catch (Exception e) {
			return ResponseEvent.serverError(e);
		}
	}
	
	@Override
	@PlusTransactional
	public ResponseEvent<ConsentTierDetail> updateConsentTier(RequestEvent<ConsentTierOp> req) {
		ConsentTierOp opDetail = req.getPayload();
		
		Long cpId = opDetail.getCpId();
		try {
			CollectionProtocol cp = daoFactory.getCollectionProtocolDao().getById(cpId);
			if (cp == null) {
				return ResponseEvent.userError(CpErrorCode.NOT_FOUND);
			}
			
			ensureUserHasUpdatePermission(cp);
			
			ConsentTierDetail input = opDetail.getConsentTier();
			ConsentTier resp = null;
			
			switch (opDetail.getOp()) {
				case ADD:
					resp = cp.addConsentTier(input.toConsentTier());
					break;
					
				case UPDATE:
					resp = cp.updateConsentTier(input.toConsentTier());
					break;
					
				case REMOVE:
					resp = cp.removeConsentTier(input.getId());
					break;			    
			}
			
			if (resp != null) {
				daoFactory.getCollectionProtocolDao().saveOrUpdate(cp, true);
			}
						
			return ResponseEvent.response(ConsentTierDetail.from(resp));
		} catch (OpenSpecimenException ose) {
			return ResponseEvent.error(ose);
		} catch (Exception e) {
			return ResponseEvent.serverError(e);
		}		
	}	

	@Override
	@PlusTransactional
	public ResponseEvent<List<CollectionProtocolEventDetail>> getProtocolEvents(RequestEvent<Long> req) {
		Long cpId = req.getPayload();
		
		try {
			CollectionProtocol cp = daoFactory.getCollectionProtocolDao().getById(cpId);
			if (cp == null) {
				return ResponseEvent.userError(CpErrorCode.NOT_FOUND);
			}

			ensureUserHasReadPermission(cp);
			return ResponseEvent.response(CollectionProtocolEventDetail.from(cp.getCollectionProtocolEvents()));
		} catch (OpenSpecimenException ose) {
			return ResponseEvent.error(ose);
		} catch (Exception e) {
			return ResponseEvent.serverError(e);
		}		
	}
	
	@Override
	@PlusTransactional
	public ResponseEvent<CollectionProtocolEventDetail> getProtocolEvent(RequestEvent<Long> req) {
		Long cpeId = req.getPayload();
		
		try {
			CollectionProtocolEvent cpe = daoFactory.getCollectionProtocolDao().getCpe(cpeId);
			if (cpe == null) {
				return ResponseEvent.userError(CpeErrorCode.NOT_FOUND);
			}
			
			ensureUserHasReadPermission(cpe.getCollectionProtocol());
			return ResponseEvent.response(CollectionProtocolEventDetail.from(cpe));
		} catch (OpenSpecimenException ose) {
			return ResponseEvent.error(ose);
		}  catch (Exception e) {
			return ResponseEvent.serverError(e);
		}
	}
		
	@Override
	@PlusTransactional
	public ResponseEvent<CollectionProtocolEventDetail> addEvent(RequestEvent<CollectionProtocolEventDetail> req) {
		try {
			CollectionProtocolEvent cpe = cpeFactory.createCpe(req.getPayload());			
			CollectionProtocol cp = cpe.getCollectionProtocol();
			cp.addCpe(cpe);
			
			ensureUserHasUpdatePermission(cp);
			daoFactory.getCollectionProtocolDao().saveOrUpdate(cp, true);			
			return ResponseEvent.response(CollectionProtocolEventDetail.from(cpe));			
		} catch (OpenSpecimenException ose) {
			return ResponseEvent.error(ose);
		} catch (Exception e) {
			return ResponseEvent.serverError(e);
		}
	}
	
	@Override
	@PlusTransactional
	public ResponseEvent<CollectionProtocolEventDetail> updateEvent(RequestEvent<CollectionProtocolEventDetail> req) {
		try {
			CollectionProtocolEvent cpe = cpeFactory.createCpe(req.getPayload());			
			CollectionProtocol cp = cpe.getCollectionProtocol();			
			cp.updateCpe(cpe);
						
			ensureUserHasUpdatePermission(cp);
			return ResponseEvent.response(CollectionProtocolEventDetail.from(cpe));			
		} catch (OpenSpecimenException ose) {		
			return ResponseEvent.error(ose);
		} catch (Exception e) {
			return ResponseEvent.serverError(e);
		}
	}
	
	@Override
	@PlusTransactional
	public ResponseEvent<CollectionProtocolEventDetail> copyEvent(RequestEvent<CopyCpeOpDetail> req) {
		try {
			CollectionProtocolDao cpDao = daoFactory.getCollectionProtocolDao();
			
			CopyCpeOpDetail opDetail = req.getPayload();
			String cpTitle = opDetail.getCollectionProtocol();
			String eventLabel = opDetail.getEventLabel();
			
			CollectionProtocolEvent existing = null;
			if (opDetail.getEventId() != null) {
				existing = cpDao.getCpe(opDetail.getEventId());
			} else if (!StringUtils.isBlank(eventLabel) && !StringUtils.isBlank(cpTitle)) {
				existing = cpDao.getCpeByEventLabel(cpTitle, eventLabel);
			}
			
			if (existing == null) {
				throw OpenSpecimenException.userError(CpeErrorCode.NOT_FOUND);
			}
			
			CollectionProtocol cp = existing.getCollectionProtocol();
			ensureUserHasUpdatePermission(cp);
			
			CollectionProtocolEvent cpe = cpeFactory.createCpeCopy(opDetail.getCpe(), existing);
			existing.copySpecimenRequirementsTo(cpe);			
			
			cp.addCpe(cpe);			
			cpDao.saveOrUpdate(cp, true);			
			return ResponseEvent.response(CollectionProtocolEventDetail.from(cpe));			
		} catch (OpenSpecimenException ose) {
			return ResponseEvent.error(ose);
		} catch (Exception e) {
			return ResponseEvent.serverError(e);
		}
	}
	
	@Override
	@PlusTransactional
	public ResponseEvent<List<SpecimenRequirementDetail>> getSpecimenRequirments(RequestEvent<Long> req) {
		Long cpeId = req.getPayload();
		try {
			CollectionProtocolEvent cpe = daoFactory.getCollectionProtocolDao().getCpe(cpeId);
			if (cpe == null) {
				return ResponseEvent.userError(CpeErrorCode.NOT_FOUND);
			}
			
			ensureUserHasReadPermission(cpe.getCollectionProtocol());
			return ResponseEvent.response(SpecimenRequirementDetail.from(cpe.getTopLevelAnticipatedSpecimens()));
		} catch (OpenSpecimenException ose) {
			return ResponseEvent.error(ose);
		}  catch (Exception e) {
			return ResponseEvent.serverError(e);
		}
	}
	
	@Override
	@PlusTransactional
	public ResponseEvent<SpecimenRequirementDetail> getSpecimenRequirement(RequestEvent<Long> req) {
		Long reqId = req.getPayload();
		try {
			SpecimenRequirement sr = daoFactory.getSpecimenRequirementDao().getById(reqId);
			if (sr == null) {
				return ResponseEvent.userError(SrErrorCode.NOT_FOUND);
			}
			
			ensureUserHasReadPermission(sr.getCollectionProtocol());
			return ResponseEvent.response(SpecimenRequirementDetail.from(sr));				
		} catch (OpenSpecimenException ose) {
			return ResponseEvent.error(ose);
		} catch (Exception e) {
			return ResponseEvent.serverError(e);
		}
	}
	
	
	@Override
	@PlusTransactional
	public ResponseEvent<SpecimenRequirementDetail> addSpecimenRequirement(RequestEvent<SpecimenRequirementDetail> req) {
		try {
			SpecimenRequirement requirement = srFactory.createSpecimenRequirement(req.getPayload());
			
			CollectionProtocolEvent cpe = requirement.getCollectionProtocolEvent();
			ensureUserHasUpdatePermission(cpe.getCollectionProtocol());
			
			cpe.addSpecimenRequirement(requirement);
			daoFactory.getCollectionProtocolDao().saveCpe(cpe, true);
			return ResponseEvent.response(SpecimenRequirementDetail.from(requirement));
		} catch (OpenSpecimenException ose) {
			return ResponseEvent.error(ose);
		} catch (Exception e) {
			return ResponseEvent.serverError(e);
		}
	}

	@Override
	@PlusTransactional
	public ResponseEvent<List<SpecimenRequirementDetail>> createAliquots(RequestEvent<AliquotSpecimensRequirement> req) {
		try {
			AliquotSpecimensRequirement requirement = req.getPayload();
			List<SpecimenRequirement> aliquots = srFactory.createAliquots(requirement);
			
			SpecimenRequirement parent = daoFactory.getSpecimenRequirementDao().getById(requirement.getParentSrId());
			ensureUserHasUpdatePermission(parent.getCollectionProtocol());

			parent.addChildRequirements(aliquots);
			
			daoFactory.getSpecimenRequirementDao().saveOrUpdate(parent, true);
			return ResponseEvent.response(SpecimenRequirementDetail.from(aliquots));
		} catch (OpenSpecimenException ose) {
			return ResponseEvent.error(ose);
		} catch (Exception e) {
			return ResponseEvent.serverError(e);
		}
	}

	@Override
	@PlusTransactional
	public ResponseEvent<SpecimenRequirementDetail> createDerived(RequestEvent<DerivedSpecimenRequirement> req) {
		try {
			DerivedSpecimenRequirement requirement = req.getPayload();
			SpecimenRequirement derived = srFactory.createDerived(requirement);						
			ensureUserHasUpdatePermission(derived.getCollectionProtocol());
			
			daoFactory.getSpecimenRequirementDao().saveOrUpdate(derived, true);
			return ResponseEvent.response(SpecimenRequirementDetail.from(derived));
		} catch (OpenSpecimenException ose) {
			return ResponseEvent.error(ose);
		} catch (Exception e) {
			return ResponseEvent.serverError(e);
		}
	}
	
	@Override
	@PlusTransactional
	public ResponseEvent<SpecimenRequirementDetail> copySpecimenRequirement(RequestEvent<Long> req) {
		try {
			Long srId = req.getPayload();
			
			SpecimenRequirement sr = daoFactory.getSpecimenRequirementDao().getById(srId);
			if (sr == null) {
				throw OpenSpecimenException.userError(SrErrorCode.NOT_FOUND);
			}
			
			ensureUserHasUpdatePermission(sr.getCollectionProtocol());
			SpecimenRequirement copy = sr.deepCopy(null);
			daoFactory.getSpecimenRequirementDao().saveOrUpdate(copy, true);
			return ResponseEvent.response(SpecimenRequirementDetail.from(copy));
		} catch (OpenSpecimenException ose) {
			return ResponseEvent.error(ose);
		} catch (Exception e) {
			return ResponseEvent.serverError(e);
		}
	}
	
	private void ensureUniqueTitle(CollectionProtocol existingCp, CollectionProtocol cp, OpenSpecimenException ose) {
		if (!existingCp.getTitle().equals(cp.getTitle())) {
			ensureUniqueTitle(cp.getTitle(), ose);
		}
	}
	
	private void ensureUniqueTitle(String title, OpenSpecimenException ose) {
		CollectionProtocol cp = daoFactory.getCollectionProtocolDao().getCollectionProtocol(title);
		if (cp != null) {
			ose.addError(CpErrorCode.DUP_TITLE);
		}		
	}
	
	private void ensureUniqueShortTitle(String shortTitle, OpenSpecimenException ose) {
		CollectionProtocol cp = daoFactory.getCollectionProtocolDao().getCpByShortTitle(shortTitle);
		if (cp != null) {
			ose.addError(CpErrorCode.DUP_SHORT_TITLE);
		}
	}
	
	private void importConsents(Long cpId, SessionDataBean sdb, List<ConsentTierDetail> consents) {
		if (CollectionUtils.isEmpty(consents)) {
			return;			
		}
		
		for (ConsentTierDetail consent : consents) {
			ConsentTierOp addOp = new ConsentTierOp();
			addOp.setConsentTier(consent);
			addOp.setCpId(cpId);
			addOp.setOp(OP.ADD);
			
			RequestEvent<ConsentTierOp> req = new RequestEvent<ConsentTierOp>(sdb, addOp);					
			ResponseEvent<ConsentTierDetail> resp = updateConsentTier(req);
			resp.throwErrorIfUnsuccessful();
		}
	}
	
	private void importEvents(String cpTitle, SessionDataBean sdb, List<CollectionProtocolEventDetail> events) {
		if (CollectionUtils.isEmpty(events)) {
			return;
		}
		
		for (CollectionProtocolEventDetail event : events) {
			event.setCollectionProtocol(cpTitle);
			RequestEvent<CollectionProtocolEventDetail> req = new RequestEvent<CollectionProtocolEventDetail>(sdb, event);
			ResponseEvent<CollectionProtocolEventDetail> resp = addEvent(req);
			resp.throwErrorIfUnsuccessful();
			
			Long eventId = resp.getPayload().getId();
			importSpecimenReqs(eventId, null, sdb, event.getSpecimenRequirements());
		}
	}
	
	private void importSpecimenReqs(Long eventId, Long parentSrId, SessionDataBean sdb, List<SpecimenRequirementDetail> srs) {
		if (CollectionUtils.isEmpty(srs)) {
			return;
		}
		
		for (SpecimenRequirementDetail sr : srs) {
			sr.setEventId(eventId);
			
			if (sr.getLineage().equals(Specimen.NEW)) {
				RequestEvent<SpecimenRequirementDetail> req = new RequestEvent<SpecimenRequirementDetail>(sdb, sr);
				ResponseEvent<SpecimenRequirementDetail> resp = addSpecimenRequirement(req);
				resp.throwErrorIfUnsuccessful();
				
				importSpecimenReqs(eventId, resp.getPayload().getId(), sdb, sr.getChildren());
			} else if (parentSrId != null && sr.getLineage().equals(Specimen.ALIQUOT)) {				
				AliquotSpecimensRequirement aliquotReq = sr.toAliquotRequirement(parentSrId, 1);
				ResponseEvent<List<SpecimenRequirementDetail>> resp = createAliquots(new RequestEvent<AliquotSpecimensRequirement>(sdb, aliquotReq));
				resp.throwErrorIfUnsuccessful();
				
				importSpecimenReqs(eventId, resp.getPayload().get(0).getId(), sdb, sr.getChildren());
			} else if (parentSrId != null && sr.getLineage().equals(Specimen.DERIVED)) {
				DerivedSpecimenRequirement derivedReq = sr.toDerivedRequirement(parentSrId);
				ResponseEvent<SpecimenRequirementDetail> resp = createDerived(new RequestEvent<DerivedSpecimenRequirement>(sdb, derivedReq));
				resp.throwErrorIfUnsuccessful();
				
				importSpecimenReqs(eventId, resp.getPayload().getId(), sdb, sr.getChildren());
			}			
		}
	}
	
	/***************************************************************
	 * Permission Checker                                          *
	 ***************************************************************/
	
	private void ensureUserHasCreatePermission(CollectionProtocol cp) {
		AccessCtrlMgr.getInstance().ensureCreatePermission(Resource.CP, cp, cp.getRepositories());
	}
	
	private void ensureUsersBelongtoCpSites(CollectionProtocol cp) {
		ensureCreatorBelongsCpSites(cp);
		ensurePiBelongsCpSites(cp);
		ensureCoordinatorsBelongsCpSites(cp);		
	}
	
	private void ensureCreatorBelongsCpSites(CollectionProtocol cp) {
		User user = AuthUtil.getCurrentUser();
		if (user.isAdmin()) {
			return;
		}
		user = loadUser(user);
		
		Set<Site> userSites = user.getSites();
		Set<Site> cpSites = cp.getRepositories();
		
		if (!userSites.containsAll(cpSites)) {
			throw OpenSpecimenException.userError(CpErrorCode.CREATOR_DOES_NOT_BELONG_CP_REPOS);
		}
	}
	
	private User loadUser(User user) {
		return daoFactory.getUserDao().getById(user.getId());
	}

	private void ensurePiBelongsCpSites(CollectionProtocol cp) {
		Set<Site> piSites = cp.getPrincipalInvestigator().getSites();
		Set<Site> cpSites = cp.getRepositories();
		
		if (!hasAtleastOneSiteInCommon(cpSites, piSites)) {
			throw OpenSpecimenException.userError(CpErrorCode.PI_DOES_NOT_BELONG_CP_REPOS);
		}
	}
	
	private void ensureCoordinatorsBelongsCpSites(CollectionProtocol cp) {
		Set<Site> cpSites = cp.getRepositories();
		Set<User> coordinators = cp.getCoordinators();
		
		for (User coordinator : coordinators) {
			Set<Site> coordinatorSites = coordinator.getSites();
			if (!hasAtleastOneSiteInCommon(cpSites, coordinatorSites)) {
				throw OpenSpecimenException.userError(CpErrorCode.CO_ORD_DOES_NOT_BELONG_CP_REPOS);
			}
		}
	}
	
	private boolean hasAtleastOneSiteInCommon(Set<Site> arg1, Set<Site> arg2) {
		return !CollectionUtils.intersection(arg1, arg2).isEmpty();
	}
	
	private void ensureUserHasReadPermission(CollectionProtocol cp) {
		AccessCtrlMgr.getInstance().ensureReadPermission(Resource.CP, cp, cp.getRepositories());		
	}
	
	private void ensureUserHasUpdatePermission(CollectionProtocol cp) {
		AccessCtrlMgr.getInstance().ensureUpdatePermission(Resource.CP, cp, cp.getRepositories());		
	}
	
	private boolean doesUserHavePhiAccess(Long cpId) {
		CollectionProtocol cp = daoFactory.getCollectionProtocolDao().getById(cpId);
		
		if (cp == null) {
			throw OpenSpecimenException.userError(CpErrorCode.NOT_FOUND);
		}
		
		ensureUserHasReadPermission(cp);
		boolean hasPhiAccess = true;
		try {
			AccessCtrlMgr.getInstance().ensureReadPermission(Resource.PARTICIPANT_PHI, cp, cp.getRepositories());
		} catch (OpenSpecimenException oce) {
			hasPhiAccess = false;
		}
		
		return hasPhiAccess;
	}
}
