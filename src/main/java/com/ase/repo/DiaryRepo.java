package com.ase.repo;

import com.ase.model.Diary;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiaryRepo extends MongoRepository<Diary,String> {

    @Query("{ 'email' : ?0 }")
    List<Diary> findByEmail(String email);

    @Query("{'friends.id':?0}")
    List<Diary> findByFriendEmail(String id);


    @Query("{'friends.id':?0}")
    List<Diary> frnd(String id);


    @Query("{'familyMembers.id':?0}")
    List<Diary> fam(String id);


    @Query("{'accessFamily.id':?0}")
    List<Diary> access(String id);
}
