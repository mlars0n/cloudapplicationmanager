package com.cloudapplicationmanager.service;

import com.cloudapplicationmanager.application.CloudApplicationManager;
import com.cloudapplicationmanager.model.Domain;
import com.cloudapplicationmanager.repository.DomainRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.route53.Route53Client;
import software.amazon.awssdk.services.route53.model.*;

import java.util.ArrayList;
import java.util.List;

public class AwsRoute53Service {

    private static Logger logger = LoggerFactory.getLogger(AwsRoute53Service.class);

    private final String awsAccessKeyId;
    private final String awsSecretKey;
    private final Route53Client route53Client;
    private DomainRepository domainRepository;

    @Autowired
    public AwsRoute53Service(Environment env, DomainRepository domainRepository) {
        this.awsAccessKeyId = env.getProperty(CloudApplicationManager.AWS_ACCESS_KEY_ID_ENV_VAR_NAME);
        this.awsSecretKey = env.getProperty(CloudApplicationManager.AWS_SECRET_KEY_ENV_NAME);

        //Set up the Route 53 client--this is thread safe and can be re-used among threads
        //See https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/creating-clients.html
        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(this.awsAccessKeyId, this.awsSecretKey);

        //Repository we need
        this.domainRepository = domainRepository;

        route53Client = Route53Client.builder()
                .region(Region.AWS_GLOBAL)
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .build();
    }

    public List<HostedZone> listZones() {

        logger.debug("Getting all available hosted zones using AWS access key ID: [{}]", awsAccessKeyId);

        List<HostedZone> checklist = new ArrayList<>();

        try {
            ListHostedZonesResponse zonesResponse = route53Client.listHostedZones();
            checklist = zonesResponse.hostedZones();
            route53Client.close();
        } catch (Route53Exception e) {
            logger.error("Exception trying to list hosted zones:", e);
        }

        return checklist;
    }

    public List<ResourceRecordSet> getResourceRecordsForZone(String zone) {

        ListResourceRecordSetsRequest listResourceRecordSetsRequest = ListResourceRecordSetsRequest.builder().hostedZoneId(zone).build();
        ListResourceRecordSetsResponse resourceRecordSetsResponse = route53Client.listResourceRecordSets(listResourceRecordSetsRequest);
        List<ResourceRecordSet> resourceRecordSets = resourceRecordSetsResponse.resourceRecordSets();

        return resourceRecordSets;
    }

    /**
     * Method for updating a record where you want to use an alias
     *
     * @param domain
     * @param subDomain
     * @param dnsName
     * @param evaluateTargetHealth
     * @return
     */
    public ServiceResponse updateResourceRecordWithAlias(String domain, String subDomain, String dnsName, boolean evaluateTargetHealth) {

        ServiceResponse serviceResponse = new ServiceResponse();

        logger.info("Updating subdomain [{}] of domain [{}] with alias [{}]", subDomain, domain, dnsName);

        logger.debug("Looking up hosted zone ID for domain [{}]", domain);

        Domain zoneDomain = domainRepository.findByName(domain);

        if (zoneDomain == null) {
            logger.info("Error, could not find domain [{}], returning with errors", domain);

            serviceResponse.setSuccess(false); //Not actually necessary but wanted to do this for clarity
            serviceResponse.setMessage("WARNING: Could not find domain [" + domain + "]. Make sure this domain is in the database.");
            return serviceResponse;
        }

        //Domain found, OK to continue
        logger.debug("Found Domain entry for [{}] with hosted zone ID of [{}]", zoneDomain.getName(), zoneDomain.getCloudId());

        //TODO add type in here to handle which type of LB this is (see https://docs.aws.amazon.com/general/latest/gr/elb.html)
        AliasTarget aliasTarget = AliasTarget.builder()
                .hostedZoneId("Z35SXDOTRQ7X7K") //This is actually the hosted zone ID that AWS uses for US-East-1
                .dnsName(dnsName) //This will be the DNS name of the load balancer usually starting with "dualstack"
                .evaluateTargetHealth(evaluateTargetHealth)
                .build();

        ResourceRecordSet resourceRecordSet = ResourceRecordSet.builder()
                .name(subDomain + "." + domain)
                .type(RRType.A)
                .aliasTarget(aliasTarget)
                .build();

        //Execute the actual change and capture any SDK exceptions
        //The AWS SDK has exceptions but they all descend from RuntimeException and I don't want to depend on the specific exceptions
        try {
            boolean success = executeRoute53Change(resourceRecordSet, zoneDomain.getCloudId());
        } catch (RuntimeException e) {
            logger.warn("Exception executing alias update: ", e);
            serviceResponse.setSuccess(false);
            serviceResponse.setMessage("ERROR: " + e.getMessage());
            return serviceResponse;
        }

        //If we made it here there were no exceptions and we'll consider this a success
        String message = "SUCCESS: Updated subdomain [" + subDomain + "] of domain [" + domain + "] with alias [" + dnsName + "]";
        logger.info(message);

        serviceResponse.setSuccess(true);
        serviceResponse.setMessage(message);
        return serviceResponse;
    }

    private boolean executeRoute53Change(ResourceRecordSet resourceRecordSet, String hostedZoneId) {
        //Specify the change type we want to make here
        Change change = Change.builder()
                .resourceRecordSet(resourceRecordSet)
                .action(ChangeAction.UPSERT).build();

        //Create the change batch
        ChangeBatch changeBatch = ChangeBatch.builder()
                .changes(change)
                .build();

        //Create the full change request
        ChangeResourceRecordSetsRequest changeResourceRecordSetsRequest = ChangeResourceRecordSetsRequest.builder()
                .hostedZoneId(hostedZoneId)
                .changeBatch(changeBatch)
                .build();

        //Make the change request
        ChangeResourceRecordSetsResponse changeResourceRecordSetsResponse = route53Client.changeResourceRecordSets(changeResourceRecordSetsRequest);

        //Check the result
        logger.debug("Done, change status is: [{}]", changeResourceRecordSetsResponse.changeInfo().statusAsString());

        return true;
    }


    /**
     * This will throw a software.amazon.awssdk.services.route53.model.InvalidInputException exception for bad input
     *
     * @param hostedZoneId - Hosted Zone ID from AWS
     * @param resourceName - the name (think of this as the domain name you want to change)
     * @param newValue - the new value you want to set
     *
     * @return
     */
    public boolean updateResourceRecordSingleValue(String hostedZoneId, String resourceName, String newValue) {

        /*AliasTarget aliasTarget = new AliasTarget() //
                .withHostedZoneId(HOSTED_ZONE_ID) //
                .withEvaluateTargetHealth(false) //
                .withDNSName("1.2.3.4"); // using a valid IP here

        ResourceRecordSet recordSet = new ResourceRecordSet() //
                .withType(RRType.A) //
                .withName("sub.domain.com") // using my own domain here
                .withTTL(300L) //
                .withAliasTarget(aliasTarget);*/

        //The actual resource record value we want to change (many to one relationship with the ResourceRecordSet)
        ResourceRecord resourceRecord = ResourceRecord.builder().value(newValue).build();

        //The ResourceRecordSet holds one or more ResourceRecord(s)
        ResourceRecordSet resourceRecordSet = ResourceRecordSet.builder()
                .name(resourceName)
                .type(RRType.A)
                .ttl(300L)
                .resourceRecords(resourceRecord)
                .build();

        //Specify the change type we want to make here
        boolean success = executeRoute53Change(resourceRecordSet, hostedZoneId);

        return success;
    }
}

