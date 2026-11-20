package com.littlehorizons.erp.dao.academic;

import com.littlehorizons.erp.model.academic.Guardian;

import java.util.List;
import java.util.Optional;

public interface GuardianRepository {
    Integer save(Guardian guardian);
    Optional<Guardian> findById(int id);
    Optional<Guardian> findByDocument(String documentNumber);
    List<Guardian> findAll();
}
