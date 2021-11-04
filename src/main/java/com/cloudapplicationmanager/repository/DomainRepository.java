package com.cloudapplicationmanager.repository;

import com.cloudapplicationmanager.model.Domain;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DomainRepository extends JpaRepository<Domain, Long> {

    //Grab the domain by its domain name
    Domain findByName(String domain);

}