package com.billing.webapp;

import org.springframework.data.repository.CrudRepository;

/*
    * This is the repository for the TotalClaimChargePerUser table
    * It is used to perform CRUD operations on the table.
 */
public interface TotalClaimChargePerUserRepository extends CrudRepository<TotalClaimChargePerUser, Integer> {
    // Custom queries if needed
}