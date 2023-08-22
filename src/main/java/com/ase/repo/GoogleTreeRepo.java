package com.ase.repo;

import com.ase.model.GoogleTree;
import com.ase.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GoogleTreeRepo extends MongoRepository<GoogleTree,String> {

    Optional<GoogleTree> findByEmail(String email);
}
