package com.ase.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;



@Data
@NoArgsConstructor
@Document
public class GoogleTree {
    @Id
    String email;
    List<Person> data;
}
