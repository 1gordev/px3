package com.id.px3.pipe.logic;

import com.id.px3.pipe.model.PipePacket;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Pipe input buffer
 */
public class PipeInputBuffer {

    private final Duration ageLimit;
    private final long sizeLimitMb;
    private final Map<String, PipePacket> packets = new LinkedHashMap<>();
    private final Map<String, String> reqIdIndex = new HashMap<>();
    private final TreeMap<Instant, String> tsIndex = new TreeMap<>();
    private final Map<String, Long> packetSize = new HashMap<>();

    public PipeInputBuffer(Duration ageLimit, long sizeLimitMb) {
        // Validate parameters
        if (ageLimit == null || ageLimit.isNegative() || ageLimit.isZero()) {
            throw new IllegalArgumentException("Age limit must be a positive duration");
        }
        if (sizeLimitMb <= 0) {
            throw new IllegalArgumentException("Size limit must be a positive number");
        }

        this.ageLimit = ageLimit;
        this.sizeLimitMb = sizeLimitMb;
    }

    /**
     * Push packet to the buffer
     *
     * @param packet - packet to push
     */
    public synchronized void push(PipePacket packet) {
        if (packet != null) {
            // Save the packet
            packets.put(packet.getPackId(), packet);

            // Index by reqId
            if (packet.getReqId() != null && !packet.getReqId().isBlank()) {
                reqIdIndex.put(packet.getReqId(), packet.getPackId());
            }

            // Index by timestamp
            tsIndex.put(packet.getTs(), packet.getPackId());

            // Calculate packets size
            long packetSize = packet.toString().length();
            this.packetSize.put(packet.getPackId(), packetSize);

            // Remove by age
            removeExpiredPackets();
            // remove by size
            removeBySizeLimit();
        }
    }

    /**
     * Peek packet by reqId
     *
     * @param reqId - request ID
     * @return packet
     */
    public synchronized PipePacket peekByReqId(String reqId) {
        String packId = reqIdIndex.get(reqId);
        return packId != null ? packets.get(packId) : null;
    }

    /**
     * Peek packet by packId
     *
     * @param packId - packet ID
     * @return packet
     */
    public synchronized PipePacket peekByPackId(String packId) {
        return packets.get(packId);
    }

    /**
     * Remove packet by packId
     *
     * @param packId - packet ID
     * @return removed packet
     */
    public synchronized PipePacket removeByPackId(String packId) {
        PipePacket packet = packets.remove(packId);
        if (packet != null) {
            reqIdIndex.remove(packet.getReqId());
            tsIndex.remove(packet.getTs());
            packetSize.remove(packId);
        }
        return packet;
    }

    /**
     * Get packet count
     * @return packet count
     */
    public synchronized Integer getPacketCount() {
        return packets.size();
    }

    /**
     * Get total packet size
     * @return total packet size
     */
    public synchronized Long getTotalPacketSize() {
        return packetSize.values().stream().mapToLong(Long::longValue).sum();
    }


    protected void removeBySizeLimit() {
        // Remove packets starting from the oldest one until the total size is below the limit
        long sizeLimit = sizeLimitMb * 1024 * 1024;
        long totalSize = packetSize.values().stream().mapToLong(Long::longValue).sum();
        while (totalSize > sizeLimit) {
            String packId = tsIndex.remove(tsIndex.firstKey());
            PipePacket packet = packets.remove(packId);
            if (packet != null) {
                reqIdIndex.remove(packet.getReqId());
                totalSize -= packetSize.get(packId);
                packetSize.remove(packId);
            }
        }
    }

    protected void removeExpiredPackets() {
        // Remove packets older than the age limit
        Instant now = Instant.now();
        Instant limit = now.minus(ageLimit);
        while (!tsIndex.isEmpty() && tsIndex.firstKey().isBefore(limit)) {
            String packId = tsIndex.remove(tsIndex.firstKey());
            PipePacket packet = packets.remove(packId);
            if (packet != null) {
                reqIdIndex.remove(packet.getReqId());
                tsIndex.remove(packet.getTs());
                packetSize.remove(packId);
            }
        }
    }

}
