package by.step.repository;

import by.step.entity.ArtistProfile;
import by.step.entity.Profile;
import by.step.entity.Studio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ArtistProfileRepository extends JpaRepository<ArtistProfile, Long> {

    // JPA Methods

    Optional<ArtistProfile> findByProfile(Profile profile);

    Optional<ArtistProfile> findByProfileUserId(Long userId);

    Optional<ArtistProfile> findByProfileId(Long profileId);

    List<ArtistProfile> findByStudio(Studio studio);

    List<ArtistProfile> findByIsAvailableTrue();

    List<ArtistProfile> findByMinPriceLessThanEqual(BigDecimal maxPrice);

    List<ArtistProfile> findByStylesContaining(String style);

    // HQL Queries

    @Query("SELECT a FROM ArtistProfile a WHERE " +
            "(:style IS NULL OR a.styles LIKE %:style%) AND " +
            "(:maxPrice IS NULL OR a.minPrice <= :maxPrice) AND " +
            "(:isAvailable IS NULL OR a.isAvailable = :isAvailable)")
    List<ArtistProfile> findByFilters(@Param("style") String style,
                                      @Param("maxPrice") BigDecimal maxPrice,
                                      @Param("isAvailable") Boolean isAvailable);

    @Query("SELECT a FROM ArtistProfile a ORDER BY a.minPrice ASC")
    List<ArtistProfile> findAllOrderByPriceAsc();

    @Query("SELECT AVG(a.minPrice) FROM ArtistProfile a WHERE a.isAvailable = true")
    Double getAverageMinPrice();

    @Query("SELECT a FROM ArtistProfile a WHERE a.studio IS NULL AND a.isAvailable = true")
    List<ArtistProfile> findIndependentArtists();

    // Modifying Queries

    @Modifying
    @Transactional
    @Query("UPDATE ArtistProfile a SET a.isAvailable = :isAvailable WHERE a.id = :artistId")
    void updateAvailability(@Param("artistId") Long artistId,
                            @Param("isAvailable") Boolean isAvailable);

    @Modifying
    @Transactional
    @Query("UPDATE ArtistProfile a SET a.studio = :studio WHERE a.id = :artistId")
    void assignToStudio(@Param("artistId") Long artistId,
                        @Param("studio") Studio studio);

    @Modifying
    @Transactional
    @Query("UPDATE ArtistProfile a SET a.studio = null WHERE a.id = :artistId")
    void removeFromStudio(@Param("artistId") Long artistId);

    // Native SQL Queries

    // Pagination
}
