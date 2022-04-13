package com.cloudapplicationmanager.model;


import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Table(name = "Environment")
public class Environment {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, length = 200)
    @NotEmpty(message = "Please supply a name for this environment")
    private String name;

    @Column(length = 500)
    private String description;

    @Column(length = 500)
    private String subDomain;

    @Column(length = 500)
    private String urlPath;

    //Whether to do the automatic health check
    private boolean healthCheckActive;

    //When you last ran the health check
    private Date lastHealthCheck;

    //Whether this environment is currently healthy
    private boolean isHealthy;

    //Always grab the domain, we are always going to want that for an environment
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "domainId")
    //@JsonBackReference(value="domain")  //Required to avoid an infinite recursion serialization scenario
    @NotNull(message = "Please select an environment")
    private Domain domain;

    @ManyToOne
    @JoinColumn(name = "serviceId")
    //@JsonBackReference(value="service")  //Required to avoid an infinite recursion serialization scenario
    public Service service;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public String getSubDomain() {
        return subDomain;
    }

    public void setSubDomain(String subDomain) {
        this.subDomain = subDomain;
    }

    public String getUrlPath() {
        return urlPath;
    }

    public void setUrlPath(String urlPath) {
        this.urlPath = urlPath;
    }

    public boolean isHealthCheckActive() {
        return healthCheckActive;
    }

    public void setHealthCheckActive(boolean healthCheckActive) {
        this.healthCheckActive = healthCheckActive;
    }

    public Date getLastHealthCheck() {
        return lastHealthCheck;
    }

    public void setLastHealthCheck(Date lastHealthCheck) {
        this.lastHealthCheck = lastHealthCheck;
    }

    public boolean getIsHealthy() {
        return isHealthy;
    }

    public void setIsHealthy(boolean healthy) {
        isHealthy = healthy;
    }

    public Domain getDomain() {
        return domain;
    }

    public void setDomain(Domain domain) {
        this.domain = domain;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

}