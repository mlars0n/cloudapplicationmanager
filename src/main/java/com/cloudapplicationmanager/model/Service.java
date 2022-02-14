package com.cloudapplicationmanager.model;


import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Service")
@NamedEntityGraph(
        name = "Service.environments",
        attributeNodes = {
            @NamedAttributeNode(value = "environments", subgraph = "domain-subgraph"),
        },
        subgraphs = { //Grab the domains for the environments any time you get the service/environments
                @NamedSubgraph(
                        name = "domain-subgraph",
                        attributeNodes = {
                                @NamedAttributeNode("domain")
                        }
                )
        }
)
public class Service {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, length = 200)
    @NotNull(message = "Name cannot be null")
    @NotEmpty(message = "Please supply a name for this service")
    //@Size(min = 1)
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
    private List<Environment> environments = new ArrayList<Environment>();

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

    public List<Environment> getEnvironments() {
        return environments;
    }

    public void setEnvironments(List<Environment> subDomains) {
        this.environments = subDomains;
    }

    @Override
    public String toString() {
        return "Service{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", healthCheckScheme='" + healthCheckScheme + '\'' +
                ", healthCheckPort=" + healthCheckPort +
                ", healthCheckPath='" + healthCheckPath + '\'' +
                ", subDomains=" + environments +
                '}';
    }
}