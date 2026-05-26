package com.peopleinfo.repository;

import com.peopleinfo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByEmployeeId(String employeeId);

    List<User> findByRole(User.Role role);

    List<User> findByStatus(User.EmploymentStatus status);

    List<User> findByDepartment(String department);

    @Query("SELECT DISTINCT u.department FROM User u WHERE u.department IS NOT NULL")
    List<String> findAllDepartments();

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = 'EMPLOYEE' AND u.status = 'ACTIVE'")
    long countActiveEmployees();

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = 'HR'")
    long countHRUsers();

    List<User> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String firstName, String lastName, String email);
}
