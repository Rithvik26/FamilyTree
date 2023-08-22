package com.ase.repo;

import com.ase.model.DiaryRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiaryRequestRepo extends MongoRepository<DiaryRequest,String> {

    //myDairyRequests
    @Query("{'diaryId':?0}")
    List<DiaryRequest> myDairyRequests(String diaryId);

    @Query("{'requestedBy':?1, 'diaryId':?0}")
    List<DiaryRequest> myDairyRequestWithUser(String diaryId,String requestedBy);

}
