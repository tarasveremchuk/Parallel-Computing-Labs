package com.iotplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.util.TimeZone;

@SpringBootApplication
public class IotMonitoringPlatformApplication {

	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone("Europe/Kyiv"));
		SpringApplication.run(IotMonitoringPlatformApplication.class, args);
	}
}
