package com.cloudapplicationmanager.service;

import com.cloudapplicationmanager.model.Environment;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EnvironmentHealthCheckService {

    //Keep a record of health checks in here, must be concurrent so as not to have threading issues
    private static Map<Environment, Boolean> healthChecks = new ConcurrentHashMap<>();

    public boolean checkHealth() {
        return false;
    }

    public String getHealthCheckUrl(Environment environment) {

        String healthCheckUrl = environment.getService().getHealthCheckScheme() +
                "://" + environment.getSubDomain() + "." + environment.getDomain().getName() + "/" + environment.getService().getHealthCheckPath();
        return healthCheckUrl;
    }


}
