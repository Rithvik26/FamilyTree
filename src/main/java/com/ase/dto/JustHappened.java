package com.ase.dto;

import com.ase.model.Comments;
import com.ase.model.Event;
import com.ase.model.User;
import lombok.Data;
import org.bson.types.Binary;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.util.Date;
import java.util.List;

@Data
public class JustHappened {
    private String pid;
    private String message;
    private String fileName;
    private Binary media;
    private String fileExtension;
    private Date postedTime;
    private List<Comments> comments;
    private String postType;
    private String section;

}
