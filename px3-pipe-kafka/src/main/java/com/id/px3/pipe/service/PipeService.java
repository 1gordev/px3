package com.id.px3.pipe.service;

import com.google.gson.Gson;
import com.id.px3.pipe.logic.KafkaTopicReader;
import com.id.px3.pipe.logic.KafkaTopicWriter;
import com.id.px3.pipe.model.PipePacket;
import com.id.px3.utils.DurationParser;
import com.id.px3.utils.json.JsonUtils;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.function.Consumer;

@Service
@Slf4j
public class PipeService {

    private final ConcurrentMap<String, KafkaTopicWriter> writers = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, KafkaTopicReader> readers = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, ScheduledFuture<?>> removalTasks = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final ExecutorService listenerExecutorService = Executors.newCachedThreadPool();
    private final Duration writerLife;
    private final Gson gson;

    @Value("${px3.kafka-pipe.bootstrap-servers:localhost:29092}")
    private String bootstrapServers;

    public PipeService(@Value("${px3.kafka-pipe.writer-life:60s}") String writerLife) {
        this.writerLife = DurationParser.parse(writerLife);
        this.gson = JsonUtils.newGson();
    }

    /**
     * Send a message to a recipient
     *
     * @param sender - sender of the message
     * @param recipient - recipient of the message
     * @param payload - message payload
     * @param funcName - function name
     * @param reqId - request ID
     *
     * @return the packet sent
     */
    public PipePacket send(String sender, String recipient, Map<String, Object> payload, String funcName, String reqId) {
        // Validate input
        if (sender == null || recipient == null || sender.isBlank() || recipient.isBlank()) {
            throw new IllegalArgumentException("Sender, recipient, and message must not be null or blank");
        }
        // Allow for void payloads
        if (payload == null || payload.isEmpty()) {
            payload = Map.of();
        }
        // If there is a function name, it must not be blank and request ID must be provided
        if (funcName != null && (funcName.isBlank() || reqId == null || reqId.isBlank())) {
            throw new IllegalArgumentException("Function name must not be blank and request ID must be provided");
        }

        // Write and return packet
        try {
            KafkaTopicWriter writer = writers.computeIfAbsent(recipient, this::createWriter);
            PipePacket packet = createPacket(sender, recipient, payload, funcName, reqId);
            writer.write(gson.toJson(packet));
            rescheduleRemoval(recipient, writer);
            return packet;
        } catch (Exception e) {
            log.error("Failed to send message: %s".formatted(e.getMessage()), e);
        }
        return null;
    }

    /**
     * Listen for messages sent to a recipient
     *
     * @param readerId - reader ID
     * @param recipient - recipient to listen for
     * @param listener - listener to register
     */
    public synchronized void registerReader(String readerId, String recipient, Consumer<PipePacket> listener) {
        // Check if the listener is already registered
        if (readers.containsKey(readerId)) {
            throw new IllegalStateException("Listener already registered for recipient: " + recipient);
        } else {
            readers.computeIfAbsent(readerId, k ->
                    new KafkaTopicReader(bootstrapServers, recipient + "-group", recipient, record -> {
                        // Process record in a separate thread managed by listenerExecutorService
                        listenerExecutorService.submit(() -> {
                            try {
                                PipePacket packet = gson.fromJson(record.value(), PipePacket.class);
                                listener.accept(packet);  // This is now explicitly offloaded to a separate thread
                            } catch (Exception e) {
                                log.error("Error processing Kafka message: %s".formatted(e.getMessage()), e);
                            }
                        });
                    }));
        }
    }

    /**
     * Stop listening for messages sent to a recipient
     *
     * @param readerId - reader ID
     */
    public synchronized void unregisterReader(String readerId) {
        KafkaTopicReader reader = readers.remove(readerId);
        if (reader != null) {
            reader.close();
        }
    }

    private PipePacket createPacket(String sndr, String rcpt, Map<String, Object> payload, String funcName, String reqId) {
        Instant now = Instant.now();
        String id = "%s-%d".formatted(UUID.randomUUID().toString(), now.toEpochMilli());
        return new PipePacket(id, reqId, funcName, now, sndr, rcpt, payload);
    }

    private KafkaTopicWriter createWriter(String topic) {
        log.debug("Creating new writer for topic: %s".formatted(topic));
        return new KafkaTopicWriter(bootstrapServers, topic);
    }

    private void rescheduleRemoval(String key, KafkaTopicWriter writer) {
        // Cancel existing removal task
        ScheduledFuture<?> existingTask = removalTasks.get(key);
        if (existingTask != null && !existingTask.isDone()) {
            existingTask.cancel(false);
        }

        // Reschedule removal task
        removalTasks.put(key, scheduler.schedule(() -> {
            log.debug("Removing writer for topic: %s".formatted(key));
            writers.remove(key);
            writer.close();
            removalTasks.remove(key);
        }, writerLife.toMillis(), TimeUnit.MILLISECONDS));
    }

    @PreDestroy
    public void shutdown() {
        try {
            scheduler.shutdownNow();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    log.error("Scheduler did not terminate in the expected time.");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            writers.values().forEach(KafkaTopicWriter::close);
            readers.values().forEach(KafkaTopicReader::close);
            writers.clear();
            readers.clear();
        } catch (Exception e) {
            log.error("Error during shutdown: %s".formatted(e.getMessage()), e);
        }
    }

}
