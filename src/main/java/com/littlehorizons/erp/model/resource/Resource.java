package com.littlehorizons.erp.model.resource;

import java.time.LocalDate;

public class Resource {

    private Integer idResource;
    private String resourceCode;
    private String resourceName;
    private Integer idCategory;
    private String description;
    private LocalDate acquisitionDate;
    private String physicalLocation;
    private String status; 

    private String categoryName;

    public Resource() {
    }

    public Resource(Integer idResource, String resourceCode, String resourceName,
                    Integer idCategory, String description, LocalDate acquisitionDate,
                    String physicalLocation, String status) {
        this.idResource = idResource;
        this.resourceCode = resourceCode;
        this.resourceName = resourceName;
        this.idCategory = idCategory;
        this.description = description;
        this.acquisitionDate = acquisitionDate;
        this.physicalLocation = physicalLocation;
        this.status = status;
    }

    public Integer getIdResource() {
        return idResource;
    }

    public void setIdResource(Integer idResource) {
        this.idResource = idResource;
    }

    public String getResourceCode() {
        return resourceCode;
    }

    public void setResourceCode(String resourceCode) {
        this.resourceCode = resourceCode;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public Integer getIdCategory() {
        return idCategory;
    }

    public void setIdCategory(Integer idCategory) {
        this.idCategory = idCategory;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getAcquisitionDate() {
        return acquisitionDate;
    }

    public void setAcquisitionDate(LocalDate acquisitionDate) {
        this.acquisitionDate = acquisitionDate;
    }

    public String getPhysicalLocation() {
        return physicalLocation;
    }

    public void setPhysicalLocation(String physicalLocation) {
        this.physicalLocation = physicalLocation;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    @Override
    public String toString() {
        return String.format("[%d] %s (%s) - Ubicación: %s - Estado: %s",
                idResource, resourceName, resourceCode, physicalLocation, status);
    }
}
