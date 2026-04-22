package by.step.repository;

import by.step.DataServiceApplication;
import by.step.entity.Profile;
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
class ProfileRepositoryTest extends PostgresTestcontainersBase  {

    /*@Autowired
    private Flyway flyway;

    @BeforeEach
    void cleanDatabase() {
        flyway.clean();
        flyway.migrate();
    }*/

    @Autowired
    private ProfileRepository profileRepository;

    @Test
    void findByUserId() {
        Optional<Profile> found = profileRepository.findByUserId(2L);
        Assertions.assertThat(found).isPresent();
        Assertions.assertThat(found.get().getIsArtist()).isTrue();
    }

    @Test
    void findByIsArtistTrue() {
        List<Profile> artists = profileRepository.findByIsArtistTrue();
        Assertions.assertThat(artists).isNotEmpty();
        Assertions.assertThat(artists)
                .allMatch(Profile::getIsArtist);
    }

    @Test
    void findByIsStudioTrue() {
        List<Profile> studios = profileRepository.findByIsStudioTrue();
        Assertions.assertThat(studios).isNotEmpty();
        Assertions.assertThat(studios)
                .allMatch(Profile::getIsStudio);
    }

    @Test
    void checkExistsByUserId() {
        boolean exists = profileRepository.existsByUserId(2L);
        boolean notExists = profileRepository.existsByUserId(999L);
        Assertions.assertThat(exists).isTrue();
        Assertions.assertThat(notExists).isFalse();
    }

    @Test
    void checkUpdateBio() {
        profileRepository.updateBio(2L, "Обновлённое био художника");
        Optional<Profile> updated = profileRepository.findById(2L);
        Assertions.assertThat(updated).isPresent();
        Assertions.assertThat(updated.get().getBio()).isEqualTo("Обновлённое био художника");
    }

    @Test
    void checkSetArtistStatus() {
        profileRepository.setArtistStatus(1L, true);
        Optional<Profile> updated = profileRepository.findById(1L);
        Assertions.assertThat(updated).isPresent();
        Assertions.assertThat(updated.get().getIsArtist()).isTrue();
    }

    @Test
    void checkSetStudioStatus() {
        profileRepository.setStudioStatus(3L, true);
        Optional<Profile> updated = profileRepository.findById(3L);
        Assertions.assertThat(updated).isPresent();
        Assertions.assertThat(updated.get().getIsStudio()).isTrue();
    }
}