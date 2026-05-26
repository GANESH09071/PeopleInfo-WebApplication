package com.peopleinfo.service;

import com.peopleinfo.dto.RegisterRequest;
import com.peopleinfo.model.User;
import com.peopleinfo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Email already registered");
        }
        String empId = generateEmployeeId();
        User user = User.builder()
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(req.getRole() != null ? req.getRole() : User.Role.EMPLOYEE)
                .employeeId(empId)
                .department(req.getDepartment())
                .position(req.getPosition())
                .phone(req.getPhone())
                .dateOfJoining(LocalDate.now())
                .salary(req.getSalary())
                .address(req.getAddress())
                .status(User.EmploymentStatus.ACTIVE)
                .build();
        return userRepository.save(user);
    }

    public List<User> getAllEmployees() {
        return userRepository.findByRole(User.Role.EMPLOYEE);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User updateUser(User user) {
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public List<User> searchUsers(String query) {
        return userRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                query, query, query);
    }

    public long countActiveEmployees() {
        return userRepository.countActiveEmployees();
    }

    public long countHRUsers() {
        return userRepository.countHRUsers();
    }

    public List<String> getAllDepartments() {
        return userRepository.findAllDepartments();
    }

    private String generateEmployeeId() {
        long count = userRepository.count() + 1;
        String id = "EMP" + String.format("%03d", count);
        while (userRepository.existsByEmployeeId(id)) {
            count++;
            id = "EMP" + String.format("%03d", count);
        }
        return id;
    }
}
