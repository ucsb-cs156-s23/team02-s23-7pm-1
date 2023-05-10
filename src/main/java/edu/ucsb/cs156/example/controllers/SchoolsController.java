package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.Schools;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.SchoolsRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

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


@Api(description = "Schools")
@RequestMapping("/api/schools")
@RestController
@Slf4j
public class SchoolsController extends ApiController {

    @Autowired
    SchoolsRepository schoolsRepository;

    @ApiOperation(value = "List all schools")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<Schools> allSchools() {
        Iterable<Schools> schools = schoolsRepository.findAll();
        return schools;
    }

    @ApiOperation(value = "Get a single school")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public Schools getById(
            @ApiParam("id") @RequestParam Long id) {
        Schools school = schoolsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Schools.class, id));

        return school;
    }

    @ApiOperation(value = "Create a new school")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public Schools postSchool(
            @ApiParam("name") @RequestParam String name,
            @ApiParam("district") @RequestParam String district,
            @ApiParam("grade range") @RequestParam String gradeRange)
            throws JsonProcessingException {


        Schools school = new Schools();
        school.setName(name);
        school.setDistrict(district);
        school.setGradeRange(gradeRange);

        Schools savedSchools = schoolsRepository.save(school);

        return savedSchools;
    }

    @ApiOperation(value = "Delete a School")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteSchool(
            @ApiParam("id") @RequestParam Long id) {
        Schools school = schoolsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Schools.class, id));

        schoolsRepository.delete(school);
        return genericMessage("School with id %s deleted".formatted(id));
    }

    @ApiOperation(value = "Update a single school")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public Schools updateSchool(
            @ApiParam("id") @RequestParam Long id,
            @RequestBody @Valid Schools incoming) {

        Schools school = schoolsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Schools.class, id));

        school.setName(incoming.getName());
        school.setDistrict(incoming.getDistrict());
        school.setGradeRange(incoming.getGradeRange());

        schoolsRepository.save(school);

        return school;
    }
}
