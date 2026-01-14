package com.hs.lab4.groupeventservice.service;

import com.hs.lab4.groupeventservice.dto.responses.UserDto;
import com.hs.lab4.groupeventservice.entity.GroupEvent;
import com.hs.lab4.groupeventservice.enums.GroupEventStatus;
import com.hs.lab4.groupeventservice.exceptions.EventNotFoundException;
import com.hs.lab4.groupeventservice.repository.GroupEventRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupEventService {
    private final GroupEventRepository groupEventRepository;
    private final UserClientService userClientService;

    public Flux<GroupEvent> getAllGroupEvents() {
        return Mono.fromCallable(groupEventRepository::findAllWithParticipants).subscribeOn(Schedulers.boundedElastic()).flatMapMany(Flux::fromIterable);
    }

    //TODO поправить метод чтобы не вызывался fallback клиента
    @Transactional
    public Mono<GroupEvent> addGroupEvent(String name,
                                          String description,
                                          List<Long> participantsIds,
                                          Long ownerId
    ) {
        return userClientService.getUserById(ownerId)
                .flatMap(owner ->
                        Flux.fromIterable(participantsIds)
//                                .flatMap(userClientService::getUserById, 1)
//                                .map(UserDto::id)
                                .collectList()
                                .flatMap(participants ->
                                        Mono.fromCallable(() -> {
                                            GroupEvent groupEvent = GroupEvent.builder()
                                                    .name(name)
                                                    .description(description)
                                                    .participantIds(participantsIds)
                                                    .ownerId(ownerId)
                                                    .status(GroupEventStatus.PENDING)
                                                    .build();

                                            return groupEventRepository.save(groupEvent);
                                        }).subscribeOn(Schedulers.boundedElastic())
                                )
                );
    }
    public Mono<GroupEvent> getGroupEventById(Long id) {
        return Mono.fromCallable(() -> groupEventRepository.findByIdWithParticipants(id).orElseThrow(() -> new EventNotFoundException("Group event with id = " + id + " not found")))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Void> deleteGroupEventById(Long id) {
        return Mono.fromRunnable(() -> {
            if (!groupEventRepository.existsById(id))
                throw new EventNotFoundException("Group event with id = " + id + " not found");
            groupEventRepository.deleteById(id);
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    public Flux<GroupEvent> getAllGroupEventsByOwnerId(Long id) {
        return Mono.fromCallable(() -> groupEventRepository.findAllByOwnerId(id))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable);
    }

    public Mono<GroupEvent> getGroupEventByIdAndOwnerId(Long id, Long ownerId) {
        return Mono.fromCallable(() -> groupEventRepository.findByIdAndOwnerId(id, ownerId).orElseThrow(() -> new EventNotFoundException("Group event with id = " + id + " not found")))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Void> deleteGroupEventByOwnerId(Long id, Long ownerId) {
        return Mono.fromRunnable(() -> {
            if (!groupEventRepository.existsByIdAndOwnerId(id, ownerId))
                throw new EventNotFoundException("Group event with id = " + id + " not found");
            groupEventRepository.deleteById(id);
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }
}

