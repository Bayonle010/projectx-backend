package com.project_x.verification.otp.service.impl;

import com.project_x.core.response.ApiResponse;
import com.project_x.core.response.ResponseUtil;
import com.project_x.core.util.NumberUtil;
import com.project_x.notification.model.MessageType;
import com.project_x.notification.model.Param;
import com.project_x.notification.service.MessagingHandler;
import com.project_x.user.entity.User;
import com.project_x.user.enums.UserType;
import com.project_x.user.service.UserService;
import com.project_x.verification.otp.OtpRepository;
import com.project_x.verification.otp.dto.response.OtpValidationResponse;
import com.project_x.verification.otp.entity.Otp;
import com.project_x.verification.otp.enums.OtpEvent;
import com.project_x.verification.otp.service.OtpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class OtpServiceImpl implements OtpService {

    private static final Logger logger = LoggerFactory.getLogger(OtpService.class);

    private final MessagingHandler messagingHandler;
    private final UserService userService;
    private final OtpRepository otpRepository;

    public OtpServiceImpl(MessagingHandler messagingHandler, UserService userService, OtpRepository otpRepository) {
        this.messagingHandler = messagingHandler;
        this.userService = userService;
        this.otpRepository = otpRepository;
    }

    @Override
    public boolean handleGenerateOtp(String otpMedium, OtpEvent otpEvent, long expirationTimeInSeconds, String emailSubject, UserType userType, String emailTemplate) {
        String numericOTP = NumberUtil.generateNumericOTP();
        String formattedOtpMedium = otpMedium.toLowerCase().trim();
        User user = userService.findUserByEmail(formattedOtpMedium);


        try {

//            TODO: Use template
            List<Param> params = new ArrayList<>();
//            params.add(Param.builder().name("otp").value(numericOTP).build());
//            params.add(Param.builder().name("customer_name").value(user.getFirstname()).build());
//            params.add(Param.builder().name("year").value(String.valueOf(LocalDate.now().getYear())).build());

            messagingHandler
                    .sendEmailNotificationToQueue(List.of(formattedOtpMedium), new ArrayList<>(), new ArrayList<>(),
                            MessageType.Text, emailTemplate, emailSubject, "noreply@siryoungtech.com", params, true);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }


        Otp otp = otpRepository.findByOtpMediumAndOtpEventAndUserType(otpMedium, otpEvent, userType);
        if (ObjectUtils.isEmpty(otp)) {
            otp = new Otp();
            otp.setOtpEvent(otpEvent);
            otp.setOtpMedium(otpMedium);
            otp.setUserType(userType);
        }
        otp.setToken(numericOTP);
        otp.setExpired(false);
        otp.setExpiryTime(Instant.now().plus(Duration.ofSeconds(expirationTimeInSeconds)));

        otpRepository.save(otp);

        return  true;

    }

    @Override
    public OtpValidationResponse validateOtp(String otpToken, String otpMedium, OtpEvent expectedEvent) {
        Otp otp = otpRepository.findByToken(otpToken);
        if(ObjectUtils.isEmpty(otp)){
            return OtpValidationResponse.failure(HttpStatus.NOT_FOUND, "Invalid OTP", "Operation failed");
        }

        if (!otp.getOtpEvent().equals(expectedEvent)) {
            return OtpValidationResponse.failure(HttpStatus.BAD_REQUEST, "Invalid OTP", "Operation failed");
        }

        if(!otp.getOtpMedium().equals(otpMedium)){
            return OtpValidationResponse.failure(HttpStatus.BAD_REQUEST, "OTP from invalid Otp medium (user)", "Operation failed");
        }

        if(isOtpExpired(otp)){
            otpRepository.delete(otp);
            return OtpValidationResponse.failure(HttpStatus.BAD_REQUEST, "OTP expired", "Expired OTP");
        }

        if(isOtpInvalid(otp, otpToken)){
            return OtpValidationResponse.failure(HttpStatus.BAD_REQUEST, "OTP Incorrect", "Invalid OTP");
        }

        return OtpValidationResponse.success(otp);
    }

    @Override
    public void deleteOtp(Otp otp) {
        otpRepository.delete(otp);
    }


    private boolean isOtpExpired(Otp otpEntity) {
        return otpEntity.isExpired() || Instant.now().isAfter(otpEntity.getExpiryTime());
    }


    private boolean isOtpInvalid(Otp otpEntity, String otp) {
        return !otpEntity.getToken().equals(otp);
    }



}
