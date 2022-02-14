package com.cloudapplicationmanager.repository;

import com.cloudapplicationmanager.model.Domain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DomainRepository extends JpaRepository<Domain, Long> {

    //Grab the domain by its domain name
    Domain findByName(String domain);

    @Query("select size(domain.environments) from Domain domain where domain = :domain")
    int getEnvironmentsCount(@Param("domain") Domain domain);

}