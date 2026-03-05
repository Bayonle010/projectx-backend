package com.project_x.authentication.service.impl;

import com.project_x.authentication.dto.request.ForgotPasswordOtpRequest;
import com.project_x.authentication.dto.request.ResetPasswordRequest;
import com.project_x.authentication.dto.request.VerifyPasswordOtpRequest;
import com.project_x.authentication.service.ForgotPasswordService;
import com.project_x.core.response.ApiResponse;
import com.project_x.core.response.ResponseUtil;
import com.project_x.notification.model.MessageType;
import com.project_x.notification.service.MessagingHandler;
import com.project_x.user.entity.User;
import com.project_x.user.enums.UserType;
import com.project_x.user.service.UserService;
import com.project_x.verification.otp.entity.Otp;
import com.project_x.verification.otp.enums.OtpEvent;
import com.project_x.verification.otp.service.OtpService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ForgotPasswordImpl implements ForgotPasswordService {

    private final static Logger log = LoggerFactory.getLogger(ForgotPasswordImpl.class);

    private final OtpService otpService;
    private final MessagingHandler messagingHandler;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public ForgotPasswordImpl(OtpService otpService, MessagingHandler messagingHandler, UserService userService, PasswordEncoder passwordEncoder) {
        this.otpService = otpService;
        this.messagingHandler = messagingHandler;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    public ResponseEntity<ApiResponse> handleSendForgotPasswordOtp(ForgotPasswordOtpRequest request) {
        String formattedEmail = request.email().toLowerCase().trim();


        boolean generateOtp = otpService.handleGenerateOtp(
                formattedEmail, OtpEvent.FORGOT_PASSWORD, 180L, "PASSWORD RESET",
                UserType.USER, "forgot password otp :"
        );

        if (!generateOtp) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseUtil.error(99, "something went wrong", null, null));
        }

        return ResponseEntity.status(HttpStatus.OK).body(ResponseUtil.success(0, "Success", String.format("OTP sent to %s ", formattedEmail), "", null));
    }

    @Override
    public ResponseEntity<ApiResponse> handlePasswordOtpVerification(VerifyPasswordOtpRequest request) {

        var result = otpService.validateOtp(request.otp(), request.email(), OtpEvent.FORGOT_PASSWORD);

        if (!result.isValid()) {
            return ResponseEntity
                    .status(result.httpStatus())
                    .body(ResponseUtil.error(99, result.errorMessage(), result.details(), null));
        }

        return ResponseEntity.status(HttpStatus.OK).body(ResponseUtil.success(0, "verification successful", "otp verified", "", ""));
    }

    @Transactional
    @Override
    public ResponseEntity<ApiResponse> resetPassword(ResetPasswordRequest request) {


        var result =  otpService.validateOtp(request.otp(), request.email(), OtpEvent.FORGOT_PASSWORD);

        if (!result.isValid()) {
            return ResponseEntity
                    .status(result.httpStatus())
                    .body(ResponseUtil.error(99, result.errorMessage(), result.details(), null));
        }

        Otp otp = result.otp();


        if (!(request.newPassword().equals(request.confirmNewPassword()))){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseUtil.error(99, "Password does not match", null, null));
        }

        otpService.deleteOtp(otp);

        String formattedEmail = request.email().toLowerCase().trim();


        User user = userService.findUserByEmail(formattedEmail);

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userService.save(user);


//        TODO: NOTIFY THE USER VIA EMAIL

//        try {
//
//
//            messagingHandler
//                    .sendEmailNotificationToQueue(List.of(user.getEmail()), new ArrayList<>(), new ArrayList<>(),
//                            MessageType.Template, EmailTemplate.PASSWORD_RESET_EMAIL_TEMPLATE, "PASSWORD RESET", "noreply@bayfiapp.com", params, true);
//        } catch (Exception e) {
//            log.error(e.getMessage());
//        }

        return ResponseEntity.status(HttpStatus.OK).body(ResponseUtil.success(0, "Password changes successfully","", null, null));
    }
}
