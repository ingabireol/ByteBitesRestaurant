package olim.com.orderservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ Configuration for Order Service
 * 
 * Sets up exchanges, queues, and bindings for event-driven communication
 */
@Configuration
public class RabbitMQConfig {

    @Value("${bytebites.messaging.exchange}")
    private String exchange;

    @Value("${bytebites.messaging.queues.order-placed}")
    private String orderPlacedQueue;

    @Value("${bytebites.messaging.queues.order-status-changed}")
    private String orderStatusChangedQueue;

    @Value("${bytebites.messaging.queues.notification}")
    private String notificationQueue;

    @Value("${bytebites.messaging.routing-keys.order-placed}")
    private String orderPlacedRoutingKey;

    @Value("${bytebites.messaging.routing-keys.order-status-changed}")
    private String orderStatusChangedRoutingKey;

    @Value("${bytebites.messaging.routing-keys.notification}")
    private String notificationRoutingKey;

    /**
     * Main exchange for ByteBites events
     */
    @Bean
    public TopicExchange byteBitesExchange() {
        return new TopicExchange(exchange);
    }

    /**
     * Queue for order placed events
     */
    @Bean
    public Queue orderPlacedQueue() {
        return QueueBuilder.durable(orderPlacedQueue).build();
    }

    /**
     * Queue for order status change events
     */
    @Bean
    public Queue orderStatusChangedQueue() {
        return QueueBuilder.durable(orderStatusChangedQueue).build();
    }

    /**
     * Queue for notification events
     */
    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(notificationQueue).build();
    }

    /**
     * Binding for order placed events
     */
    @Bean
    public Binding orderPlacedBinding() {
        return BindingBuilder
                .bind(orderPlacedQueue())
                .to(byteBitesExchange())
                .with(orderPlacedRoutingKey);
    }

    /**
     * Binding for order status changed events
     */
    @Bean
    public Binding orderStatusChangedBinding() {
        return BindingBuilder
                .bind(orderStatusChangedQueue())
                .to(byteBitesExchange())
                .with(orderStatusChangedRoutingKey);
    }

    /**
     * Binding for notification events
     */
    @Bean
    public Binding notificationBinding() {
        return BindingBuilder
                .bind(notificationQueue())
                .to(byteBitesExchange())
                .with(notificationRoutingKey);
    }

    /**
     * JSON message converter for RabbitMQ
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * RabbitTemplate with JSON converter
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}