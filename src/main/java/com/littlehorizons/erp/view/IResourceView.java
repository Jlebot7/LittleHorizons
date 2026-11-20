package com.littlehorizons.erp.view;

import com.littlehorizons.erp.model.resource.Resource;
import com.littlehorizons.erp.model.resource.ResourceCategory;
import com.littlehorizons.erp.model.resource.StudentLoan;
import com.littlehorizons.erp.model.resource.TeacherLoan;

import java.util.List;

public interface IResourceView {
    void showMenu();
    void showResources(List<Resource> resources);
    void showRegisterResourceHeader();
    void showCategories(List<ResourceCategory> categories);
    void showLoanToStudentHeader();
    void showReturnStudentLoanHeader();
    void showLoanToTeacherHeader();
    void showReturnTeacherLoanHeader();
    void showOverdueLoansReport(List<StudentLoan> studentLoans, List<TeacherLoan> teacherLoans);
    void showSuccess(String message);
    void showError(String message);
    void showWarning(String message);
}
