package com.hs.lab4.eventservice.mapper;

import com.hs.lab4.eventservice.dto.responses.EventDto;
import com.hs.lab4.eventservice.entity.Event;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface EventMapper {
    EventDto toEventDto(Event Event);
    List<EventDto> toEventDtoList(List<Event> events);
}
