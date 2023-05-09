package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.Phone;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.PhoneRepository;
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


@Api(description = "Phones")
@RequestMapping("/api/phones")
@RestController
@Slf4j
public class PhonesController extends ApiController {

    @Autowired
    PhoneRepository phoneRepository;

    @ApiOperation(value = "List all phones")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<Phone> allPhones() {
        Iterable<Phone> dates = phoneRepository.findAll();
        return dates;
    }

    @ApiOperation(value = "Get a single phone")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public Phone getById(
            @ApiParam("id") @RequestParam Long id) {
        Phone phone = phoneRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Phone.class, id));

        return phone;
    }

    @ApiOperation(value = "Create a new phone")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public Phone postPhone(
            @ApiParam("brand") @RequestParam String brand,
            @ApiParam("model") @RequestParam String model,
            @ApiParam("price") @RequestParam int price)
            //throws JsonProcessingException 
            {



     

        Phone phone = new Phone();
        phone.setBrand(brand);
        phone.setModel(model);
        phone.setPrice(price);

        Phone savedphone = phoneRepository.save(phone);

        return savedphone;
    }

    @ApiOperation(value = "Delete a Phone")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deletePhone(
            @ApiParam("id") @RequestParam Long id) {
        Phone phone = phoneRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Phone.class, id));

        phoneRepository.delete(phone);
        return genericMessage("Phone with id %s deleted".formatted(id));
    }

    @ApiOperation(value = "Update a single phone")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public Phone updatePhone(
            @ApiParam("id") @RequestParam Long id,
            @RequestBody @Valid Phone incoming) {

        Phone phone = phoneRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Phone.class, id));

        phone.setBrand(incoming.getBrand());
        phone.setModel(incoming.getModel());
        phone.setPrice(incoming.getPrice());

        phoneRepository.save(phone);

        return phone;
    }
}
