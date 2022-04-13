package com.cloudapplicationmanager.job;

import com.cloudapplicationmanager.model.Environment;
import com.cloudapplicationmanager.repository.EnvironmentRepository;
import com.cloudapplicationmanager.service.EnvironmentHealthCheckService;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class HealthCheckJob implements Job {

    private static Logger logger = LoggerFactory.getLogger(HealthCheckJob.class);

    private EnvironmentRepository environmentRepository;
    private EnvironmentHealthCheckService environmentHealthCheckService;

    @Value("${health.check.interval}")
    private int HEALTH_CHECK_INTERVAL;

    public HealthCheckJob (@Autowired EnvironmentRepository environmentRepository,
                           @Autowired EnvironmentHealthCheckService environmentHealthCheckService) {
        this.environmentRepository = environmentRepository;
        this.environmentHealthCheckService = environmentHealthCheckService;
    }

    public void execute(JobExecutionContext context) throws JobExecutionException {

        //TODO this needs to be multithreaded or the job needs to be executed by multiple nodes
        //But using a simple sequential approach for now to keep this moving
        List<Environment> environmentsToCheck = environmentRepository.findEnvironmentByHealthCheckActiveIsTrue();
        for (Environment environment: environmentsToCheck) {
            logger.debug("Executing health check for environment [{}]", environment.getName());

            boolean environmentIsHealthy = environmentHealthCheckService.checkHealth(environment);
            environment.setIsHealthy(environmentIsHealthy);
            environment.setLastHealthCheck(new Date());
            environmentRepository.save(environment);
        }
    }

    @Bean
    public JobDetailFactoryBean jobDetail() {
        JobDetailFactoryBean jobDetailFactory = new JobDetailFactoryBean();
        jobDetailFactory.setJobClass(HealthCheckJob.class);
        jobDetailFactory.setDescription("Health check job service");
        jobDetailFactory.setDurability(true);
        return jobDetailFactory;
    }

    @Bean
    public SimpleTriggerFactoryBean trigger(JobDetail job) {
        SimpleTriggerFactoryBean trigger = new SimpleTriggerFactoryBean();
        trigger.setJobDetail(job);
        trigger.setRepeatInterval(HEALTH_CHECK_INTERVAL);
        trigger.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
        return trigger;
    }
}
