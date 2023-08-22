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
public class BucketList {

    @Id
    private String blId;
    @DBRef
    private User listedBy;
    private String bucketListName;
    private String bucketListDescription;
    private String status;
    private Date createdAt;
    private List<Comments> comments;
    private String fileName;
    private Binary media;
    private String fileExtension;


}
