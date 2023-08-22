package com.ase.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@Document
public class DiaryRequest {

    @Id
    private String did;
    private String requestedBy;
    private String diaryId;
    private String requestedAs;
}
