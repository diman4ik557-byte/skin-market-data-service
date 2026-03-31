package by.step.service.impl;


import by.step.dto.ArtistProfileDto;
import by.step.dto.StudioDto;
import by.step.dto.StudioMemberDto;
import by.step.entity.*;
import by.step.entity.enums.StudioRole;
import by.step.mapper.ArtistProfileMapper;
import by.step.mapper.StudioMapper;
import by.step.mapper.StudioMemberMapper;
import by.step.repository.*;
import by.step.service.StudioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudioServiceImpl implements StudioService {

    private final StudioRepository studioRepository;
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final ArtistProfileRepository artistProfileRepository;
    private final StudioMemberRepository studioMemberRepository;
    private final StudioMapper studioMapper = StudioMapper.INSTANCE;
    private final StudioMemberMapper studioMemberMapper = StudioMemberMapper.INSTANCE;
    private final ArtistProfileMapper artistProfileMapper = ArtistProfileMapper.INSTANCE;

    @Override
    @Transactional
    public StudioDto createStudio(Long userId, String name, String description) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден - " + userId));

        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Профиль пользователя не найден - " + userId));

        if (studioRepository.findByProfile(profile).isPresent()) {
            throw new IllegalArgumentException("Студия уже существует");
        }

        Studio studio = Studio.builder()
                .profile(profile)
                .name(name)
                .description(description)
                .foundedAt(LocalDate.now())
                .manager(user)
                .build();

        profile.setIsStudio(true);
        profileRepository.save(profile);

        Studio saved = studioRepository.save(studio);
        return studioMapper.toDto(saved);
    }

    @Override
    public Optional<StudioDto> findById(Long studioId) {
        return studioRepository.findById(studioId)
                .map(studioMapper::toDto);
    }

    @Override
    public Optional<StudioDto> findByUserId(Long userId) {
        return studioRepository.findByProfileUserId(userId)
                .map(studioMapper::toDto);
    }

    @Override
    public List<StudioDto> findAllStudios() {
        return studioRepository.findAll().stream()
                .map(studioMapper::toDto)
                .toList();
    }

    @Override
    public List<StudioDto> findStudiosByName(String name) {
        return studioRepository.findByNameContainingIgnoreCase(name).stream()
                .map(studioMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public StudioDto updateDescription(Long studioId, String description) {
        Studio studio = studioRepository.findById(studioId)
                .orElseThrow(() -> new IllegalArgumentException("Студия не найдена - " + studioId));

        studio.setDescription(description);
        Studio updated = studioRepository.save(studio);
        return studioMapper.toDto(updated);
    }

    @Override
    @Transactional
    public void addMember(Long studioId, Long artistId, String role) {
        Studio studio = studioRepository.findById(studioId)
                .orElseThrow(() -> new IllegalArgumentException("Студия не найдена - " + studioId));

        ArtistProfile artist = artistProfileRepository.findById(artistId)
                .orElseThrow(() -> new IllegalArgumentException("Художник не найден - " + artistId));

        if (studioMemberRepository.existsByStudioAndMember(studio, artist)) {
            throw new IllegalArgumentException("Художник уже в студии");
        }

        artist.setStudio(studio);
        artistProfileRepository.save(artist);
    }

    @Override
    @Transactional
    public void removeMember(Long studioId, Long artistId) {
        Studio studio = studioRepository.findById(studioId)
                .orElseThrow(() -> new IllegalArgumentException("Студия не найдена - " + studioId));

        ArtistProfile artist = artistProfileRepository.findById(artistId)
                .orElseThrow(() -> new IllegalArgumentException("Художник не найден - " + artistId));

        if (artist.getStudio() == null || !artist.getStudio().getId().equals(studioId)) {
            throw new IllegalArgumentException("Художник не состоит в студии");
        }

        artist.setStudio(null);
        artistProfileRepository.save(artist);
    }

    @Override
    @Transactional
    public StudioMemberDto updateMemberRole(Long studioId, Long artistId, String role) {
        StudioMember member = studioMemberRepository.findByStudioIdAndMemberId(studioId, artistId)
                .orElseThrow(() -> new IllegalArgumentException("Участник не найден в этой студии"));

        try {
            member.setRole(by.step.entity.enums.StudioRole.valueOf(role.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Недопустимая роль: " + role);
        }

        StudioMember saved = studioMemberRepository.save(member);
        return studioMemberMapper.toDto(saved);
    }

    @Override
    public List<ArtistProfileDto> getStudioMembers(Long studioId) {
        Studio studio = studioRepository.findById(studioId)
                .orElseThrow(() -> new IllegalArgumentException("Студия не найдена - " + studioId));

        return artistProfileRepository.findByStudio(studio).stream()
                .map(artistProfileMapper::toDto)
                .toList();
    }

    @Override
    public long getMemberCount(Long studioId) {
        Studio studio = studioRepository.findById(studioId)
                .orElseThrow(() -> new IllegalArgumentException("Студия не найдена - " + studioId));

        return artistProfileRepository.findByStudio(studio).size();
    }

    @Override
    @Transactional
    public void deleteStudio(Long studioId) {
        Studio studio = studioRepository.findById(studioId)
                .orElseThrow(() -> new IllegalArgumentException("Студия не найдена - " + studioId));

        List<ArtistProfile> members = artistProfileRepository.findByStudio(studio);
        for (ArtistProfile member : members) {
            member.setStudio(null);
            artistProfileRepository.save(member);
        }

        studioRepository.delete(studio);

    }

}
