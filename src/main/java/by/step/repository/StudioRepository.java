package by.step.repository;

import by.step.entity.Profile;
import by.step.entity.Studio;
import by.step.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudioRepository extends JpaRepository<Studio,Long> {

    // JPA Methods

    Optional<Studio> findByProfile(Profile profile);

    Optional<Studio> findByProfileUserId(Long userId);

    Optional<Studio> findByManager(User manager);

    List<Studio> findByNameContainingIgnoreCase(String name);

    // HQL Queries

    @Query("SELECT s FROM Studio s WHERE s.manager.id = :managerId")
    List<Studio> findByManagerId(@Param("managerId") Long managerId);

    @Query("SELECT COUNT(m) FROM StudioMember m WHERE m.studio.id = :studioId")
    long countMembers(@Param("studioId") Long studioId);

    @Query("SELECT s FROM Studio s WHERE s.profile.bio LIKE %:keyword%")
    List<Studio> searchByDescription(@Param("keyword") String keyword);

    // Modifying Queries

    // Native SQL Queries

    // Pagination

}
