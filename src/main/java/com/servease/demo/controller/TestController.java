package com.servease.demo.controller;

import com.servease.demo.service.event.OrderFullyPaidEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dev/")
public class TestController {
    private final ApplicationEventPublisher publisher;

    @PostMapping("/publish-event")
    public void publishEvent(
            @RequestParam Long orderId
    ) {
        var event = new OrderFullyPaidEvent(orderId);
        publisher.publishEvent(event);
    }
}
