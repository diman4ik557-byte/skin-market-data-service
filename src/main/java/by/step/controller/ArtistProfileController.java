package by.step.controller;

import by.step.dto.ArtistProfileDto;
import by.step.service.ArtistProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/artist-profiles")
@RequiredArgsConstructor
public class ArtistProfileController {

    private final ArtistProfileService artistProfileService;

    @PostMapping("/user/{userId}")
    public ResponseEntity<ArtistProfileDto> createArtistProfile(
            @PathVariable Long userId,
            @RequestParam String styles,
            @RequestParam BigDecimal minPrice,
            @RequestParam Integer averageTime) {
        ArtistProfileDto artist = artistProfileService.createArtistProfile(userId, styles, minPrice, averageTime);
        return ResponseEntity.ok(artist);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ArtistProfileDto> findByUserId(@PathVariable Long userId) {
        return artistProfileService.findByUserId(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<ArtistProfileDto>> findAll() {
        return ResponseEntity.ok(artistProfileService.findAllArtists());
    }

    @GetMapping("/available")
    public ResponseEntity<List<ArtistProfileDto>> findAvailable() {
        return ResponseEntity.ok(artistProfileService.findAvailableArtists());
    }

    @GetMapping("/style/{style}")
    public ResponseEntity<List<ArtistProfileDto>> findByStyle(@PathVariable String style) {
        return ResponseEntity.ok(artistProfileService.findArtistsByStyle(style));
    }

    @GetMapping("/max-price/{maxPrice}")
    public ResponseEntity<List<ArtistProfileDto>> findByMaxPrice(@PathVariable BigDecimal maxPrice) {
        return ResponseEntity.ok(artistProfileService.findArtistsByMaxPrice(maxPrice));
    }

    @GetMapping("/filter")
    public ResponseEntity<List<ArtistProfileDto>> findByFilters(
            @RequestParam(required = false) String style,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean isAvailable) {
        return ResponseEntity.ok(artistProfileService.findArtistsByFilters(style, maxPrice, isAvailable));
    }

    @PutMapping("/{artistId}/styles")
    public ResponseEntity<ArtistProfileDto> updateStyles(
            @PathVariable Long artistId,
            @RequestParam String styles) {
        ArtistProfileDto artist = artistProfileService.updateStyles(artistId, styles);
        return ResponseEntity.ok(artist);
    }

    @PutMapping("/{artistId}/min-price")
    public ResponseEntity<ArtistProfileDto> updateMinPrice(
            @PathVariable Long artistId,
            @RequestParam BigDecimal minPrice) {
        ArtistProfileDto artist = artistProfileService.updateMinPrice(artistId, minPrice);
        return ResponseEntity.ok(artist);
    }

    @PutMapping("/{artistId}/average-time")
    public ResponseEntity<ArtistProfileDto> updateAverageTime(
            @PathVariable Long artistId,
            @RequestParam Integer averageTime) {
        ArtistProfileDto artist = artistProfileService.updateAverageTime(artistId, averageTime);
        return ResponseEntity.ok(artist);
    }

    @PutMapping("/{artistId}/availability")
    public ResponseEntity<Void> updateAvailability(
            @PathVariable Long artistId,
            @RequestParam boolean isAvailable) {
        artistProfileService.updateAvailability(artistId, isAvailable);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{artistId}/studio/{studioId}")
    public ResponseEntity<Void> assignToStudio(
            @PathVariable Long artistId,
            @PathVariable Long studioId) {
        artistProfileService.assignToStudio(artistId, studioId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{artistId}/studio")
    public ResponseEntity<Void> removeFromStudio(@PathVariable Long artistId) {
        artistProfileService.removeFromStudio(artistId);
        return ResponseEntity.ok().build();
    }
}