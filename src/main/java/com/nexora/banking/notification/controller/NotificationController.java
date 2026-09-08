package com.nexora.banking.notification.controller;

import com.nexora.banking.common.response.PageResponse;

import com.nexora.banking.notification.dto.response.NotificationResponse;
import com.nexora.banking.notification.service.NotificationService;
import com.nexora.banking.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public PageResponse<NotificationResponse> getNotifications(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0")int page,
            @RequestParam(defaultValue = "20")int size
    ) {

        Pageable pageable = PageRequest.of(page, size);

        Page<NotificationResponse> notifications =
            notificationService.getUserNotifications(
                    currentUser.getId(),
                    pageable
            );

        return PageResponse.from(notifications);
    }
    

    @PatchMapping("/{notificationId}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markAsRead(
            @AuthenticationPrincipal User user,
            @PathVariable UUID notificationId
    ) {

        notificationService.markAsRead(
                user.getId(),
                notificationId
        );
    }
}