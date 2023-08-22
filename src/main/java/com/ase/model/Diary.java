package com.ase.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@NoArgsConstructor
@Document
public class Diary {
    @Id
    private String id;
    private String email;
    private String name;
    @DBRef
    private List<User> familyMembers;
    @DBRef
    private List<User> friends;
    private List<User> accessFamily;
}
