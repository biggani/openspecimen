
package com.krishagni.catissueplus.core.biospecimen.services.impl;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.krishagni.catissueplus.core.biospecimen.domain.Visit;
import com.krishagni.catissueplus.core.biospecimen.domain.factory.VisitErrorCode;
import com.krishagni.catissueplus.core.biospecimen.domain.factory.VisitFactory;
import com.krishagni.catissueplus.core.biospecimen.events.SpecimenDetail;
import com.krishagni.catissueplus.core.biospecimen.events.VisitDetail;
import com.krishagni.catissueplus.core.biospecimen.events.VisitSpecimenDetail;
import com.krishagni.catissueplus.core.biospecimen.repository.DaoFactory;
import com.krishagni.catissueplus.core.biospecimen.services.SpecimenService;
import com.krishagni.catissueplus.core.biospecimen.services.VisitService;
import com.krishagni.catissueplus.core.common.PlusTransactional;
import com.krishagni.catissueplus.core.common.access.AccessCtrlMgr;
import com.krishagni.catissueplus.core.common.errors.ErrorType;
import com.krishagni.catissueplus.core.common.errors.OpenSpecimenException;
import com.krishagni.catissueplus.core.common.events.EntityQueryCriteria;
import com.krishagni.catissueplus.core.common.events.RequestEvent;
import com.krishagni.catissueplus.core.common.events.Resource;
import com.krishagni.catissueplus.core.common.events.ResponseEvent;

public class VisitServiceImpl implements VisitService {
	private DaoFactory daoFactory;

	private VisitFactory visitFactory;
	
	private SpecimenService specimenSvc;

	public void setDaoFactory(DaoFactory daoFactory) {
		this.daoFactory = daoFactory;
	}

	public void setVisitFactory(VisitFactory visitFactory) {
		this.visitFactory = visitFactory;
	}
	
	public void setSpecimenSvc(SpecimenService specimenSvc) {
		this.specimenSvc = specimenSvc;
	}

	@Override
	@PlusTransactional
	public ResponseEvent<VisitDetail> getVisit(RequestEvent<EntityQueryCriteria> req) {
		try {
			EntityQueryCriteria crit = req.getPayload();
			
			Visit visit = getVisit(crit.getId(), crit.getName());
			if (visit == null) {
				return ResponseEvent.userError(VisitErrorCode.NOT_FOUND);
			}
			
			ensureHasReadPermissionsOnVisit(visit);
			return ResponseEvent.response(VisitDetail.from(visit));			
		} catch (OpenSpecimenException ose) {
			return ResponseEvent.error(ose);
		} catch (Exception e) {
			return ResponseEvent.serverError(e);
		}
	}	
	
	@Override
	@PlusTransactional
	public ResponseEvent<VisitDetail> addOrUpdateVisit(RequestEvent<VisitDetail> req) {
		try {
			VisitDetail respPayload = saveOrUpdateVisit(req.getPayload());
			return ResponseEvent.response(respPayload);
		} catch (OpenSpecimenException ose) {
			return ResponseEvent.error(ose);
		} catch (Exception ex) {
			return ResponseEvent.serverError(ex);
		}
	}

	@Override
	@PlusTransactional
	public ResponseEvent<VisitSpecimenDetail> collectVisitAndSpecimens(
			RequestEvent<VisitSpecimenDetail> req) {
		
		try {
			VisitDetail visit = saveOrUpdateVisit(req.getPayload().getVisit());			
			
			List<SpecimenDetail> specimens = req.getPayload().getSpecimens();
			setVisitId(visit.getId(), specimens);
						
			RequestEvent<List<SpecimenDetail>> collectSpecimensReq =
					new RequestEvent<List<SpecimenDetail>>(req.getSessionDataBean(), specimens);
			ResponseEvent<List<SpecimenDetail>> collectSpecimensResp =
					specimenSvc.collectSpecimens(collectSpecimensReq);
			collectSpecimensResp.throwErrorIfUnsuccessful();
			
			VisitSpecimenDetail resp = new VisitSpecimenDetail();
			resp.setVisit(visit);
			resp.setSpecimens(collectSpecimensResp.getPayload());
			return ResponseEvent.response(resp);
		} catch (OpenSpecimenException ose) {
			return ResponseEvent.error(ose);
		} catch (Exception e) {
			return ResponseEvent.serverError(e);
		}
	}
	
	private VisitDetail saveOrUpdateVisit(VisitDetail input) {		
		Visit existing = null;
		
		if (input.getId() != null || StringUtils.isNotEmpty(input.getName())) {
			existing = getVisit(input.getId(), input.getName());
			if (existing == null) {
				throw OpenSpecimenException.userError(VisitErrorCode.NOT_FOUND);
			}			
		}
							
		OpenSpecimenException ose = new OpenSpecimenException(ErrorType.USER_ERROR);
		
		Visit visit = visitFactory.createVisit(input);		
		if (existing == null || !existing.getName().equals(visit.getName())) {
			ensureUniqueVisitName(visit.getName(), ose);
		}
		
		ose.checkAndThrow();
		if (existing != null) {
			ensureHasUpdatePermissionOnVisit(existing);
			existing.update(visit);
		} else {
			ensureHasCreatePermissionOnVisit(visit);
			existing = visit;
		}
		
		existing.setNameIfEmpty();
		daoFactory.getVisitsDao().saveOrUpdate(existing);
		return VisitDetail.from(existing);		
	}
	
	private Visit getVisit(Long visitId, String visitName) {
		Visit visit = null;
		
		if (visitId != null) {
			visit = daoFactory.getVisitsDao().getById(visitId);
		} else if (StringUtils.isNotBlank(visitName)) {
			visit = daoFactory.getVisitsDao().getByName(visitName);
		}		
		
		return visit;
	}
	
	private void ensureUniqueVisitName(String visitName, OpenSpecimenException ose) {
		if (daoFactory.getVisitsDao().getByName(visitName) != null) {
			ose.addError(VisitErrorCode.DUP_NAME);
		}		
	}
	
	private void setVisitId(Long visitId, List<SpecimenDetail> specimens) {
		for (SpecimenDetail specimen : specimens) {
			specimen.setVisitId(visitId);
			setVisitId(visitId, specimen.getChildren());
		}
	}
	
	/***************************************************************
	 * Permission Checker                                          *
	 ***************************************************************/
	
	private void ensureHasReadPermissionsOnVisit(Visit visit) {
		AccessCtrlMgr.getInstance().ensureReadPermission(Resource.VISIT, visit.getCollectionProtocol(), visit.getParticipant().getMrnSites());
	}
	
	private void ensureHasCreatePermissionOnVisit(Visit visit) {
		AccessCtrlMgr.getInstance().ensureCreatePermission(Resource.VISIT, visit.getCollectionProtocol(), visit.getParticipant().getMrnSites());
	}
	
	private void ensureHasUpdatePermissionOnVisit(Visit visit) {
		AccessCtrlMgr.getInstance().ensureUpdatePermission(Resource.VISIT, visit.getCollectionProtocol(), visit.getParticipant().getMrnSites());
	}
}
