package by.step.controller;

import by.step.dto.ArtistProfileDto;
import by.step.dto.StudioDto;
import by.step.dto.StudioMemberDto;
import by.step.service.StudioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST контроллер для работы со студиями художников.
 * Предоставляет API для создания, поиска, управления участниками и удаления студий.
 *
 * @author Skin Market Team
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/studios")
@RequiredArgsConstructor
public class StudioController {

    private final StudioService studioService;

    /**
     * Создает новую студию.
     * Создатель автоматически становится менеджером студии.
     *
     * @param userId ID пользователя-создателя (должен быть художником)
     * @param name название студии
     * @param description описание студии
     * @return созданная студия
     */
    @PostMapping
    public ResponseEntity<StudioDto> createStudio(
            @RequestParam Long userId,
            @RequestParam String name,
            @RequestParam String description) {
        log.info("REST запрос: создание студии userId={}, name={}", userId, name);
        StudioDto studio = studioService.createStudio(userId, name, description);
        return ResponseEntity.ok(studio);
    }

    /**
     * Находит студию по ID.
     *
     * @param studioId ID студии
     * @return студия, если найдена, иначе 404
     */
    @GetMapping("/{studioId}")
    public ResponseEntity<StudioDto> getStudioById(@PathVariable Long studioId) {
        log.debug("REST запрос: поиск студии по id={}", studioId);
        return studioService.findById(studioId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Возвращает список всех студий.
     *
     * @return список всех студий
     */
    @GetMapping
    public ResponseEntity<List<StudioDto>> getAllStudios() {
        log.debug("REST запрос: получение всех студий");
        List<StudioDto> studios = studioService.findAllStudios();
        return ResponseEntity.ok(studios);
    }

    /**
     * Находит студию по ID пользователя (владельца).
     *
     * @param userId ID пользователя
     * @return студия пользователя, если найдена, иначе 404
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<StudioDto> getStudioByUserId(@PathVariable Long userId) {
        log.debug("REST запрос: поиск студии по userId={}", userId);
        return studioService.findByUserId(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    /**
     * Возвращает список участников студии.
     *
     * @param studioId ID студии
     * @return список участников
     */
    @GetMapping("/{studioId}/members")
    public ResponseEntity<List<ArtistProfileDto>> getStudioMembers(@PathVariable Long studioId) {
        log.debug("REST запрос: получение участников студии {}", studioId);
        List<ArtistProfileDto> members = studioService.getStudioMembers(studioId);
        return ResponseEntity.ok(members);
    }

    /**
     * Подает заявку на вступление в студию.
     *
     * @param studioId ID студии
     * @param artistId ID художника
     * @return пустой ответ при успехе
     */
    @PostMapping("/{studioId}/members/{artistId}/request")
    public ResponseEntity<Void> requestToJoinStudio(
            @PathVariable Long studioId,
            @PathVariable Long artistId) {
        log.info("REST запрос: заявка на вступление в студию {} от художника {}", studioId, artistId);
        studioService.requestToJoinStudio(studioId, artistId);
        return ResponseEntity.ok().build();
    }

    /**
     * Одобряет заявку на вступление в студию.
     * Только для менеджера или администратора.
     *
     * @param studioId ID студии
     * @param artistId ID художника
     * @param managerId ID менеджера (для проверки прав)
     * @return пустой ответ при успехе
     */
    @PostMapping("/{studioId}/members/{artistId}/approve")
    public ResponseEntity<Void> approveMember(
            @PathVariable Long studioId,
            @PathVariable Long artistId,
            @RequestParam Long managerId) {
        log.info("REST запрос: одобрение заявки художника {} в студию {} менеджером {}", artistId, studioId, managerId);
        studioService.approveMember(studioId, artistId, managerId);
        return ResponseEntity.ok().build();
    }

    /**
     * Удаляет участника из студии.
     * Только для менеджера или администратора.
     *
     * @param studioId ID студии
     * @param artistId ID художника
     * @param managerId ID менеджера (для проверки прав)
     * @return пустой ответ при успехе
     */
    @DeleteMapping("/{studioId}/members/{artistId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable Long studioId,
            @PathVariable Long artistId,
            @RequestParam Long managerId) {
        log.info("REST запрос: удаление художника {} из студии {} менеджером {}", artistId, studioId, managerId);
        studioService.removeMember(studioId, artistId, managerId);
        return ResponseEntity.ok().build();
    }

    /**
     * Выход участника из студии по собственному желанию.
     *
     * @param studioId ID студии
     * @param artistId ID художника
     * @return пустой ответ при успехе
     */
    @PostMapping("/{studioId}/members/{artistId}/leave")
    public ResponseEntity<Void> leaveStudio(
            @PathVariable Long studioId,
            @PathVariable Long artistId) {
        log.info("REST запрос: выход художника {} из студии {}", artistId, studioId);
        studioService.leaveStudio(studioId, artistId);
        return ResponseEntity.ok().build();
    }

    /**
     * Возвращает список ожидающих заявок на вступление.
     * Только для менеджера или администратора.
     *
     * @param studioId ID студии
     * @param managerId ID менеджера (для проверки прав)
     * @return список ожидающих заявок
     */
    @GetMapping("/{studioId}/pending")
    public ResponseEntity<List<StudioMemberDto>> getPendingRequests(
            @PathVariable Long studioId,
            @RequestParam Long managerId) {
        log.debug("REST запрос: получение ожидающих заявок для студии {} менеджером {}", studioId, managerId);
        List<StudioMemberDto> requests = studioService.getPendingRequests(studioId, managerId);
        return ResponseEntity.ok(requests);
    }

    /**
     * Проверяет, является ли пользователь менеджером студии.
     *
     * @param studioId ID студии
     * @param userId ID пользователя
     * @return true если пользователь менеджер или администратор
     */
    @GetMapping("/{studioId}/is-manager")
    public ResponseEntity<Boolean> isManager(
            @PathVariable Long studioId,
            @RequestParam Long userId) {
        log.debug("REST запрос: проверка прав менеджера для пользователя {} в студии {}", userId, studioId);
        boolean isManager = studioService.isManager(userId, studioId);
        return ResponseEntity.ok(isManager);
    }


    /**
     * Обновляет описание студии.
     * Только для менеджера или администратора.
     *
     * @param studioId ID студии
     * @param description новое описание
     * @param managerId ID менеджера (для проверки прав)
     * @return обновленная студия
     */
    @PutMapping("/{studioId}")
    public ResponseEntity<StudioDto> updateStudio(
            @PathVariable Long studioId,
            @RequestParam String description,
            @RequestParam Long managerId) {
        log.info("REST запрос: обновление описания студии {} менеджером {}", studioId, managerId);
        StudioDto studio = studioService.updateDescription(studioId, description, managerId);
        return ResponseEntity.ok(studio);
    }

    /**
     * Удаляет студию.
     * Только для менеджера или администратора.
     *
     * @param studioId ID студии
     * @param managerId ID менеджера (для проверки прав)
     * @return пустой ответ при успехе
     */
    @DeleteMapping("/{studioId}")
    public ResponseEntity<Void> deleteStudio(
            @PathVariable Long studioId,
            @RequestParam Long managerId) {
        log.info("REST запрос: удаление студии {} менеджером {}", studioId, managerId);
        studioService.deleteStudio(studioId, managerId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/page")
    public ResponseEntity<Page<StudioDto>> getAllStudiosPage(Pageable pageable) {
        return ResponseEntity.ok(studioService.findAllStudios(pageable));
    }
}