package com.ase.repo;

import com.ase.model.BucketList;
import com.ase.model.EventPost;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BucketListRepo extends MongoRepository<BucketList, String> {

    @Query("{'listedBy.id':?0}")
    List<BucketList> findByUser(String id);
}
