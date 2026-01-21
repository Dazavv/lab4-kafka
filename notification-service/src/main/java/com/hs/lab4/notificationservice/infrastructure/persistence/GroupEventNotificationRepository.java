package com.hs.lab4.notificationservice.infrastructure.persistence;

import com.hs.lab4.notificationservice.infrastructure.entity.GroupEventNotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupEventNotificationRepository extends JpaRepository<GroupEventNotificationEntity, Long> {
}
