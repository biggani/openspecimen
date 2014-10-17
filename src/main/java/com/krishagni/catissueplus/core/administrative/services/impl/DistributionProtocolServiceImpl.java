
package com.krishagni.catissueplus.core.administrative.services.impl;

import java.util.ArrayList;
import java.util.List;

import krishagni.catissueplus.util.CommonUtil;

import com.krishagni.catissueplus.core.administrative.domain.DistributionProtocol;
import com.krishagni.catissueplus.core.administrative.domain.factory.DistributionProtocolErrorCode;
import com.krishagni.catissueplus.core.administrative.domain.factory.DistributionProtocolFactory;
import com.krishagni.catissueplus.core.administrative.events.AllDistributionProtocolsEvent;
import com.krishagni.catissueplus.core.administrative.events.CreateDistributionProtocolEvent;
import com.krishagni.catissueplus.core.administrative.events.DeleteDistributionProtocolEvent;
import com.krishagni.catissueplus.core.administrative.events.DistributionProtocolCreatedEvent;
import com.krishagni.catissueplus.core.administrative.events.DistributionProtocolDeletedEvent;
import com.krishagni.catissueplus.core.administrative.events.DistributionProtocolDetails;
import com.krishagni.catissueplus.core.administrative.events.DistributionProtocolPatchedEvent;
import com.krishagni.catissueplus.core.administrative.events.DistributionProtocolUpdatedEvent;
import com.krishagni.catissueplus.core.administrative.events.GetDistributionProtocolEvent;
import com.krishagni.catissueplus.core.administrative.events.GotDistributionProtocolEvent;
import com.krishagni.catissueplus.core.administrative.events.PatchDistributionProtocolEvent;
import com.krishagni.catissueplus.core.administrative.events.ReqAllDistributionProtocolEvent;
import com.krishagni.catissueplus.core.administrative.events.UpdateDistributionProtocolEvent;
import com.krishagni.catissueplus.core.administrative.services.DistributionProtocolService;
import com.krishagni.catissueplus.core.biospecimen.repository.DaoFactory;
import com.krishagni.catissueplus.core.common.PlusTransactional;
import com.krishagni.catissueplus.core.common.errors.ObjectCreationException;
import com.krishagni.catissueplus.core.common.util.Status;

public class DistributionProtocolServiceImpl implements DistributionProtocolService {

	public void setDaoFactory(DaoFactory daoFactory) {
		this.daoFactory = daoFactory;
	}

	public void setDistributionProtocolFactory(DistributionProtocolFactory distributionProtocolFactory) {
		this.distributionProtocolFactory = distributionProtocolFactory;
	}

	@Override
	@PlusTransactional
	public AllDistributionProtocolsEvent getAllDistributionProtocols(ReqAllDistributionProtocolEvent event) {
		List<DistributionProtocol> distributionProtocols = daoFactory.getDistributionProtocolDao()
				.getAllDistributionProtocol(event.getMaxResults());
		List<DistributionProtocolDetails> result = new ArrayList<DistributionProtocolDetails>();

		for (DistributionProtocol distributionProtocol : distributionProtocols) {
			result.add(DistributionProtocolDetails.fromDomain(distributionProtocol));
		}

		return AllDistributionProtocolsEvent.ok(result);
	}

	@Override
	@PlusTransactional
	public GotDistributionProtocolEvent getDistributionProtocol(GetDistributionProtocolEvent event) {
		DistributionProtocol distributionProtocol;
		if (event.getId() != null) {
			distributionProtocol = daoFactory.getDistributionProtocolDao().getDistributionProtocol(event.getId());
			if (distributionProtocol == null) {
				return GotDistributionProtocolEvent.notFound(event.getId());
			}
		}
		else {
			distributionProtocol = daoFactory.getDistributionProtocolDao().getDistributionProtocol(event.getTitle());
			if (distributionProtocol == null) {
				return GotDistributionProtocolEvent.notFound(event.getTitle());
			}
		}
		DistributionProtocolDetails details = DistributionProtocolDetails.fromDomain(distributionProtocol);
		return GotDistributionProtocolEvent.ok(details);
	}

	@Override
	@PlusTransactional
	public DistributionProtocolCreatedEvent createDistributionProtocol(CreateDistributionProtocolEvent event) {
		try {
			DistributionProtocol distributionProtocol = distributionProtocolFactory.create(event
					.getDistributionProtocolDetails());
			ObjectCreationException exceptionHandler = new ObjectCreationException();
			ensureUniqueTitle(distributionProtocol.getTitle(), exceptionHandler);
			ensureUniqueShortTitle(distributionProtocol.getShortTitle(), exceptionHandler);

			exceptionHandler.checkErrorAndThrow();
			daoFactory.getDistributionProtocolDao().saveOrUpdate(distributionProtocol);
			return DistributionProtocolCreatedEvent.ok(DistributionProtocolDetails.fromDomain(distributionProtocol));
		}
		catch (ObjectCreationException exception) {
			return DistributionProtocolCreatedEvent.invalidRequest(exception.getMessage(), exception.getErroneousFields());
		}
		catch (Exception e) {
			return DistributionProtocolCreatedEvent.serverError(e);
		}
	}

	@Override
	@PlusTransactional
	public DistributionProtocolUpdatedEvent updateDistributionProtocol(UpdateDistributionProtocolEvent event) {
		try {
			DistributionProtocol oldDistributionProtocol;

			if (event.getId() != null) {
				Long id = event.getId();
				oldDistributionProtocol = daoFactory.getDistributionProtocolDao().getDistributionProtocol(id);

				if (oldDistributionProtocol == null) {
					return DistributionProtocolUpdatedEvent.notFound(id);
				}
			}
			else {
				String title = event.getTitle();
				oldDistributionProtocol = daoFactory.getDistributionProtocolDao().getDistributionProtocol(title);

				if (oldDistributionProtocol == null) {
					return DistributionProtocolUpdatedEvent.notFound(title);
				}

			}

			ObjectCreationException exceptionHandler = new ObjectCreationException();
			DistributionProtocol distributionProtocol = distributionProtocolFactory.create(event.getDetails());
			checkShortTitle(oldDistributionProtocol.getShortTitle(), distributionProtocol.getShortTitle(), exceptionHandler);
			checkTitle(oldDistributionProtocol.getTitle(), distributionProtocol.getTitle(), exceptionHandler);
			exceptionHandler.checkErrorAndThrow();
			oldDistributionProtocol.update(distributionProtocol);
			daoFactory.getDistributionProtocolDao().saveOrUpdate(oldDistributionProtocol);

			return DistributionProtocolUpdatedEvent.ok(DistributionProtocolDetails.fromDomain(oldDistributionProtocol));
		}
		catch (ObjectCreationException exception) {
			return DistributionProtocolUpdatedEvent.invalidRequest(DistributionProtocolErrorCode.ERRORS.message(),
					exception.getErroneousFields());
		}
		catch (Exception ex) {
			return DistributionProtocolUpdatedEvent.serverError(ex);
		}
	}

	@Override
	@PlusTransactional
	public DistributionProtocolPatchedEvent patchDistributionProtocol(PatchDistributionProtocolEvent reqEvent) {
		try {
			DistributionProtocol oldDistributionProtocol;

			if (reqEvent.getId() != null) {
				Long id = reqEvent.getId();
				oldDistributionProtocol = daoFactory.getDistributionProtocolDao().getDistributionProtocol(id);
				if (oldDistributionProtocol == null) {
					return DistributionProtocolPatchedEvent.notFound(id);
				}
			}
			else {
				String title = reqEvent.getTitle();
				oldDistributionProtocol = daoFactory.getDistributionProtocolDao().getDistributionProtocol(title);
				if (oldDistributionProtocol == null) {
					return DistributionProtocolPatchedEvent.notFound(title);
				}
			}
			String oldShortTitle = oldDistributionProtocol.getShortTitle();
			String oldTitle = oldDistributionProtocol.getTitle();
			DistributionProtocol distributionProtocol = distributionProtocolFactory.patch(oldDistributionProtocol,
					reqEvent.getDetails());

			ObjectCreationException exceptionHandler = new ObjectCreationException();

			checkShortTitle(oldShortTitle, reqEvent.getDetails().getShortTitle(), exceptionHandler);
			checkTitle(oldTitle, reqEvent.getDetails().getTitle(), exceptionHandler);
			if (reqEvent.getDetails().isDistributionProtocolActivityStatusModified()) {
				checkActivityStatus(distributionProtocol);
			}
			exceptionHandler.checkErrorAndThrow();

			daoFactory.getDistributionProtocolDao().saveOrUpdate(distributionProtocol);
			return DistributionProtocolPatchedEvent.ok(DistributionProtocolDetails.fromDomain(distributionProtocol));
		}
		catch (ObjectCreationException exception) {
			return DistributionProtocolPatchedEvent.invalidRequest(DistributionProtocolErrorCode.ERRORS.message(),
					exception.getErroneousFields());
		}
		catch (Exception ex) {
			return DistributionProtocolPatchedEvent.serverError(ex);
		}
	}

	@Override
	@PlusTransactional
	public DistributionProtocolDeletedEvent deleteDistributionProtocol(DeleteDistributionProtocolEvent event) {
		try {
			DistributionProtocol oldDistributionProtocol;

			if (event.getId() != null) {
				oldDistributionProtocol = daoFactory.getDistributionProtocolDao().getDistributionProtocol(event.getId());
				if (oldDistributionProtocol == null) {
					return DistributionProtocolDeletedEvent.notFound(event.getId());
				}
			}
			else {
				oldDistributionProtocol = daoFactory.getDistributionProtocolDao().getDistributionProtocol(event.getTitle());
				if (oldDistributionProtocol == null) {
					return DistributionProtocolDeletedEvent.notFound(event.getTitle());
				}
			}

			oldDistributionProtocol.delete();
			daoFactory.getDistributionProtocolDao().saveOrUpdate(oldDistributionProtocol);
			return DistributionProtocolDeletedEvent.ok();
		}
		catch (Exception e) {
			return DistributionProtocolDeletedEvent.serverError(e);
		}
	}

	private void checkActivityStatus(DistributionProtocol distributionProtocol) {
		if (distributionProtocol.getActivityStatus().equals(Status.ACTIVITY_STATUS_DISABLED.getStatus())) {
			distributionProtocol.setTitle(CommonUtil.appendTimestamp(distributionProtocol.getTitle()));
			distributionProtocol.setShortTitle(CommonUtil.appendTimestamp(distributionProtocol.getShortTitle()));
		}

	}

	private void checkTitle(String oldTitle, String newTitle, ObjectCreationException exceptionHandler) {
		if (!oldTitle.equals(newTitle)) {
			ensureUniqueTitle(newTitle, exceptionHandler);
		}
	}

	private void checkShortTitle(String oldShortTitle, String newShortTitle, ObjectCreationException exceptionHandler) {
		if (!oldShortTitle.equals(newShortTitle)) {
			ensureUniqueShortTitle(newShortTitle, exceptionHandler);
		}
	}

	private void ensureUniqueShortTitle(String shortTitle, ObjectCreationException exceptionHandler) {

		if (!daoFactory.getDistributionProtocolDao().isUniqueShortTitle(shortTitle)) {
			exceptionHandler.addError(DistributionProtocolErrorCode.DUPLICATE_PROTOCOL_SHORT_TITLE, SHORT_TITLE);
		}

	}

	private void ensureUniqueTitle(String title, ObjectCreationException exceptionHandler) {
		if (!daoFactory.getDistributionProtocolDao().isUniqueTitle(title)) {
			exceptionHandler.addError(DistributionProtocolErrorCode.DUPLICATE_PROTOCOL_TITLE, TITLE);
		}

	}

	private static final String SHORT_TITLE = "distribution protocol short title";

	private static final String TITLE = "distribution protocol title";

	private DaoFactory daoFactory;

	private DistributionProtocolFactory distributionProtocolFactory;
}
