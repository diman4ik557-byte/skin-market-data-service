package by.step.service.impl;

import by.step.dto.ProfileDto;
import by.step.entity.Profile;
import by.step.entity.User;
import by.step.mapper.ProfileMapper;
import by.step.repository.ProfileRepository;
import by.step.repository.UserRepository;
import by.step.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Реализация сервиса для работы с профилями пользователей.
 * Предоставляет операции создания, обновления и проверки статусов профилей.
 *
 * @author Skin Market Team
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileServiceImpl implements ProfileService {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final ProfileMapper profileMapper = ProfileMapper.INSTANCE;


    /**
     * Создаёт новый профиль для пользователя.
     * Профиль создаётся с пустым bio и статусами isArtist=false, isStudio=false.
     *
     * @param userId идентификатор пользователя
     * @return ProfileDto созданного профиля
     * @throws IllegalArgumentException если пользователь не найден или профиль уже существует
     */
    @Override
    @Transactional
    public ProfileDto createProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден - " +userId));

        if (profileRepository.findByUserId(userId).isPresent()) {
            throw new IllegalArgumentException("Профиль пользователя уже существует - " +userId);
        }

        Profile profile = Profile.builder()
                .user(user)
                .bio("")
                .isArtist(false)
                .isStudio(false)
                .build();

        Profile savedProfile = profileRepository.save(profile);
        return profileMapper.toDto(savedProfile);
    }

    /**
     * Находит профиль по идентификатору пользователя.
     *
     * @param userId идентификатор пользователя
     * @return Optional с ProfileDto если профиль найден, иначе пустой Optional
     */
    @Override
    public Optional<ProfileDto> findByUserId(Long userId) {
        return profileRepository.findByUserId(userId)
                .map(profileMapper::toDto);
    }


    /**
     * Обновляет текстовое описание (bio) профиля.
     *
     * @param profileId идентификатор профиля
     * @param bio новое текстовое описание
     * @return ProfileDto с обновлёнными данными
     * @throws IllegalArgumentException если профиль не найден
     */
    @Override
    @Transactional
    public ProfileDto updateBio(Long profileId, String bio) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("Профиль не найден - " + profileId));

        profile.setBio(bio);
        Profile updatedProfile = profileRepository.save(profile);
        return profileMapper.toDto(updatedProfile);
    }

    /**
     * Устанавливает статус художника для профиля.
     *
     * @param profileId идентификатор профиля
     * @param isArtist true - пользователь становится художником, false - снимает статус
     * @throws IllegalArgumentException если профиль не найден
     */
    @Override
    @Transactional
    public void setArtistStatus(Long profileId, boolean isArtist) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("Профиль не найден - " + profileId));

        profile.setIsArtist(isArtist);
        profileRepository.save(profile);
    }

    /**
     * Устанавливает статус студии для профиля.
     *
     * @param profileId идентификатор профиля
     * @param isStudio true - пользователь становится студией, false - снимает статус
     * @throws IllegalArgumentException если профиль не найден
     */
    @Override
    @Transactional
    public void setStudioStatus(Long profileId, boolean isStudio) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("Профиль не найден - " + profileId));

        profile.setIsStudio(isStudio);
        profileRepository.save(profile);
    }

    /**
     * Проверяет, является ли пользователь художником.
     *
     * @param userId идентификатор пользователя
     * @return true если пользователь художник, false в противном случае
     */
    @Override
    public boolean isArtist(Long userId) {
        return profileRepository.findByUserId(userId)
                .map(Profile::getIsArtist)
                .orElse(false);
    }

    /**
     * Проверяет, является ли пользователь студией.
     *
     * @param userId идентификатор пользователя
     * @return true если пользователь студия, false в противном случае
     */
    @Override
    public boolean isStudio(Long userId) {
        return profileRepository.findByUserId(userId)
                .map(Profile::getIsStudio)
                .orElse(false);
    }

}
