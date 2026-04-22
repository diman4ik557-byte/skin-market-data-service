package by.step.entity;

import by.step.enums.StudioRole;
import by.step.enums.StudioMemberStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "studio_members")
public class StudioMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "studio_id", nullable = false)
    private Studio studio;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private ArtistProfile member;

    @Enumerated(EnumType.STRING)
    private StudioRole role;

    @Enumerated(EnumType.STRING)
    private StudioMemberStatus status;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;
}