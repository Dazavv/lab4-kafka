package com.hs.lab4.notificationservice.application.service;

import com.hs.lab4.notificationservice.domain.model.UserRoleAddedEvent;
import com.hs.lab4.notificationservice.infrastructure.persistence.UserRoleAddedRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserRoleNotificationService {

    private final UserRoleAddedRepository repository;

    @Transactional
    public UserRoleAddedEvent handleUserRoleAdded(UserRoleAddedEvent event) {
        return repository.save(event);
    }
}