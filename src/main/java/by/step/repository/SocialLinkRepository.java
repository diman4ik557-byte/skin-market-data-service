package by.step.repository;

import by.step.entity.Profile;
import by.step.entity.SocialLink;
import by.step.entity.enums.SocialPlatform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface SocialLinkRepository extends JpaRepository<SocialLink,Long> {

    // JPA Repository

    List<SocialLink> findByProfile(Profile profile);

    Optional<SocialLink> findByProfileAndPlatform(Profile profile,
                                                  SocialPlatform platform);

    List<SocialLink> findByProfileAndIsPrimaryTrue(Profile profile);

    void deleteByProfile(Profile profile);

    // HQL Queries

    @Query("SELECT s FROM SocialLink s WHERE s.profile.user.id = :userId")
    List<SocialLink> findByUserId(@Param("userId") Long userId);

    @Query("SELECT s FROM SocialLink s WHERE s.platform = :platform")
    List<SocialLink> findByPlatform(@Param("platform") SocialPlatform platform);

    // Modifying Queries

    @Modifying
    @Transactional
    @Query("UPDATE SocialLink s SET s.isPrimary = false WHERE s.profile = :profile")
    void resetPrimaryFlag(@Param("profile") Profile profile);

    @Modifying
    @Transactional
    @Query("UPDATE SocialLink s SET s.isPrimary = true WHERE s.id = :linkId")
    void setPrimary(@Param("linkId") Long linkId);

    // Native SQL Queries

    // Pagination

}
