package edu.ucsb.cs156.example.repositories;

import edu.ucsb.cs156.example.entities.Park;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParkRepository extends CrudRepository<Park, Long> {

}
