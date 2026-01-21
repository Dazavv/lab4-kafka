package com.hs.lab4.notificationservice.infrastructure.persistence;

import com.hs.lab4.notificationservice.domain.model.UserRoleAddedEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRoleAddedRepository extends JpaRepository<UserRoleAddedEvent, Long> {
}
