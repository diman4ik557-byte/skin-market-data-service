package by.step.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "artist_profiles")
public class ArtistProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "profile_id",nullable = false)
    private Profile profile;

    @ManyToOne
    @JoinColumn(name = "studio_id")
    private Studio studio;

    private String styles;

    @Column(name = "min_price")
    private BigDecimal minPrice;

    @Column(name = "average_time")
    private Integer averageTime;

    @Column(name = "is_available")
    private Boolean isAvailable;
}
