package nav.portal.core.repositories;

import nav.portal.core.entities.*;

import java.util.List;
import java.util.Random;


public class SampleData {

    private final Random random = new Random();
    private static final List<String> firstnames = List.of("Harry", "Ronny", "Hermione");
    private static final List<String> lastnames = List.of("Potter", "Weasly", "Granger", "L'aprostroph");
    private static final List<String> adresses = List.of("4 Privet Drive", "12 Grimmauld Place", "The Burrow");
    private static final List<String> postalNum = List.of("1234", "4321", "1425", "1337");
    private static final List<String> place = List.of("London", "Fantasy", "City");


    public ExampleEntity sampleExample() {
        return  new ExampleEntity();
    }


}
