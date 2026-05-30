package com.peopleinfo.repository;

import com.peopleinfo.model.Notification;
import com.peopleinfo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByEmployeeOrderByCreatedAtDesc(User employee);
    long countByEmployeeAndIsReadFalse(User employee);
}
