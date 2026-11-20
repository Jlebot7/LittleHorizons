package com.littlehorizons.erp.dao.academic;

import com.littlehorizons.erp.model.academic.AcademicRecord;

import java.util.List;

public interface AcademicRecordRepository {
    boolean saveOrUpdate(AcademicRecord record);
    List<AcademicRecord> findByStudent(int studentId);
    List<AcademicRecord> findBySubjectAndPeriod(int subjectId, int year, String period);
}
