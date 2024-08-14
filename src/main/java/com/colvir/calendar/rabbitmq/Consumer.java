package com.colvir.calendar.rabbitmq;

import com.colvir.calendar.config.RabbitConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Component
@RequiredArgsConstructor
public class Consumer {

    private final Logger logger = Logger.getLogger(Consumer.class.getName());

    private final RabbitConfig rabbitConfig;

    private void logMessage(String queueName, String message) {
        logger.info(String.format("RabbitMQ. Received message. Queue: %s. Message: %s", queueName, message));
    }

    // Очередь с информационными сообщениями при обработке исходного календаря
    @RabbitListener(queues = "${rabbitmq.queue.originalInfo}")
    public void listenOriginalInfo(String message) {
        logMessage(rabbitConfig.getQueueOriginalInfo(), message);
    }

    // Очередь с сообщениями по ошибкам при обработке исходного календаря
    @RabbitListener(queues = "${rabbitmq.queue.originalError}")
    public void listenOriginalError(String message) {
        logMessage(rabbitConfig.getQueueOriginalError(), message);
    }

    // Очередь с информационными сообщениями при обработке месяцев календаря
    @RabbitListener(queues = "${rabbitmq.queue.finalMonthsInfo}")
    public void listenFinalMonthsInfo(String message) {
        logMessage(rabbitConfig.getQueueFinalMonthsInfo(), message);
    }

    // Очередь с сообщениями по ошибкам при обработке месяцев календаря
    @RabbitListener(queues = "${rabbitmq.queue.finalMonthsError}")
    public void listenFinalMonthsError(String message) {
        logMessage(rabbitConfig.getQueueFinalMonthsError(), message);
    }
}
