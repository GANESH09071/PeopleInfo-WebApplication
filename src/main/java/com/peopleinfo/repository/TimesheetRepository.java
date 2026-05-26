package com.peopleinfo.repository;

import com.peopleinfo.model.Timesheet;
import com.peopleinfo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TimesheetRepository extends JpaRepository<Timesheet, Long> {

    List<Timesheet> findByEmployee(User employee);

    List<Timesheet> findByEmployeeOrderByDateDesc(User employee);

    List<Timesheet> findAllByOrderByDateDesc();

    Optional<Timesheet> findByEmployeeAndDate(User employee, LocalDate date);

    List<Timesheet> findByEmployeeAndDateBetween(User employee, LocalDate startDate, LocalDate endDate);

    @Query("SELECT SUM(t.hoursWorked) FROM Timesheet t WHERE t.employee = ?1 AND t.date BETWEEN ?2 AND ?3")
    Double sumHoursByEmployeeAndDateRange(User employee, LocalDate startDate, LocalDate endDate);

    @Query("SELECT COUNT(t) FROM Timesheet t WHERE t.status = 'SUBMITTED'")
    long countPendingTimesheets();

    List<Timesheet> findByDateBetweenOrderByDateDesc(LocalDate startDate, LocalDate endDate);

    @Query("SELECT t FROM Timesheet t WHERE " +
           "LOWER(t.employee.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(t.employee.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "CAST(t.employee.id AS string) LIKE CONCAT('%', :search, '%') " +
           "ORDER BY t.date DESC")
    List<Timesheet> searchByEmployeeNameOrId(@org.springframework.data.repository.query.Param("search") String search);
}
