package com.cloudapplicationmanager.repository;

import com.cloudapplicationmanager.model.Environment;
import com.cloudapplicationmanager.model.Service;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EnvironmentRepository extends JpaRepository<Environment, Long> {

    /**
     * Get all environments where health check active is true
     * @return
     */
    List<Environment> findEnvironmentByHealthCheckActiveIsTrue();
}