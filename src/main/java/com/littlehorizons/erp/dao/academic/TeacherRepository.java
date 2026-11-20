package com.littlehorizons.erp.dao.academic;

import com.littlehorizons.erp.model.academic.Teacher;

import java.util.List;
import java.util.Optional;

public interface TeacherRepository {
    Integer save(Teacher teacher);
    Optional<Teacher> findById(int id);
    List<Teacher> findAll();
    boolean assignToSubject(int teacherId, int subjectId, int year, String section);
}
