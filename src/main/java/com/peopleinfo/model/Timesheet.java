package com.peopleinfo.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "timesheets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Timesheet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id", nullable = false)
    private User employee;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "check_in")
    private LocalTime checkIn;

    @Column(name = "check_out")
    private LocalTime checkOut;

    @Column(name = "hours_worked")
    private Double hoursWorked = 0.0;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Enumerated(EnumType.STRING)
    private TimesheetStatus status;

    @Column(name = "project_name")
    private String projectName;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "is_tracking")
    private Boolean isTracking = false;

    @Column(name = "last_check_in_time")
    private LocalDateTime lastCheckInTime;

    @Column(name = "accumulated_seconds")
    private Long accumulatedSeconds = 0L;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) status = TimesheetStatus.SUBMITTED;
        if (accumulatedSeconds == null) accumulatedSeconds = 0L;
        if (isTracking == null) isTracking = false;
        if (hoursWorked == null) hoursWorked = 0.0;
        calculateHours();
    }

    @PreUpdate
    protected void onUpdate() {
        calculateHours();
    }

    public void calculateHours() {
        if (accumulatedSeconds != null) {
            double hours = accumulatedSeconds / 3600.0;
            this.hoursWorked = Math.round(hours * 10.0) / 10.0;
        }
    }

    public enum TimesheetStatus {
        SUBMITTED, APPROVED, REJECTED
    }
}
