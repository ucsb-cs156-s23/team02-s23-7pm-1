package edu.ucsb.cs156.example.repositories;

import edu.ucsb.cs156.example.entities.Majors;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface MajorRepository extends CrudRepository<Majors, Long> {
  Iterable<Majors> findAllByDepartment(String department);
}