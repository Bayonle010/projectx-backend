package com.project_x.verification.otp.entity;

import com.project_x.user.enums.UserType;
import com.project_x.verification.otp.enums.OtpEvent;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "otp")
public class Otp {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "token")
    @ColumnDefault(value = "''")
    private String token;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "otp_medium")
    @ColumnDefault(value = "''")
    private String otpMedium;

    @Column(name = "expired")
    @ColumnDefault(value = "false")
    private boolean expired;

    @Column(name = "expiry_time")
    private Instant expiryTime;

    @Column(name = "user_type")
    @Enumerated(EnumType.STRING)
    private UserType userType;

    @Column(name = "otp_event")
    @Enumerated(EnumType.STRING)
    @ColumnDefault(value = "'NONE'")
    private OtpEvent otpEvent;
}
