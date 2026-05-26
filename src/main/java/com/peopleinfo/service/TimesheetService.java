package com.peopleinfo.service;

import com.peopleinfo.model.Timesheet;
import com.peopleinfo.model.User;
import com.peopleinfo.repository.TimesheetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TimesheetService {

    private final TimesheetRepository timesheetRepository;

    public Timesheet saveTimesheet(Timesheet timesheet) {
        return timesheetRepository.save(timesheet);
    }

    public List<Timesheet> getAllTimesheets() {
        return timesheetRepository.findAllByOrderByDateDesc();
    }

    public List<Timesheet> getTimesheetsByEmployee(User employee) {
        return timesheetRepository.findByEmployeeOrderByDateDesc(employee);
    }

    public Optional<Timesheet> findById(Long id) {
        return timesheetRepository.findById(id);
    }

    public Optional<Timesheet> findByEmployeeAndDate(User employee, LocalDate date) {
        return timesheetRepository.findByEmployeeAndDate(employee, date);
    }

    public Timesheet approveTimesheet(Long id) {
        Timesheet ts = timesheetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Timesheet not found"));
        ts.setStatus(Timesheet.TimesheetStatus.APPROVED);
        return timesheetRepository.save(ts);
    }

    public Timesheet rejectTimesheet(Long id) {
        Timesheet ts = timesheetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Timesheet not found"));
        ts.setStatus(Timesheet.TimesheetStatus.REJECTED);
        return timesheetRepository.save(ts);
    }

    public Double getTotalHoursThisMonth(User employee) {
        LocalDate start = LocalDate.now().withDayOfMonth(1);
        LocalDate end = LocalDate.now();
        Double hours = timesheetRepository.sumHoursByEmployeeAndDateRange(employee, start, end);
        return hours != null ? hours : 0.0;
    }

    public long countPendingTimesheets() {
        return timesheetRepository.countPendingTimesheets();
    }

    public void deleteTimesheet(Long id) {
        timesheetRepository.deleteById(id);
    }

    public List<Timesheet> getTimesheetsByDateRange(LocalDate start, LocalDate end) {
        return timesheetRepository.findByDateBetweenOrderByDateDesc(start, end);
    }

    public List<Timesheet> searchTimesheets(String search) {
        return timesheetRepository.searchByEmployeeNameOrId(search);
    }

    public Timesheet checkIn(User employee) {
        LocalDate today = LocalDate.now();
        Timesheet ts = timesheetRepository.findByEmployeeAndDate(employee, today)
                .orElse(Timesheet.builder()
                        .employee(employee)
                        .date(today)
                        .status(Timesheet.TimesheetStatus.SUBMITTED)
                        .accumulatedSeconds(0L)
                        .isTracking(false)
                        .build());
        
        if (!ts.getIsTracking()) {
            if (ts.getCheckIn() == null) {
                ts.setCheckIn(java.time.LocalTime.now());
            }
            ts.setIsTracking(true);
            ts.setLastCheckInTime(java.time.LocalDateTime.now());
            return timesheetRepository.save(ts);
        }
        return ts;
    }

    public Timesheet checkOut(User employee) {
        LocalDate today = LocalDate.now();
        Timesheet ts = timesheetRepository.findByEmployeeAndDate(employee, today)
                .orElseThrow(() -> new RuntimeException("No active check-in found for today."));
        
        if (ts.getIsTracking() && ts.getLastCheckInTime() != null) {
            long secondsSinceLastCheckIn = java.time.Duration.between(ts.getLastCheckInTime(), java.time.LocalDateTime.now()).getSeconds();
            ts.setAccumulatedSeconds(ts.getAccumulatedSeconds() + secondsSinceLastCheckIn);
            ts.setIsTracking(false);
            ts.setCheckOut(java.time.LocalTime.now());
            ts.calculateHours();
            return timesheetRepository.save(ts);
        }
        return ts;
    }
}
