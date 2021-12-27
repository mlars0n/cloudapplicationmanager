package com.cloudapplicationmanager.model;


import com.fasterxml.jackson.annotation.JsonManagedReference;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Domain")
public class Domain {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length=100)
    private String cloudId;

    @OneToMany(mappedBy="domain")
    @JsonManagedReference(value = "environments") //Required to avoid an infinite recursion serialization scenario
    private List<Environment> environments = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCloudId(String zoneId) {
        this.cloudId = zoneId;
    }

    public List<Environment> getEnvironments() {
        return environments;
    }

    public void setEnvironments(List<Environment> subDomains) {
        this.environments = subDomains;
    }

    //THe name of the hosted zone, i.e. the domain
    public String getName() {
        return name;
    }

    public String getCloudId() {
        return cloudId;
    }
}