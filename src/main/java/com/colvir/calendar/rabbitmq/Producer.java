package com.colvir.calendar.rabbitmq;

import com.colvir.calendar.config.RabbitConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class Producer {

    private final RabbitTemplate rabbitTemplate;

    private final RabbitConfig rabbitConfig;

    public void sendMessage(String routingKey, String text) {

        rabbitTemplate.convertAndSend(rabbitConfig.getExchange(), routingKey, String.format("%s %s", LocalDateTime.now(), text));
    }
}
