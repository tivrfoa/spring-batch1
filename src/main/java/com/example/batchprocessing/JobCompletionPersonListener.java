package com.example.batchprocessing;

import com.example.batchprocessing.BatchDemoApplication.Person2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class JobCompletionPersonListener extends JobExecutionListenerSupport {

    private static final Logger log = LoggerFactory.getLogger(JobCompletionPersonListener.class);
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public JobCompletionPersonListener(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            log.info("!!! JOB FINISHED! Time to verify the results");

            jdbcTemplate.query("SELECT first_name, age, email FROM person",
                    (rs, row) -> new Person2(
                        rs.getString(1),
                        rs.getInt(2),
                        rs.getString(3))
            ).forEach(person -> log.info("Found <" + person + "> in the database."));
        }
    }
}