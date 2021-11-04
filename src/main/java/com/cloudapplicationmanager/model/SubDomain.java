package com.cloudapplicationmanager.model;


import javax.persistence.*;

@Entity
@Table(name = "SubDomain")
public class SubDomain {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 200)
    private String value;

    @ManyToOne
    @JoinColumn(name = "domainId")
    //@JsonBackReference(value="domain")  //Required to avoid an infinite recursion serialization scenario
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

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
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