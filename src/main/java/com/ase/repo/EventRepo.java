package com.ase.repo;

import com.ase.model.Event;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepo extends MongoRepository<Event, String> {
    List<Event> findByEventAddedBy(String addedBy);
}
