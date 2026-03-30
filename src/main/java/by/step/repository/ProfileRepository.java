package by.step.repository;

import by.step.entity.Profile;
import by.step.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile,Long> {

    // JPA Methods

    Optional<Profile> findByUser(User user);

    Optional<Profile> findByUserId(Long userId);

    List<Profile> findByIsArtistTrue();

    List<Profile> findByIsStudioTrue();

    boolean existsByUserId(Long userId);


    // HQL Queries

    @Query("SELECT p FROM Profile p WHERE p.isArtist = true AND p.user.balance > :minBalance")
    List<Profile> findArtistsWithMinBalance(@Param("minBalance") java.math.BigDecimal minBalance);

    @Query("SELECT p FROM Profile p WHERE p.isArtist = true AND p.bio LIKE %:keyword%")
    List<Profile> searchArtistsByBio(@Param("keyword") String keyword);

    // Modifying Queries

    @Modifying
    @Transactional
    @Query("UPDATE Profile p SET p.bio = :bio WHERE p.id = :profileId")
    void updateBio(@Param("profileId") Long profileId,
                   @Param("bio") String bio);

    @Modifying
    @Transactional
    @Query("UPDATE Profile p SET p.isArtist = :isArtist WHERE p.id = :profileId")
    void setArtistStatus(@Param("profileId") Long profileId,
                         @Param("isArtist") boolean isArtist);

    @Modifying
    @Transactional
    @Query("UPDATE Profile p SET p.isStudio = :isStudio WHERE p.id = :profileId")
    void setStudioStatus(@Param("profileId") Long profileId,
                         @Param("isStudio") boolean isStudio);

    // Native SQL Queries

    // Pagination
}
