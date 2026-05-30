package com.peopleinfo.service;

import com.peopleinfo.model.LeaveRequest;
import com.peopleinfo.model.User;
import com.peopleinfo.repository.LeaveRequestRepository;
import com.peopleinfo.repository.NotificationRepository;
import com.peopleinfo.model.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class LeaveService {

    private final LeaveRequestRepository leaveRepository;
    private final NotificationRepository notificationRepository;
    private final NlpUrgencyService nlpUrgencyService;

    public LeaveRequest applyLeave(LeaveRequest leave) {
        if (leave.getReason() != null) {
            boolean urgent = nlpUrgencyService.isUrgent(leave.getReason());
            leave.setUrgent(urgent);
        }
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
        LeaveRequest saved = leaveRepository.save(leave);

        notificationRepository.save(Notification.builder()
                .employee(leave.getEmployee())
                .message("Your " + leave.getLeaveType() + " leave from " + leave.getStartDate() + " to " + leave.getEndDate() + " has been APPROVED.")
                .build());

        return saved;
    }

    public LeaveRequest rejectLeave(Long id, User approver, String comments) {
        LeaveRequest leave = leaveRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave not found"));
        leave.setStatus(LeaveRequest.LeaveStatus.REJECTED);
        leave.setApprovedBy(approver);
        leave.setHrComments(comments);
        LeaveRequest saved = leaveRepository.save(leave);

        notificationRepository.save(Notification.builder()
                .employee(leave.getEmployee())
                .message("Your " + leave.getLeaveType() + " leave from " + leave.getStartDate() + " to " + leave.getEndDate() + " has been REJECTED. Reason: " + comments)
                .build());

        return saved;
    }

    public long countPendingLeaves() {
        return leaveRepository.countPendingLeaves();
    }

    public void deleteLeave(Long id) {
        leaveRepository.deleteById(id);
    }

    private int getQuartersPassedSince(LocalDate startDate) {
        if (startDate == null) return 0;
        LocalDate now = LocalDate.now();
        int years = now.getYear() - startDate.getYear();
        int startQuarter = (startDate.getMonthValue() - 1) / 3 + 1;
        int currentQuarter = (now.getMonthValue() - 1) / 3 + 1;
        
        int totalQuarters = (years * 4) - startQuarter + currentQuarter + 1;
        return Math.max(0, totalQuarters);
    }

    public double getAvailableSickLeave(User employee) {
        LocalDate doj = employee.getDateOfJoining() != null ? employee.getDateOfJoining() : LocalDate.of(LocalDate.now().getYear(), 1, 1);
        int quarters = getQuartersPassedSince(doj);
        double accrued = quarters * 2.0;
        
        long taken = leaveRepository.findByEmployeeAndStatus(employee, LeaveRequest.LeaveStatus.APPROVED)
                .stream().filter(l -> l.getLeaveType() == LeaveRequest.LeaveType.SICK)
                .mapToLong(LeaveRequest::getTotalDays).sum();
        
        return Math.max(0, accrued - taken);
    }

    public double getAvailableEarnedLeave(User employee) {
        LocalDate doj = employee.getDateOfJoining() != null ? employee.getDateOfJoining() : LocalDate.of(LocalDate.now().getYear(), 1, 1);
        int quarters = getQuartersPassedSince(doj);
        double accrued = quarters * 2.0;
        
        long taken = leaveRepository.findByEmployeeAndStatus(employee, LeaveRequest.LeaveStatus.APPROVED)
                .stream().filter(l -> l.getLeaveType() == LeaveRequest.LeaveType.EARNED)
                .mapToLong(LeaveRequest::getTotalDays).sum();
        
        return Math.max(0, accrued - taken);
    }

    public double getAvailableCasualLeave(User employee) {
        int currentQuarter = (LocalDate.now().getMonthValue() - 1) / 3 + 1;
        double accrued = currentQuarter * 1.5;
        
        long taken = leaveRepository.findApprovedLeavesByUserAndTypeAndYear(employee, LeaveRequest.LeaveType.CASUAL, LocalDate.now().getYear())
                .stream().mapToLong(LeaveRequest::getTotalDays).sum();
        
        return Math.max(0, accrued - taken);
    }

    public double getAvailableFlexiLeave(User employee) {
        double accrued = 2.0; // 2 per year
        
        long taken = leaveRepository.findApprovedLeavesByUserAndTypeAndYear(employee, LeaveRequest.LeaveType.FLEXI, LocalDate.now().getYear())
                .stream().mapToLong(LeaveRequest::getTotalDays).sum();
        
        return Math.max(0, accrued - taken);
    }
}
