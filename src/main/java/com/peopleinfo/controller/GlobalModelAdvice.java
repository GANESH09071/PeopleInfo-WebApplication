package com.peopleinfo.controller;

import com.peopleinfo.repository.NotificationRepository;
import com.peopleinfo.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAdvice {

    private final NotificationRepository notificationRepository;

    @ModelAttribute
    public void addGlobalAttributes(@AuthenticationPrincipal UserPrincipal principal, Model model) {
        if (principal != null) {
            model.addAttribute("unreadNotificationCount", notificationRepository.countByEmployeeAndIsReadFalse(principal.getUser()));
            model.addAttribute("notifications", notificationRepository.findByEmployeeOrderByCreatedAtDesc(principal.getUser()));
        }
    }
}
