package com.ase.repo;

import com.ase.model.Post;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepo extends MongoRepository<Post,String> {

    @Query("{'postedBy.id':?0}")
    List<Post> findByUser(String id);
}
