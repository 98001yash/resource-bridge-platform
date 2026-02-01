package com.resourcebridge.auth_service.entity;


import com.resourcebridge.auth_service.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;

@Entity
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    // organization (for NGO/ hotel/ clinic)
    private String organizationName;

    @Column(length = 500)
    private String address;

    @Column(columnDefinition = "geography(Point,4326)")
    private Point location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // account status
    private boolean enabled = true;
    private boolean locked= false;

    // Admin verification (NGO/CLINIC)

    private boolean verified = false;
}
