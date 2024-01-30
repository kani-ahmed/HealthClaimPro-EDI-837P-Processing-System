package com.billing.webapp;

import org.springframework.data.repository.CrudRepository;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.List;
import java.util.Optional;

/*
    * This is the repository for the LegacyData table
    * It is used to perform CRUD operations on the table.
    * The @CrossOrigin annotation is used to allow the frontend to access the backend.
    * return type of Optional is used to avoid null pointer exceptions.
    * The findBy methods are used to search the table by the specified field.
 */
@CrossOrigin
public interface LegacyDataRepository extends CrudRepository<LegacyData, Integer> {
    // Method to search users by their id number
    @CrossOrigin
    Optional<LegacyData> findByIdNumber(String idNumber);

    // Method to search users by part of their first name
    @CrossOrigin
    List<LegacyData> findByFirstNameContainingIgnoreCase(String firstName);

    // Method to search users by first and last name
    @CrossOrigin
    List<LegacyData> findByFirstNameAndLastName(String firstName, String lastName);
}