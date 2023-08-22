package com.ase.config;


import com.ase.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    MongoTemplate mongoTemplate;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Criteria criteria = Criteria.where("email").is(username);
        Query query = new Query(criteria);
        List<User> user =  mongoTemplate.find(query, User.class);
        if(user.size()==1){
            return new CustomUserDetails(user.get(0));
        }
       throw new UsernameNotFoundException(" User Email is not found our Records..");

    }
}
