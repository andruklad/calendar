package com.colvir.calendar.config;


import lombok.Getter;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class RabbitConfig {

    // Название точки обмена
    @Value("${rabbitmq.exchange}")
    private String exchange;

    // Названия очередей
    @Value("${rabbitmq.queue.originalInfo}")
    private String queueOriginalInfo;

    @Value("${rabbitmq.queue.originalError}")
    private String queueOriginalError;

    @Value("${rabbitmq.queue.finalMonthsInfo}")
    private String queueFinalMonthsInfo;

    @Value("${rabbitmq.queue.finalMonthsError}")
    private String queueFinalMonthsError;

    // Названия ключей маршрутизации
    @Value("${rabbitmq.routing.originalInfoKey}")
    private String routingOriginalInfoKey;

    @Value("${rabbitmq.routing.originalErrorKey}")
    private String routingOriginalErrorKey;

    @Value("${rabbitmq.routing.finalMonthsInfoKey}")
    private String routingFinalMonthsInfoKey;

    @Value("${rabbitmq.routing.finalMonthsErrorKey}")
    private String routingFinalMonthsErrorKey;

    // Очереди
    // Очередь информационных сообщений при загрузке исходного календаря
    @Bean
    public Queue originalInfoQueue() {
        return new Queue(queueOriginalInfo);
    }

    // Очередь сообщений по ошибкам при загрузке исходного календаря
    @Bean
    public Queue originalErrorQueue() {
        return new Queue(queueOriginalError);
    }

    // Очередь информационных сообщений при загрузке месяцев итогового календаря
    @Bean
    public Queue finalMonthsInfoQueue() {
        return new Queue(queueFinalMonthsInfo);
    }

    // Очередь сообщений по ошибкам при загрузке месяцев итогового календаря
    @Bean
    public Queue finalMonthsErrorQueue() {
        return new Queue(queueFinalMonthsError);
    }

    // Точка обмена
    @Bean
    public TopicExchange exchange(){
        return new TopicExchange(exchange);
    }

    // Привязки (соответствия ключей маршрутизации и очередей)
    @Bean
    public Binding bindingOriginalInfoQueue(){
        return BindingBuilder
                .bind(originalInfoQueue())
                .to(exchange())
                .with(routingOriginalInfoKey);
    }

    @Bean
    public Binding bindingOriginalErrorQueue(){
        return BindingBuilder
                .bind(originalErrorQueue())
                .to(exchange())
                .with(routingOriginalErrorKey);
    }

    @Bean
    public Binding bindingFinalMonthsInfoQueue(){
        return BindingBuilder
                .bind(finalMonthsInfoQueue())
                .to(exchange())
                .with(routingFinalMonthsInfoKey);
    }

    @Bean
    public Binding bindingFinalMonthsErrorQueue(){
        return BindingBuilder
                .bind(finalMonthsErrorQueue())
                .to(exchange())
                .with(routingFinalMonthsErrorKey);
    }

    // Соединение с брокером RabbitMQ
    @Bean
    public ConnectionFactory connectionFactory() {
        return new CachingConnectionFactory("localhost");
    }

    // Шаблон для отправки сообщений
    @Bean
    public RabbitTemplate rabbitTemplate() {
        return new RabbitTemplate(connectionFactory());
    }
}
