package com.id.px3.pipe.service;

import static org.junit.jupiter.api.Assertions.*;

import com.id.px3.pipe.config.TestConfig;
import com.id.px3.pipe.model.PipePacket;
import com.id.px3.utils.SafeConvert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@SpringBootTest(classes = PipeService.class)
@EnableAutoConfiguration
@Testcontainers
@Import(TestConfig.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PipeServiceTest {

    @Container
    public static KafkaContainer kafkaContainer = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:latest"));


    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        kafkaContainer.start();
        registry.add("px3.kafka-pipe.bootstrap-servers", kafkaContainer::getBootstrapServers);
    }

    @Autowired
    private PipeService pipeService;

    @AfterEach
    void cleanup() {
        pipeService.unregisterReader("testreader");
    }

    @AfterAll
    void tearDown() {
        pipeService.shutdown();
        kafkaContainer.stop();
    }

    @Test
    void testSendAndReceive() throws InterruptedException {
        String testTopic = "test-topic";
        Map<String, Object> payload = Map.of("Hello", "Pipes");
        String sender = "sender";
        String recipient = testTopic; // recipient is the topic name for simplicity

        CountDownLatch latch = new CountDownLatch(1);

        pipeService.registerReader("testreader", recipient, packet -> {
            System.out.println("Received: " + packet.getPayload());
            latch.countDown();
        });

        // Send a message
        PipePacket sentPacket = pipeService.send(sender, recipient, payload, null, null);
        assertNotNull(sentPacket, "Sent packet should not be null");

        // Await for the listener to process the received message
        boolean awaitResult = latch.await(10, TimeUnit.SECONDS);

        // Assert that the message was processed within the timeout
        assertTrue(awaitResult, "Message was not processed within 5 seconds");
    }

    @Test
    void testSendAndReceiveMultipleMessages() throws InterruptedException {
        String testTopic = "multi-message-test-topic";
        String sender = "sender";
        String recipient = testTopic; // Using the topic name as the recipient for simplicity
        int numberOfMessages = 250;

        CountDownLatch latch = new CountDownLatch(numberOfMessages);
        List<Long> latencies = new ArrayList<>();

        // Setup the listener
        pipeService.registerReader("testreader", recipient, packet -> {
            long receiveTime = System.currentTimeMillis();
            long sendTime = SafeConvert.toLong(packet.getPayload().get("ts")).orElse(0L);
            long latency = receiveTime - sendTime;
            latencies.add(latency);
            System.out.printf("Received: %d with latency: %d ms%n", sendTime, latency);
            latch.countDown();
        });

        // Give the listener a moment to fully initialize
        Thread.sleep(10000);

        // Send multiple messages
        for (int i = 0; i < numberOfMessages; i++) {
            long sendTime = System.currentTimeMillis();
            Map<String, Object> payload = Map.of("ts", sendTime);
            PipePacket sentPacket = pipeService.send(sender, recipient, payload, null, null);
            Thread.sleep(10);
        }

        // Await for the listener to process all received messages
        boolean awaitResult = latch.await(60, TimeUnit.SECONDS);

        // Calculate average latency
        long totalLatency = latencies.stream().mapToLong(Long::longValue).sum();
        long averageLatency = totalLatency / numberOfMessages;

        // Assert that all messages were processed within the timeout
        assertTrue(awaitResult, "Not all messages were processed within 60 seconds");
        System.out.println("Average latency: " + averageLatency + " ms");
    }
}
