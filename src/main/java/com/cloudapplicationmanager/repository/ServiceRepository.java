package com.cloudapplicationmanager.repository;

import com.cloudapplicationmanager.model.Service;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ServiceRepository extends JpaRepository<Service, Long> {

    /**
     * This method mimics the standard findById in order to provide an option for populating the environments at the
     * same time we query for the Service, i.e. to do an eager fetch on environments
     * @param id
     * @return
     */
    @EntityGraph(value = "Service.environments")
    Optional<Service> findServiceById(Long id);
}