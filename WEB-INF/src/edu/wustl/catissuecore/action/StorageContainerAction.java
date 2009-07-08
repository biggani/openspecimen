/**
 * <p>
 * Title: StorageContainerAction Class>
 * <p>
 * Description: This class initializes the fields of StorageContainer.jsp Page
 * </p>
 * Copyright: Copyright (c) year Company: Washington University, School of
 * Medicine, St. Louis.
 *
 * @author Aniruddha Phadnis
 * @version 1.00 Created on Jul 18, 2005
 */

package edu.wustl.catissuecore.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.json.JSONObject;

import edu.wustl.catissuecore.actionForm.StorageContainerForm;
import edu.wustl.catissuecore.bean.StorageContainerBean;
import edu.wustl.catissuecore.bizlogic.StorageContainerBizLogic;
import edu.wustl.catissuecore.bizlogic.StorageTypeBizLogic;
import edu.wustl.catissuecore.domain.Container;
import edu.wustl.catissuecore.domain.Site;
import edu.wustl.catissuecore.domain.SpecimenArrayType;
import edu.wustl.catissuecore.domain.StorageContainer;
import edu.wustl.catissuecore.domain.StorageType;
import edu.wustl.catissuecore.util.StorageContainerUtil;
import edu.wustl.catissuecore.util.global.AppUtility;
import edu.wustl.catissuecore.util.global.Constants;
import edu.wustl.common.action.SecureAction;
import edu.wustl.common.beans.NameValueBean;
import edu.wustl.common.beans.SessionDataBean;
import edu.wustl.common.exception.ApplicationException;
import edu.wustl.common.exception.BizLogicException;
import edu.wustl.common.factory.AbstractFactoryConfig;
import edu.wustl.common.factory.IFactory;
import edu.wustl.common.util.global.CommonUtilities;
import edu.wustl.common.util.global.Status;
import edu.wustl.common.util.logger.Logger;

/**
 * @author renuka_bajpai
 */
public class StorageContainerAction extends SecureAction
{

	/**
	 * logger.
	 */
	private transient final Logger logger = Logger.getCommonLogger(StorageContainerAction.class);

	/**
	 * Overrides the executeSecureAction method of SecureAction class.
	 * @param mapping : obj of ActionMapping
	 * @param form
	 *            object of ActionForm
	 * @param request
	 *            object of HttpServletRequest
	 * @param response
	 *            object of HttpServletResponse
	 * @throws Exception
	 *             generic exception
	 * @return ActionForward : ActionForward
	 */
	@Override
	protected ActionForward executeSecureAction(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		final StorageContainerForm storageContainerForm = (StorageContainerForm) form;
		final HttpSession session = request.getSession();
		final StorageContainerBean storageContainerBean = (StorageContainerBean) session
				.getAttribute(Constants.STORAGE_CONTAINER_SESSION_BEAN);
		// boolean to indicate whether the suitable containers to be shown in
		// dropdown
		// is exceeding the max limit.
		final String exceedingMaxLimit = "false";
		final String pageOf = request.getParameter(Constants.PAGE_OF);
		final String containerId = request.getParameter("containerIdentifier");
		final String isPageFromStorageType = (String) session.getAttribute("isPageFromStorageType");
		// String isSiteChanged=(String)request.getParameter("isSiteChanged");
		final String isContainerChanged = request.getParameter("isContainerChanged");

		final SessionDataBean sessionDataBean = this.getSessionData(request);

		if (storageContainerForm.getSpecimenOrArrayType() == null)
		{
			storageContainerForm.setSpecimenOrArrayType("Specimen");
		}
		// set the menu selection
		request.setAttribute(Constants.MENU_SELECTED, "7");

		boolean isTypeChange = false;
		// boolean isSiteOrParentContainerChange = false;

		String str = request.getParameter("isOnChange");
		str = request.getParameter("typeChange");
		if (str != null && str.equals("true"))
		{
			isTypeChange = true;

		}
		/*
		 * str = request.getParameter("isSiteOrParentContainerChange"); if (str
		 * != null && str.equals("true")) { isSiteOrParentContainerChange =
		 * true; }
		 */

		// Gets the value of the operation parameter.
		final String operation = request.getParameter(Constants.OPERATION);
		request.setAttribute(Constants.OPERATION, operation);
		if (operation.equals(Constants.EDIT) && storageContainerBean != null
				&& Constants.PAGE_OF_STORAGE_CONTAINER.equals(pageOf))
		{
			this.initStorageContainerForm(storageContainerForm, storageContainerBean);
		}
		this.setRequestAttributes(request, storageContainerForm);
		this.setStorageType(request, storageContainerForm, session);
		final IFactory factory = AbstractFactoryConfig.getInstance().getBizLogicFactory();
		final StorageContainerBizLogic bizLogic = (StorageContainerBizLogic) factory
				.getBizLogic(Constants.STORAGE_CONTAINER_FORM_ID);
		// ---- chetan 15-06-06 ----
		// if (isSiteChanged != null && isSiteChanged.equals("true"))
		// {
		// setCollectionProtocolList(request,storageContainerForm.getSiteId());
		//
		// }
		if (isContainerChanged != null && isContainerChanged.equals("true"))
		{
			final List<JSONObject> jsonList = new ArrayList<JSONObject>();
			final String parentEleType = request.getParameter("parentEleType");
			if (parentEleType.equals("parentContAuto"))
			{
				final long contId = new Long(request.getParameter("parentContainerId"));
				// long contId = storageContainerForm.getParentContainerId();
				final Site site = bizLogic.getRelatedSite(contId);
				if (site != null)
				{
					final long siteId = site.getId();
					AppUtility.setCollectionProtocolList(request, siteId);
				}
			}
			else if (parentEleType.equals("parentContManual"))
			{
				final String contName = request.getParameter("selectedContainerName");
				// String contName =
				// storageContainerForm.getSelectedContainerName();
				final Site site = bizLogic.getRelatedSiteForManual(contName);
				if (site != null)
				{
					final long siteId = site.getId();
					AppUtility.setCollectionProtocolList(request, siteId);
				}
			}
			else if (parentEleType.equals("parentContSite"))
			{
				final long siteId = new Long(request.getParameter("siteId"));
				// String contName =
				// storageContainerForm.getSelectedContainerName();
				AppUtility.setCollectionProtocolList(request, siteId);
			}
			JSONObject jsonObject = null;
			final List<NameValueBean> cpList = (List<NameValueBean>) request
					.getAttribute(Constants.PROTOCOL_LIST);
			if (cpList != null && !cpList.isEmpty())
			{
				for (final NameValueBean nvbean : cpList)
				{
					jsonObject = new JSONObject();
					jsonObject.append("cpName", nvbean.getName());
					jsonObject.append("cpValue", nvbean.getValue());
					jsonList.add(jsonObject);
				}
			}
			response.flushBuffer();
			response.getWriter().write(new JSONObject().put("locations", jsonList).toString());

			return null;

		}

		TreeMap containerMap = new TreeMap();
		if (storageContainerForm.getTypeId() != -1)
		{
			final long start = System.currentTimeMillis();
			containerMap = bizLogic.getAllocatedContaienrMapForContainer(storageContainerForm
					.getTypeId(), exceedingMaxLimit, null, sessionDataBean);
			final long end = System.currentTimeMillis();

			System.out.println("Time taken for getAllocatedMapForCOntainer:" + (end - start));
		}
		if (containerId != null)
		{
			final StorageContainerBizLogic storageContaineriBzLogic =
				(StorageContainerBizLogic) factory
					.getBizLogic(Constants.STORAGE_CONTAINER_FORM_ID);
			final String name = StorageContainer.class.getName();
			final Long long1 = new Long(containerId);
			final StorageType storageType = (StorageType) storageContaineriBzLogic
					.retrieveAttribute(name, long1, "storageType");
			final Long typeId = storageType.getId();
			final long start = System.currentTimeMillis();
			containerMap = bizLogic.getAllocatedContaienrMapForContainer(typeId, exceedingMaxLimit,
					null, sessionDataBean);
			final long end = System.currentTimeMillis();
			System.out.println("Time taken for getAllocatedMapForCOntainer:" + (end - start));
		}

		if (operation.equals(Constants.ADD))
		{
			this.SetParentStorageContainersForAdd(containerMap, storageContainerForm, request);
		}

		if (operation.equals(Constants.EDIT))
		{

			if (StorageContainerUtil.chkContainerFull(new Long(storageContainerForm.getId())
					.toString(), storageContainerForm.getContainerName()))
			{
				storageContainerForm.setIsFull("true");
			}
			final List storagetypeList = new ArrayList();
			final NameValueBean nvb = new NameValueBean(storageContainerForm.getTypeName(),
					new Long(storageContainerForm.getTypeId()));
			storagetypeList.add(nvb);
			request.setAttribute(Constants.STORAGETYPELIST, storagetypeList);
			this.SetParentStorageCOntainersForEdit(containerMap, storageContainerForm, request);
			// request =
			// Utility.setCollectionProtocolList(request,storageContainerForm
			// .getSiteId());
		}
		request.setAttribute("storageContainerIdentifier", storageContainerForm.getId());
		request.setAttribute(Constants.EXCEEDS_MAX_LIMIT, exceedingMaxLimit);
		request.setAttribute(Constants.AVAILABLE_CONTAINER_MAP, containerMap);

		/*
		 * if (isSiteOrParentContainerChange) {
		 * onSiteOrParentContChange(request, response,storageContainerForm);
		 * return null; }
		 */

		this.setFormAttributesForAddNew(request, storageContainerForm);
		// -- 24-Jan-06 end
		if (isTypeChange || request.getAttribute(Constants.SUBMITTED_FOR) != null
				|| Constants.YES.equals(isPageFromStorageType))
		{
			this.onTypeChange(storageContainerForm, operation, request);
		}

		if (request.getAttribute(Constants.SUBMITTED_FOR) != null)
		{
			final long[] collectionIds = this.parentContChange(request);
			storageContainerForm.setCollectionIds(collectionIds);
		}

		// ---------- Add new
		final String reqPath = request.getParameter(Constants.REQ_PATH);

		if (reqPath != null)
		{
			request.setAttribute(Constants.REQ_PATH, reqPath);
		}
		final List<NameValueBean> parentContainerTypeList = AppUtility.getParentContainerTypeList();
		request.setAttribute("parentContainerTypeList", parentContainerTypeList);
		request.setAttribute("parentContainerSelected", storageContainerForm
				.getParentContainerSelected());
		session.removeAttribute(Constants.STORAGE_CONTAINER_SESSION_BEAN);
		session.removeAttribute("isPageFromStorageType");
		AppUtility.setDefaultPrinterTypeLocation(storageContainerForm);
		return mapping.findForward(request.getParameter(Constants.PAGE_OF));
	}

	/**
	 * @param request
	 *            : request
	 * @param storageContainerForm
	 *            : storageContainerForm
	 * @param session
	 *            : session
	 */
	private void setStorageType(HttpServletRequest request,
			StorageContainerForm storageContainerForm, HttpSession session)
	{
		// *************Start Bug:1938 ForwardTo implementation *************
		HashMap forwardToHashMap = (HashMap) request.getAttribute("forwardToHashMap");
		if (forwardToHashMap == null)
		{
			forwardToHashMap = (HashMap) session.getAttribute("forwardToHashMap");
			session.removeAttribute("forwardToHashMap");
		}
		if (forwardToHashMap != null && forwardToHashMap.size() > 0)
		{
			final Long storageTypeId = (Long) forwardToHashMap.get("storageTypeId");
			this.logger.debug("storageTypeId found in forwardToHashMap========>>>>>>"
					+ storageTypeId);
			storageContainerForm.setTypeId(storageTypeId.longValue());
		}
		else
		{
			if (request.getParameter("storageTypeId") != null)
			{
				final Long storageTypeId = new Long(request.getParameter("storageTypeId"));
				storageContainerForm.setTypeId(storageTypeId.longValue());
			}
			else if (session.getAttribute("storageTypeIdentifier") != null)
			{
				final Long storageTypeId = (Long) session.getAttribute("storageTypeIdentifier");
				storageContainerForm.setTypeId(storageTypeId.longValue());
				session.removeAttribute("storageTypeIdentifier");
			}
		}
		// *************End Bug:1938 ForwardTo implementation *************

	}

	/**
	 * @param request
	 *            : request
	 * @param storageContainerForm
	 *            : storageContainerForm
	 * @throws ApplicationException
	 *             : ApplicationException
	 */
	private void setRequestAttributes(HttpServletRequest request,
			StorageContainerForm storageContainerForm) throws ApplicationException
	{
		final IFactory factory = AbstractFactoryConfig.getInstance().getBizLogicFactory();
		final StorageContainerBizLogic bizLogic = (StorageContainerBizLogic) factory
				.getBizLogic(Constants.STORAGE_CONTAINER_FORM_ID);
		// Gets the value of the operation parameter.
		final String operation = request.getParameter(Constants.OPERATION);
		// Sets the operation attribute to be used in the Add/Edit Institute
		// Page.
		request.setAttribute(Constants.OPERATION, operation);
		// Sets the activityStatusList attribute to be used in the Site Add/Edit
		// Page.
		request.setAttribute(Constants.ACTIVITYSTATUSLIST, Constants.ACTIVITY_STATUS_VALUES);

		// Populating the Site Array
		final String[] siteDisplayField = {"name"};
		final String valueField = "id";

		/**
		 * Name : kalpana thakur Reviewer Name : Vaishali Bug ID: 4922
		 * Description: get the list of site with activity status "Active"
		 */
		final String[] activityStatusArray = {Status.ACTIVITY_STATUS_DISABLED.toString(),
				Status.ACTIVITY_STATUS_CLOSED.toString()};
		final SessionDataBean sessionDataBean = this.getSessionData(request);

		// List list = bizLogic.getSiteList(Site.class.getName(),
		// siteDisplayField, valueField,activityStatusArray, false);
		final List list = bizLogic.getSiteList(siteDisplayField, valueField, activityStatusArray,
				sessionDataBean.getUserId());
		NameValueBean nvbForSelect = null;
		if (list != null && !list.isEmpty())
		{
			nvbForSelect = (NameValueBean) list.get(0);

			if (!"-1".equals(nvbForSelect.getValue()))
			{
				final NameValueBean nvb = new NameValueBean("--Select--", "-1");
				list.add(0, nvb);
			}
		}
		request.setAttribute(Constants.SITELIST, list);
		// get the Specimen class and type from the cde
		final List specimenClassTypeList = AppUtility.getSpecimenClassTypeListWithAny();
		request.setAttribute(Constants.HOLDS_LIST2, specimenClassTypeList);
		// Gets the Specimen array Type List and sets it in request
		final List list3 = bizLogic.retrieve(SpecimenArrayType.class.getName());
		final List spArrayTypeList = AppUtility.getSpecimenArrayTypeList(list3);
		request.setAttribute(Constants.HOLDS_LIST3, spArrayTypeList);

		final List list2 = bizLogic.retrieve(StorageType.class.getName());
		final List storageTypeListWithAny = AppUtility.getStorageTypeList(list2, true);
		request.setAttribute(Constants.HOLDS_LIST1, storageTypeListWithAny);

		if (Constants.ADD.equals(request.getAttribute(Constants.OPERATION)))
		{
			final List StorageTypeListWithoutAny = AppUtility.getStorageTypeList(list2, false);
			request.setAttribute(Constants.STORAGETYPELIST, StorageTypeListWithoutAny);
		}
		if ("Site".equals(storageContainerForm.getParentContainerSelected()))
		{
			request = AppUtility.setCollectionProtocolList(request, storageContainerForm
					.getSiteId());
		}
		else if ("Auto".equals(storageContainerForm.getParentContainerSelected()))
		{
			final long parentContId = storageContainerForm.getParentContainerId();
			final Site site = bizLogic.getRelatedSite(parentContId);
			if (site != null)
			{
				request = AppUtility.setCollectionProtocolList(request, site.getId());
			}
			else
			{
				final List<NameValueBean> cpList = new ArrayList<NameValueBean>();
				final Map<Long, String> cpTitleMap = new HashMap<Long, String>();
				request.setAttribute(Constants.PROTOCOL_LIST, cpList);
				request.setAttribute(Constants.CP_ID_TITLE_MAP, cpTitleMap);
			}
		}
		else if ("Manual".equals(storageContainerForm.getParentContainerSelected()))
		{
			final String containerName = storageContainerForm.getSelectedContainerName();
			final Site site = bizLogic.getRelatedSiteForManual(containerName);
			if (site != null)
			{
				request = AppUtility.setCollectionProtocolList(request, site.getId());
			}
			else
			{
				final List<NameValueBean> cpList = new ArrayList<NameValueBean>();
				final Map<Long, String> cpTitleMap = new HashMap<Long, String>();
				request.setAttribute(Constants.PROTOCOL_LIST, cpList);
				request.setAttribute(Constants.CP_ID_TITLE_MAP, cpTitleMap);
			}
		}
	}

	/**
	 * @param storageContainerForm
	 *            : storageContainerForm
	 * @param operation
	 *            : operation
	 * @param request
	 *            : request
	 * @throws BizLogicException
	 *             : BizLogicException
	 */
	private void onTypeChange(StorageContainerForm storageContainerForm, String operation,
			HttpServletRequest request) throws BizLogicException
	{
		final IFactory factory = AbstractFactoryConfig.getInstance().getBizLogicFactory();
		final StorageContainerBizLogic bizLogic = (StorageContainerBizLogic) factory
				.getBizLogic(Constants.STORAGE_CONTAINER_FORM_ID);
		// long typeSelected = -1;

		final String selectedType = String.valueOf(storageContainerForm.getTypeId());
		this.logger.debug(">>>>>>>>>>><<<<<<<<<<<<<<<<>>>>>>>>>>>>> ST : " + selectedType);
		if (selectedType != null && !selectedType.equals("-1"))
		{

			final Object object = bizLogic.retrieve(StorageType.class.getName(),
					storageContainerForm.getTypeId());
			if (object != null)
			{
				final StorageType type = (StorageType) object;
				// setFormAttributesForSelectedType(type,storageContainerForm);
				if (type.getDefaultTempratureInCentigrade() != null)
				{
					storageContainerForm.setDefaultTemperature(type
							.getDefaultTempratureInCentigrade().toString());
				}

				storageContainerForm.setOneDimensionCapacity(type.getCapacity()
						.getOneDimensionCapacity().intValue());
				storageContainerForm.setTwoDimensionCapacity(type.getCapacity()
						.getTwoDimensionCapacity().intValue());
				storageContainerForm.setOneDimensionLabel(type.getOneDimensionLabel());
				storageContainerForm.setTwoDimensionLabel(CommonUtilities.toString(type
						.getTwoDimensionLabel()));
				storageContainerForm.setTypeName(type.getName());

				if (type.getHoldsSpecimenClassCollection().size() > 0)
				{
					storageContainerForm.setSpecimenOrArrayType("Specimen");
				}
				final Collection holdsSpArrayTypeCollection = (Collection) bizLogic
						.retrieveAttribute(StorageType.class.getName(), type.getId(),
								"elements(holdsSpecimenArrayTypeCollection)");
				type.setHoldsSpecimenArrayTypeCollection(holdsSpArrayTypeCollection);
				if (holdsSpArrayTypeCollection.size() > 0)
				{
					storageContainerForm.setSpecimenOrArrayType("SpecimenArray");
				}

				// type_name=type.getType();

				this.logger.debug("Type Name:" + storageContainerForm.getTypeName());

				// If operation is add opeartion then set the holds list
				// according to storage type selected.
				if (operation != null && operation.equals(Constants.ADD))
				{
					final StorageTypeBizLogic storageTypebizLogic =
						(StorageTypeBizLogic) factory
							.getBizLogic(Constants.STORAGE_TYPE_FORM_ID);
					final long[] defHoldsStorageTypeList = storageTypebizLogic
							.getDefaultHoldStorageTypeList(type);
					if (defHoldsStorageTypeList != null)
					{
						storageContainerForm.setHoldsStorageTypeIds
						(defHoldsStorageTypeList);
					}

					final String[] defHoldsSpecimenClassTypeList = storageTypebizLogic
							.getDefaultHoldsSpecimenClasstypeList(type);
					if (defHoldsSpecimenClassTypeList != null)
					{
						storageContainerForm
								.setHoldsSpecimenClassTypes
								(defHoldsSpecimenClassTypeList);
					}

					final long[] defHoldsSpecimenArrayTypeList = storageTypebizLogic
							.getDefaultHoldSpecimenArrayTypeList(type);
					if (defHoldsSpecimenArrayTypeList != null)
					{
						storageContainerForm
								.setHoldsSpecimenArrTypeIds
								(defHoldsSpecimenArrayTypeList);
					}
				}
			}

		}
		else
		{
			request.setAttribute("storageType", null);
			storageContainerForm.setDefaultTemperature("");
			storageContainerForm.setOneDimensionCapacity(0);
			storageContainerForm.setTwoDimensionCapacity(0);
			storageContainerForm.setOneDimensionLabel("Dimension One");
			storageContainerForm.setTwoDimensionLabel("Dimension Two");
			storageContainerForm.setTypeName("");
			// type_name="";
		}

	}

	/**
	 * @param containerMap
	 *            : containerMap
	 * @param storageContainerForm
	 *            : storageContainerForm
	 * @param request
	 *            : request
	 * @throws ApplicationException
	 *             : ApplicationException
	 */
	private void SetParentStorageContainersForAdd(TreeMap containerMap,
			StorageContainerForm storageContainerForm, HttpServletRequest request)
			throws ApplicationException
	{
		List initialValues = null;
		final IFactory factory = AbstractFactoryConfig.getInstance().getBizLogicFactory();
		final StorageContainerBizLogic bizLogic = (StorageContainerBizLogic) factory
				.getBizLogic(Constants.STORAGE_CONTAINER_FORM_ID);
		initialValues = StorageContainerUtil.checkForInitialValues(containerMap);
		if (initialValues != null)
		{
			// Getting the default values in add case
			String[] initValues = new String[3];
			initValues = (String[]) initialValues.get(0);

			// getting collection protocol list and name of the container for
			// default selected parent container

			final Object object = bizLogic.retrieve(StorageContainer.class.getName(), new Long(
					initValues[0]));
			if (object != null)
			{
				if ("Auto".equals(storageContainerForm.getParentContainerSelected()))
				{
					final StorageContainer container = (StorageContainer) object;
					final Site site = container.getSite();
					if (site != null)
					{
						AppUtility.setCollectionProtocolList(request, site.getId());
					}
					// storageContainerForm.setCollectionIds(collectionIds);
					// storageContainerForm.setCollectionIds(bizLogic.
					// getDefaultHoldCollectionProtocolList(container));
				}
				// else
				// {
				// storageContainerForm.setCollectionIds(new long[]{-1});
				// }
			}

		}
		request.setAttribute("initValues", initialValues);
	}

	/**
	 * @param containerMap
	 *            : containerMap
	 * @param storageContainerForm
	 *            : storageContainerForm
	 * @param request
	 *            : request
	 * @throws BizLogicException
	 *             : BizLogicException
	 */
	private void SetParentStorageCOntainersForEdit(TreeMap containerMap,
			StorageContainerForm storageContainerForm, HttpServletRequest request)
			throws BizLogicException
	{
		List initialValues = null;
		final IFactory factory = AbstractFactoryConfig.getInstance().getBizLogicFactory();
		final StorageContainerBizLogic bizLogic = (StorageContainerBizLogic) factory
				.getBizLogic(Constants.STORAGE_CONTAINER_FORM_ID);
		if (!Constants.SITE.equals(storageContainerForm.getParentContainerSelected()))
		{
			final String[] startingPoints = new String[]{"-1", "-1", "-1"};

			final Object object = bizLogic.retrieve(StorageContainer.class.getName(),
					storageContainerForm.getId());
			if (object != null)
			{
				final StorageContainer cont = (StorageContainer) object;

				final Container parent = (Container) bizLogic.retrieveAttribute(
						StorageContainer.class.getName(), cont.getId(),
						"locatedAtPosition.parentContainer");
				if (parent != null)
				{
					final Long id = parent.getId();
					if (cont != null && cont.getLocatedAtPosition() != null)
					{
						final Integer pos1 = cont.getLocatedAtPosition().
						getPositionDimensionOne();
						final Integer pos2 = cont.getLocatedAtPosition().
						getPositionDimensionTwo();
						final String parentContainerName = parent.getName();

						StorageContainerUtil.addPostions(containerMap, id,
								parentContainerName,
								pos1, pos2);
					}
				}
			}
			if (storageContainerForm.getParentContainerId() != -1)
			{
				startingPoints[0] = new Long(storageContainerForm.getParentContainerId())
						.toString();
			}
			if (storageContainerForm.getPositionDimensionOne() != -1)
			{
				startingPoints[1] = new Integer(storageContainerForm.getPositionDimensionOne())
						.toString();
			}
			if (storageContainerForm.getPositionDimensionTwo() != -1)
			{
				startingPoints[2] = new Integer(storageContainerForm.getPositionDimensionTwo())
						.toString();
			}

			initialValues = new Vector();
			initialValues.add(startingPoints);
		}
		else if (Constants.SITE.equals(storageContainerForm.getParentContainerSelected()))
		{
			initialValues = StorageContainerUtil.checkForInitialValues(containerMap);
			// falguni
			// get container name by getting storage container object from db.
			if (storageContainerForm.getContainerName() == null)
			{
				final Object object = bizLogic.retrieve(StorageContainer.class.getName(),
						storageContainerForm.getId());
				if (object != null)
				{
					final StorageContainer cont = (StorageContainer) object;
					storageContainerForm.setContainerName(cont.getName());
				}
			}

		}
		request.setAttribute("initValues", initialValues);
	}

	/**
	 * @param request
	 *            : request
	 * @param storageContainerForm
	 *            : storageContainerForm
	 */
	private void setFormAttributesForAddNew(HttpServletRequest request,
			StorageContainerForm storageContainerForm)
	{
		// Mandar : code for Addnew Storage Type data 23-Jan-06
		final String storageTypeID = (String) request
				.getAttribute(Constants.ADD_NEW_STORAGE_TYPE_ID);
		if (storageTypeID != null && storageTypeID.trim().length() > 0)
		{
			this.logger.debug(">>>>>>>>>>><<<<<<<<<<<<<<<<>>>>>>>>>>>>> ST : " + storageTypeID);
			storageContainerForm.setTypeId(Long.parseLong(storageTypeID));
		}
		// -- 23-Jan-06 end
		// Mandar : code for Addnew Site data 24-Jan-06
		final String siteID = (String) request.getAttribute(Constants.ADD_NEW_SITE_ID);
		if (siteID != null && siteID.trim().length() > 0)
		{
			this.logger.debug(" ToSite ID in Distribution Action : " + siteID);
			storageContainerForm.setSiteId(Long.parseLong(siteID));
		}

	}

	/*
	 * private void onSiteOrParentContChange(HttpServletRequest
	 * request,HttpServletResponse response, StorageContainerForm
	 * storageContainerForm) throws DAOException, IOException { if
	 * (!Constants.SITE
	 * .equals(storageContainerForm.getParentContainerSelected())) { String[]
	 * startingPoints = new String[]{"-1", "-1", "-1"}; if
	 * (request.getParameter("parentContainerId") != null) { startingPoints[0] =
	 * request.getParameter("parentContainerId"); } if
	 * (request.getParameter("positionDimensionOne") != null) {
	 * startingPoints[1] = request.getParameter("positionDimensionOne"); } if
	 * (request.getParameter("positionDimensionTwo") != null) {
	 * startingPoints[2] = request.getParameter("positionDimensionTwo"); }
	 * Vector initialValues = new Vector(); initialValues.add(startingPoints);
	 * request.setAttribute("initValues", initialValues); } long[] collectionIds
	 * = parentContChange(request);
	 * storageContainerForm.setCollectionIds(collectionIds);
	 * sendCollectionIds(collectionIds,response);; }
	 */
	/**
	 * @param request
	 *            : request
	 * @return long[] : long[]
	 * @throws BizLogicException
	 *             : BizLogicException
	 */
	private long[] parentContChange(HttpServletRequest request) throws BizLogicException
	{
		final IFactory factory = AbstractFactoryConfig.getInstance().getBizLogicFactory();
		final StorageContainerBizLogic bizLogic = (StorageContainerBizLogic) factory
				.getBizLogic(Constants.STORAGE_CONTAINER_FORM_ID);

		final String parentContId = request.getParameter("parentContainerId");
		if (parentContId != null)
		{
			final Object object = bizLogic.retrieve(StorageContainer.class.getName(), new Long(
					parentContId));
			if (object != null)
			{
				final StorageContainer container = (StorageContainer) object;
				return bizLogic.getDefaultHoldCollectionProtocolList(container);
			}
		}
		return new long[]{-1};
	}

	/*
	 * private void sendCollectionIds(long[] collectionIds, HttpServletResponse
	 * response) throws IOException { PrintWriter out = response.getWriter();
	 * response.setContentType("text/html"); String collectionIdStr = ""; for
	 * (int i = 0; i < collectionIds.length - 1; i++) { long id =
	 * collectionIds[i]; collectionIdStr = collectionIdStr + new
	 * Long(id).toString() + "|"; } long id = collectionIds[collectionIds.length
	 * - 1]; collectionIdStr = collectionIdStr + new Long(id).toString();
	 * out.write(collectionIdStr); }
	 */
	/**
	 * @param storageContainerForm
	 *            : storageContainerForm
	 * @param storageContainerBean
	 *            : storageContainerBean
	 */

	private void initStorageContainerForm(StorageContainerForm storageContainerForm,
			StorageContainerBean storageContainerBean)
	{
		storageContainerForm.setBarcode(storageContainerBean.getBarcode());
		storageContainerForm.setCollectionIds(storageContainerBean.getCollectionIds());
		storageContainerForm.setContainerId(storageContainerBean.getContainerId());
		storageContainerForm.setContainerName(storageContainerBean.getContainerName());
		storageContainerForm.setDefaultTemperature(storageContainerBean.getDefaultTemperature());
		storageContainerForm.setHoldsSpecimenArrTypeIds(storageContainerBean
				.getHoldsSpecimenArrTypeIds());
		storageContainerForm.setHoldsSpecimenClassTypes(storageContainerBean
				.getHoldsSpecimenClassTypes());
		storageContainerForm.setHoldsStorageTypeIds(storageContainerBean.getHoldsStorageTypeIds());
		storageContainerForm.setTypeId(storageContainerBean.getTypeId());
		storageContainerForm.setTypeName(storageContainerBean.getTypeName());
		storageContainerForm.setId(storageContainerBean.getID());
		storageContainerForm.setParentContainerId(storageContainerBean.getParentContainerId());
		storageContainerForm.setPos1(storageContainerBean.getPos1());
		storageContainerForm.setPos2(storageContainerBean.getPos2());
		storageContainerForm
				.setPositionDimensionOne(storageContainerBean.getPositionDimensionOne());
		storageContainerForm
				.setPositionDimensionTwo(storageContainerBean.getPositionDimensionTwo());
		storageContainerForm.setContainerId(storageContainerBean.getContainerId());
		storageContainerForm.setContainerName(storageContainerBean.getContainerName());
		// storageContainerForm.setCheckedButton(storageContainerBean.
		// getCheckedButton());
		storageContainerForm.setHoldsSpecimenArrTypeIds(storageContainerBean
				.getHoldsSpecimenArrTypeIds());
		storageContainerForm.setHoldsSpecimenClassTypes(storageContainerBean
				.getHoldsSpecimenClassTypes());
		storageContainerForm.setHoldsStorageTypeIds(storageContainerBean.getHoldsStorageTypeIds());
		storageContainerForm
				.setOneDimensionCapacity(storageContainerBean.getOneDimensionCapacity());
		storageContainerForm
				.setTwoDimensionCapacity(storageContainerBean.getTwoDimensionCapacity());
		storageContainerForm.setOneDimensionLabel(storageContainerBean.getOneDimensionLabel());
		storageContainerForm.setTwoDimensionLabel(storageContainerBean.getTwoDimensionLabel());
		storageContainerForm.setSiteId(storageContainerBean.getSiteId());
		storageContainerForm.setSiteName(storageContainerBean.getSiteName());
		storageContainerForm.setSiteForParentContainer(storageContainerBean
				.getSiteForParentContainer());
		storageContainerForm.setParentContainerSelected(storageContainerBean
				.getParentContainerSelected());
		// 12064 S
		storageContainerForm.setActivityStatus(storageContainerBean.getActivityStatus());
		storageContainerForm.setIsFull(storageContainerBean.getIsFull());
		// 12064 E
	}

}