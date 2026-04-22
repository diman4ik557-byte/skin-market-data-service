package by.step.repository;

import by.step.DataServiceApplication;
import by.step.entity.Studio;
import org.assertj.core.api.Assertions;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Disabled("Testcontainers временно отключены")
@SpringBootTest(classes = DataServiceApplication.class)
@ActiveProfiles("test")
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class StudioRepositoryTest extends PostgresTestcontainersBase  {

    /*@Autowired
    private Flyway flyway;

    @BeforeEach
    void cleanDatabase() {
        flyway.clean();
        flyway.migrate();
    }*/

    @Autowired
    private StudioRepository studioRepository;

    @Test
    void findById() {
        Optional<Studio> found = studioRepository.findById(1L);
        Assertions.assertThat(found).isPresent();
        Assertions.assertThat(found.get().getName()).isEqualTo("Art Masters");
    }

    @Test
    void findByProfileUserId() {
        Optional<Studio> found = studioRepository.findByProfileUserId(3L);
        Assertions.assertThat(found).isPresent();
        Assertions.assertThat(found.get().getName()).isEqualTo("Art Masters");
    }

    @Test
    void findByManagerId() {
        List<Studio> studios = studioRepository.findByManagerId(3L);
        Assertions.assertThat(studios).isNotEmpty();
        Assertions.assertThat(studios)
                .extracting(Studio::getName)
                .contains("Art Masters");
    }

    @Test
    void findByNameContainingIgnoreCase() {
        List<Studio> studios = studioRepository.findByNameContainingIgnoreCase("art");
        Assertions.assertThat(studios).isNotEmpty();
        Assertions.assertThat(studios)
                .extracting(Studio::getName)
                .allMatch(name -> name.toLowerCase().contains("art"));
    }

    @Test
    void checkCountMembers() {
        Long count = studioRepository.countMembers(1L);
        Assertions.assertThat(count).isEqualTo(2);
    }


    @Test
    @Transactional
    void checkUpdateDescription() {
        Studio studio = studioRepository.findById(1L).orElseThrow();
        studio.setDescription("Обновлённое описание студии");
        studioRepository.save(studio);

        Optional<Studio> updated = studioRepository.findById(1L);
        Assertions.assertThat(updated).isPresent();
        Assertions.assertThat(updated.get().getDescription()).isEqualTo("Обновлённое описание студии");
    }
}