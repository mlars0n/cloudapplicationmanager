package com.cloudapplicationmanager.model;


import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Service")
public class Service {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(length = 10)
    private String healthCheckScheme;

    @Column
    private Integer healthCheckPort;

    @Column(length = 500)
    private String healthCheckPath;

    @OneToMany(mappedBy="service")
    //@JsonManagedReference(value="service") //Required to avoid an infinite recursion serialization scenario
    @JsonIgnore
    private List<SubDomain> subDomains = new ArrayList<>();

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

    public String getHealthCheckPath() {
        return healthCheckPath;
    }

    public void setHealthCheckPath(String checkUrlPath) {
        this.healthCheckPath = checkUrlPath;
    }

    public String getHealthCheckScheme() {
        return healthCheckScheme;
    }

    public void setHealthCheckScheme(String scheme) {
        this.healthCheckScheme = scheme;
    }

    public Integer getHealthCheckPort() {
        return healthCheckPort;
    }

    public void setHealthCheckPort(Integer port) {
        this.healthCheckPort = port;
    }

    public List<SubDomain> getSubDomains() {
        return subDomains;
    }

    public void setSubDomains(List<SubDomain> subDomains) {
        this.subDomains = subDomains;
    }

    @Override
    public String toString() {
        return "Service{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", healthCheckScheme='" + healthCheckScheme + '\'' +
                ", healthCheckPort=" + healthCheckPort +
                ", healthCheckPath='" + healthCheckPath + '\'' +
                ", subDomains=" + subDomains +
                '}';
    }
}