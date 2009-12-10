package edu.wustl.catissuecore.testcase.bizlogic;

import java.util.Iterator;
import java.util.List;

import edu.wustl.catissuecore.domain.Capacity;
import edu.wustl.catissuecore.domain.StorageType;
import edu.wustl.catissuecore.testcase.CaTissueSuiteBaseTest;
import edu.wustl.common.beans.SessionDataBean;
import edu.wustl.common.domain.AbstractDomainObject;
import edu.wustl.common.util.logger.Logger;


public class StorageTypeBizTestCases extends CaTissueSuiteBaseTest {
	
		AbstractDomainObject domainObject = null;
		public void testAddStorageType()
		{
			try{
				SessionDataBean bean = (SessionDataBean)getSession().getAttribute("sessionData");
				StorageType storagetype = BaseTestCaseUtility.initStorageType();			
				System.out.println(storagetype);
				
				storagetype = (StorageType) appService.createObject(storagetype,bean);
				TestCaseUtility.setObjectMap(storagetype, StorageType.class);
				System.out.println("Object created successfully");
				assertTrue("Object added successfully", true);
			 }
			 catch(Exception e){
				 e.printStackTrace();
				 assertFalse("could not add object", true);
			 }
		}
		
		public void testSearchStorageType()
		{
			StorageType storagetype = new StorageType();
	    	Logger.out.info(" searching domain object");
	    	storagetype.setId(new Long(1));
	   
	         try {
	        	 SessionDataBean bean = (SessionDataBean)getSession().getAttribute("sessionData");
	        	 String query = "from edu.wustl.catissuecore.domain.StorageType as storagetype where "
	 				+ "storagetype.id= 1";	
	        	 List resultList = appService.search(query);
	        	 for (Iterator resultsIterator = resultList.iterator(); resultsIterator.hasNext();) {
	        		 StorageType returnedSite = (StorageType) resultsIterator.next();
	        		 Logger.out.info(" Domain Object is successfully Found ---->  :: " + returnedSite.getName());
	        		// System.out.println(" Domain Object is successfully Found ---->  :: " + returnedDepartment.getName());
	             }
	          } 
	          catch (Exception e) {
	           	Logger.out.error(e.getMessage(),e);
	           	e.printStackTrace();
	           	assertFalse("Does not find Domain Object", true);
		 		
	          }
		}
		
		public void testUpdateStorageType()
		{
			StorageType storagetype =  BaseTestCaseUtility.initStorageType();
	    	Logger.out.info("updating domain object------->"+storagetype);
		    try 
			{
		    	SessionDataBean bean = (SessionDataBean)getSession().getAttribute("sessionData");
		    	storagetype = (StorageType) appService.createObject(storagetype,bean);
		    	BaseTestCaseUtility.updateStorageType(storagetype);	
		    	StorageType updatedStorageType = (StorageType) appService.updateObject(storagetype,bean);
		       	Logger.out.info("Domain object successfully updated ---->"+updatedStorageType);
		       	assertTrue("Domain object successfully updated ---->"+updatedStorageType, true);
		    } 
		    catch (Exception e) {
		       	Logger.out.error(e.getMessage(),e);
		 		e.printStackTrace();
		 		assertFalse("failed to update Object", true);
		    }
		}
		
		
		
		public void testWithEmptyStorageTypeName()
		{
			try{
				SessionDataBean bean = (SessionDataBean)getSession().getAttribute("sessionData");
				StorageType storagetype = BaseTestCaseUtility.initStorageType();		
				//te.setId(new Long("1"));
				storagetype.setName("");
				System.out.println(storagetype);
				storagetype = (StorageType) appService.createObject(storagetype,bean); 
				assertFalse("Empty storagetype name should thorw Exception", true);
			 }
			 catch(Exception e){
				 Logger.out.error(e.getMessage(),e);
				 e.printStackTrace();
				 assertTrue("Name is required", true);
			 }
		}
		
		public void testWithDuplicateStorageTypeName()
		{
			try{
				SessionDataBean bean = (SessionDataBean)getSession().getAttribute("sessionData");
				StorageType storagetype = BaseTestCaseUtility.initStorageType();	
				StorageType dupStorageTypeName = BaseTestCaseUtility.initStorageType();
				dupStorageTypeName.setName(storagetype.getName());
				storagetype = (StorageType) appService.createObject(storagetype,bean); 
				dupStorageTypeName = (StorageType) appService.createObject(dupStorageTypeName,bean); 
				assertFalse("Test Failed. Duplicate storagetype name should throw exception", true);
			}
			 catch(Exception e){
				Logger.out.error(e.getMessage(),e);
				e.printStackTrace();
				assertTrue("Submission failed since a storagetype with the same NAME already exists" , true);
				 
			 }
		}
		
		public void testWithNegativeDimensionCapacity_StorageType()
		{
			try{
				SessionDataBean bean = (SessionDataBean)getSession().getAttribute("sessionData");
				StorageType storagetype = BaseTestCaseUtility.initStorageType();		
				//te.setId(new Long("1"));
				Capacity capacity = new Capacity();
				capacity.setOneDimensionCapacity(new Integer(-1));
				capacity.setTwoDimensionCapacity(new Integer(-1));
				storagetype.setCapacity(capacity);		
				
				System.out.println(storagetype);
				storagetype = (StorageType) appService.createObject(storagetype,bean); 
				assertFalse("Negative Dimension capacity should thorw Exception", true);
			 }
			 catch(Exception e){
				 Logger.out.error(e.getMessage(),e);
				 e.printStackTrace();
				 assertTrue("Name is required", true);
			 }
		}
		
		public void testWithEmptyDimensionLabel_StorageType()
		{
			try{
				SessionDataBean bean = (SessionDataBean)getSession().getAttribute("sessionData");
				StorageType storagetype = BaseTestCaseUtility.initStorageType();	
				storagetype.setOneDimensionLabel("");
				storagetype.setTwoDimensionLabel("");
				System.out.println(storagetype);
				storagetype = (StorageType) appService.createObject(storagetype,bean); 
				assertFalse("Empty Text Label should thorw Exception", true);
			 }
			 catch(Exception e){
				 Logger.out.error(e.getMessage(),e);
				 e.printStackTrace();
				 assertTrue("Text Label For Dimension One is required", true);
			 }
		}
		
		/*public void testNullDomainObjectInInsert()
		{
			domainObject = new StorageType(); 
			testNullDomainObjectInInsert(domainObject);
		}*/
		
//		public void testNullSessionDataBeanInInsert_StorageType()
//		{
//			domainObject = new StorageType();
//			testNullSessionDataBeanInInsert(domainObject);
//		}
//			
//		public void testWrongDaoTypeInInsert_StorageType()
//		{
//			domainObject = new StorageType();
//			testWrongDaoTypeInInsert(domainObject);
//		}
//		public void testNullSessionDataBeanInUpdate_StorageType()
//		{
//			domainObject = new StorageType();
//			testNullSessionDataBeanInUpdate(domainObject);
//		}
//		
//		public void testNullOldDomainObjectInUpdate_StorageType()
//		{
//			domainObject = new StorageType();
//			testNullOldDomainObjectInUpdate(domainObject);
//		}
//		
//			
//		/*public void testNullCurrentDomainObjectInUpdate()
//		{
//			domainObject = new StorageType();
//			testNullCurrentDomainObjectInUpdate(domainObject);
//		}*/
//		
//		public void testEmptyCurrentDomainObjectInUpdate_StorageType()
//		{
//			domainObject = new StorageType();
//			AbstractDomainObject initialisedDomainObject = BaseTestCaseUtility.initDistributionProtocol();
//			testEmptyCurrentDomainObjectInUpdate(domainObject, initialisedDomainObject);
//		}
//		
//		public void testEmptyOldDomainObjectInUpdate_StorageType()
//		{
//			domainObject = new StorageType();
//			AbstractDomainObject initialisedDomainObject = BaseTestCaseUtility.initDistributionProtocol();
//			testEmptyOldDomainObjectInUpdate(domainObject,initialisedDomainObject);
//		}
//		
//		public void testNullDomainObjectInRetrieve_StorageType()
//		{
//			domainObject = new StorageType();
//			testNullCurrentDomainObjectInRetrieve(domainObject);
//		}
}
