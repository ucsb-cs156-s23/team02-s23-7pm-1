package edu.ucsb.cs156.example.repositories;

import edu.ucsb.cs156.example.entities.Schools;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface SchoolsRepository extends CrudRepository<Schools, Long> {

}