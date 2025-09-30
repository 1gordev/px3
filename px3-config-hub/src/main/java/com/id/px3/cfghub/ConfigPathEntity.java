package com.id.px3.cfghub;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfigPathEntity {

    @Id
    @Field("_id")
    private String id;

    @Indexed(unique = true)
    private String path;
    private String value;

}
