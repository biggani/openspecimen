catissue_reported_problem;select AFFILIATION,NAME_OF_REPORTER,REPORTERS_EMAIL_ID,MESSAGE_BODY,SUBJECT from catissue_reported_problem where SUBJECT='Error in DB connection'
catissue_audit_event_details;select ELEMENT_NAME,CURRENT_VALUE from catissue_audit_event_details where ELEMENT_NAME='SUBJECT'
catissue_audit_event;select EVENT_TYPE from catissue_audit_event