package com.ase.repo;

import com.ase.model.ForgetPasswordToken;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ForgetPasswordTokenRepo extends MongoRepository<ForgetPasswordToken,String> {
}
