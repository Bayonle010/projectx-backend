package com.project_x.verification.otp.service.impl;

import com.project_x.core.response.ApiResponse;
import com.project_x.core.response.ResponseUtil;
import com.project_x.core.util.NumberUtil;
import com.project_x.notification.model.MessageType;
import com.project_x.notification.model.Param;
import com.project_x.notification.service.MessagingHandler;
import com.project_x.user.entity.User;
import com.project_x.user.enums.UserType;
import com.project_x.verification.otp.enums.OtpEvent;
import com.project_x.verification.otp.service.OtpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class OtpServiceImpl implements OtpService {

    private static final Logger logger = LoggerFactory.getLogger(OtpService.class)

    private final MessagingHandler messagingHandler;

    public OtpServiceImpl(MessagingHandler messagingHandler) {
        this.messagingHandler = messagingHandler;
    }

    @Override
    public boolean handleGenerateOtp(String otpMedium, OtpEvent otpEvent, long expirationTimeInSeconds, String emailSubject, UserType userType, String emailTemplate) {
        String numericOTP = NumberUtil.generateNumericOTP();
        String formattedOtpMedium = otpMedium.toLowerCase().trim();
        User user = userRepository.findByEmail(formattedOtpMedium).orElse(User.builder().firstname("No one").build());



        try {
            List<Param> params = new ArrayList<>();
            params.add(Param.builder().name("otp").value(numericOTP).build());
            params.add(Param.builder().name("customer_name").value(user.getFirstname()).build());
            params.add(Param.builder().name("year").value(String.valueOf(LocalDate.now().getYear())).build());

            messagingHandler
                    .sendEmailNotificationToQueue(List.of(formattedOtpMedium), new ArrayList<>(), new ArrayList<>(),
                            MessageType.Template, emailTemplate, emailSubject, "noreply@bayfiapp.com", params, true);
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
}
