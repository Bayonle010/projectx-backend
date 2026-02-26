package com.project_x.verification.otp;

import com.project_x.user.enums.UserType;
import com.project_x.verification.otp.entity.Otp;
import com.project_x.verification.otp.enums.OtpEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OtpRepository extends JpaRepository<Otp, UUID> {
    Otp findByOtpMediumAndOtpEventAndUserType(String otp, OtpEvent event, UserType userType);
    Otp findByToken(String token);
}
