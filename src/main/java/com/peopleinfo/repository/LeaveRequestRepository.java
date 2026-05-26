package com.peopleinfo.repository;

import com.peopleinfo.model.LeaveRequest;
import com.peopleinfo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByEmployee(User employee);

    List<LeaveRequest> findByStatus(LeaveRequest.LeaveStatus status);

    List<LeaveRequest> findByEmployeeOrderByAppliedOnDesc(User employee);

    List<LeaveRequest> findAllByOrderByAppliedOnDesc();

    @Query("SELECT COUNT(l) FROM LeaveRequest l WHERE l.status = 'PENDING'")
    long countPendingLeaves();

    @Query("SELECT COUNT(l) FROM LeaveRequest l WHERE l.employee = ?1 AND l.status = 'APPROVED'")
    long countApprovedLeavesByEmployee(User employee);

    List<LeaveRequest> findByEmployeeAndStatus(User employee, LeaveRequest.LeaveStatus status);
}
