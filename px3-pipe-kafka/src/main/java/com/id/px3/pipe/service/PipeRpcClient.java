package com.id.px3.pipe.service;

import com.id.px3.pipe.logic.PipeInputBuffer;
import com.id.px3.pipe.model.PipePacket;
import com.id.px3.utils.DurationParser;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

@Service
@Slf4j
public class PipeRpcClient {

    private final PipeService pipeService;
    @Setter
    private Duration rpcTimeout;
    private final PipeInputBuffer responseBuffer;
    private final Integer rpcRetries;


    public PipeRpcClient(PipeService pipeService,
                         @Value("${px3.kafka-pipe.rpc-timeout:30s}") String rpcTimeout,
                         @Value("${px3.kafka-pipe.rpc-retries:3}") Integer rpcRetries,
                         @Value("${px3.kafka-pipe.rpc-input-buffer-max-age:10m}") String rpcInputBufferMaxAge,
                         @Value("${px3.kafka-pipe.rpc-input-buffer-max-size-mb:50}") Long rpcInputBufferMaxSizeMb) {
        this.pipeService = pipeService;
        this.rpcTimeout = DurationParser.parse(rpcTimeout);
        this.rpcRetries = rpcRetries;
        this.responseBuffer = new PipeInputBuffer(
                DurationParser.parse(rpcInputBufferMaxAge),
                rpcInputBufferMaxSizeMb);
    }

    @Async
    public CompletableFuture<PipePacket> call(String sender, String funcName, Map<String, Object> params) {
        // Validate input
        if (sender == null || funcName == null || sender.isBlank() || funcName.isBlank()) {
            throw new IllegalArgumentException("Sender and function name must not be null or blank");
        }
        // Allow for void params
        if (params == null) {
            params = Map.of();
        }

        CompletableFuture<PipePacket> future = new CompletableFuture<>();
        String rpcSender = "%s-rpc".formatted(sender);
        String rpcRecipient = "%s-rpc".formatted(funcName);
        String reqId = "%s-%d".formatted(UUID.randomUUID().toString(), System.currentTimeMillis());

        try {
            PipePacket sentPacket = pipeService.send(rpcSender, rpcRecipient, params, funcName, reqId);
            if (sentPacket == null) {
                throw new IllegalStateException("Failed to send RPC request");
            }

            // Register reader to receive response
            // User rpcSender as reader to avoid conflicts with other listeners
            try {
                pipeService.registerReader(rpcSender, rpcSender, responseBuffer::push);
            } catch (IllegalStateException ignored) {
                // Listener already exists - reuse it
            } catch (Exception e) {
                throw new IllegalStateException("Failed to register listener for RPC response", e);
            }

            // Wait for response with retries
            for (int attempt = 0; attempt < rpcRetries; attempt++) {
                try {
                    PipePacket response = awaitResponse(reqId, rpcTimeout);
                    future.complete(response);
                    return future;
                } catch (TimeoutException e) {
                    log.warn("Timeout waiting for response on attempt %d".formatted(attempt + 1));

                    // Retry sending if necessary or you can also choose to retry waiting for the response
                    pipeService.send(rpcSender, rpcRecipient, params, funcName, reqId);
                }
            }

            throw new TimeoutException("RPC call did not succeed after " + rpcRetries + " attempts");
        } catch (Exception e) {
            future.completeExceptionally(e);
        }
        return future;
    }

    private PipePacket awaitResponse(String reqId, Duration timeout) throws InterruptedException, TimeoutException {
        long waitUntil = System.currentTimeMillis() + timeout.toMillis();
        while (System.currentTimeMillis() < waitUntil) {
            PipePacket packet = responseBuffer.peekByReqId(reqId);
            if (packet != null) {
                responseBuffer.removeByPackId(packet.getPackId());
                return packet;
            }
            // Short delay to avoid busy waiting
            Thread.sleep(10);
        }
        throw new TimeoutException("Timeout while waiting for RPC response");
    }
}
