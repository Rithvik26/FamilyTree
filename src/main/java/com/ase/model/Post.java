package com.ase.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.Binary;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@NoArgsConstructor
@Document
public class Post {
    @Id
    private String pid;
    private String message;
    private String fileName;
    private Binary media;
    private String fileExtension;
    @DBRef
    private User postedBy;
    private Date postedTime;

}
