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

/**
 * Реализация сервиса для работы с профилями художников.
 * Предоставляет операции создания, поиска, фильтрации и обновления профилей художников,
 * а также управления их принадлежностью к студиям.
 *
 * @author Skin Market Team
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArtistProfileServiceImpl implements ArtistProfileService {

    private final ArtistProfileRepository artistProfileRepository;
    private final ProfileRepository profileRepository;
    private final StudioRepository studioRepository;
    private final ArtistProfileMapper artistProfileMapper = ArtistProfileMapper.INSTANCE;

    /**
     * Создаёт профиль художника для пользователя.
     * Автоматически устанавливает статус isAvailable = true.
     *
     * @param userId идентификатор пользователя
     * @param styles стили художника (например, "классический, аниме")
     * @param minPrice минимальная цена за работу
     * @param averageTime среднее время выполнения заказа в днях
     * @return ArtistProfileDto созданного профиля художника
     * @throws IllegalArgumentException если профиль пользователя не найден
     *         или профиль художника уже существует
     */
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

    /**
     * Находит профиль художника по идентификатору пользователя.
     *
     * @param userId идентификатор пользователя
     * @return Optional с ArtistProfileDto если найден, иначе пустой Optional
     */
    @Override
    public Optional<ArtistProfileDto> findByUserId(Long userId) {
        return artistProfileRepository.findByProfileUserId(userId)
                .map(artistProfileMapper::toDto);
    }

    /**
     * Возвращает список всех художников.
     *
     * @return список ArtistProfileDto всех художников
     */
    @Override
    public List<ArtistProfileDto> findAllArtists() {
        return artistProfileRepository.findAll().stream()
                .map(artistProfileMapper::toDto)
                .toList();
    }

    /**
     * Возвращает список доступных для заказа художников.
     *
     * @return список ArtistProfileDto доступных художников
     */
    @Override
    public List<ArtistProfileDto> findAvailableArtists() {
        return artistProfileRepository.findByIsAvailableTrue().stream()
                .map(artistProfileMapper::toDto)
                .toList();
    }

    /**
     * Находит художников по стилю (частичное совпадение).
     *
     * @param style стиль для поиска
     * @return список ArtistProfileDto художников с указанным стилем
     */
    @Override
    public List<ArtistProfileDto> findArtistsByStyle(String style) {
        return artistProfileRepository.findByStylesContaining(style).stream()
                .map(artistProfileMapper::toDto)
                .toList();
    }

    /**
     * Находит художников с минимальной ценой не выше указанной.
     *
     * @param maxPrice максимальная допустимая цена
     * @return список ArtistProfileDto художников в ценовом диапазоне
     */
    @Override
    public List<ArtistProfileDto> findArtistsByMaxPrice(BigDecimal maxPrice) {
        return artistProfileRepository.findByMinPriceLessThanEqual(maxPrice).stream()
                .map(artistProfileMapper::toDto)
                .toList();
    }

    /**
     * Находит художников по комбинации фильтров.
     * Все фильтры опциональны.
     *
     * @param style стиль (может быть null)
     * @param maxPrice максимальная цена (может быть null)
     * @param isAvailable доступность (может быть null)
     * @return список ArtistProfileDto отфильтрованных художников
     */
    @Override
    public List<ArtistProfileDto> findArtistsByFilters(String style, BigDecimal maxPrice, Boolean isAvailable) {
        return artistProfileRepository.findByFilters(style, maxPrice, isAvailable).stream()
                .map(artistProfileMapper::toDto)
                .toList();
    }

    /**
     * Обновляет список стилей художника.
     *
     * @param artistId идентификатор профиля художника
     * @param styles новый список стилей
     * @return ArtistProfileDto с обновлёнными данными
     * @throws IllegalArgumentException если профиль художника не найден
     */
    @Override
    @Transactional
    public ArtistProfileDto updateStyles(Long artistId, String styles) {
        ArtistProfile artist = artistProfileRepository.findById(artistId)
                .orElseThrow(() -> new IllegalArgumentException("Профиль художника не найден - " + artistId));

        artist.setStyles(styles);
        ArtistProfile updated = artistProfileRepository.save(artist);
        return artistProfileMapper.toDto(updated);
    }

    /**
     * Обновляет минимальную цену художника.
     *
     * @param artistId идентификатор профиля художника
     * @param minPrice новая минимальная цена
     * @return ArtistProfileDto с обновлёнными данными
     * @throws IllegalArgumentException если профиль художника не найден
     */
    @Override
    @Transactional
    public ArtistProfileDto updateMinPrice(Long artistId, BigDecimal minPrice) {
        ArtistProfile artist = artistProfileRepository.findById(artistId)
                .orElseThrow(() -> new IllegalArgumentException("Профиль художника не найден - " + artistId));

        artist.setMinPrice(minPrice);
        ArtistProfile updated = artistProfileRepository.save(artist);
        return artistProfileMapper.toDto(updated);

    }

    /**
     * Обновляет среднее время выполнения заказа.
     *
     * @param artistId идентификатор профиля художника
     * @param averageTime новое среднее время в днях
     * @return ArtistProfileDto с обновлёнными данными
     * @throws IllegalArgumentException если профиль художника не найден
     */
    @Override
    @Transactional
    public ArtistProfileDto updateAverageTime(Long artistId, Integer averageTime) {
        ArtistProfile artist = artistProfileRepository.findById(artistId)
                .orElseThrow(() -> new IllegalArgumentException("Профиль художника не найден - " + artistId));

        artist.setAverageTime(averageTime);
        ArtistProfile updated = artistProfileRepository.save(artist);
        return artistProfileMapper.toDto(updated);
    }

    /**
     * Обновляет статус доступности художника.
     *
     * @param artistId идентификатор профиля художника
     * @param isAvailable true - доступен для заказов, false - недоступен
     */
    @Override
    @Transactional
    public void updateAvailability(Long artistId, boolean isAvailable) {
        artistProfileRepository.updateAvailability(artistId, isAvailable);
    }


    /**
     * Назначает художника в студию.
     *
     * @param artistId идентификатор профиля художника
     * @param studioId идентификатор студии
     * @throws IllegalArgumentException если художник или студия не найдены
     */
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

    /**
     * Удаляет художника из студии.
     *
     * @param artistId идентификатор профиля художника
     */
    @Override
    @Transactional
    public void removeFromStudio(Long artistId) {
        artistProfileRepository.removeFromStudio(artistId);
    }

}
