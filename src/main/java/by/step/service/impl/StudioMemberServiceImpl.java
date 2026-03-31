package by.step.service.impl;

import by.step.dto.StudioMemberDto;
import by.step.entity.ArtistProfile;
import by.step.entity.Studio;
import by.step.entity.StudioMember;
import by.step.entity.enums.StudioRole;
import by.step.mapper.StudioMemberMapper;
import by.step.repository.ArtistProfileRepository;
import by.step.repository.StudioMemberRepository;
import by.step.repository.StudioRepository;
import by.step.service.StudioMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudioMemberServiceImpl implements StudioMemberService {

    private final StudioMemberRepository studioMemberRepository;
    private final StudioRepository studioRepository;
    private final ArtistProfileRepository artistProfileRepository;
    private final StudioMemberMapper studioMemberMapper = StudioMemberMapper.INSTANCE;

    @Override
    public StudioMemberDto addMember(Long studioId, Long artistId, StudioRole role) {
        Studio studio = studioRepository.findById(studioId)
                .orElseThrow(() -> new IllegalArgumentException("Студия не найдена - " + studioId));

        ArtistProfile artist = artistProfileRepository.findById(artistId)
                .orElseThrow(() -> new IllegalArgumentException("Художник не найден - " + artistId));

        if (studioMemberRepository.existsByStudioAndMember(studio, artist)) {
            throw new IllegalArgumentException("Художник уже в студии");
        }

        StudioMember member = StudioMember.builder()
                .studio(studio)
                .member(artist)
                .role(role)
                .joinedAt(LocalDateTime.now())
                .build();

        StudioMember saved = studioMemberRepository.save(member);
        return studioMemberMapper.toDto(saved);
    }

    @Override
    public Optional<StudioMemberDto> findByStudioAndArtist(Long studioId, Long artistId) {
        Studio studio = studioRepository.findById(studioId).orElse(null);
        ArtistProfile artist = artistProfileRepository.findById(artistId).orElse(null);

        if (studio == null || artist == null) {
            return Optional.empty();
        }

        return studioMemberRepository.findByStudioAndMember(studio, artist)
                .map(studioMemberMapper::toDto);
    }

    @Override
    public Optional<StudioMemberDto> findByStudioIdAndMemberId(Long studioId, Long memberId) {
        return studioMemberRepository.findByStudioIdAndMemberId(studioId, memberId)
                .map(studioMemberMapper::toDto);
    }

    @Override
    public List<StudioMemberDto> findByStudio(Long studioId) {
        Studio studio = studioRepository.findById(studioId)
                .orElseThrow(() -> new IllegalArgumentException("Студия не найдена - " + studioId));

        return studioMemberRepository.findByStudio(studio).stream()
                .map(studioMemberMapper::toDto)
                .toList();
    }

    @Override
    public List<StudioMemberDto> findByArtist(Long artistId) {
        ArtistProfile artist = artistProfileRepository.findById(artistId)
                .orElseThrow(() -> new IllegalArgumentException("Художник не найден - " + artistId));

        return studioMemberRepository.findByMember(artist).stream()
                .map(studioMemberMapper::toDto)
                .toList();
    }

    @Override
    public List<StudioMemberDto> findByStudioAndRole(Long studioId, String role) {
        Studio studio = studioRepository.findById(studioId)
                .orElseThrow(() -> new IllegalArgumentException("Студия не найдена - " + studioId));

        return studioMemberRepository.findByStudioAndRole(studio, role).stream()
                .map(studioMemberMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public void updateRole(Long memberId, StudioRole role) {
        StudioMember member = studioMemberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Участник студии не найден - " + memberId));

        member.setRole(role);
        studioMemberRepository.save(member);

    }

    @Override
    @Transactional
    public void removeMember(Long studioId, Long artistId) {
        Studio studio = studioRepository.findById(studioId)
                .orElseThrow(() -> new IllegalArgumentException("Студия не найдена - " + studioId));

        ArtistProfile artist = artistProfileRepository.findById(artistId)
                .orElseThrow(() -> new IllegalArgumentException("Художник не найден - " + artistId));

        studioMemberRepository.deleteByStudioAndMember(studio, artist);

    }

    @Override
    public boolean isMember(Long studioId, Long artistId) {
        Studio studio = studioRepository.findById(studioId).orElse(null);
        ArtistProfile artist = artistProfileRepository.findById(artistId).orElse(null);

        if (studio == null || artist == null) {
            return false;
        }

        return studioMemberRepository.existsByStudioAndMember(studio, artist);
    }

    @Override
    public boolean isManager(Long studioId, Long artistId) {
        Studio studio = studioRepository.findById(studioId).orElse(null);
        ArtistProfile artist = artistProfileRepository.findById(artistId).orElse(null);

        if (studio == null || artist == null) {
            return false;
        }

        return studioMemberRepository.findByStudioAndMember(studio, artist)
                .map(member -> StudioRole.MANAGER.equals(member.getRole()))
                .orElse(false);
    }

}
