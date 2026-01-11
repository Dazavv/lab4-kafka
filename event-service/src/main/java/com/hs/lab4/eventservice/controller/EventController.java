package com.hs.lab3.eventservice.controller;

import com.hs.lab3.eventservice.auth.jwt.JwtAuthentication;
import com.hs.lab3.eventservice.dto.requests.CreateEventRequest;
import com.hs.lab3.eventservice.dto.requests.CreateUserEventRequest;
import com.hs.lab3.eventservice.dto.responses.EventDto;
import com.hs.lab3.eventservice.mapper.EventMapper;
import com.hs.lab3.eventservice.service.EventService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/event")
@RequiredArgsConstructor
@Slf4j
public class EventController {

    private final EventService eventService;
    private final EventMapper eventMapper;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('REDACTOR')")
    public Mono<EventDto> addEvent(@Valid @RequestBody CreateEventRequest request) {
        return eventService.addEvent(
                        request.name(),
                        request.description(),
                        request.date(),
                        request.startTime(),
                        request.endTime(),
                        request.ownerId()
                )
                .map(eventMapper::toEventDto);
    }

    @PostMapping("/my")
    @PreAuthorize("hasAnyAuthority('USER', 'LEAD')")
    public Mono<EventDto> addMyEvent(
            @Valid @RequestBody CreateUserEventRequest request,
            Authentication authentication
    ) {
        JwtAuthentication auth = (JwtAuthentication) authentication;

        return eventService.addEvent(
                        request.name(),
                        request.description(),
                        request.date(),
                        request.startTime(),
                        request.endTime(),
                        auth.getId()
                )
                .map(eventMapper::toEventDto);
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyAuthority('USER', 'LEAD')")
    public Flux<EventDto> getMyEvents(Authentication authentication) {
        JwtAuthentication auth = (JwtAuthentication) authentication;
        return eventService.getEventsByOwnerId(auth.getId())
                .map(eventMapper::toEventDto);
    }

    @GetMapping("/my/{id}")
    @PreAuthorize("hasAnyAuthority('USER', 'LEAD')")
    public Mono<EventDto> getMyEventById(Authentication authentication, @PathVariable @Min(1) Long id) {
        JwtAuthentication auth = (JwtAuthentication) authentication;
        return eventService.getEventByOwnerId(auth.getId(), id)
                .map(eventMapper::toEventDto);
    }

    @DeleteMapping("/my/{id}")
    @PreAuthorize("hasAnyAuthority('USER', 'LEAD')")
    public Mono<Void> deleteMyEventById(Authentication authentication, @PathVariable @Min(1) Long id) {
        JwtAuthentication auth = (JwtAuthentication) authentication;
        return eventService.deleteEventByOwnerId(auth.getId(), id);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('REDACTOR')")
    public Flux<EventDto> getAllEvents() {
        return eventService.getAllEvents()
                .map(eventMapper::toEventDto);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('REDACTOR')")
    public Mono<EventDto> getEventById(@PathVariable @Min(1) Long id) {
        return eventService.getEventById(id)
                .map(eventMapper::toEventDto);
    }

    @GetMapping("/owner/{id}")
    @PreAuthorize("hasAnyAuthority('REDACTOR')")
    public Flux<EventDto> getUserEvents(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return eventService.getUserEventsById(id, pageable)
                .map(eventMapper::toEventDto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('REDACTOR')")
    public Mono<Void> deleteEventById(@PathVariable @Min(1) Long id) {
        return eventService.deleteEventById(id);
    }

    @GetMapping("/busy")
    @PreAuthorize("hasAnyAuthority('SERVICE')")
    public Flux<EventDto> getBusyEventsForUsersBetweenDates(
            @RequestParam List<Long> userIds,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return eventService.getBusyEventsForUsersBetweenDates(userIds, startDate, endDate)
                .map(eventMapper::toEventDto);
    }
}
