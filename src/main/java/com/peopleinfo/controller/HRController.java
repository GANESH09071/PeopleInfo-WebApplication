package com.peopleinfo.controller;

import com.peopleinfo.model.*;
import com.peopleinfo.security.UserPrincipal;
import com.peopleinfo.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/hr")
@RequiredArgsConstructor
public class HRController {

    private final UserService userService;
    private final LeaveService leaveService;
    private final TimesheetService timesheetService;
    private final HiringService hiringService;
    private final NlpJobGeneratorService nlpJobGeneratorService;

    // ─── DASHBOARD ───────────────────────────────────────────────────────────────
    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserPrincipal principal, Model model) {
        model.addAttribute("currentUser", principal.getUser());
        model.addAttribute("totalEmployees", userService.countActiveEmployees());
        model.addAttribute("pendingLeaves", leaveService.countPendingLeaves());
        model.addAttribute("pendingTimesheets", timesheetService.countPendingTimesheets());
        model.addAttribute("openJobs", hiringService.countOpenJobs());
        
        List<LeaveRequest> pending = leaveService.getPendingLeaves();
        List<LeaveRequest> sortedPending = new java.util.ArrayList<>(pending);
        sortedPending.sort((l1, l2) -> {
            if (l1.isUrgent() != l2.isUrgent()) {
                return l1.isUrgent() ? -1 : 1;
            }
            if (l1.getAppliedOn() != null && l2.getAppliedOn() != null) {
                return l2.getAppliedOn().compareTo(l1.getAppliedOn());
            }
            return 0;
        });

        model.addAttribute("recentLeaves", sortedPending);
        model.addAttribute("allEmployees", userService.getAllEmployees());
        return "hr/dashboard";
    }

    // ─── EMPLOYEES ────────────────────────────────────────────────────────────────
    @GetMapping("/employees")
    public String employees(@RequestParam(required = false) String search, Model model,
                            @AuthenticationPrincipal UserPrincipal principal) {
        model.addAttribute("currentUser", principal.getUser());
        List<User> employees = (search != null && !search.isBlank())
                ? userService.searchUsers(search)
                : userService.getAllUsers();
        model.addAttribute("employees", employees);
        model.addAttribute("search", search);
        return "hr/employees";
    }

    @GetMapping("/employees/new")
    public String newEmployeeForm(Model model, @AuthenticationPrincipal UserPrincipal principal) {
        model.addAttribute("currentUser", principal.getUser());
        model.addAttribute("employee", new User());
        model.addAttribute("roles", User.Role.values());
        model.addAttribute("departments", userService.getAllDepartments());
        model.addAttribute("isNew", true);
        return "hr/employee-form";
    }

    @GetMapping("/employees/{id}/edit")
    public String editEmployee(@PathVariable Long id, Model model,
                               @AuthenticationPrincipal UserPrincipal principal) {
        model.addAttribute("currentUser", principal.getUser());
        User emp = userService.findById(id).orElseThrow(() -> new RuntimeException("Employee not found"));
        model.addAttribute("employee", emp);
        model.addAttribute("roles", User.Role.values());
        model.addAttribute("statuses", User.EmploymentStatus.values());
        model.addAttribute("departments", userService.getAllDepartments());
        model.addAttribute("isNew", false);
        return "hr/employee-form";
    }

    @PostMapping("/employees/save")
    public String saveEmployee(@ModelAttribute User employee,
                               @RequestParam(required = false) String newPassword,
                               RedirectAttributes ra,
                               @AuthenticationPrincipal UserPrincipal principal) {
        try {
            if (employee.getId() == null) {
                // New employee via HR — encode a temporary password
                com.peopleinfo.dto.RegisterRequest req = new com.peopleinfo.dto.RegisterRequest();
                req.setFirstName(employee.getFirstName());
                req.setLastName(employee.getLastName());
                req.setEmail(employee.getEmail());
                req.setPassword(newPassword != null && !newPassword.isBlank() ? newPassword : "Welcome@123");
                req.setRole(employee.getRole());
                req.setDepartment(employee.getDepartment());
                req.setPosition(employee.getPosition());
                req.setPhone(employee.getPhone());
                req.setSalary(employee.getSalary());
                req.setAddress(employee.getAddress());
                userService.register(req);
            } else {
                User existing = userService.findById(employee.getId())
                        .orElseThrow(() -> new RuntimeException("Not found"));
                existing.setFirstName(employee.getFirstName());
                existing.setLastName(employee.getLastName());
                existing.setDepartment(employee.getDepartment());
                existing.setPosition(employee.getPosition());
                existing.setPhone(employee.getPhone());
                existing.setSalary(employee.getSalary());
                existing.setAddress(employee.getAddress());
                existing.setStatus(employee.getStatus());
                existing.setRole(employee.getRole());
                if (employee.getDateOfBirth() != null) existing.setDateOfBirth(employee.getDateOfBirth());
                if (employee.getDateOfJoining() != null) existing.setDateOfJoining(employee.getDateOfJoining());
                userService.updateUser(existing);
            }
            ra.addFlashAttribute("success", "Employee saved successfully.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/hr/employees";
    }

    @PostMapping("/employees/{id}/delete")
    public String deleteEmployee(@PathVariable Long id, RedirectAttributes ra) {
        userService.deleteUser(id);
        ra.addFlashAttribute("success", "Employee deleted.");
        return "redirect:/hr/employees";
    }

    // ─── LEAVES ───────────────────────────────────────────────────────────────────
    @GetMapping("/leaves")
    public String leaves(@RequestParam(required = false) String status, Model model,
                         @AuthenticationPrincipal UserPrincipal principal) {
        model.addAttribute("currentUser", principal.getUser());
        List<LeaveRequest> fetchedLeaves = (status != null && !status.isBlank())
                ? leaveService.getAllLeaves().stream()
                    .filter(l -> l.getStatus().name().equalsIgnoreCase(status)).toList()
                : leaveService.getAllLeaves();
        
        List<LeaveRequest> leaves = new java.util.ArrayList<>(fetchedLeaves);
        leaves.sort((l1, l2) -> {
            if (l1.isUrgent() != l2.isUrgent()) {
                return l1.isUrgent() ? -1 : 1;
            }
            if (l1.getAppliedOn() != null && l2.getAppliedOn() != null) {
                return l2.getAppliedOn().compareTo(l1.getAppliedOn());
            }
            return 0;
        });

        model.addAttribute("leaves", leaves);
        model.addAttribute("statusFilter", status);
        return "hr/leaves";
    }

    @PostMapping("/leaves/{id}/approve")
    public String approveLeave(@PathVariable Long id,
                               @RequestParam(required = false) String comments,
                               @AuthenticationPrincipal UserPrincipal principal,
                               RedirectAttributes ra) {
        leaveService.approveLeave(id, principal.getUser(), comments);
        ra.addFlashAttribute("success", "Leave approved.");
        return "redirect:/hr/leaves";
    }

    @PostMapping("/leaves/{id}/reject")
    public String rejectLeave(@PathVariable Long id,
                              @RequestParam(required = false) String comments,
                              @AuthenticationPrincipal UserPrincipal principal,
                              RedirectAttributes ra) {
        leaveService.rejectLeave(id, principal.getUser(), comments);
        ra.addFlashAttribute("success", "Leave rejected.");
        return "redirect:/hr/leaves";
    }

    // ─── TIMESHEETS ───────────────────────────────────────────────────────────────
    @GetMapping("/timesheets")
    public String timesheets(@RequestParam(required = false) String search,
                             Model model,
                             @AuthenticationPrincipal UserPrincipal principal) {
        User me = principal.getUser();
        model.addAttribute("currentUser", me);
        
        List<com.peopleinfo.model.Timesheet> timesheets;
        if (search != null && !search.isBlank()) {
            timesheets = timesheetService.searchTimesheets(search);
        } else {
            timesheets = timesheetService.getAllTimesheets();
        }
        model.addAttribute("timesheets", timesheets);
        model.addAttribute("search", search);

        java.util.Optional<Timesheet> activeOpt = timesheetService.findByEmployeeAndDate(me, LocalDate.now());
        if (activeOpt.isPresent()) {
            model.addAttribute("activeTimesheet", activeOpt.get());
        } else {
            model.addAttribute("activeTimesheet", null);
        }

        return "hr/timesheets";
    }

    @PostMapping("/timesheets/checkin")
    public String checkIn(@AuthenticationPrincipal UserPrincipal principal, RedirectAttributes ra) {
        try {
            timesheetService.checkIn(principal.getUser());
            ra.addFlashAttribute("success", "Checked in successfully.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error during check-in: " + e.getMessage());
        }
        return "redirect:/hr/timesheets";
    }

    @PostMapping("/timesheets/checkout")
    public String checkOut(@AuthenticationPrincipal UserPrincipal principal, RedirectAttributes ra) {
        try {
            timesheetService.checkOut(principal.getUser());
            ra.addFlashAttribute("success", "Checked out successfully.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error during check-out: " + e.getMessage());
        }
        return "redirect:/hr/timesheets";
    }

    // ─── HIRING ───────────────────────────────────────────────────────────────────
    @GetMapping("/hiring")
    public String hiring(Model model, @AuthenticationPrincipal UserPrincipal principal) {
        model.addAttribute("currentUser", principal.getUser());
        model.addAttribute("jobs", hiringService.getAll());
        return "hr/hiring";
    }

    @GetMapping("/hiring/new")
    public String newHiringForm(Model model, @AuthenticationPrincipal UserPrincipal principal) {
        model.addAttribute("currentUser", principal.getUser());
        model.addAttribute("job", new HiringRequirement());
        model.addAttribute("departments", userService.getAllDepartments());
        model.addAttribute("experienceLevels", HiringRequirement.ExperienceLevel.values());
        model.addAttribute("jobTypes", HiringRequirement.JobType.values());
        model.addAttribute("isNew", true);
        return "hr/hiring-form";
    }

    @GetMapping("/hiring/{id}/edit")
    public String editHiring(@PathVariable Long id, Model model,
                             @AuthenticationPrincipal UserPrincipal principal) {
        model.addAttribute("currentUser", principal.getUser());
        HiringRequirement job = hiringService.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found"));
        model.addAttribute("job", job);
        model.addAttribute("departments", userService.getAllDepartments());
        model.addAttribute("experienceLevels", HiringRequirement.ExperienceLevel.values());
        model.addAttribute("jobTypes", HiringRequirement.JobType.values());
        model.addAttribute("statuses", HiringRequirement.HiringStatus.values());
        model.addAttribute("isNew", false);
        return "hr/hiring-form";
    }

    @PostMapping("/hiring/save")
    public String saveHiring(@ModelAttribute HiringRequirement job,
                             @AuthenticationPrincipal UserPrincipal principal,
                             RedirectAttributes ra) {
        job.setPostedBy(principal.getUser());
        hiringService.save(job);
        ra.addFlashAttribute("success", "Job posting saved.");
        return "redirect:/hr/hiring";
    }

    @PostMapping("/hiring/{id}/delete")
    public String deleteHiring(@PathVariable Long id, RedirectAttributes ra) {
        hiringService.delete(id);
        ra.addFlashAttribute("success", "Job posting deleted.");
        return "redirect:/hr/hiring";
    }

    @GetMapping("/hiring/generate")
    @ResponseBody
    public GeneratedJobDetails generateJobDetails(
            @RequestParam String jobTitle,
            @RequestParam(required = false) Integer yearsOfExperience,
            @RequestParam(required = false) String experienceLevel) {
        return nlpJobGeneratorService.generateJobDetails(jobTitle, yearsOfExperience, experienceLevel);
    }
}
