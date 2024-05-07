package com.id.px3.pipe.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.Instant;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PipePacket {

    private String packId;
    private String reqId;
    private String func;

    private Instant ts;
    private String sndr;
    private String rcpt;

    private Map<String, Object> payload;

}
