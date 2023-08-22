package com.ase.repo;

import com.ase.model.Event;
import com.ase.model.Travel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TravelRepo extends MongoRepository<Travel, String> {

    List<Travel> findByTravelAddedBy(String addedBy);
}
