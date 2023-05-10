package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.Major;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.MajorRepository;
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

@Api(description = "Majors")
@RequestMapping("/api/majors")
@RestController
public class MajorsController extends ApiController {

    @Autowired
    MajorRepository MajorRepository;

    @ApiOperation(value = "List all majors")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<Major> allMajors() {
        Iterable<Major> majors = MajorRepository.findAll();
        return majors;
    }

    @ApiOperation(value = "Get a single major")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public Major getById(
            @ApiParam("id") @RequestParam Long id) {
        Major Major = MajorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Major.class, id));

        return Major;
    }

    @ApiOperation(value = "Add a new major")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public Major postMajor(
            @ApiParam("name") @RequestParam String name,
            @ApiParam("department") @RequestParam String department,
            @ApiParam("degree pursued") @RequestParam String degreePursued)
            throws JsonProcessingException {

        Major Major = new Major();
        Major.setName(name);
        Major.setDepartment(department);
        Major.setDegreePursued(degreePursued);

        Major savedMajor = MajorRepository.save(Major);

        return savedMajor;
    }

    @ApiOperation(value = "Delete a major")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteMajor(
            @ApiParam("id") @RequestParam Long id) {
        Major Major = MajorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Major.class, id));

        MajorRepository.delete(Major);
        return genericMessage("Major with id %s deleted".formatted(id));
    }

    @ApiOperation(value = "Update a single major")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public Major updateMajor(
            @ApiParam("id") @RequestParam Long id,
            @RequestBody @Valid Major incoming) {

        Major Major = MajorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Major.class, id));

        Major.setName(incoming.getName());
        Major.setDepartment(incoming.getDepartment());
        Major.setDegreePursued(incoming.getDegreePursued());

        MajorRepository.save(Major);

        return Major;
    }
}
