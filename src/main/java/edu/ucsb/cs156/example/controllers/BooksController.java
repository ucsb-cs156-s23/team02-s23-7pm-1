package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.Book;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.BookRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Api(description = "Books")
@RequestMapping("/api/books")
@RestController
public class BooksController extends ApiController {

    @Autowired
    BookRepository BookRepository;

    @ApiOperation(value = "List all books")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<Book> allBooks() {
        Iterable<Book> books = BookRepository.findAll();
        return books;
    }

    @ApiOperation(value = "Get a single book")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public Book getById(
            @ApiParam("id") @RequestParam Long id) {
        Book Book = BookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Book.class, id));

        return Book;
    }

    @ApiOperation(value = "Add a new book")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public Book postBook(
            @ApiParam("title") @RequestParam String title,
            @ApiParam("author") @RequestParam String author,
            @ApiParam("genre") @RequestParam String genre)
            throws JsonProcessingException {

        Book Book = new Book();
        Book.setTitle(title);
        Book.setAuthor(author);
        Book.setGenre(genre);

        Book savedBook = BookRepository.save(Book);

        return savedBook;
    }

    @ApiOperation(value = "Delete a book")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteBook(
            @ApiParam("id") @RequestParam Long id) {
        Book Book = BookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Book.class, id));

        BookRepository.delete(Book);
        return genericMessage("Book with id %s deleted".formatted(id));
    }

    @ApiOperation(value = "Update a single book")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public Book updateBook(
            @ApiParam("id") @RequestParam Long id,
            @RequestBody @Valid Book incoming) {

        Book Book = BookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Book.class, id));

        Book.setTitle(incoming.getTitle());
        Book.setAuthor(incoming.getAuthor());
        Book.setGenre(incoming.getGenre());

        BookRepository.save(Book);

        return Book;
    }
}
