package by.step.repository.testcontainers;

import by.step.DataServiceApplication;
import by.step.entity.ArtistProfile;
import by.step.repository.ArtistProfileRepository;
import by.step.repository.ProfileRepository;
import by.step.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@SpringBootTest(classes = DataServiceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class ArtistProfileRepositoryTestcontainersTest extends PostgresTestcontainersBase {

    @Autowired
    private ArtistProfileRepository artistProfileRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByProfile() {
        Optional<ArtistProfile> found = artistProfileRepository.findByProfileId(2L);
        Assertions.assertThat(found).isPresent();
        Assertions.assertThat(found.get().getStyles()).isEqualTo("классический, реализм");
    }

    @Test
    void findByProfileUserId() {
        Optional<ArtistProfile> found = artistProfileRepository.findByProfileUserId(2L);
        Assertions.assertThat(found).isPresent();
        Assertions.assertThat(found.get().getMinPrice()).isEqualByComparingTo(BigDecimal.valueOf(1000));
    }

    @Test
    void findByIsAvailableTrue() {
        List<ArtistProfile> available = artistProfileRepository.findByIsAvailableTrue();
        Assertions.assertThat(available).isNotEmpty();
        Assertions.assertThat(available).allMatch(ArtistProfile::getIsAvailable);
    }

    @Test
    void findByMinPriceLessThanEqual() {
        List<ArtistProfile> artists = artistProfileRepository.findByMinPriceLessThanEqual(BigDecimal.valueOf(900));
        Assertions.assertThat(artists).isNotEmpty();
        Assertions.assertThat(artists)
                .allMatch(artist -> artist.getMinPrice().compareTo(BigDecimal.valueOf(900)) <= 0);
    }

    @Test
    void findByStylesContaining() {
        List<ArtistProfile> artists = artistProfileRepository.findByStylesContaining("классический");
        Assertions.assertThat(artists).isNotEmpty();
        Assertions.assertThat(artists)
                .allMatch(artist -> artist.getStyles().contains("классический"));
    }

    @Test
    void findAllOrderByPriceAsc() {
        List<ArtistProfile> artists = artistProfileRepository.findAllOrderByPriceAsc();
        Assertions.assertThat(artists).isNotEmpty();
        Assertions.assertThat(artists).isSortedAccordingTo(Comparator.comparing(ArtistProfile::getMinPrice));
    }

    @Test
    void checkGetAverageMinPrice() {
        Double avg = artistProfileRepository.getAverageMinPrice();
        Assertions.assertThat(avg).isNotNull();
        Assertions.assertThat(avg).isGreaterThan(0);
    }

    @Test
    void checkFindIndependentArtists() {
        List<ArtistProfile> independent = artistProfileRepository.findIndependentArtists();
        Assertions.assertThat(independent).isNotEmpty();
        Assertions.assertThat(independent).allMatch(artist -> artist.getStudio() == null);
    }

    @Test
    @Transactional
    void checkUpdateAvailability() {
        artistProfileRepository.updateAvailability(1L, false);
        Optional<ArtistProfile> updated = artistProfileRepository.findById(1L);
        Assertions.assertThat(updated).isPresent();
        Assertions.assertThat(updated.get().getIsAvailable()).isFalse();
    }

    @Test
    @Transactional
    void checkUpdateStyles() {
        ArtistProfile artist = artistProfileRepository.findById(1L).orElseThrow();
        artist.setStyles("реализм, мягкий");
        artistProfileRepository.save(artist);

        Optional<ArtistProfile> updated = artistProfileRepository.findById(1L);
        Assertions.assertThat(updated).isPresent();
        Assertions.assertThat(updated.get().getStyles()).isEqualTo("реализм, мягкий");
    }

    @Test
    @Transactional
    void checkUpdateMinPrice() {
        ArtistProfile artist = artistProfileRepository.findById(1L).orElseThrow();
        artist.setMinPrice(BigDecimal.valueOf(2000));
        artistProfileRepository.save(artist);

        Optional<ArtistProfile> updated = artistProfileRepository.findById(1L);
        Assertions.assertThat(updated).isPresent();
        Assertions.assertThat(updated.get().getMinPrice()).isEqualByComparingTo(BigDecimal.valueOf(2000));
    }
}