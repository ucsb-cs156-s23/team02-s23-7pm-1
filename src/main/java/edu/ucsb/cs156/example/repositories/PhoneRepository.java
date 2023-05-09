package edu.ucsb.cs156.example.repositories;

import edu.ucsb.cs156.example.entities.Phone;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface PhoneRepository extends CrudRepository<Phone, Long> {
  Iterable<Phone> findAllByBrand(String brand);
}