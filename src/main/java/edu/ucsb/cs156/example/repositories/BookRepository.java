package edu.ucsb.cs156.example.repositories;

import edu.ucsb.cs156.example.entities.Book;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface BookRepository extends CrudRepository<Book, Long> {
  Iterable<Book> findAllByTitle(String title);
}
