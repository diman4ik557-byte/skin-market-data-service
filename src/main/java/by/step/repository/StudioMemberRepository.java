package by.step.repository;

import by.step.entity.ArtistProfile;
import by.step.entity.Studio;
import by.step.entity.StudioMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface StudioMemberRepository extends JpaRepository<StudioMember,Long> {

    // JPA Methods

    List<StudioMember> findByStudio(Studio studio);

    List<StudioMember> findByMember(ArtistProfile member);

    Optional<StudioMember> findByStudioAndMember(Studio studio, ArtistProfile member);

    List<StudioMember> findByStudioAndRole(Studio studio, String role);

    boolean existsByStudioAndMember(Studio studio, ArtistProfile member);

    // HQL Queries

    @Query("SELECT sm.member FROM StudioMember sm WHERE sm.studio.id = :studioId AND sm.role = 'MANAGER'")
    List<ArtistProfile> findManagersByStudioId(@Param("studioId") Long studioId);

    @Query("SELECT sm FROM StudioMember sm WHERE sm.member.id = :artistId")
    List<StudioMember> findByArtistId(@Param("artistId") Long artistId);

    // Modifying Queries

    @Modifying
    @Transactional
    @Query("UPDATE StudioMember sm SET sm.role = :role WHERE sm.id = :memberId")
    void updateRole(@Param("memberId") Long memberId,
                    @Param("role") String role);

    @Modifying
    @Transactional
    void deleteByStudioAndMember(Studio studio, ArtistProfile member);

    // Native SQL Queries

    // Pagination


}
