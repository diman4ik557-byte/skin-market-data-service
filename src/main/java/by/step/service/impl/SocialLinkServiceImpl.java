package by.step.service.impl;

import by.step.dto.SocialLinkDto;
import by.step.entity.Profile;
import by.step.entity.SocialLink;
import by.step.enums.SocialPlatform;
import by.step.mapper.SocialLinkMapper;
import by.step.repository.ProfileRepository;
import by.step.repository.SocialLinkRepository;
import by.step.service.SocialLinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Реализация сервиса для работы с социальными ссылками профиля.
 * Предоставляет операции добавления, поиска, обновления и удаления ссылок
 * на социальные платформы (VK, Telegram, Instagram, Discord, NameMC).
 *
 * @author Skin Market Team
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SocialLinkServiceImpl implements SocialLinkService {

    private final SocialLinkRepository socialLinkRepository;
    private final ProfileRepository profileRepository;
    private final SocialLinkMapper socialLinkMapper = SocialLinkMapper.INSTANCE;

    /**
     * Добавляет новую социальную ссылку к профилю.
     * Первая добавленная ссылка автоматически становится основной.
     *
     * @param profileId идентификатор профиля
     * @param platform платформа (VK, TELEGRAM, INSTAGRAM, DISCORD, NAMEMC)
     * @param userIdentifier идентификатор пользователя на платформе
     * @return SocialLinkDto добавленной ссылки
     * @throws IllegalArgumentException если профиль не найден или ссылка уже существует
     */
    @Override
    @Transactional
    public SocialLinkDto addSocialLink(Long profileId, SocialPlatform platform, String userIdentifier) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("Профиль не найден - " + profileId));

        if (socialLinkRepository.findByProfileAndPlatform(profile, platform).isPresent()) {
            throw new IllegalArgumentException("Ссыылка на платформу" + platform + " уже указана");
        }

        boolean isPrimary = socialLinkRepository.findByProfile(profile).isEmpty();

        SocialLink link = SocialLink.builder()
                .profile(profile)
                .platform(platform)
                .userIdentifier(userIdentifier)
                .isPrimary(isPrimary)
                .build();

        SocialLink saved = socialLinkRepository.save(link);
        return socialLinkMapper.toDto(saved);
    }

    /**
     * Находит социальную ссылку по профилю и платформе.
     *
     * @param profileId идентификатор профиля
     * @param platform платформа для поиска
     * @return Optional с SocialLinkDto если найдена, иначе пустой Optional
     */
    @Override
    public Optional<SocialLinkDto> findByProfileAndPlatform(Long profileId, SocialPlatform platform) {
        Profile profile = profileRepository.findById(profileId).orElse(null);
        if (profile == null) {
            return Optional.empty();
        }
        return socialLinkRepository.findByProfileAndPlatform(profile, platform)
                .map(socialLinkMapper::toDto);
    }

    /**
     * Возвращает все социальные ссылки профиля.
     *
     * @param profileId идентификатор профиля
     * @return список SocialLinkDto всех ссылок профиля
     * @throws IllegalArgumentException если профиль не найден
     */
    @Override
    public List<SocialLinkDto> findByProfile(Long profileId) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("Профиль не найден " + profileId));

        return socialLinkRepository.findByProfile(profile).stream()
                .map(socialLinkMapper::toDto)
                .toList();
    }

    /**
     * Возвращает основные социальные ссылки профиля.
     * Основная ссылка отображается первой в интерфейсе пользователя.
     *
     * @param profileId идентификатор профиля
     * @return список основных SocialLinkDto
     * @throws IllegalArgumentException если профиль не найден
     */
    @Override
    public List<SocialLinkDto> findPrimaryLinks(Long profileId) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found: " + profileId));

        return socialLinkRepository.findByProfileAndIsPrimaryTrue(profile)
                .stream()
                .map(socialLinkMapper::toDto)
                .toList();
    }

    /**
     * Устанавливает указанную ссылку как основную.
     * Сбрасывает флаг isPrimary у всех остальных ссылок профиля.
     *
     * @param linkId идентификатор социальной ссылки
     * @throws IllegalArgumentException если ссылка не найдена
     */
    @Override
    @Transactional
    public void setPrimary(Long linkId) {
        SocialLink link = socialLinkRepository.findById(linkId)
                .orElseThrow(() -> new IllegalArgumentException("Ссылка не найдена - " + linkId));

        socialLinkRepository.resetPrimaryFlag(link.getProfile());

        link.setIsPrimary(true);
        socialLinkRepository.save(link);
    }

    /**
     * Обновляет идентификатор пользователя в социальной ссылке.
     *
     * @param linkId идентификатор социальной ссылки
     * @param userIdentifier новый идентификатор пользователя
     * @throws IllegalArgumentException если ссылка не найдена
     */
    @Override
    @Transactional
    public void updateUserIdentifier(Long linkId, String userIdentifier) {
        SocialLink link = socialLinkRepository.findById(linkId)
                .orElseThrow(() -> new IllegalArgumentException("Ссылка не найдена - " + linkId));

        link.setUserIdentifier(userIdentifier);
        socialLinkRepository.save(link);
    }

    /**
     * Удаляет социальную ссылку.
     *
     * @param linkId идентификатор социальной ссылки
     * @throws IllegalArgumentException если ссылка не найдена
     */
    @Override
    @Transactional
    public void removeSocialLink(Long linkId) {
        SocialLink link = socialLinkRepository.findById(linkId)
                .orElseThrow(() -> new IllegalArgumentException("Ссылка не найдена - " + linkId));

        socialLinkRepository.delete(link);
    }

    /**
     * Удаляет все социальные ссылки профиля.
     *
     * @param profileId идентификатор профиля
     * @throws IllegalArgumentException если профиль не найден
     */
    @Override
    @Transactional
    public void removeAllByProfile(Long profileId) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("Профиль не найден " + profileId));

        socialLinkRepository.deleteByProfile(profile);
    }

    /**
     * Формирует полный URL социальной ссылки на основе платформы и идентификатора.
     *
     * @param platform платформа (определяет префикс URL)
     * @param userIdentifier идентификатор пользователя
     * @return полный URL для перехода на профиль пользователя
     */
    @Override
    public String getFullUrl(SocialPlatform platform, String userIdentifier) {
        return platform.getUrlPrefix() + userIdentifier;
    }

}
