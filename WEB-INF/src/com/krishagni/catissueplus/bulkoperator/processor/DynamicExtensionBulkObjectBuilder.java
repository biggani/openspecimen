/*L
 *  Copyright Washington University in St. Louis
 *  Copyright SemanticBits
 *  Copyright Persistent Systems
 *  Copyright Krishagni
 *
 *  Distributed under the OSI-approved BSD 3-Clause License.
 *  See http://ncip.github.com/catissue-migration-tool/LICENSE.txt for details.
 */

package com.krishagni.catissueplus.bulkoperator.processor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.krishagni.catissueplus.bulkoperator.csv.CsvReader;
import com.krishagni.catissueplus.bulkoperator.events.DERecordInformation;
import com.krishagni.catissueplus.bulkoperator.metadata.Attribute;
import com.krishagni.catissueplus.bulkoperator.metadata.BulkOperationClass;
import com.krishagni.catissueplus.bulkoperator.metadata.HookingInformation;
import com.krishagni.catissueplus.bulkoperator.util.BulkOperationException;
import com.krishagni.catissueplus.bulkoperator.util.BulkOperationUtility;

import edu.wustl.common.exception.ErrorKey;
import edu.wustl.common.util.global.Validator;
import edu.wustl.common.util.logger.Logger;

public class DynamicExtensionBulkObjectBuilder extends AbstractBulkObjectBuilder implements BulkObjectBuilder
{
	private static final Logger logger = Logger.getCommonLogger(DynamicExtensionBulkObjectBuilder.class);

	public DynamicExtensionBulkObjectBuilder(BulkOperationClass dynExtEntityBOClass)
	{
		super(dynExtEntityBOClass);
	}

	public Object process(CsvReader csvReader, int csvRowCounter)
			throws Exception
	{
		try
		{
			HashMap<String, Object> dynExtObject = new HashMap<String, Object>();
			processObject(dynExtObject, bulkOperationClass, csvReader, "", false, csvRowCounter);
			
			HookingInformation hookingInformationFromTag = bulkOperationClass.getHookingInformation();
			getinformationForHookingData(csvReader ,hookingInformationFromTag);
			String entityClassName = null;
			entityClassName = bulkOperationClass.getClassName();
		
			hookingInformationFromTag.setEntityGroupName(bulkOperationClass.getClassName());
			hookingInformationFromTag.setEntityName(entityClassName);
			
			return new DERecordInformation(entityClassName, dynExtObject, hookingInformationFromTag);
		}
		catch (Exception exp)
		{
			logger.error(exp.getMessage(), exp);
			throw exp;
		}
	}

	@Override
	Object processObject(Map<String, String> csvData) throws BulkOperationException
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 *
	 * @param mainObj
	 * @param bulkOperationClass
	 * @param columnSuffix
	 * @param validate
	 * @throws BulkOperationException
	 */
	protected void processContainments(Object mainObj, BulkOperationClass bulkOperationClass,
			CsvReader csvReader, String columnSuffix, boolean validate, int csvRowNumber)
			throws BulkOperationException
	{
		try
		{
			Map<String, Object> categoryDataValueMap = (Map<String, Object>) mainObj;

			Iterator<BulkOperationClass> containmentItert = bulkOperationClass
					.getContainmentAssociationCollection().iterator();
			while (containmentItert.hasNext())
			{
				BulkOperationClass containmentObjectCollection = containmentItert.next();
				if (containmentObjectCollection.getCardinality() != null)
				{
					List<Map<Long, Object>> list = new ArrayList<Map<Long, Object>>();

//					int maxNoOfRecords = containmentObjectCollection.getMaxNoOfRecords().intValue();
					for (int i = 1; ; i++) {
						if (!BulkOperationUtility.checkIfAtLeastOneColumnHasAValueForInnerContainment(csvRowNumber,containmentObjectCollection,
										columnSuffix + "#" + i,csvReader)) {
                            break;
                        } else {
                            Object obj = new HashMap<Long, Object>();
							processObject(obj, containmentObjectCollection, csvReader, columnSuffix
                                    + "#" + i, validate, csvRowNumber);
                            list.add((Map<Long, Object>) obj);
                        }
                    	categoryDataValueMap.put(containmentObjectCollection.getClassName(), list);
					}
				}
			}
		}
		catch (BulkOperationException bulkExp)
		{
			logger.error(bulkExp.getMessage(), bulkExp);
			throw new BulkOperationException(bulkExp.getErrorKey(), bulkExp, bulkExp.getMsgValues());
		}
		catch (Exception exp)
		{
			logger.error(exp.getMessage(), exp);
			ErrorKey errorkey = ErrorKey.getErrorKey("bulk.operation.issues");
			throw new BulkOperationException(errorkey, exp, exp.getMessage());
		}
	}

	protected void setValueToObject(Object mainObj,
			BulkOperationClass mainMigrationClass,CsvReader csvReader,
			String columnSuffix, boolean validate, Attribute attribute) throws BulkOperationException {
		
		String csvDataValue=null;
		if(csvReader.getColumn(attribute.getCsvColumnName()+ columnSuffix)!=null)
		{
			csvDataValue=csvReader.getColumn(attribute.getCsvColumnName()+ columnSuffix).trim();
		}
		if(Validator.isEmpty(csvDataValue) && attribute.getDefaultValue()!=null)
		{
			csvDataValue=attribute.getDefaultValue();
		}
		
		if (!Validator.isEmpty(csvDataValue)) {
			Map<String, Object> categoryDataValueMap = (Map<String, Object>) mainObj;

			if (csvDataValue == null || "".equals(csvDataValue)) {
				categoryDataValueMap.put(attribute.getName(), "");
			} else {
				categoryDataValueMap.put(attribute.getName(), csvDataValue);
			}
		}
	}
}