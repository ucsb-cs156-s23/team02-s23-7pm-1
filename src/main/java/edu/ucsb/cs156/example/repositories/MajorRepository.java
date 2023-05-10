package edu.ucsb.cs156.example.repositories;

import edu.ucsb.cs156.example.entities.Major;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface MajorRepository extends CrudRepository<Major, Long> {
  Iterable<Major> findAllByDepartment(String department);
}