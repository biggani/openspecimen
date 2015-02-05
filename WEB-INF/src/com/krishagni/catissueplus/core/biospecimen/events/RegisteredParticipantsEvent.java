
package com.krishagni.catissueplus.core.biospecimen.events;

import java.util.List;

import com.krishagni.catissueplus.core.common.events.EventStatus;
import com.krishagni.catissueplus.core.common.events.ResponseEvent;

public class RegisteredParticipantsEvent extends ResponseEvent {
	private Long cpId;
	
	private List<CprSummary> registeredParticipants;

	public Long getCpId() {
		return cpId;
	}

	public void setCpId(Long cpId) {
		this.cpId = cpId;
	}

	public List<CprSummary> getParticipants() {
		return registeredParticipants;
	}

	public void setParticipantsInfo(List<CprSummary> registeredParticipants) {
		this.registeredParticipants = registeredParticipants;
	}

	public static RegisteredParticipantsEvent ok(List<CprSummary> registeredParticipants) {
		RegisteredParticipantsEvent resp = new RegisteredParticipantsEvent();
		resp.setStatus(EventStatus.OK);
		resp.setParticipantsInfo(registeredParticipants);		
		return resp;
	}

	public static RegisteredParticipantsEvent serverError(Throwable... t) {
		Throwable t1 = t != null && t.length > 0 ? t[0] : null;
		
		RegisteredParticipantsEvent resp = new RegisteredParticipantsEvent();
		resp.setStatus(EventStatus.INTERNAL_SERVER_ERROR);
		resp.setException(t1);
		resp.setMessage(t1 != null ? t1.getMessage() : null);
		return resp;
	}
	
	public static RegisteredParticipantsEvent notFound(Long cpId) {
		RegisteredParticipantsEvent req = new RegisteredParticipantsEvent();
		req.setStatus(EventStatus.NOT_FOUND);
		req.setCpId(cpId);
		return req;
	}
}