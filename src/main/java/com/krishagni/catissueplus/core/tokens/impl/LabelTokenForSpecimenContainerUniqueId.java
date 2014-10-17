
package com.krishagni.catissueplus.core.tokens.impl;

import org.springframework.context.ApplicationContext;

import com.krishagni.catissueplus.core.administrative.domain.StorageContainer;
import com.krishagni.catissueplus.core.common.OpenSpecimenAppCtxProvider;
import com.krishagni.catissueplus.core.common.util.KeyGenFactory;
import com.krishagni.catissueplus.core.tokens.LabelToken;

public class LabelTokenForSpecimenContainerUniqueId implements LabelToken<StorageContainer> {

	private static String CONTAINER_UNIQUE_ID = "CONTAINER_UNIQUE_ID";

	@Override
	public String getTokenValue(StorageContainer t) {
		ApplicationContext appCtx = OpenSpecimenAppCtxProvider.getAppCtx();
		KeyGenFactory keyFactory = (KeyGenFactory) appCtx.getBean("keyFactory");
		Long value = keyFactory.getValueByKey(CONTAINER_UNIQUE_ID, CONTAINER_UNIQUE_ID);
		return value.toString();
	}

}
