package by.step.service;

import by.step.dto.ArtistProfileDto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ArtistProfileService {

    ArtistProfileDto createArtistProfile(Long userId, String styles, BigDecimal minPrice, Integer averageTime);

    Optional<ArtistProfileDto> findByUserId(Long userId);

    List<ArtistProfileDto> findAllArtists();

    List<ArtistProfileDto> findAvailableArtists();

    List<ArtistProfileDto> findArtistsByStyle(String style);

    List<ArtistProfileDto> findArtistsByMaxPrice(BigDecimal maxPrice);

    List<ArtistProfileDto> findArtistsByFilters(String style, BigDecimal maxPrice, Boolean isAvailable);

    ArtistProfileDto updateStyles(Long artistId, String styles);

    ArtistProfileDto updateMinPrice(Long artistId, BigDecimal minPrice);

    ArtistProfileDto updateAverageTime(Long artistId, Integer averageTime);

    void updateAvailability(Long artistId, boolean isAvailable);

    void assignToStudio(Long artistId, Long studioId);

    void removeFromStudio(Long artistId);
}
