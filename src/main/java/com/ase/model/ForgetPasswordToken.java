package com.ase.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@Document
public class ForgetPasswordToken {
    @Id
    private String id;
    private String email;
    private String token;
}
