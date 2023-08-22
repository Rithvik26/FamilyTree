package com.ase.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.Binary;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@Document
public class EventPost {

    @Id
    private String eid;
    private String message;
    private String fileName;
    private Binary media;
    private String fileExtension;
    @DBRef
    private User postedBy;
    private Date postedTime;
    private List<Comments> comments;
    private Event event;
}
