package com.tracker.app.service;
import com.tracker.app.dto.OtpRequest;
import com.tracker.app.dto.UpdateProfileRequest;
import com.tracker.app.entity.User;
import com.tracker.app.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public User register(User user) {

        Optional<User> existingOpt = userRepository.findByEmail(user.getEmail());

        if (existingOpt.isPresent()) {
            User existing = existingOpt.get();

            // ✅ Case 1: Email already verified → must login
            if (Boolean.TRUE.equals(existing.isVerified())) {
                throw new IllegalStateException("Account already exists. Please login.");
            }

            // ✅ Case 2: Email exists but not verified → resend OTP
            String otpCode = String.valueOf((int) (Math.random() * 900000) + 100000);
            existing.setOtp(otpCode);
            existing.setOtpExpiry(LocalDateTime.now().plusMinutes(5));

            emailService.sendOTP(existing.getEmail(), otpCode);
            userRepository.save(existing);

            throw new RuntimeException("OTP already sent. Please verify.");
        }

        // ✅ Case 3: New user registration
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setVerified(false);

        String otpCode = String.valueOf((int) (Math.random() * 900000) + 100000);
        user.setOtp(otpCode);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));

        emailService.sendOTP(user.getEmail(), otpCode);
        return userRepository.save(user);
    }

    public String verifyOtp(String email, String otp) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty()) return "User not found";
        User existingUser = user.get();
        if (existingUser.isVerified()) return "User already verified";
        if (existingUser.getOtp() == null) return "OTP not generated";
        if (!existingUser.getOtp().equals(otp)) return "Invalid OTP";
        if (existingUser.getOtpExpiry().isBefore(LocalDateTime.now()))
            return "OTP expired";

        existingUser.setVerified(true);
        existingUser.setOtp(null);
        existingUser.setOtpExpiry(null);
        userRepository.save(existingUser);
        return "Email verified successfully";
    }

    public String resendOtp(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            return "User not found";
        }
        User user = optionalUser.get();
        if (Boolean.TRUE.equals(user.isVerified())) {
            return "User already verified";
        }
        String otpCode = String.valueOf((int) (Math.random() * 900000) + 100000);
        user.setOtp(otpCode);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
        emailService.sendOTP(user.getEmail(), otpCode);
        userRepository.save(user);
        return "OTP resent successfully";
    }

    public String loginUser(String email, String password, HttpSession session) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty()) {
            return "User not found";
        }
        if (!user.get().isVerified()) {
            return "User is not verified. Please verify OTP first.";
        }
        if (!passwordEncoder.matches(password, user.get().getPassword())) {
            return "Invalid password";
        }
        session.setAttribute("loggedInUser", user.get().getId());
        session.setAttribute("userId", user.get().getId());
        session.setAttribute("email", user.get().getEmail());
        session.setAttribute("name", user.get().getName());
        session.setAttribute("profileImage", user.get().getProfileImage());

        return "Login successful";
    }

    public User getUserById(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User updateProfile(Integer userId, UpdateProfileRequest dto) {

        User user = getUserById(userId);

        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setMobile(dto.getMobile());

        MultipartFile image = dto.getProfileImage();

        if (image != null && !image.isEmpty()) {
            try {
                String uploadDir = "uploads/profile-images/";
                Files.createDirectories(Paths.get(uploadDir));

                String original = image.getOriginalFilename();
                String extension = original.substring(original.lastIndexOf('.'));

                String filename = "user_" + userId + "_" + System.currentTimeMillis() + extension;
                Path filePath = Paths.get(uploadDir, filename);

                Files.write(filePath, image.getBytes());

                user.setProfileImage(filename);

            } catch (Exception e) {
                throw new RuntimeException("Failed to upload profile image", e);
            }
        }

        return userRepository.save(user);
    }

    public Optional<User> findById(Integer id) {
        return userRepository.findById(id);
    }

}
