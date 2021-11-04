package com.cloudapplicationmanager.application;

import com.vaadin.flow.spring.annotation.EnableVaadin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;

@SpringBootApplication
@ComponentScan(basePackages = "com.cloudapplicationmanager") //Get all the annotated Spring Boot components
@EnableJpaRepositories("com.cloudapplicationmanager.repository") //Adds the JPA repository interface classes
@EntityScan("com.cloudapplicationmanager.model") //This is the package where the JPA entities live
@EnableVaadin(value = "com.cloudapplicationmanager.view") //Where the Vaadin views live (have to specify this if they are not in this package)
public class CloudApplicationManager {

	private static Logger logger = LoggerFactory.getLogger(CloudApplicationManager.class);

	private static String CONFIG_FILE_SYSTEM_PROPERTY_NAME = "config.file";

	//Make these public so we can refer to them from anywhere as property names
	//TODO maybe make these more generic and not AWS/Spring specific
	public static String DB_USERNAME_ENV_VAR_NAME = "spring.datasource.username";
	public static String DB_PASSWORD_ENV_VAR_NAME = "spring.datasource.password";
	public static String DB_DRIVER_CLASS_NAME = "spring.datasource.driverClassName";
	public static String DB_URL = "spring.datasource.url";
	public static String AWS_ACCESS_KEY_ID_ENV_VAR_NAME = "AWS_ACCESS_KEY_ID";
	public static String AWS_SECRET_KEY_ENV_NAME = "AWS_SECRET_KEY";

	public static void main(String[] args) {
		//Get the properties for this application--these can be set by a file and referred to by the "config.file" system property
		//or they can be set by any acceptable Spring Boot property setting method

		//Check if the env.config file exists as pointed to by a "config.file" Java system property
		String configFileLocation = System.getProperty(CONFIG_FILE_SYSTEM_PROPERTY_NAME);
		if (configFileLocation!= null) {

			logger.info("The system property \"-Dconfig.file\" is set. Reading properties from [{}].", configFileLocation);

			final Properties props = new Properties();
			try (InputStream input = new FileInputStream(configFileLocation)) {
				props.load(input);
			} catch (IOException e) {

				//Exit if we could not find the properties file
				logger.error("Could not load properties file from [{}], exiting", configFileLocation, e);
				System.exit(1);
			}

			//Now set the critical properties as system properties (or if these are set as environment variables Spring will pick them up that way)
			Arrays.stream(new String[] {
					DB_USERNAME_ENV_VAR_NAME,
					DB_PASSWORD_ENV_VAR_NAME,
					DB_DRIVER_CLASS_NAME,
					DB_URL,
					AWS_ACCESS_KEY_ID_ENV_VAR_NAME,
					AWS_SECRET_KEY_ENV_NAME
			}).forEach((keyName) -> System.setProperty(keyName, props.getProperty(keyName)));
		}

		SpringApplication.run(CloudApplicationManager.class, args);
	}

}
