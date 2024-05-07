package com.id.px3.pipe.service;

import com.id.px3.pipe.model.PipePacket;
import com.id.px3.pipe.model.PipeRpcResult;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

@Service
@Slf4j
public class PipeRpcServer {

    private final PipeService pipeService;
    private final ConcurrentMap<String, Function<PipePacket, PipeRpcResult>> handlers = new ConcurrentHashMap<>();

    public PipeRpcServer(PipeService pipeService) {
        this.pipeService = pipeService;
    }

    /**
     * Register a function handler. Packets will be received on the 'function name'-rpc topic.
     * Responses will be sent to the sender on the same topic, resusing the request ID as a reference.
     *
     * @param funcName - function name
     * @param handler - function handler
     */
    public synchronized void serve(String funcName, Function<PipePacket, PipeRpcResult> handler) {
        // Validate input
        if (funcName == null || funcName.isBlank()) {
            throw new IllegalArgumentException("Function name must not be null or blank");
        }
        if (handler == null) {
            throw new IllegalArgumentException("Handler must not be null");
        }

        // Register the handler, allow for only one handler per function name
        if (handlers.get(funcName) != null) {
            throw new IllegalArgumentException("Handler for function %s already exists".formatted(funcName));
        }
        handlers.put(funcName, handler);

        // Listen to function calls and invoke handler
        String rpcRecipient = "%s-rpc".formatted(funcName);
        try {
            pipeService.registerReader(rpcRecipient, rpcRecipient, packet -> {

                // Handle request
                log.debug("Received packet for function %s".formatted(funcName));
                PipeRpcResult rpcResult;
                try {
                    rpcResult = handler.apply(packet);
                } catch (Exception e) {
                    log.error("Error processing packet for function %s - reqId %s".formatted(funcName, packet.getReqId()), e);
                    rpcResult = PipeRpcResult.error(e.getMessage());
                }

                // Send response
                pipeService.send(packet.getRcpt(), packet.getSndr(), rpcResult, funcName, packet.getReqId());
            });
        } catch (Exception e) {
            log.error("Error registering reader for function %s".formatted(funcName), e);
            throw e;
        }
    }

    @PreDestroy
    public void close() {
        handlers.clear();
    }

}
