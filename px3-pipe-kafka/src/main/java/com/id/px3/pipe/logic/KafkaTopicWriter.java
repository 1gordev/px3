package com.id.px3.pipe.logic;

import com.id.px3.pipe.config.KafkaProducerConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.Producer;

@Slf4j
public class KafkaTopicWriter {

    private final Producer<String, String> producer;
    private final String topic;

    public KafkaTopicWriter(String bootstrapServers, String topic) {
        KafkaProducerConfig producerConfig = new KafkaProducerConfig(bootstrapServers);
        this.producer = new KafkaProducer<>(producerConfig.getConfig());
        this.topic = topic;
    }

    public void write(String message) {
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, message);
        producer.send(record, (metadata, exception) -> {
            if (exception != null) {
                log.error("Error sending message to Kafka: %s".formatted(exception.getMessage()), exception);
            } else {
                log.debug("Message sent to topic %s partition %d at offset %d".formatted(metadata.topic(), metadata.partition(), metadata.offset()));
            }
        });
        int i = 1;
    }

    public void close() {
        producer.close();
    }
}
