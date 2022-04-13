package com.cloudapplicationmanager.service;

import com.cloudapplicationmanager.model.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.time.temporal.ChronoUnit.SECONDS;

@Service
public class EnvironmentHealthCheckService {

    private static Logger logger = LoggerFactory.getLogger(EnvironmentHealthCheckService.class);

    //Keep a record of health checks in here, must be concurrent so as not to have threading issues
    private static Map<Environment, Boolean> healthChecks = new ConcurrentHashMap<>();

    public boolean checkHealth(Environment environment) {

        String urlToCheck = getHealthCheckUrl(environment);
        boolean healthy = false;

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(urlToCheck))
                    .GET()
                    .timeout(Duration.of(2, SECONDS)) //Make this configurable
                    .build();

            HttpResponse<String> response = HttpClient.newBuilder()
                    .build().send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == HttpURLConnection.HTTP_OK) {
                healthy = true;
            } else {
                healthy = false;
            }

        } catch (URISyntaxException | IOException | InterruptedException e) {
            logger.warn("Error checking URL [" + urlToCheck + "]: " + e.getMessage());
            //throw new HealthCheckException("Error checking URL [" + urlToCheck + "]: " + e.getMessage(), e);
        }

        return healthy;
    }

    public String getHealthCheckUrl(Environment environment) {

        String healthCheckUrl = environment.getService().getHealthCheckScheme() +
                "://" + environment.getSubDomain() + "." + environment.getDomain().getName() + "/" + environment.getService().getHealthCheckPath();
        return healthCheckUrl;
    }


}
