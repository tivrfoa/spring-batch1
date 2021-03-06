package com.example.batchprocessing;

import java.util.Collections;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

/**
 * https://www.youtube.com/watch?v=x4nBNLoizOc
 * 
 */
@EnableBatchProcessing
@Configuration
public class BatchDemoApplication {

    public static class Person2 {
        private String firstName;
        private int age;
        private String email;
        

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public Person2() {}

        public Person2(String firstName, int age, String email) {
            this.firstName = firstName;
            this.age = age;
            this.email = email;
        }

        @Override
        public String toString() {
            return "Person2 [age=" + age + ", email=" + email + ", firstName=" + firstName + "]";
        }
        
    }
    
    @Configuration
    public static class Step1Configuration {
        @Bean
        FlatFileItemReader<Person2> fileReader() {
            return new FlatFileItemReaderBuilder<Person2>()
                .name("file-reader")
                .resource(new ClassPathResource("in.csv"))
                .targetType(Person2.class)
                .delimited().delimiter(",").names(new String[] {"firstName", "age", "email"})
                .build();
        }

        @Bean
        JdbcBatchItemWriter<Person2> jdbcWriter(DataSource ds) {
            return new JdbcBatchItemWriterBuilder<Person2>()
                .dataSource(ds)
                .sql("insert into person(age, first_name, email) values (:age, :firstName, :email)")
                .beanMapped()
                .build();
        }
    }

    @Configuration
    public static class Step2Configuration {
        @Bean
        ItemReader <Map<Integer, Integer>> jdbcReader(DataSource dataSource) {
            return new JdbcCursorItemReaderBuilder<Map<Integer, Integer>>()
                .name("jdbc-reader")
                .dataSource(dataSource)
                .sql("select count(age) c, age a from person group by age")
                .rowMapper((rs, rowNum) -> Collections.singletonMap(
				    rs.getInt("a"),
				    rs.getInt("c")))
                .build();
        }

        @Bean
        ItemWriter <Map<Integer, Integer>> fileWriter() {
            return new FlatFileItemWriterBuilder<Map<Integer, Integer>>()
                .name("file-writer")
                .resource(new FileSystemResource("src/main/resources/out.txt"))
                .lineAggregator(new DelimitedLineAggregator<Map<Integer, Integer>>() {
                    {
                        setDelimiter("|==|");
                        setFieldExtractor(item -> {
                            System.out.println(item);
                            Map.Entry<Integer, Integer> next = item.entrySet().iterator().next();
                            return new Object[] {next.getKey(), next.getValue()};
                        });
                    }
                })
                .build();
        }
    }

    @Bean
    Job job(JobBuilderFactory jbf,
            StepBuilderFactory sbf,
            Step1Configuration step1Configuration,
            Step2Configuration step2Configuration,
            JobCompletionPersonListener listener) {
        
        Step s1 = sbf.get("file-db")
            .<Person2, Person2>chunk(100)
            .reader(step1Configuration.fileReader())
            .writer(step1Configuration.jdbcWriter(null))
            .build();

        Step s2 = sbf.get("db-file")
            .<Map<Integer, Integer>, Map<Integer, Integer>>chunk(1000)
            .reader(step2Configuration.jdbcReader(null))
            .writer(step2Configuration.fileWriter())
            .build();

        return jbf.get("etl")
            .listener(listener)
            .incrementer(new RunIdIncrementer())
            .start(s1)
            .next(s2)
            .build();
    }
            
}