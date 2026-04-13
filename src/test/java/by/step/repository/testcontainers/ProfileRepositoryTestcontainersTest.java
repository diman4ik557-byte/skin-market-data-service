package by.step.repository.testcontainers;

import by.step.DataServiceApplication;
import by.step.entity.Profile;
import by.step.repository.ProfileRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

@SpringBootTest(classes = DataServiceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class ProfileRepositoryTestcontainersTest extends PostgresTestcontainersBase {

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