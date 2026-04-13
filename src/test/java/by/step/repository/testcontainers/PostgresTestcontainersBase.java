package by.step.repository.testcontainers;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;


import javax.sql.DataSource;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class PostgresTestcontainersBase {


    @ServiceConnection
    static final PostgreSQLContainer<?> postgres;

    static {
        postgres = new PostgreSQLContainer<>("postgres:17")
                .withDatabaseName("testdb")
                .withUsername("postgres")
                .withPassword("postgres");
        postgres.start();
    }

    @Autowired
    private DataSource dataSource;

    @BeforeAll
    void resetDatabase() {
        System.out.println("--- Starting Flyway Clean & Migrate ---");
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration/flyway")
                .baselineOnMigrate(true)
                .validateOnMigrate(false)
                .cleanDisabled(false)
                .load();

        flyway.clean();
        flyway.migrate();
        System.out.println("--- Flyway Finished ---");
    }
}
