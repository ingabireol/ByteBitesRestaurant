package olim.com.notificationservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ Configuration for Notification Service
 * 
 * Sets up the same exchanges and queues as Order Service for event consumption
 */
@Configuration
public class RabbitMQConfig {

    @Value("${bytebites.messaging.exchange}")
    private String exchange;

    @Value("${bytebites.messaging.queues.order-placed}")
    private String orderPlacedQueue;

    @Value("${bytebites.messaging.queues.order-status-changed}")
    private String orderStatusChangedQueue;

    @Value("${bytebites.messaging.routing-keys.order-placed}")
    private String orderPlacedRoutingKey;

    @Value("${bytebites.messaging.routing-keys.order-status-changed}")
    private String orderStatusChangedRoutingKey;

    /**
     * Main exchange for ByteBites events (should already exist from Order Service)
     */
    @Bean
    public TopicExchange byteBitesExchange() {
        return new TopicExchange(exchange);
    }

    /**
     * Queue for order placed events (should already exist from Order Service)
     */
    @Bean
    public Queue orderPlacedQueue() {
        return QueueBuilder.durable(orderPlacedQueue).build();
    }

    /**
     * Queue for order status change events (should already exist from Order Service)
     */
    @Bean
    public Queue orderStatusChangedQueue() {
        return QueueBuilder.durable(orderStatusChangedQueue).build();
    }

    /**
     * Binding for order placed events (should already exist from Order Service)
     */
    @Bean
    public Binding orderPlacedBinding() {
        return BindingBuilder
                .bind(orderPlacedQueue())
                .to(byteBitesExchange())
                .with(orderPlacedRoutingKey);
    }

    /**
     * Binding for order status changed events (should already exist from Order Service)
     */
    @Bean
    public Binding orderStatusChangedBinding() {
        return BindingBuilder
                .bind(orderStatusChangedQueue())
                .to(byteBitesExchange())
                .with(orderStatusChangedRoutingKey);
    }

    /**
     * JSON message converter for RabbitMQ
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * RabbitTemplate with JSON converter (for potential message sending)
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}