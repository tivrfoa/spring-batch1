package com.example.batchprocessing;

import java.io.File;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BatchProcessingApplication {

	public static void main(String[] args) {
		System.setProperty("input", "file://" + new File("/home/leandro/dev/java/spring-batch/batch-processing/in.csv").getAbsolutePath());
		System.setProperty("output", "file://" + new File("/home/leandro/dev/java/spring-batch/batch-processing/out.csv").getAbsolutePath());

		// System.out.println(System.getProperty("input"));
		// System.out.println(System.getenv());
		// System.out.println(System.getProperties());

		SpringApplication.run(BatchProcessingApplication.class, args);
	}

}
