package com.cloudapplicationmanager.repository;

import com.cloudapplicationmanager.model.SubDomain;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubDomainRepository extends JpaRepository<SubDomain, Long> {

    //Grab the domain by its domain name
    //SubDomain findById(Long id);
}