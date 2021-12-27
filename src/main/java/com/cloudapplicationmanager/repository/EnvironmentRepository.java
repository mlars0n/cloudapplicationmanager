package com.cloudapplicationmanager.repository;

import com.cloudapplicationmanager.model.Environment;
import com.cloudapplicationmanager.model.Service;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnvironmentRepository extends JpaRepository<Environment, Long> {
}