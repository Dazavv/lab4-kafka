package com.hs.lab4.groupeventservice.service;

import com.hs.lab4.groupeventservice.dto.responses.GroupEventDto;
import com.hs.lab4.groupeventservice.dto.responses.RecommendTimeSlotDto;
import com.hs.lab4.groupeventservice.entity.GroupEvent;
import com.hs.lab4.groupeventservice.enums.GroupEventStatus;
import com.hs.lab4.groupeventservice.exceptions.EventNotFoundException;
import com.hs.lab4.groupeventservice.exceptions.NoAvailableSlotsException;
import com.hs.lab4.groupeventservice.kafka.EventNotificationMessage;
import com.hs.lab4.groupeventservice.kafka.EventNotificationProducer;
import com.hs.lab4.groupeventservice.mapper.GroupEventMapper;
import com.hs.lab4.groupeventservice.repository.GroupEventRepository;
import com.hs.lab4.groupeventservice.util.SlotCalculator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecommendationService {
    private final GroupEventRepository groupEventRepository;
    private final GroupEventMapper groupEventMapper;
    private final EventClientService eventClientService;
    private final GroupEventLoader loader;
    private final EventNotificationProducer eventNotificationProducer;

    @Transactional
    public Flux<RecommendTimeSlotDto> recommendSlots(LocalDate periodStart, LocalDate periodEnd, Duration duration, Long groupEventId) {
        Mono<List<Long>> participantIdsMono = Mono.fromCallable(
                () -> loader.loadParticipantIds(groupEventId)).subscribeOn(Schedulers.boundedElastic());

        return participantIdsMono.flatMapMany(participantIds -> eventClientService.getBusyEventsForUsersBetweenDates(participantIds, periodStart.toString(), periodEnd.toString()).collectList().flatMapMany(busyIntervals -> {
            var free = SlotCalculator.findCommonFreeSlots(periodStart, periodEnd, busyIntervals, duration);
            if (free.isEmpty()) return Flux.error(new NoAvailableSlotsException("No free slots available"));
            return Flux.fromIterable(free.stream().limit(5).toList());
        }));
    }

    @Transactional
    public Mono<GroupEventDto> bookSlot(Long id, LocalDate date, LocalTime startTime, LocalTime endTime) {
        return Mono.fromCallable(() -> {
            GroupEvent ge = groupEventRepository.findByIdWithParticipants(id)
                    .orElseThrow(() -> new EventNotFoundException("GroupEvent not found: " + id));

            ge.setDate(date);
            ge.setStartTime(startTime);
            ge.setEndTime(endTime);
            ge.setStatus(GroupEventStatus.CONFIRMED);

            groupEventRepository.save(ge);

            ge.getParticipantIds().forEach(participantId -> {
                EventNotificationMessage message = EventNotificationMessage.builder()
                        .eventId(ge.getId())
                        .userId(participantId)
                        .message("Group event '" + ge.getName() + "' confirmed for " +
                                date + " from " + startTime + " to " + endTime)
                        .status("NEW")
                        .createdAt(LocalDateTime.now())
                        .build();

                eventNotificationProducer.sendNotification(message);
            });


            return groupEventMapper.toGroupEventDto(ge);
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
