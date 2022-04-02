package com.dryadandnaiad.sethlans.repositories;

import com.dryadandnaiad.sethlans.models.system.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
