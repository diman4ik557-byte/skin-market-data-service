package by.step.controller;

import by.step.dto.UserDto;
import by.step.entity.enums.UserRole;
import by.step.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam UserRole role) {
        UserDto user = userService.register(username, email, password, role);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> findById(@PathVariable Long id) {
        return userService.findByUsername(id.toString())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<UserDto> findByUsername(@PathVariable String username) {
        return userService.findByUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserDto> findByEmail(@PathVariable String email) {
        return userService.findByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> findAll() {
        return ResponseEntity.ok(userService.findAllUsers());
    }

    @GetMapping("/role/{role}")
    public ResponseEntity<List<UserDto>> findByRole(@PathVariable UserRole role) {
        return ResponseEntity.ok(userService.findUserByRole(role));
    }

    @GetMapping("/{id}/balance")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable Long id) {
        return userService.findByUsername(id.toString())
                .map(user -> ResponseEntity.ok(user.getBalance()))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/balance/add")
    public ResponseEntity<Void> addToBalance(
            @PathVariable Long id,
            @RequestParam BigDecimal amount) {
        userService.addToBalance(id, amount);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/balance/subtract")
    public ResponseEntity<Void> subtractFromBalance(
            @PathVariable Long id,
            @RequestParam BigDecimal amount) {
        userService.subtractFromBalance(id, amount);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/exists/username/{username}")
    public ResponseEntity<Boolean> existsByUsername(@PathVariable String username) {
        return ResponseEntity.ok(userService.existByUsername(username));
    }

    @GetMapping("/exists/email/{email}")
    public ResponseEntity<Boolean> existsByEmail(@PathVariable String email) {
        return ResponseEntity.ok(userService.existByEmail(email));
    }
}