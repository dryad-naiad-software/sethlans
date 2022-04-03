package com.dryadandnaiad.sethlans.repositories;

import com.dryadandnaiad.sethlans.models.system.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
