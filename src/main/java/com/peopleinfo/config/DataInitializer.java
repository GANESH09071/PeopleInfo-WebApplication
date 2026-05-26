package com.peopleinfo.config;

import com.peopleinfo.model.*;
import com.peopleinfo.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final HiringRequirementRepository hiringRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            initializeDemoData();
        }
    }

    private void initializeDemoData() {
        // HR Admin
        User hr = userRepository.save(User.builder()
                .firstName("Sarah").lastName("Johnson")
                .email("hr@peopleinfo.com")
                .password(passwordEncoder.encode("hr123"))
                .role(User.Role.HR)
                .employeeId("EMP001")
                .department("Human Resources")
                .position("HR Manager")
                .phone("+1-555-0101")
                .dateOfJoining(LocalDate.of(2020, 1, 15))
                .dateOfBirth(LocalDate.of(1985, 6, 10))
                .salary(85000.0)
                .address("123 Main St, New York, NY 10001")
                .status(User.EmploymentStatus.ACTIVE)
                .build());

        // Employees
        userRepository.save(User.builder()
                .firstName("Michael").lastName("Chen")
                .email("michael.chen@peopleinfo.com")
                .password(passwordEncoder.encode("emp123"))
                .role(User.Role.EMPLOYEE)
                .employeeId("EMP002")
                .department("Engineering")
                .position("Senior Software Engineer")
                .phone("+1-555-0102")
                .dateOfJoining(LocalDate.of(2021, 3, 20))
                .dateOfBirth(LocalDate.of(1990, 4, 22))
                .salary(95000.0)
                .address("456 Oak Ave, San Francisco, CA 94102")
                .status(User.EmploymentStatus.ACTIVE)
                .build());

        userRepository.save(User.builder()
                .firstName("Emily").lastName("Rodriguez")
                .email("emily.r@peopleinfo.com")
                .password(passwordEncoder.encode("emp123"))
                .role(User.Role.EMPLOYEE)
                .employeeId("EMP003")
                .department("Marketing")
                .position("Marketing Specialist")
                .phone("+1-555-0103")
                .dateOfJoining(LocalDate.of(2022, 6, 1))
                .dateOfBirth(LocalDate.of(1993, 8, 15))
                .salary(72000.0)
                .address("789 Pine Rd, Austin, TX 73301")
                .status(User.EmploymentStatus.ACTIVE)
                .build());

        userRepository.save(User.builder()
                .firstName("David").lastName("Kim")
                .email("david.kim@peopleinfo.com")
                .password(passwordEncoder.encode("emp123"))
                .role(User.Role.EMPLOYEE)
                .employeeId("EMP004")
                .department("Finance")
                .position("Financial Analyst")
                .phone("+1-555-0104")
                .dateOfJoining(LocalDate.of(2021, 9, 10))
                .dateOfBirth(LocalDate.of(1988, 12, 3))
                .salary(80000.0)
                .address("321 Elm St, Chicago, IL 60601")
                .status(User.EmploymentStatus.ACTIVE)
                .build());

        // Hiring Requirements
        hiringRepository.save(HiringRequirement.builder()
                .jobTitle("Full Stack Developer")
                .department("Engineering")
                .description("We are looking for a talented Full Stack Developer to join our growing engineering team.")
                .requirements("5+ years Java/Spring Boot, React/Angular experience\nStrong SQL skills\nREST API design\nMicroservices knowledge")
                .responsibilities("Design and develop web applications\nCollaborate with cross-functional teams\nCode reviews\nMentor junior developers")
                .experienceLevel(HiringRequirement.ExperienceLevel.SENIOR)
                .jobType(HiringRequirement.JobType.FULL_TIME)
                .positionsAvailable(2)
                .salaryRangeMin(90000.0).salaryRangeMax(130000.0)
                .location("New York, NY (Hybrid)")
                .deadlineDate(LocalDate.now().plusDays(30))
                .status(HiringRequirement.HiringStatus.OPEN)
                .postedBy(hr)
                .build());

        hiringRepository.save(HiringRequirement.builder()
                .jobTitle("Product Marketing Manager")
                .department("Marketing")
                .description("Seeking a strategic Product Marketing Manager to drive our go-to-market initiatives.")
                .requirements("3+ years product marketing\nExperience with B2B SaaS\nStrong analytical skills\nExcellent communication")
                .responsibilities("Develop product positioning\nCreate marketing collateral\nConduct market research\nSupport sales enablement")
                .experienceLevel(HiringRequirement.ExperienceLevel.MID)
                .jobType(HiringRequirement.JobType.FULL_TIME)
                .positionsAvailable(1)
                .salaryRangeMin(75000.0).salaryRangeMax(95000.0)
                .location("San Francisco, CA (Remote)")
                .deadlineDate(LocalDate.now().plusDays(21))
                .status(HiringRequirement.HiringStatus.OPEN)
                .postedBy(hr)
                .build());

        hiringRepository.save(HiringRequirement.builder()
                .jobTitle("Data Science Intern")
                .department("Engineering")
                .description("Join our data team for a 6-month internship working on machine learning projects.")
                .requirements("Currently pursuing CS/Statistics degree\nPython proficiency\nML fundamentals\nGood communication")
                .responsibilities("Assist in data pipeline development\nRun experiments\nPrepare reports\nCollaborate with senior team")
                .experienceLevel(HiringRequirement.ExperienceLevel.ENTRY)
                .jobType(HiringRequirement.JobType.INTERNSHIP)
                .positionsAvailable(3)
                .salaryRangeMin(20000.0).salaryRangeMax(30000.0)
                .location("Remote")
                .deadlineDate(LocalDate.now().plusDays(14))
                .status(HiringRequirement.HiringStatus.OPEN)
                .postedBy(hr)
                .build());

        log.info("=== PeopleInfo Demo Data Initialized ===");
        log.info("HR Login:       hr@peopleinfo.com / hr123");
        log.info("Employee Login: michael.chen@peopleinfo.com / emp123");
        log.info("==========================================");
    }
}
