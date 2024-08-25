package com.id.px3.auth.model.entity;

import com.id.px3.auth.model.UserConfigValueType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Document(collection = "user_roles_list")
public class UserConfig {

    public static final String ID = "id";
    public static final String CODE = "code";
    public static final String DESCRIPTION = "description";
    public static final String VALUE_TYPE = "valueType";

    @Id
    private String id;
    @Indexed(unique = true)
    private String code = "";
    private String description = "";
    private UserConfigValueType valueType = UserConfigValueType.STRING;
}
