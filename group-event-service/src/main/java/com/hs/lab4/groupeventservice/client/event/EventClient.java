package com.hs.lab4.groupeventservice.client.event;

import com.hs.lab4.groupeventservice.client.config.FeignConfig;
import com.hs.lab4.groupeventservice.dto.responses.EventDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactivefeign.spring.config.ReactiveFeignClient;
import reactor.core.publisher.Flux;

import java.util.List;

@ReactiveFeignClient(name = "event-service", configuration = FeignConfig.class)
public interface EventClient {

    @GetMapping("/api/v1/event/busy")
    Flux<EventDto> getBusyEventsForUsersBetweenDates(
            @RequestParam("userIds") List<Long> userIds,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate
    );
}
