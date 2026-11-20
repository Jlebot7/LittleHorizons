package com.littlehorizons.erp.dao.academic;

import com.littlehorizons.erp.model.academic.Attendance;

import java.util.List;

public interface AttendanceRepository {
    boolean recordAttendance(Attendance attendance);
    List<Attendance> findByStudent(int studentId);
    List<Attendance> findAllRecent();
}
