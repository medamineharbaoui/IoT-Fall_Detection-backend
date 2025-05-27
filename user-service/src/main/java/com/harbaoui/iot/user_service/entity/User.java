package com.harbaoui.iot.user_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    private String name;

    private String password;

    private String phoneNumber;

    @Column(nullable = false)
    @Builder.Default
    private boolean isVerified = false;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Device> devices = new ArrayList<>();

    // === Implemented methods from UserDetails ===

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // You can later implement roles/authorities properly.
        return Collections.emptyList();
    }

    @Override
    public String getUsername() {
        return email; // use email as the login username
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // you can customize this
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // you can customize this
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // you can customize this
    }

    @Override
    public boolean isEnabled() {
        return isVerified; // user is enabled only if verified
    }
}
