package com.hs.lab4.groupeventservice.service;

import com.hs.lab4.groupeventservice.dto.responses.GroupEventDto;
import com.hs.lab4.groupeventservice.dto.responses.TimeInterval;
import com.hs.lab4.groupeventservice.entity.GroupEvent;
import com.hs.lab4.groupeventservice.enums.GroupEventStatus;
import com.hs.lab4.groupeventservice.exceptions.EventNotFoundException;
import com.hs.lab4.groupeventservice.exceptions.NoAvailableSlotsException;
import com.hs.lab4.groupeventservice.mapper.GroupEventMapper;
import com.hs.lab4.groupeventservice.repository.GroupEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    private GroupEventRepository groupEventRepository;

    @Mock
    private GroupEventMapper groupEventMapper;

    @Mock
    private EventClientService eventClientService;

    @Mock
    private GroupEventLoader loader;

    @InjectMocks
    private RecommendationService recommendationService;

    private GroupEvent existing;

    @BeforeEach
    void setUp() {
        existing = GroupEvent.builder()
                .id(1L)
                .name("Test Group Event")
                .participantIds(List.of(2L, 3L))
                .ownerId(1L)
                .status(GroupEventStatus.PENDING)
                .build();
    }

    @Test
    void recommendSlots_success_returns1to5() {
        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = start.plusDays(3);
        Duration duration = Duration.ofHours(1);

        when(loader.loadParticipantIds(1L)).thenReturn(List.of(2L, 3L));
        when(eventClientService.getBusyEventsForUsersBetweenDates(eq(List.of(2L, 3L)), eq(start.toString()), eq(end.toString())))
                .thenReturn(Flux.empty());

        StepVerifier.create(recommendationService.recommendSlots(start, end, duration, 1L).collectList())
                .assertNext(list -> {
                    org.assertj.core.api.Assertions.assertThat(list).isNotEmpty();
                    org.assertj.core.api.Assertions.assertThat(list.size()).isBetween(1, 5);
                })
                .verifyComplete();

        verify(loader).loadParticipantIds(1L);
        verify(eventClientService).getBusyEventsForUsersBetweenDates(eq(List.of(2L, 3L)), eq(start.toString()), eq(end.toString()));
    }


    @Test
    void recommendSlots_noAvailableSlots_throws() {
        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = start;
        Duration duration = Duration.ofHours(24);

        when(loader.loadParticipantIds(1L)).thenReturn(List.of(2L, 3L));
        when(eventClientService.getBusyEventsForUsersBetweenDates(eq(List.of(2L, 3L)), eq(start.toString()), eq(end.toString())))
                .thenReturn(Flux.just(new TimeInterval(start, LocalTime.of(0, 0), LocalTime.of(23, 59))));

        StepVerifier.create(recommendationService.recommendSlots(start, end, duration, 1L))
                .expectError(NoAvailableSlotsException.class)
                .verify();

        verify(loader).loadParticipantIds(1L);
        verify(eventClientService).getBusyEventsForUsersBetweenDates(eq(List.of(2L, 3L)), eq(start.toString()), eq(end.toString()));
    }

    @Test
    void bookSlot_success_updatesAndMaps() {
        LocalDate date = LocalDate.now().plusDays(1);
        LocalTime startTime = LocalTime.of(10, 0);
        LocalTime endTime = LocalTime.of(11, 0);

        GroupEventDto dto = new GroupEventDto(
                1L,
                "Test Group Event",
                null,
                date,
                startTime,
                endTime,
                List.of(2L, 3L),
                1L,
                GroupEventStatus.CONFIRMED
        );

        when(groupEventRepository.findByIdWithParticipants(1L)).thenReturn(Optional.of(existing));
        when(groupEventMapper.toGroupEventDto(any(GroupEvent.class))).thenReturn(dto);
        when(groupEventRepository.save(any(GroupEvent.class))).thenAnswer(inv -> inv.getArgument(0));

        StepVerifier.create(recommendationService.bookSlot(1L, date, startTime, endTime))
                .expectNextMatches(r ->
                        r.status() == GroupEventStatus.CONFIRMED
                                && date.equals(r.date())
                                && startTime.equals(r.startTime())
                                && endTime.equals(r.endTime()))
                .verifyComplete();

        ArgumentCaptor<GroupEvent> captor = ArgumentCaptor.forClass(GroupEvent.class);
        verify(groupEventRepository).save(captor.capture());
        GroupEvent saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(GroupEventStatus.CONFIRMED);
        assertThat(saved.getDate()).isEqualTo(date);
        assertThat(saved.getStartTime()).isEqualTo(startTime);
        assertThat(saved.getEndTime()).isEqualTo(endTime);

        verify(groupEventRepository).findByIdWithParticipants(1L);
        verify(groupEventMapper).toGroupEventDto(any(GroupEvent.class));
    }

    @Test
    void bookSlot_notFound_throws() {
        when(groupEventRepository.findByIdWithParticipants(1L)).thenReturn(Optional.empty());

        StepVerifier.create(recommendationService.bookSlot(1L, LocalDate.now().plusDays(1), LocalTime.of(10, 0), LocalTime.of(11, 0)))
                .expectError(EventNotFoundException.class)
                .verify();

        verify(groupEventRepository).findByIdWithParticipants(1L);
        verify(groupEventRepository, never()).save(any());
        verifyNoInteractions(groupEventMapper);
        verifyNoInteractions(eventClientService);
        verifyNoInteractions(loader);
    }
}
