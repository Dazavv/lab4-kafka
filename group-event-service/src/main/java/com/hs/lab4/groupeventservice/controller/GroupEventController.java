package com.hs.lab3.groupeventservice.controller;

import com.hs.lab3.groupeventservice.auth.jwt.JwtAuthentication;
import com.hs.lab3.groupeventservice.dto.requests.BookSlotRequest;
import com.hs.lab3.groupeventservice.dto.requests.CreateGroupEventRequest;
import com.hs.lab3.groupeventservice.dto.requests.CreateUserGroupEventRequest;
import com.hs.lab3.groupeventservice.dto.requests.RecommendSlotsRequest;
import com.hs.lab3.groupeventservice.dto.responses.GroupEventDto;
import com.hs.lab3.groupeventservice.dto.responses.RecommendTimeSlotDto;
import com.hs.lab3.groupeventservice.mapper.GroupEventMapper;
import com.hs.lab3.groupeventservice.service.GroupEventService;
import com.hs.lab3.groupeventservice.service.RecommendationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/group-event")
@RequiredArgsConstructor
public class GroupEventController {

    private final GroupEventService groupEventService;
    private final GroupEventMapper groupEventMapper;
    private final RecommendationService recommendationService;

    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    @PreAuthorize("hasAnyAuthority('REDACTOR')")
    public Mono<GroupEventDto> addGroupEvent(@RequestBody CreateGroupEventRequest request) {
        return groupEventService.addGroupEvent(
                        request.name(),
                        request.description(),
                        request.participantIds(),
                        request.ownerId()
                )
                .map(groupEventMapper::toGroupEventDto);
    }

    @PostMapping("/my")
    @ResponseStatus(code = HttpStatus.CREATED)
    @PreAuthorize("hasAnyAuthority('LEAD')")
    public Mono<GroupEventDto> addMyGroupEvent(@RequestBody CreateUserGroupEventRequest request,
                                               Authentication authentication) {
        JwtAuthentication auth = (JwtAuthentication) authentication;

        return groupEventService.addGroupEvent(
                        request.name(),
                        request.description(),
                        request.participantIds(),
                        auth.getId()
                )
                .map(groupEventMapper::toGroupEventDto);
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyAuthority('LEAD')")
    public Flux<GroupEventDto> getAllMyGroupEvents() {
        return groupEventService.getAllGroupEvents()
                .map(groupEventMapper::toGroupEventDto);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('REDACTOR')")
    public Flux<GroupEventDto> getAllGroupEvents(Authentication authentication) {
        JwtAuthentication auth = (JwtAuthentication) authentication;
        return groupEventService.getAllGroupEventsByOwnerId(auth.getId())
                .map(groupEventMapper::toGroupEventDto);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('REDACTOR')")
    public Mono<GroupEventDto> getGroupEventById(@PathVariable @Min(1) Long id) {
        return groupEventService.getGroupEventById(id)
                .map(groupEventMapper::toGroupEventDto);
    }
    @GetMapping("/my/{id}")
    @PreAuthorize("hasAnyAuthority('LEAD')")
    public Mono<GroupEventDto> getMyGroupEventById(@PathVariable @Min(1) Long id,
                                                   Authentication authentication) {
        JwtAuthentication auth = (JwtAuthentication) authentication;
        return groupEventService.getGroupEventByIdAndOwnerId(id, auth.getId())
                .map(groupEventMapper::toGroupEventDto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('LEAD')")
    public Mono<Void> deleteGroupEventById(@PathVariable @Min(1) Long id) {
        return groupEventService.deleteGroupEventById(id);
    }

    @DeleteMapping("/my/{id}")
    @PreAuthorize("hasAnyAuthority('REDACTOR')")
    public Mono<Void> deleteMyGroupEventById(@PathVariable @Min(1) Long id,
                                           Authentication authentication) {
        JwtAuthentication auth = (JwtAuthentication) authentication;
        return groupEventService.deleteGroupEventByOwnerId(id, auth.getId());
    }

    @PostMapping("/recommend")
    @PreAuthorize("hasAnyAuthority('LEAD', 'REDACTOR')")
    public Flux<RecommendTimeSlotDto> recommendGroupEvents(@RequestBody RecommendSlotsRequest request) {
        return recommendationService.recommendSlots(
                request.periodStart(),
                request.periodEnd(),
                request.duration(),
                request.groupEventId()
        );
    }

    @PostMapping("/book")
    @PreAuthorize("hasAnyAuthority('LEAD', 'REDACTOR')")
    public Mono<GroupEventDto> bookGroupEvent(@Valid @RequestBody BookSlotRequest req) {
        return recommendationService.bookSlot(
                        req.groupEventId(),
                        req.date(),
                        req.startTime(),
                        req.endTime()
                );
    }
}
