package com.ase.repo;

import com.ase.model.EventPost;
import com.ase.model.Post;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventPostRepo extends MongoRepository<EventPost, String> {

    @Query("{'event.eventId':?0}")
    List<EventPost> findByEvent(String eventId);

    @Query("{'postedBy.id':?0}")
    List<EventPost> findByUser(String id);
}
