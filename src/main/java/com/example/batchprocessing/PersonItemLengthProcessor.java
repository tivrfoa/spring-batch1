package com.example.batchprocessing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.batch.item.ItemProcessor;

public class PersonItemLengthProcessor implements ItemProcessor<Person, Person> {

  private static final Logger log = LoggerFactory.getLogger(PersonItemLengthProcessor.class);

  @Override
  public Person process(final Person person) throws Exception {
    final int length = person.getFirstName().length() + person.getLastName().length();

    final Person transformedPerson = new Person(person.getFirstName(), person.getLastName() + " " + length);

    log.info("Converting (" + person + ") into (" + transformedPerson + ")");

    return transformedPerson;
  }

}