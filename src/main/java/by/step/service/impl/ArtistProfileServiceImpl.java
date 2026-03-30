package by.step.service.impl;

import by.step.dto.ArtistProfileDto;
import by.step.entity.ArtistProfile;
import by.step.entity.Profile;
import by.step.entity.Studio;
import by.step.mapper.ArtistProfileMapper;
import by.step.repository.ArtistProfileRepository;
import by.step.repository.ProfileRepository;
import by.step.repository.StudioRepository;
import by.step.service.ArtistProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArtistProfileServiceImpl implements ArtistProfileService {

    private final ArtistProfileRepository artistProfileRepository;
    private final ProfileRepository profileRepository;
    private final StudioRepository studioRepository;
    private final ArtistProfileMapper artistProfileMapper = ArtistProfileMapper.INSTANCE;

    @Override
    @Transactional
    public ArtistProfileDto createArtistProfile(Long userId, String styles, BigDecimal minPrice, Integer averageTime) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Профиль пользователя не найден - " + userId));

        if (artistProfileRepository.findByProfile(profile).isPresent()) {
            throw new IllegalArgumentException("Профиль художника уже существует - " + userId);
        }

        ArtistProfile artistProfile = ArtistProfile.builder()
                .profile(profile)
                .styles(styles)
                .minPrice(minPrice)
                .averageTime(averageTime)
                .isAvailable(true)
                .build();

        profile.setIsArtist(true);
        profileRepository.save(profile);

        ArtistProfile saved = artistProfileRepository.save(artistProfile);
        return artistProfileMapper.toDto(saved);
    }

    @Override
    public Optional<ArtistProfileDto> findByUserId(Long userId) {
        return artistProfileRepository.findByProfileUserId(userId)
                .map(artistProfileMapper::toDto);
    }

    @Override
    public List<ArtistProfileDto> findAllArtists() {
        return artistProfileRepository.findAll().stream()
                .map(artistProfileMapper::toDto)
                .toList();
    }

    @Override
    public List<ArtistProfileDto> findAvailableArtists() {
        return artistProfileRepository.findByIsAvailableTrue().stream()
                .map(artistProfileMapper::toDto)
                .toList();
    }

    @Override
    public List<ArtistProfileDto> findArtistsByStyle(String style) {
        return artistProfileRepository.findByStylesContaining(style).stream()
                .map(artistProfileMapper::toDto)
                .toList();
    }

    @Override
    public List<ArtistProfileDto> findArtistsByMaxPrice(BigDecimal maxPrice) {
        return artistProfileRepository.findByMinPriceLessThanEqual(maxPrice).stream()
                .map(artistProfileMapper::toDto)
                .toList();
    }

    @Override
    public List<ArtistProfileDto> findArtistsByFilters(String style, BigDecimal maxPrice, Boolean isAvailable) {
        return artistProfileRepository.findByFilters(style, maxPrice, isAvailable).stream()
                .map(artistProfileMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public ArtistProfileDto updateStyles(Long artistId, String styles) {
        ArtistProfile artist = artistProfileRepository.findById(artistId)
                .orElseThrow(() -> new IllegalArgumentException("Профиль художника не найден - " + artistId));

        artist.setStyles(styles);
        ArtistProfile updated = artistProfileRepository.save(artist);
        return artistProfileMapper.toDto(updated);
    }

    @Override
    @Transactional
    public ArtistProfileDto updateMinPrice(Long artistId, BigDecimal minPrice) {
        ArtistProfile artist = artistProfileRepository.findById(artistId)
                .orElseThrow(() -> new IllegalArgumentException("Профиль художника не найден - " + artistId));

        artist.setMinPrice(minPrice);
        ArtistProfile updated = artistProfileRepository.save(artist);
        return artistProfileMapper.toDto(updated);

    }

    @Override
    @Transactional
    public ArtistProfileDto updateAverageTime(Long artistId, Integer averageTime) {
        ArtistProfile artist = artistProfileRepository.findById(artistId)
                .orElseThrow(() -> new IllegalArgumentException("Профиль художника не найден - " + artistId));

        artist.setAverageTime(averageTime);
        ArtistProfile updated = artistProfileRepository.save(artist);
        return artistProfileMapper.toDto(updated);
    }

    @Override
    @Transactional
    public void updateAvailability(Long artistId, boolean isAvailable) {
        artistProfileRepository.updateAvailability(artistId, isAvailable);
    }

    @Override
    @Transactional
    public void assignToStudio(Long artistId, Long studioId) {
        ArtistProfile artist = artistProfileRepository.findById(artistId)
                .orElseThrow(() -> new IllegalArgumentException("Профиль художника не найден - " + artistId));

        Studio studio = studioRepository.findById(studioId)
                .orElseThrow(() -> new IllegalArgumentException("Студия не найдена - " + studioId));

        artist.setStudio(studio);
        artistProfileRepository.save(artist);

    }

    @Override
    @Transactional
    public void removeFromStudio(Long artistId) {
        artistProfileRepository.removeFromStudio(artistId);
    }

}
