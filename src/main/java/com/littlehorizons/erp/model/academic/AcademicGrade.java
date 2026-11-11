package com.littlehorizons.erp.model.academic;

/**
 * Domain entity representing an Academic Grade Level (e.g., Preescolar, Primero, ..., Once).
 */
public class AcademicGrade {

    private Integer idGrade;
    private String gradeName;
    private Integer numericLevel;

    public AcademicGrade() {
    }

    public AcademicGrade(Integer idGrade, String gradeName, Integer numericLevel) {
        this.idGrade = idGrade;
        this.gradeName = gradeName;
        this.numericLevel = numericLevel;
    }

    public Integer getIdGrade() {
        return idGrade;
    }

    public void setIdGrade(Integer idGrade) {
        this.idGrade = idGrade;
    }

    public String getGradeName() {
        return gradeName;
    }

    public void setGradeName(String gradeName) {
        this.gradeName = gradeName;
    }

    public Integer getNumericLevel() {
        return numericLevel;
    }

    public void setNumericLevel(Integer numericLevel) {
        this.numericLevel = numericLevel;
    }

    @Override
    public String toString() {
        return String.format("[%d] %s (Nivel: %d)", idGrade, gradeName, numericLevel);
    }
}
