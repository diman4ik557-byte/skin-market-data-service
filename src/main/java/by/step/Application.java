package by.step;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

public class Application {

    @SpringBootApplication
    public class DataServiceApplication{
        public static void main(String[] args) {
            SpringApplication.run(DataServiceApplication.class, args);
        }
    }
}
