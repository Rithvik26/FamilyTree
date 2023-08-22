package com.ase.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@Document
public class Event {

    @Id
    private String eventId;
    private String eventName;
    private String eventAddedBy;
}
