package com.ase.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@Document
public class User {

    @Id
    private String id;
    private String name;
    private String email;
    private String password;
    private String role;

}
