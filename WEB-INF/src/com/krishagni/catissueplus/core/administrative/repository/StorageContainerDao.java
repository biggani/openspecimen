
package com.krishagni.catissueplus.core.administrative.repository;

import java.util.List;

import com.krishagni.catissueplus.core.administrative.domain.StorageContainer;
import com.krishagni.catissueplus.core.administrative.domain.StorageContainerPosition;
import com.krishagni.catissueplus.core.common.repository.Dao;

public interface StorageContainerDao extends Dao<StorageContainer> {
	public List<StorageContainer> getStorageContainers(StorageContainerListCriteria listCrit);

	public StorageContainer getByName(String name);
	
	public StorageContainer getByBarcode(String barcode);

	public void delete(StorageContainerPosition position);
	
	public List<Long> getContainerIdsBySiteIds(List<Long> siteIds);
}
	