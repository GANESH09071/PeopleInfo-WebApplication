package com.peopleinfo.repository;

import com.peopleinfo.model.HiringRequirement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HiringRequirementRepository extends JpaRepository<HiringRequirement, Long> {

    List<HiringRequirement> findByStatus(HiringRequirement.HiringStatus status);

    List<HiringRequirement> findAllByOrderByCreatedAtDesc();

    List<HiringRequirement> findByDepartment(String department);

    long countByStatus(HiringRequirement.HiringStatus status);
}
