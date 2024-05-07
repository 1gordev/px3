package com.id.px3.pipe.logic;

import com.id.px3.pipe.config.KafkaConsumerConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@Slf4j
public class KafkaTopicReader {

    private final KafkaConsumer<String, String> consumer;
    private final ExecutorService executorService;

    public KafkaTopicReader(String bootstrapServers, String groupId, String topic, Consumer<ConsumerRecord<String, String>> listener) {
        KafkaConsumerConfig consumerConfig = new KafkaConsumerConfig(bootstrapServers, groupId);
        this.consumer = new KafkaConsumer<>(consumerConfig.getConfig());
        this.consumer.subscribe(Collections.singletonList(topic));
        this.executorService = Executors.newSingleThreadExecutor();

        executorService.submit(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    consumer.poll(Duration.ofMillis(100)).forEach(listener);
                    consumer.commitAsync();
                }
            } catch (Exception e) {
                log.error("Error in consuming messages: %s".formatted(e.getMessage()), e);
            } finally {
                consumer.close();
            }
        });
    }

    public void close() {
        executorService.shutdown();

        // This will cause consumer.poll() to throw WakeupException
        consumer.wakeup();
    }
}
