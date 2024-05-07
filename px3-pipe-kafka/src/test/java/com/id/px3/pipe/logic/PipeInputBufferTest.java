package com.id.px3.pipe.logic;

import com.id.px3.pipe.model.PipePacket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PipeInputBufferTest {

    private PipeInputBuffer buffer;

    @BeforeEach
    void setUp() {
        buffer = new PipeInputBuffer(Duration.ofMinutes(5), 1); // Set age limit to 5 minutes and size limit to 1MB
    }

    @Test
    void testPushAndRetrievePacket() {
        String packId = UUID.randomUUID().toString();
        String reqId = UUID.randomUUID().toString();
        PipePacket packet = new PipePacket(packId, reqId, "compute", Instant.now(), "sender", "recipient", new HashMap<>());

        // Push packet to buffer
        buffer.push(packet);

        // Retrieve packet by reqId
        PipePacket retrievedPacket = buffer.peekByReqId(reqId);
        assertNotNull(retrievedPacket, "Packet should not be null after pushing.");
        assertEquals(packet, retrievedPacket, "Retrieved packet should be the same as the one pushed.");

        // Check packet count and total size
        assertEquals(1, buffer.getPacketCount(), "There should be exactly one packet in the buffer.");
        assertTrue(buffer.getTotalPacketSize() > 0, "Total packet size should be greater than 0.");
    }

    @Test
    void testPacketExpiration() {
        String packId = UUID.randomUUID().toString();
        String reqId = UUID.randomUUID().toString();
        PipePacket packet = new PipePacket(packId, reqId, "compute", Instant.now().minus(Duration.ofMinutes(10)), "sender", "recipient", new HashMap<>());

        // Push packet to buffer
        buffer.push(packet);

        // Force cleanup
        buffer.removeExpiredPackets();

        // Attempt to retrieve expired packet
        PipePacket expiredPacket = buffer.peekByReqId(reqId);
        assertNull(expiredPacket, "Expired packet should be removed from buffer.");

        // Verify packet count and size after removal
        assertEquals(0, buffer.getPacketCount(), "Buffer should be empty after removing expired packet.");
        assertEquals(0, buffer.getTotalPacketSize(), "Total packet size should be 0 after removing packets.");
    }

    @Test
    void testSizeLimitEnforcement() {
        String packId1 = UUID.randomUUID().toString();
        String reqId1 = UUID.randomUUID().toString();
        Map<String, Object> largePayload1 = new HashMap<>();
        // Fill the payload with large data to exceed buffer limits
        largePayload1.put("data", new String(new char[600000]).replace("\0", "a")); // Approximately 0.6MB of data

        PipePacket packet1 = new PipePacket(packId1, reqId1, "compute", Instant.now(), "sender", "recipient", largePayload1);

        String packId2 = UUID.randomUUID().toString();
        String reqId2 = UUID.randomUUID().toString();
        Map<String, Object> largePayload2 = new HashMap<>();
        // Fill the payload with large data to exceed buffer limits
        largePayload2.put("data", new String(new char[600000]).replace("\0", "b")); // Approximately 0.6MB of data

        PipePacket packet2 = new PipePacket(packId2, reqId2, "compute", Instant.now(), "sender", "recipient", largePayload2);

        // Push packets to buffer
        buffer.push(packet1);
        buffer.push(packet2);

        // Force size limit enforcement
        buffer.removeBySizeLimit();

        // Depending on the internal order of packet removal, one packet should be removed
        // This is to simulate realistic behavior where either packet could be the first to be removed based on internal mechanics.
        boolean packet1Exists = buffer.peekByReqId(reqId1) != null;
        boolean packet2Exists = buffer.peekByReqId(reqId2) != null;

        // Assert that exactly one of the packets still exists
        assertTrue(packet1Exists != packet2Exists, "Exactly one of the packets should remain after enforcing size limit.");
        assertEquals(1, buffer.getPacketCount(), "There should be exactly one packet remaining.");
        assertTrue(buffer.getTotalPacketSize() > 0 && buffer.getTotalPacketSize() <= 700000, "Total packet size should be reduced but non-zero.");
    }

    @Test
    void testRemovePacketById() {
        String packId = UUID.randomUUID().toString();
        String reqId = UUID.randomUUID().toString();
        PipePacket packet = new PipePacket(packId, reqId, "compute", Instant.now(), "sender", "recipient", new HashMap<>());

        // Push and then remove packet
        buffer.push(packet);
        buffer.removeByPackId(packId);

        // Attempt to retrieve the removed packet
        assertNull(buffer.peekByReqId(reqId), "Packet should be null after removal.");

        // Check packet count and total size after packet removal
        assertEquals(0, buffer.getPacketCount(), "Buffer should be empty after removing a packet.");
        assertEquals(0, buffer.getTotalPacketSize(), "Total packet size should be 0 after removing a packet.");
    }
}
