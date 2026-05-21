package com.mdsg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.mdsg.config.MdsgProperties;
import com.mdsg.wikidata.WikidataProperties;

@SpringBootApplication
@EnableConfigurationProperties({ MdsgProperties.class, WikidataProperties.class })
public class MdsgServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(MdsgServerApplication.class, args);
	}

}
