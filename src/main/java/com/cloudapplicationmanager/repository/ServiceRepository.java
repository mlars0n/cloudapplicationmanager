package com.cloudapplicationmanager.repository;

import com.cloudapplicationmanager.model.Service;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceRepository extends JpaRepository<Service, Long> {
}