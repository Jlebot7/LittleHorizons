package com.littlehorizons.erp.model.resource;

/**
 * Domain entity representing a Resource Category (Book, Lab, Sports, Tech, Didactic).
 */
public class ResourceCategory {

    private Integer idCategory;
    private String categoryName;
    private String description;

    public ResourceCategory() {
    }

    public ResourceCategory(Integer idCategory, String categoryName, String description) {
        this.idCategory = idCategory;
        this.categoryName = categoryName;
        this.description = description;
    }

    public Integer getIdCategory() {
        return idCategory;
    }

    public void setIdCategory(Integer idCategory) {
        this.idCategory = idCategory;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return String.format("[%d] %s", idCategory, categoryName);
    }
}
