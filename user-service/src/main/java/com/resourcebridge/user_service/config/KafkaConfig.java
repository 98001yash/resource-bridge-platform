package com.resourcebridge.user_service.config;

import com.resourcebridge.events.AdminVerificationEvent;
import com.resourcebridge.events.UserEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfig {

    /* ================= BASE CONFIG ================= */

    private Map<String, Object> baseProps() {

        Map<String, Object> props = new HashMap<>();

        props.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                "localhost:9092"
        );

        props.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                "user-service"
        );

        props.put(
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class
        );

        props.put(
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "earliest"
        );

        return props;
    }


    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UserEvent>
    userEventFactory() {

        JsonDeserializer<UserEvent> deserializer =
                new JsonDeserializer<>(UserEvent.class);


        deserializer.addTrustedPackages(
                "com.resourcebridge.events"
        );

        deserializer.setRemoveTypeHeaders(true);
        deserializer.setUseTypeMapperForKey(false);

        DefaultKafkaConsumerFactory<String, UserEvent> factory =
                new DefaultKafkaConsumerFactory<>(
                        baseProps(),
                        new StringDeserializer(),
                        deserializer
                );

        ConcurrentKafkaListenerContainerFactory<String, UserEvent> container =
                new ConcurrentKafkaListenerContainerFactory<>();

        container.setConsumerFactory(factory);

        container.setConcurrency(3);

        return container;
    }


    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, AdminVerificationEvent>
    userVerifiedFactory() {

        JsonDeserializer<AdminVerificationEvent> deserializer =
                new JsonDeserializer<>(AdminVerificationEvent.class);

        deserializer.addTrustedPackages(
                "com.resourcebridge.events"
        );

        deserializer.setRemoveTypeHeaders(true);
        deserializer.setUseTypeMapperForKey(false);

        DefaultKafkaConsumerFactory<String, AdminVerificationEvent> factory =
                new DefaultKafkaConsumerFactory<>(
                        baseProps(),
                        new StringDeserializer(),
                        deserializer
                );

        ConcurrentKafkaListenerContainerFactory<String, AdminVerificationEvent> container =
                new ConcurrentKafkaListenerContainerFactory<>();

        container.setConsumerFactory(factory);
        container.setConcurrency(2);

        return container;
    }
}
