package com.thiagsilvadev.helpdesk.messaging.rabbitmq;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableRabbit
@EnableScheduling
public class RabbitMqConfig {

    public static final String NOTIFICATIONS_EXCHANGE = "helpdesk.notifications.exchange";
    public static final String NOTIFICATIONS_QUEUE = "helpdesk.notifications.requests";
    public static final String NOTIFICATIONS_ROUTING_KEY = "notifications.create";
    public static final String NOTIFICATIONS_DLX = "helpdesk.notifications.dlx";
    public static final String NOTIFICATIONS_DEAD_QUEUE = "helpdesk.notifications.dead";

    @Bean
    public DirectExchange notificationsExchange() {
        return ExchangeBuilder.directExchange(NOTIFICATIONS_EXCHANGE).durable(true).build();
    }

    @Bean
    public DirectExchange notificationsDeadLetterExchange() {
        return ExchangeBuilder.directExchange(NOTIFICATIONS_DLX).durable(true).build();
    }

    @Bean
    public Queue notificationsQueue() {
        return QueueBuilder.durable(NOTIFICATIONS_QUEUE)
                .deadLetterExchange(NOTIFICATIONS_DLX)
                .deadLetterRoutingKey(NOTIFICATIONS_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue notificationsDeadLetterQueue() {
        return QueueBuilder.durable(NOTIFICATIONS_DEAD_QUEUE).build();
    }

    @Bean
    public Binding notificationsBinding(Queue notificationsQueue, DirectExchange notificationsExchange) {
        return BindingBuilder.bind(notificationsQueue).to(notificationsExchange).with(NOTIFICATIONS_ROUTING_KEY);
    }

    @Bean
    public Binding notificationsDeadLetterBinding(Queue notificationsDeadLetterQueue,
                                                  DirectExchange notificationsDeadLetterExchange) {
        return BindingBuilder.bind(notificationsDeadLetterQueue)
                .to(notificationsDeadLetterExchange)
                .with(NOTIFICATIONS_ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
