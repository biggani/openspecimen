
package com.krishagni.catissueplus.core.biospecimen.events;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import com.krishagni.catissueplus.core.biospecimen.domain.Specimen;
import com.krishagni.catissueplus.core.biospecimen.domain.SpecimenPosition;
import com.krishagni.catissueplus.core.biospecimen.domain.SpecimenRequirement;

public class SpecimenInfo implements Comparable<SpecimenInfo> {
	public static class StorageLocationSummary {
		public Long id;
		
		public String name;
		
		public String positionX;
		
		public String positionY;
	}
	
	private Long id;
	
	private Long cprId;
	
	private Long eventId;
	
	private Long visitId;
	
	private Long reqId;
	
	private String label;
	
	private String barcode;

	private String type;
	
	private String specimenClass;
		
	private String lineage;

	private String anatomicSite;

	private String laterality;
	
	private String status;
	
	private String reqLabel;
	
	private String pathology;
	
	private Double initialQty;
	
	private Double availableQty;
	
	private Boolean available;
	
	private Long parentId;
	
	private String parentLabel;
	
	private StorageLocationSummary storageLocation;
	
	private String storageType;
	
	private String activityStatus;
	
	private Date createdOn;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getCprId() {
		return cprId;
	}

	public void setCprId(Long cprId) {
		this.cprId = cprId;
	}

	public Long getEventId() {
		return eventId;
	}

	public void setEventId(Long eventId) {
		this.eventId = eventId;
	}

	public Long getVisitId() {
		return visitId;
	}

	public void setVisitId(Long visitId) {
		this.visitId = visitId;
	}

	public Long getReqId() {
		return reqId;
	}

	public void setReqId(Long reqId) {
		this.reqId = reqId;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getBarcode() {
		return barcode;
	}

	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getSpecimenClass() {
		return specimenClass;
	}

	public void setSpecimenClass(String specimenClass) {
		this.specimenClass = specimenClass;
	}

	public String getLineage() {
		return lineage;
	}

	public void setLineage(String lineage) {
		this.lineage = lineage;
	}

	public String getAnatomicSite() {
		return anatomicSite;
	}

	public void setAnatomicSite(String anatomicSite) {
		this.anatomicSite = anatomicSite;
	}

	public String getLaterality() {
		return laterality;
	}

	public void setLaterality(String laterality) {
		this.laterality = laterality;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getReqLabel() {
		return reqLabel;
	}

	public void setReqLabel(String reqLabel) {
		this.reqLabel = reqLabel;
	}

	public String getPathology() {
		return pathology;
	}

	public void setPathology(String pathology) {
		this.pathology = pathology;
	}

	public Double getInitialQty() {
		return initialQty;
	}

	public void setInitialQty(Double initialQty) {
		this.initialQty = initialQty;
	}

	public Double getAvailableQty() {
		return availableQty;
	}

	public void setAvailableQty(Double availableQty) {
		this.availableQty = availableQty;
	}

	public Boolean getAvailable() {
		return available;
	}

	public void setAvailable(Boolean available) {
		this.available = available;
	}

	public Long getParentId() {
		return parentId;
	}

	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}

	public String getParentLabel() {
		return parentLabel;
	}

	public void setParentLabel(String parentLabel) {
		this.parentLabel = parentLabel;
	}

	public StorageLocationSummary getStorageLocation() {
		return storageLocation;
	}

	public void setStorageLocation(StorageLocationSummary storageLocation) {
		this.storageLocation = storageLocation;
	}
	
	public String getStorageType() {
		return storageType;
	}

	public void setStorageType(String storageType) {
		this.storageType = storageType;
	}

	public String getActivityStatus() {
		return activityStatus;
	}

	public void setActivityStatus(String activityStatus) {
		this.activityStatus = activityStatus;
	}

	public Date getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

	public static void fromTo(Specimen specimen, SpecimenInfo result) {
		result.setId(specimen.getId());
		
		SpecimenRequirement sr= specimen.getSpecimenRequirement();
		result.setReqId(sr != null ? sr.getId() : null);
		result.setReqLabel(sr != null ? sr.getName() : null);
		result.setLabel(specimen.getLabel());
		result.setBarcode(specimen.getBarcode());
		result.setType(specimen.getSpecimenType());
		result.setSpecimenClass(specimen.getSpecimenClass());
		result.setLineage(specimen.getLineage());
		result.setAnatomicSite(specimen.getTissueSite());
		result.setLaterality(specimen.getTissueSide());
		result.setStatus(specimen.getCollectionStatus());
		result.setPathology(specimen.getPathologicalStatus());
		result.setInitialQty(specimen.getInitialQuantity());
		result.setAvailableQty(specimen.getAvailableQuantity());
		result.setAvailable(specimen.getIsAvailable());
		if (specimen.getParentSpecimen() != null) {
			result.setParentId(specimen.getParentSpecimen().getId());
			result.setParentLabel(specimen.getParentSpecimen().getLabel());
		}
	
		StorageLocationSummary location = new StorageLocationSummary();
		SpecimenPosition position = specimen.getSpecimenPosition();
		if (position == null) {
			location.id = -1L;
		} else {
			location.id = position.getStorageContainer().getId();
			location.name = position.getStorageContainer().getName();
			location.positionX = position.getPositionDimensionOneString();
			location.positionY = position.getPositionDimensionTwoString();
		}
		result.setStorageLocation(location);		
		result.setActivityStatus(specimen.getActivityStatus());
		result.setCreatedOn(specimen.getCreatedOn());
		result.setStorageType(sr != null ? sr.getStorageType() : null);
	}	
	
	public static void fromTo(SpecimenRequirement anticipated, SpecimenInfo result) {
		result.setId(null);	
		result.setReqId(anticipated.getId());
		result.setReqLabel(anticipated.getName());
		result.setBarcode(null);
		result.setType(anticipated.getSpecimenType());
		result.setSpecimenClass(anticipated.getSpecimenClass());
		result.setLineage(anticipated.getLineage());
		result.setAnatomicSite(anticipated.getAnatomicSite());
		result.setLaterality(anticipated.getLaterality());
		result.setPathology(anticipated.getPathologyStatus());
		result.setInitialQty(anticipated.getInitialQuantity());
		result.setParentId(null);
	
		StorageLocationSummary location = new StorageLocationSummary();
		result.setStorageLocation(location);
		result.setStorageType(anticipated.getStorageType());
	}	
	
	public static void sort(List<SpecimenDetail> specimens) {
		Collections.sort(specimens, new Comparator<SpecimenDetail>() {
			@Override
			public int compare(SpecimenDetail specimen1, SpecimenDetail specimen2) {
				return specimen1.getType().compareTo(specimen2.getType());
			}
		});
		
		for (SpecimenDetail specimen : specimens) {
			if (specimen.getChildren() != null) {
				sort(specimen.getChildren());
			}
		}
	}

	@Override
	public int compareTo(SpecimenInfo other) {
		return getType().compareTo(other.getType());
	}	
}
