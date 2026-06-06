package com.eduardomango.pricetracker.auth.permissions;

import jakarta.persistence.*;
import lombok.*;
import org.mapstruct.EnumMapping;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PermitEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "permit", nullable = false, unique = true)
    private Permit permit;
}
