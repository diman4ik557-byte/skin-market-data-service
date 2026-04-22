package by.step.service.impl;

import by.step.dto.ArtistProfileDto;
import by.step.dto.StudioDto;
import by.step.dto.StudioMemberDto;
import by.step.entity.*;
import by.step.enums.StudioMemberStatus;
import by.step.enums.StudioRole;
import by.step.mapper.ArtistProfileMapper;
import by.step.mapper.StudioMapper;
import by.step.mapper.StudioMemberMapper;
import by.step.repository.*;
import by.step.service.StudioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Реализация сервиса для работы со студиями художников.
 * Управляет созданием студий, участниками, заявками и правами доступа.
 *
 * @author Skin Market Team
 * @version 1.0
 */
@Slf4j
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

    /**
     * Создает новую студию.
     * Создатель автоматически становится менеджером студии.
     *
     * @param userId ID пользователя-создателя (должен быть художником)
     * @param name название студии
     * @param description описание студии
     * @return StudioDto созданной студии
     * @throws IllegalArgumentException если пользователь не найден,
     *         профиль не найден, или студия уже существует
     */
    @Override
    @Transactional
    public StudioDto createStudio(Long userId, String name, String description) {
        log.info("=== НАЧАЛО СОЗДАНИЯ СТУДИИ ===");
        log.info("Пользователь ID: {}, Название: {}", userId, name);
        long startTime = System.currentTimeMillis();

        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        log.error("Пользователь с id={} не найден", userId);
                        return new IllegalArgumentException("Пользователь не найден - " + userId);
                    });
            log.debug("Пользователь найден: username={}, роль={}", user.getUsername(), user.getRole());

            Profile profile = profileRepository.findByUserId(userId)
                    .orElseThrow(() -> {
                        log.error("Профиль пользователя {} не найден", userId);
                        return new IllegalArgumentException("Профиль пользователя не найден - " + userId);
                    });
            log.debug("Профиль пользователя найден");

            if (studioRepository.findByProfile(profile).isPresent()) {
                log.warn("Студия для пользователя {} уже существует", userId);
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
            log.debug("Профиль обновлен: isStudio=true");

            Studio saved = studioRepository.save(studio);
            log.debug("Студия сохранена в БД с id={}", saved.getId());

            ArtistProfile artist = getOrCreateArtistProfile(profile);
            addFounderAsManager(saved, artist);

            long elapsedTime = System.currentTimeMillis() - startTime;
            log.info("Студия '{}' создана успешно за {} мс", name, elapsedTime);
            log.info("=== ЗАВЕРШЕНИЕ СОЗДАНИЯ СТУДИИ ===");

            return studioMapper.toDto(saved);

        } catch (Exception e) {
            log.error("Ошибка при создании студии: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Получает или создает профиль художника для пользователя.
     *
     * @param profile профиль пользователя
     * @return ArtistProfile художника
     */
    private ArtistProfile getOrCreateArtistProfile(Profile profile) {
        return artistProfileRepository.findByProfile(profile).orElseGet(() -> {
            ArtistProfile newArtist = ArtistProfile.builder()
                    .profile(profile)
                    .isAvailable(true)
                    .build();
            return artistProfileRepository.save(newArtist);
        });
    }

    /**
     * Добавляет основателя как менеджера студии.
     *
     * @param studio созданная студия
     * @param artist профиль художника-основателя
     */
    private void addFounderAsManager(Studio studio, ArtistProfile artist) {
        StudioMember member = StudioMember.builder()
                .studio(studio)
                .member(artist)
                .role(StudioRole.FOUNDER)
                .status(StudioMemberStatus.APPROVED)
                .joinedAt(LocalDateTime.now())
                .build();
        studioMemberRepository.save(member);
        log.debug("Основатель добавлен как менеджер студии");
    }

    /**
     * Находит студию по ID.
     *
     * @param studioId ID студии
     * @return Optional с StudioDto если найдена
     */
    @Override
    public Optional<StudioDto> findById(Long studioId) {
        log.debug("Поиск студии по id: {}", studioId);
        return studioRepository.findById(studioId)
                .map(studioMapper::toDto);
    }

    /**
     * Находит студию по ID пользователя (владельца).
     *
     * @param userId ID пользователя
     * @return Optional с StudioDto если найдена
     */
    @Override
    public Optional<StudioDto> findByUserId(Long userId) {
        log.debug("Поиск студии по userId: {}", userId);
        return studioRepository.findByProfileUserId(userId)
                .map(studioMapper::toDto);
    }

    /**
     * Возвращает список всех студий.
     *
     * @return список всех StudioDto
     */
    @Override
    public List<StudioDto> findAllStudios() {
        log.debug("Получение всех студий");
        return studioRepository.findAll().stream()
                .map(studioMapper::toDto)
                .toList();
    }

    /**
     * Находит студии по названию (частичное совпадение, без учета регистра).
     *
     * @param name часть названия студии
     * @return список найденных студий
     */
    @Override
    public List<StudioDto> findStudiosByName(String name) {
        log.debug("Поиск студий по названию: {}", name);
        return studioRepository.findByNameContainingIgnoreCase(name).stream()
                .map(studioMapper::toDto)
                .toList();
    }

    /**
     * Обновляет описание студии.
     * Только для менеджера или администратора.
     *
     * @param studioId ID студии
     * @param description новое описание
     * @param managerId ID менеджера (для проверки прав)
     * @return обновленный StudioDto
     * @throws SecurityException если у пользователя нет прав
     */
    @Override
    @Transactional
    public StudioDto updateDescription(Long studioId, String description, Long managerId) {
        log.info("Обновление описания студии: studioId={}, managerId={}", studioId, managerId);
        Studio studio = studioRepository.findById(studioId)
                .orElseThrow(() -> new IllegalArgumentException("Студия не найдена - " + studioId));

        checkManagerAccess(studio, managerId);

        studio.setDescription(description);
        Studio updated = studioRepository.save(studio);
        log.info("Описание студии {} обновлено", studioId);
        return studioMapper.toDto(updated);
    }

    /**
     * Подача заявки на вступление в студию.
     *
     * @param studioId ID студии
     * @param artistId ID художника
     * @throws IllegalArgumentException если студия или художник не найдены,
     *         художник уже в студии, или заявка уже подана
     */
    @Override
    @Transactional
    public void requestToJoinStudio(Long studioId, Long artistId) {
        log.info("Подача заявки: художник ID={} в студию ID={}", artistId, studioId);

        Studio studio = studioRepository.findById(studioId)
                .orElseThrow(() -> {
                    log.error("Студия с id={} не найдена", studioId);
                    return new IllegalArgumentException("Студия не найдена - " + studioId);
                });
        log.debug("Студия найдена: name={}", studio.getName());

        ArtistProfile artist = artistProfileRepository.findById(artistId)
                .orElseThrow(() -> {
                    log.error("Художник с id={} не найден", artistId);
                    return new IllegalArgumentException("Художник не найден - " + artistId);
                });
        log.debug("Художник найден: username={}", artist.getProfile().getUser().getUsername());

        if (artist.getStudio() != null) {
            log.warn("Художник уже состоит в студии: {}", artist.getStudio().getName());
            throw new IllegalArgumentException("Художник уже состоит в студии: " + artist.getStudio().getName());
        }

        if (studioMemberRepository.existsByStudioAndMember(studio, artist)) {
            log.warn("Заявка от художника {} уже была подана", artistId);
            throw new IllegalArgumentException("Заявка уже была подана");
        }

        StudioMember member = StudioMember.builder()
                .studio(studio)
                .member(artist)
                .role(StudioRole.ARTIST)
                .status(StudioMemberStatus.PENDING)
                .joinedAt(LocalDateTime.now())
                .build();

        studioMemberRepository.save(member);
        log.info("Заявка художника {} в студию {} сохранена, статус PENDING", artistId, studioId);
    }

    /**
     * Одобрение заявки на вступление.
     * Только для менеджера или администратора.
     *
     * @param studioId ID студии
     * @param artistId ID художника
     * @param managerId ID менеджера (для проверки прав)
     * @throws SecurityException если у пользователя нет прав
     */
    @Override
    @Transactional
    public void approveMember(Long studioId, Long artistId, Long managerId) {
        log.info("Одобрение заявки: artistId={} в studioId={}, managerId={}", artistId, studioId, managerId);

        Studio studio = studioRepository.findById(studioId)
                .orElseThrow(() -> new IllegalArgumentException("Студия не найдена - " + studioId));

        checkManagerAccess(studio, managerId);

        ArtistProfile artist = artistProfileRepository.findById(artistId)
                .orElseThrow(() -> new IllegalArgumentException("Художник не найден - " + artistId));

        StudioMember member = studioMemberRepository.findByStudioAndMember(studio, artist)
                .orElseThrow(() -> new IllegalArgumentException("Заявка не найдена"));

        member.setStatus(StudioMemberStatus.APPROVED);
        studioMemberRepository.save(member);

        artist.setStudio(studio);
        artistProfileRepository.save(artist);

        log.info("Художник {} принят в студию {}", artistId, studioId);
    }

    /**
     * Удаление участника из студии.
     * Только для менеджера или администратора.
     * Менеджер не может удалить сам себя.
     *
     * @param studioId ID студии
     * @param artistId ID художника
     * @param managerId ID менеджера (для проверки прав)
     * @throws SecurityException если у пользователя нет прав или менеджер удаляет себя
     */
    @Override
    @Transactional
    public void removeMember(Long studioId, Long artistId, Long managerId) {
        log.info("Удаление участника: artistId={} из studioId={}, managerId={}", artistId, studioId, managerId);

        if (artistId.equals(managerId)) {
            throw new SecurityException("Менеджер не может удалить сам себя. Используйте выход из студии.");
        }

        Studio studio = studioRepository.findById(studioId)
                .orElseThrow(() -> new IllegalArgumentException("Студия не найдена - " + studioId));

        checkManagerAccess(studio, managerId);

        ArtistProfile artist = artistProfileRepository.findById(artistId)
                .orElseThrow(() -> new IllegalArgumentException("Художник не найден - " + artistId));

        studioMemberRepository.deleteByStudioAndMember(studio, artist);
        artist.setStudio(null);
        artistProfileRepository.save(artist);

        log.info("Художник {} удален из студии {}", artistId, studioId);
    }

    /**
     * Выход участника из студии по собственному желанию.
     *
     * @param studioId ID студии
     * @param artistId ID художника
     */
    @Override
    @Transactional
    public void leaveStudio(Long studioId, Long artistId) {
        log.info("Выход из студии: artistId={} из studioId={}", artistId, studioId);

        Studio studio = studioRepository.findById(studioId)
                .orElseThrow(() -> new IllegalArgumentException("Студия не найдена - " + studioId));

        ArtistProfile artist = artistProfileRepository.findById(artistId)
                .orElseThrow(() -> new IllegalArgumentException("Художник не найден - " + artistId));

        studioMemberRepository.deleteByStudioAndMember(studio, artist);
        artist.setStudio(null);
        artistProfileRepository.save(artist);

        log.info("Художник {} вышел из студии {}", artistId, studioId);
    }

    /**
     * Возвращает список участников студии.
     *
     * @param studioId ID студии
     * @return список ArtistProfileDto участников
     */
    @Override
    public List<ArtistProfileDto> getStudioMembers(Long studioId) {
        log.debug("Получение участников студии: {}", studioId);
        Studio studio = studioRepository.findById(studioId)
                .orElseThrow(() -> new IllegalArgumentException("Студия не найдена - " + studioId));

        return studioMemberRepository.findByStudioAndStatus(studio, StudioMemberStatus.APPROVED).stream()
                .map(StudioMember::getMember)
                .map(artistProfileMapper::toDto)
                .toList();
    }

    /**
     * Возвращает список ожидающих заявок на вступление.
     * Только для менеджера или администратора.
     *
     * @param studioId ID студии
     * @param managerId ID менеджера (для проверки прав)
     * @return список StudioMemberDto ожидающих заявок
     */
    @Override
    public List<StudioMemberDto> getPendingRequests(Long studioId, Long managerId) {
        log.debug("Получение ожидающих заявок для студии: {}, менеджер: {}", studioId, managerId);
        Studio studio = studioRepository.findById(studioId)
                .orElseThrow(() -> new IllegalArgumentException("Студия не найдена - " + studioId));

        checkManagerAccess(studio, managerId);

        return studioMemberRepository.findByStudioAndStatus(studio, StudioMemberStatus.PENDING).stream()
                .map(studioMemberMapper::toDto)
                .toList();
    }

    /**
     * Возвращает количество участников студии.
     *
     * @param studioId ID студии
     * @return количество участников
     */
    @Override
    public long getMemberCount(Long studioId) {
        Studio studio = studioRepository.findById(studioId)
                .orElseThrow(() -> new IllegalArgumentException("Студия не найдена - " + studioId));
        return studioMemberRepository.findByStudioAndStatus(studio, StudioMemberStatus.APPROVED).size();
    }

    /**
     * Удаляет студию.
     * Только для менеджера или администратора.
     *
     * @param studioId ID студии
     * @param managerId ID менеджера (для проверки прав)
     */
    @Override
    @Transactional
    public void deleteStudio(Long studioId, Long managerId) {
        log.info("Удаление студии: studioId={}, managerId={}", studioId, managerId);
        Studio studio = studioRepository.findById(studioId)
                .orElseThrow(() -> new IllegalArgumentException("Студия не найдена - " + studioId));

        checkManagerAccess(studio, managerId);

        List<ArtistProfile> members = artistProfileRepository.findByStudio(studio);
        for (ArtistProfile member : members) {
            member.setStudio(null);
            artistProfileRepository.save(member);
        }

        studioRepository.delete(studio);
        log.info("Студия {} удалена", studioId);
    }

    /**
     * Проверяет, является ли пользователь менеджером студии.
     *
     * @param userId ID пользователя
     * @param studioId ID студии
     * @return true если пользователь менеджер или администратор
     */
    @Override
    public boolean isManager(Long userId, Long studioId) {
        Studio studio = studioRepository.findById(studioId).orElse(null);
        if (studio == null) return false;

        ArtistProfile artist = artistProfileRepository.findByProfileUserId(userId).orElse(null);
        if (artist == null) return false;

        return studioMemberRepository.findByStudioAndMember(studio, artist)
                .map(m -> m.getRole() == StudioRole.MANAGER || m.getRole() == StudioRole.FOUNDER)
                .orElse(false);
    }

    /**
     * Проверяет права доступа менеджера к студии.
     *
     * @param studio студия
     * @param managerId ID менеджера
     * @throws SecurityException если у пользователя нет прав
     */
    private void checkManagerAccess(Studio studio, Long managerId) {
        boolean isManager = studioMemberRepository.findByStudioAndMember(studio,
                        artistProfileRepository.findByProfileUserId(managerId).orElse(null))
                .map(m -> m.getRole() == StudioRole.MANAGER || m.getRole() == StudioRole.FOUNDER)
                .orElse(false);

        boolean isAdmin = userRepository.findById(managerId)
                .map(u -> u.getRole() == by.step.enums.UserRole.ADMIN)
                .orElse(false);

        if (!isManager && !isAdmin) {
            throw new SecurityException("Только менеджер или администратор может выполнить это действие");
        }
    }

    /**
     * Возвращает все студии с пагинацией.
     *
     * @param pageable параметры пагинации
     * @return страница со студиями
     */
    @Override
    public Page<StudioDto> findAllStudios(Pageable pageable) {
        log.debug("Получение всех студий с пагинацией: page={}, size={}",
                pageable.getPageNumber(), pageable.getPageSize());
        return studioRepository.findAll(pageable).map(studioMapper::toDto);
    }
}