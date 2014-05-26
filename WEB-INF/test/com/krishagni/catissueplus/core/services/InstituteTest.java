
package com.krishagni.catissueplus.core.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.krishagni.catissueplus.core.administrative.domain.Institute;
import com.krishagni.catissueplus.core.administrative.domain.factory.InstituteFactory;
import com.krishagni.catissueplus.core.administrative.domain.factory.UserErrorCode;
import com.krishagni.catissueplus.core.administrative.domain.factory.impl.InstituteFactoryImpl;
import com.krishagni.catissueplus.core.administrative.events.CreateInstituteEvent;
import com.krishagni.catissueplus.core.administrative.events.InstituteCreatedEvent;
import com.krishagni.catissueplus.core.administrative.events.InstituteDetails;
import com.krishagni.catissueplus.core.administrative.events.InstituteUpdatedEvent;
import com.krishagni.catissueplus.core.administrative.events.UpdateInstituteEvent;
import com.krishagni.catissueplus.core.administrative.repository.InstituteDao;
import com.krishagni.catissueplus.core.administrative.services.InstituteService;
import com.krishagni.catissueplus.core.administrative.services.impl.InstituteServiceImpl;
import com.krishagni.catissueplus.core.biospecimen.repository.DaoFactory;
import com.krishagni.catissueplus.core.common.events.EventStatus;
import com.krishagni.catissueplus.core.privileges.domain.factory.PrivilegeErrorCode;
import com.krishagni.catissueplus.core.services.testdata.InstituteTestData;

public class InstituteTest {

	@Mock
	private DaoFactory daoFactory;

	@Mock
	InstituteDao instituteDao;

	private InstituteFactory instituteFactory;
	private InstituteService instituteService;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		when(daoFactory.getInstituteDao()).thenReturn(instituteDao);
		instituteService = new InstituteServiceImpl();
		((InstituteServiceImpl) instituteService).setDaoFactory(daoFactory);
		instituteFactory = new InstituteFactoryImpl();
		((InstituteServiceImpl) instituteService).setInstituteFactory(instituteFactory);
		when(instituteDao.getInstituteByName(anyString())).thenReturn(null);
	}

	@Test
	public void testForSuccessfulInstituteCreation() {
		CreateInstituteEvent reqEvent = InstituteTestData.getCreateInstituteEvent();
		InstituteCreatedEvent response = instituteService.createInstitute(reqEvent);
		assertNotNull("response cannot be null", response);
		assertEquals(EventStatus.OK, response.getStatus());
		assertNotNull(response.getInstituteDetails().getId());
		assertEquals(reqEvent.getInstituteDetails().getName(), response.getInstituteDetails().getName());
	}

	@Test
	public void testInstituteCreationWithDuplicateInstituteName() {
		when(instituteDao.getInstituteByName(anyString())).thenReturn(InstituteTestData.getInstitute(1l));
		CreateInstituteEvent reqEvent = InstituteTestData.getCreateInstituteEvent();
		InstituteCreatedEvent response = instituteService.createInstitute(reqEvent);
		assertEquals(EventStatus.BAD_REQUEST, response.getStatus());
		assertEquals(InstituteTestData.INSTITUTE_NAME, response.getErroneousFields()[0].getFieldName());
		assertEquals(UserErrorCode.DUPLICATE_INSTITUTE_NAME.message(), response.getErroneousFields()[0].getErrorMessage());
	}

	@Test
	public void testInstituteCreationWithEmptyInstituteName() {
		CreateInstituteEvent reqEvent = InstituteTestData.getCreateInstituteEventForEmptyName();
		InstituteCreatedEvent response = instituteService.createInstitute(reqEvent);
		assertEquals(EventStatus.BAD_REQUEST, response.getStatus());
		assertEquals(1, response.getErroneousFields().length);
		assertEquals(InstituteTestData.INSTITUTE_NAME, response.getErroneousFields()[0].getFieldName());
		assertEquals(PrivilegeErrorCode.INVALID_ATTR_VALUE.message(), response.getErroneousFields()[0].getErrorMessage());
	}

	@Test
	public void testInstituteCreationWithServerErr() {
		CreateInstituteEvent reqEvent = InstituteTestData.getCreateInstituteEvent();

		doThrow(new RuntimeException()).when(instituteDao).saveOrUpdate(any(Institute.class));
		InstituteCreatedEvent response = instituteService.createInstitute(reqEvent);
		assertNotNull("response cannot be null", response);
		assertEquals(EventStatus.INTERNAL_SERVER_ERROR, response.getStatus());
	}

	@Test
	public void testForSuccessfulInstituteUpdate() {
		when(instituteDao.getInstitute(anyLong())).thenReturn(InstituteTestData.getInstitute(1L));
		UpdateInstituteEvent reqEvent = InstituteTestData.getUpdateInstituteEvent();

		InstituteUpdatedEvent response = instituteService.updateInstitute(reqEvent);
		assertEquals(EventStatus.OK, response.getStatus());
		InstituteDetails createdInstitute = response.getInstituteDetails();
		assertEquals(reqEvent.getInstituteDetails().getName(), createdInstitute.getName());
	}

	@Test
	public void testForInvalidInstituteUpdate() {
		when(instituteDao.getInstitute(anyLong())).thenReturn(null);
		UpdateInstituteEvent reqEvent = InstituteTestData.getUpdateInstituteEvent();

		InstituteUpdatedEvent response = instituteService.updateInstitute(reqEvent);
		assertEquals(EventStatus.NOT_FOUND, response.getStatus());
	}

	@Test
	public void testInstituteUpdateWithServerErr() {
		when(instituteDao.getInstitute(anyLong())).thenReturn(InstituteTestData.getInstitute(1L));
		UpdateInstituteEvent reqEvent = InstituteTestData.getUpdateInstituteEvent();

		doThrow(new RuntimeException()).when(instituteDao).saveOrUpdate(any(Institute.class));
		InstituteUpdatedEvent response = instituteService.updateInstitute(reqEvent);
		assertNotNull("response cannot be null", response);
		assertEquals(EventStatus.INTERNAL_SERVER_ERROR, response.getStatus());
	}
}