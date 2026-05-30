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
import java.time.LocalTime;

@Controller
@RequestMapping("/employee")
@RequiredArgsConstructor
public class EmployeeController {

    private final UserService userService;
    private final LeaveService leaveService;
    private final TimesheetService timesheetService;
    private final HiringService hiringService;
    private final com.peopleinfo.repository.NotificationRepository notificationRepository;

    // ─── DASHBOARD ───────────────────────────────────────────────────────────────
    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserPrincipal principal, Model model) {
        User me = principal.getUser();
        model.addAttribute("currentUser", me);
        model.addAttribute("myLeaves", leaveService.getLeavesByEmployee(me));
        model.addAttribute("myTimesheets", timesheetService.getTimesheetsByEmployee(me));
        model.addAttribute("hoursThisMonth", timesheetService.getTotalHoursThisMonth(me));
        model.addAttribute("openJobs", hiringService.countOpenJobs());
        model.addAttribute("totalEmployees", userService.countActiveEmployees());
        return "employee/dashboard";
    }

    // ─── MY TIMESHEETS ────────────────────────────────────────────────────────────
    @GetMapping("/timesheets")
    public String myTimesheets(@AuthenticationPrincipal UserPrincipal principal, Model model) {
        User me = principal.getUser();
        java.util.List<com.peopleinfo.model.Timesheet> timesheets = timesheetService.getTimesheetsByEmployee(me);
        model.addAttribute("currentUser", me);
        model.addAttribute("timesheets", timesheets);
        model.addAttribute("hoursThisMonth", timesheetService.getTotalHoursThisMonth(me));
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("todayStr", LocalDate.now().toString());

        java.util.Optional<Timesheet> activeOpt = timesheetService.findByEmployeeAndDate(me, LocalDate.now());
        if (activeOpt.isPresent()) {
            model.addAttribute("activeTimesheet", activeOpt.get());
        } else {
            model.addAttribute("activeTimesheet", null);
        }

        return "employee/timesheets";
    }

    @PostMapping("/timesheets/checkin")
    public String checkIn(@AuthenticationPrincipal UserPrincipal principal, RedirectAttributes ra) {
        try {
            timesheetService.checkIn(principal.getUser());
            ra.addFlashAttribute("success", "Checked in successfully.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error during check-in: " + e.getMessage());
        }
        return "redirect:/employee/timesheets";
    }

    @PostMapping("/timesheets/checkout")
    public String checkOut(@AuthenticationPrincipal UserPrincipal principal, RedirectAttributes ra) {
        try {
            timesheetService.checkOut(principal.getUser());
            ra.addFlashAttribute("success", "Checked out successfully.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error during check-out: " + e.getMessage());
        }
        return "redirect:/employee/timesheets";
    }

    @PostMapping("/timesheets/submit")
    public String submitTimesheet(@AuthenticationPrincipal UserPrincipal principal,
                                  @RequestParam String date,
                                  @RequestParam String checkIn,
                                  @RequestParam String checkOut,
                                  @RequestParam(required = false) String notes,
                                  @RequestParam(required = false) String projectName,
                                  RedirectAttributes ra) {
        User me = principal.getUser();
        LocalDate tsDate = LocalDate.parse(date);

        if (timesheetService.findByEmployeeAndDate(me, tsDate).isPresent()) {
            ra.addFlashAttribute("error", "Timesheet already submitted for " + date);
            return "redirect:/employee/timesheets";
        }

        Timesheet ts = Timesheet.builder()
                .employee(me)
                .date(tsDate)
                .checkIn(LocalTime.parse(checkIn))
                .checkOut(LocalTime.parse(checkOut))
                .notes(notes)
                .projectName(projectName)
                .status(Timesheet.TimesheetStatus.SUBMITTED)
                .build();
        timesheetService.saveTimesheet(ts);
        ra.addFlashAttribute("success", "Timesheet submitted successfully.");
        return "redirect:/employee/timesheets";
    }

    // ─── LEAVES ───────────────────────────────────────────────────────────────────
    @GetMapping("/leaves")
    public String myLeaves(@AuthenticationPrincipal UserPrincipal principal, Model model) {
        User me = principal.getUser();
        java.util.List<LeaveRequest> leaves = leaveService.getLeavesByEmployee(me);
        model.addAttribute("currentUser", me);
        model.addAttribute("leaves", leaves);
        model.addAttribute("leaveTypes", LeaveRequest.LeaveType.values());
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("todayStr", LocalDate.now().toString()); // yyyy-MM-dd for th:min
        // pre-computed counts to avoid Thymeleaf selection variable issues
        long pending  = leaves.stream().filter(l -> l.getStatus() == LeaveRequest.LeaveStatus.PENDING).count();
        long approved = leaves.stream().filter(l -> l.getStatus() == LeaveRequest.LeaveStatus.APPROVED).count();
        long rejected = leaves.stream().filter(l -> l.getStatus() == LeaveRequest.LeaveStatus.REJECTED).count();
        model.addAttribute("pendingCount",  pending);
        model.addAttribute("approvedCount", approved);
        model.addAttribute("rejectedCount", rejected);

        model.addAttribute("slBalance", leaveService.getAvailableSickLeave(me));
        model.addAttribute("elBalance", leaveService.getAvailableEarnedLeave(me));
        model.addAttribute("clBalance", leaveService.getAvailableCasualLeave(me));
        model.addAttribute("flBalance", leaveService.getAvailableFlexiLeave(me));
        
        return "employee/leaves";
    }

    @PostMapping("/leaves/apply")
    public String applyLeave(@AuthenticationPrincipal UserPrincipal principal,
                             @RequestParam String leaveType,
                             @RequestParam String startDate,
                             @RequestParam String endDate,
                             @RequestParam(required = false) String reason,
                             RedirectAttributes ra) {
        User me = principal.getUser();
        LeaveRequest.LeaveType type = LeaveRequest.LeaveType.valueOf(leaveType);
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        
        long requestedDays = java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1;
        double available = 0;
        
        switch (type) {
            case SICK: available = leaveService.getAvailableSickLeave(me); break;
            case EARNED: available = leaveService.getAvailableEarnedLeave(me); break;
            case CASUAL: available = leaveService.getAvailableCasualLeave(me); break;
            case FLEXI: available = leaveService.getAvailableFlexiLeave(me); break;
        }
        
        if (requestedDays > available) {
            ra.addFlashAttribute("error", "Insufficient balance for " + type + " leave. You requested " + requestedDays + " days, but only have " + available + " available.");
            return "redirect:/employee/leaves";
        }

        LeaveRequest leave = LeaveRequest.builder()
                .employee(me)
                .leaveType(type)
                .startDate(start)
                .endDate(end)
                .reason(reason)
                .status(LeaveRequest.LeaveStatus.PENDING)
                .build();
        leaveService.applyLeave(leave);
        ra.addFlashAttribute("success", "Leave application submitted.");
        return "redirect:/employee/leaves";
    }

    // ─── ALL EMPLOYEES ────────────────────────────────────────────────────────────
    @GetMapping("/directory")
    public String employeeDirectory(@RequestParam(required = false) String search,
                                    @AuthenticationPrincipal UserPrincipal principal, Model model) {
        model.addAttribute("currentUser", principal.getUser());
        var employees = (search != null && !search.isBlank())
                ? userService.searchUsers(search)
                : userService.getAllUsers();
        model.addAttribute("employees", employees);
        model.addAttribute("search", search);
        return "employee/directory";
    }

    // ─── HIRING ───────────────────────────────────────────────────────────────────
    @GetMapping("/hiring")
    public String viewHiring(@AuthenticationPrincipal UserPrincipal principal, Model model) {
        model.addAttribute("currentUser", principal.getUser());
        model.addAttribute("jobs", hiringService.getOpenJobs());
        return "employee/hiring";
    }

    // ─── NOTIFICATIONS ────────────────────────────────────────────────────────────
    @PostMapping("/notifications/read")
    public String readNotifications(@AuthenticationPrincipal UserPrincipal principal, 
                                    @RequestHeader(value = "Referer", required = false) String referer) {
        User me = principal.getUser();
        java.util.List<Notification> unread = notificationRepository.findByEmployeeOrderByCreatedAtDesc(me).stream()
                .filter(n -> !n.isRead()).toList();
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
        
        return "redirect:" + (referer != null ? referer : "/employee/dashboard");
    }
}
