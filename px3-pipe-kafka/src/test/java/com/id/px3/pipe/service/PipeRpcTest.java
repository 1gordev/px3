package com.id.px3.pipe.service;

import com.id.px3.pipe.config.TestConfig;
import com.id.px3.pipe.model.PipePacket;
import com.id.px3.pipe.model.PipeRpcResult;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Order;
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

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {
        PipeRpcServer.class,
        PipeRpcClient.class,
        PipeService.class
})
@EnableAutoConfiguration
@Testcontainers
@Import(TestConfig.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PipeRpcTest {

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
    @Autowired
    private PipeRpcServer pipeRpcServer;
    @Autowired
    private PipeRpcClient pipeRpcClient;

    @AfterAll
    void tearDown() {
        pipeService.shutdown();
        kafkaContainer.stop();
    }

    @Test
    @Order(1)
    public void testSingleCallerSingleServer() throws Exception {
        // Setup the function name and server to handle the function
        String funcName = "compute";
        pipeRpcServer.serve(funcName, packet -> {
            // Simulate processing the RPC request
            Map<String, Object> params = packet.getPayload();
            return PipeRpcResult.ok(Map.of("processed", params.get("data")));
        });

        // Simulate the sender and recipient details
        String sender = "caller";

        // Create the parameters for the RPC call
        Map<String, Object> params = Map.of("data", "123");

        // Perform the RPC call and get the future result
        CompletableFuture<PipePacket> futureResult = pipeRpcClient.call(sender, funcName, params);

        // Await the future to get the response
        PipePacket responsePacket = futureResult.get(30, TimeUnit.SECONDS); // Timeout set to 30 seconds

        // Validate the response
        assertNotNull(responsePacket, "The response packet should not be null.");
        assertEquals(responsePacket.getPayload().get("processed"), params.get("data"), "The response content should match the processed result.");

        // Optionally, check other properties of the response packet
        assertEquals(funcName + "-rpc", responsePacket.getSndr(), "The sender in the response should match 'funcName'+'-rpc'.");
    }

    @Test
    @Order(2)
    public void testSingleCallerSingleServerWithDelayedResponse() throws Exception {
        // Setup the function name and a server to handle the function with a delay
        String funcName = "computeDelayed";
        CompletableFuture<Void> delay = new CompletableFuture<>();

        pipeRpcServer.serve(funcName, packet -> {
            // Delay the processing to trigger a retry from the client
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Delay interrupted or timed out", e);
            }
            Map<String, Object> params = packet.getPayload();
            return PipeRpcResult.ok(Map.of("processed", params.get("data")));
        });

        // Simulate the sender and recipient details
        String sender = "caller";

        // Create the parameters for the RPC call
        Map<String, Object> params = Map.of("data", "123");

        // Perform the RPC call and get the future result
        pipeRpcClient.setRpcTimeout(Duration.ofSeconds(3));
        CompletableFuture<PipePacket> futureResult = pipeRpcClient.call(sender, funcName, params);

        // Await the future to get the response
        PipePacket responsePacket = futureResult.get(10, TimeUnit.SECONDS); // Timeout set to 10 seconds to account for the delay

        // Validate the response
        assertNotNull(responsePacket, "The response packet should not be null.");
        assertEquals("123", responsePacket.getPayload().get("processed"), "The response content should match the processed result.");

        // Optionally, check other properties of the response packet
        assertEquals(funcName + "-rpc", responsePacket.getSndr(), "The sender in the response should match 'funcName'+'-rpc'.");
    }

    @Test
    @Order(3)
    public void testHighLoadWithLimitedCallers() throws Exception {
        int numberOfCalls = 1000;
        int numberOfCallers = 10;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfCallers);
        CountDownLatch latch = new CountDownLatch(numberOfCalls);

        // Setup multiple function handlers
        String fastFuncName = "fastCompute";
        pipeRpcServer.serve(fastFuncName, packet -> {
            Map<String, Object> params = packet.getPayload();
            System.out.println("Processing fast: " + params.get("data"));
            return PipeRpcResult.ok(Map.of("processedFast", params.get("data")));
        });

        String slowFuncName = "slowCompute";
        pipeRpcServer.serve(slowFuncName, packet -> {
            try {
                if (Math.random() < 0.1) { // 10% chance to delay
                    Thread.sleep(5000); // 5 seconds delay
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            Map<String, Object> params = packet.getPayload();
            System.out.println("Processing slow: " + params.get("data"));
            return PipeRpcResult.ok(Map.of("processedSlow", params.get("data")));
        });

        // Simulate multiple calls from a limited number of callers
        for (int i = 0; i < numberOfCalls; i++) {
            final String sender = "caller-" + (i % numberOfCallers);  // Reuse caller IDs
            final String funcName = (i % 2 == 0) ? fastFuncName : slowFuncName;
            final Map<String, Object> params = Map.of("data", "Data-" + i);

            executor.submit(() -> {
                try {
                    CompletableFuture<PipePacket> futureResult = pipeRpcClient.call(sender, funcName, params);
                    PipePacket responsePacket = futureResult.get(10, TimeUnit.SECONDS); // Allow sufficient time considering delays
                    assertNotNull(responsePacket, "Response should not be null for call from " + sender);
                    latch.countDown();
                } catch (Exception e) {
                    System.out.println("Failed RPC call for " + sender + " to function " + funcName);
                    e.printStackTrace();
                }
            });
        }

        // Await completion of all RPC calls
        assertTrue(latch.await(15, TimeUnit.MINUTES), "Not all RPC calls completed within the expected time.");

        executor.shutdownNow();
    }

}
