package com.littlehorizons.erp.dao.extracurricular;

import com.littlehorizons.erp.model.extracurricular.Club;
import com.littlehorizons.erp.model.extracurricular.ClubMembership;

import java.util.List;
import java.util.Optional;

public interface ClubRepository {
    Integer save(Club club);
    List<Club> findAll();
    Optional<Club> findById(int id);
    boolean enrollStudent(int studentId, int clubId);
    boolean withdrawStudent(int membershipId, String reason);
    int getActiveMembersCount(int clubId);
    List<ClubMembership> findMembershipsByClub(int clubId);
}
