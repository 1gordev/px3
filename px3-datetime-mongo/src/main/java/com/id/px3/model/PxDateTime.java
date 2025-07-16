package com.id.px3.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PxDateTime {

    private Long utc;
    private Long offHH;
    private Long offMM;

}
