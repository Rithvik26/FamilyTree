package com.ase.repo;

import com.ase.model.D3JS;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface D3JSRepo extends MongoRepository<D3JS,String> {

//    @Query("{$graphLookup: {from: 'd3js', startWith: '$_id', connectFromField: '_id', connectToField: 'children.$id', as: 'descendants', maxDepth: 10, restrictSearchWithMatch: {name: ?0}}}, {$match: {'name': ?0}}")
//    D3JS findByName(String name);

//    @Query("{$match: {'name': ?0}}, {$graphLookup: {from: 'd3js', startWith: '$_id', connectFromField: '_id', connectToField: 'children.$id', as: 'descendants', maxDepth: 10, restrictSearchWithMatch: {name: ?0}}}")
//    List<D3JS> findByName(String name);

//    @Query("{ $graphLookup: { from: 'd3js', startWith: '$_id', connectFromField: '_id', connectToField: 'children.$id', as: 'descendants', maxDepth: 10, restrictSearchWithMatch: { name: ?0 } } }")
//    List<D3JS> findByName(String name);

    @Query("[{ $match: { name: ?0 } }, { $graphLookup: { from: 'd3js', startWith: '$_id', connectFromField: '_id', connectToField: 'children.$id', as: 'descendants', maxDepth: 10 } }]")
    List<D3JS> findByName(String name);
}
