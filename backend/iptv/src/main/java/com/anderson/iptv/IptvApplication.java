package com.anderson.iptv;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class IptvApplication {

	public static void main(String[] args) {
		SpringApplication.run(IptvApplication.class, args);
	}

}
