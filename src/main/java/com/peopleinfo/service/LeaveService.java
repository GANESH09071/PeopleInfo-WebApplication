package com.peopleinfo.service;

import com.peopleinfo.model.LeaveRequest;
import com.peopleinfo.model.User;
import com.peopleinfo.repository.LeaveRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LeaveService {

    private final LeaveRequestRepository leaveRepository;

    public LeaveRequest applyLeave(LeaveRequest leave) {
        return leaveRepository.save(leave);
    }

    public List<LeaveRequest> getAllLeaves() {
        return leaveRepository.findAllByOrderByAppliedOnDesc();
    }

    public List<LeaveRequest> getLeavesByEmployee(User employee) {
        return leaveRepository.findByEmployeeOrderByAppliedOnDesc(employee);
    }

    public List<LeaveRequest> getPendingLeaves() {
        return leaveRepository.findByStatus(LeaveRequest.LeaveStatus.PENDING);
    }

    public Optional<LeaveRequest> findById(Long id) {
        return leaveRepository.findById(id);
    }

    public LeaveRequest approveLeave(Long id, User approver, String comments) {
        LeaveRequest leave = leaveRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave not found"));
        leave.setStatus(LeaveRequest.LeaveStatus.APPROVED);
        leave.setApprovedBy(approver);
        leave.setHrComments(comments);
        return leaveRepository.save(leave);
    }

    public LeaveRequest rejectLeave(Long id, User approver, String comments) {
        LeaveRequest leave = leaveRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave not found"));
        leave.setStatus(LeaveRequest.LeaveStatus.REJECTED);
        leave.setApprovedBy(approver);
        leave.setHrComments(comments);
        return leaveRepository.save(leave);
    }

    public long countPendingLeaves() {
        return leaveRepository.countPendingLeaves();
    }

    public void deleteLeave(Long id) {
        leaveRepository.deleteById(id);
    }
}
