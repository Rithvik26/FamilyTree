package com.ase.repo;

import com.ase.model.EventPost;
import com.ase.model.Post;
import com.ase.model.TravelPost;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TravelPostRepo extends MongoRepository<TravelPost, String> {

    @Query("{'travel.travelId':?0}")
    List<TravelPost> findByEvent(String travelId);


    @Query("{'postedBy.id':?0}")
    List<TravelPost> findByUser(String id);
}
