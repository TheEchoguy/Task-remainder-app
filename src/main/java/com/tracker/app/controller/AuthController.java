package com.tracker.app.controller;
import com.tracker.app.dto.LoginRequest;
import com.tracker.app.dto.OtpRequest;
import com.tracker.app.entity.User;
import com.tracker.app.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
public class AuthController {
    @Autowired
    private UserService userService;

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute User user,
                           RedirectAttributes redirect,
                           HttpSession session) {

        try {
            userService.register(user);

            session.setAttribute("otpEmail", user.getEmail());

            redirect.addFlashAttribute(
                    "success",
                    "Account created successfully! Please verify OTP."
            );
            return "redirect:/verify-otp";

        } catch (IllegalStateException ex) {
            //  Already registered
            redirect.addFlashAttribute("error", ex.getMessage());
            return "redirect:/register";

        } catch (RuntimeException ex) {
            //  OTP already sent / retry flow
            session.setAttribute("otpEmail", user.getEmail());
            redirect.addFlashAttribute("success", ex.getMessage());
            return "redirect:/verify-otp";
        }
    }




    @GetMapping("/verify-otp")
    public String showVerifyOtpPage() {
        return "verify-otp";
    }


    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody OtpRequest request,
                                    HttpSession session) {

        String email = (String) session.getAttribute("otpEmail");

        if (email == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Session expired. Please try again.");
        }

        Boolean resetFlow = (Boolean) session.getAttribute("resetFlow");
        boolean isReset = Boolean.TRUE.equals(resetFlow);

        String response = userService.verifyOtp(
                email,
                request.getOtp(),
                isReset
        );

        // ✅ FORGOT PASSWORD FLOW
        if ("OTP verified".equals(response) && isReset) {
            return ResponseEntity.ok("RESET_PASSWORD");
        }

        // ✅ REGISTRATION FLOW
        if ("OTP verified".equals(response)) {
            session.removeAttribute("otpEmail");
            return ResponseEntity.ok("Email verified successfully");
        }

        // ❌ ACTUAL ERRORS ONLY
        return ResponseEntity.badRequest().body(response);
    }





    @GetMapping("/login")
    public String showLoginPage(HttpSession session) {

        if (session.getAttribute("userId") != null) {
            return "redirect:/dashboard"; // already logged in
        }

        return "login";
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpSession session) {

        String response = userService.loginUser(
                request.getEmail(),
                request.getPassword(),
                session
        );

        if (response.equals("Login successful")) {
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.badRequest().body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(HttpSession session) {

        Integer userId = (Integer) session.getAttribute("userId");

        if (userId == null) {
            return ResponseEntity.ok(Map.of("loggedIn", false));
        }

        String email = (String) session.getAttribute("email");
        String name = (String) session.getAttribute("name");

        return ResponseEntity.ok(
                Map.of(
                        "loggedIn", true,
                        "id", userId,
                        "name", name,
                        "email", email
                )
        );
    }
    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOtp(HttpSession session) {

        String email = (String) session.getAttribute("otpEmail");

        if (email == null) {
            return ResponseEntity.badRequest()
                    .body("Session expired. Please register again.");
        }

        String result = userService.resendOtp(email);

        if (result.equals("OTP resent successfully")) {
            return ResponseEntity.ok(result);
        }

        return ResponseEntity.badRequest().body(result);
    }



    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
    @GetMapping("/forgot-password")
    public String showForgotPasswordPage() {
        return "forgot-password";
    }
    @PostMapping("/forgot-password")
    public String processForgotPassword(
            @RequestParam String email,
            HttpSession session,
            RedirectAttributes redirect) {

        userService.sendForgotOtp(email);
        session.setAttribute("otpEmail", email);
        session.setAttribute("resetFlow", true);

        redirect.addFlashAttribute(
                "success",
                "OTP sent to your email"
        );
        return "redirect:/verify-otp";
    }
    @GetMapping("/reset-password")
    public String showResetPasswordPage(HttpSession session) {

        // Optional safety check
        if (session.getAttribute("otpEmail") == null) {
            return "redirect:/login";
        }

        return "reset-password";
    }
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @RequestBody Map<String, String> body,
            HttpSession session) {

        String newPassword = body.get("password");

        if (newPassword == null || newPassword.length() < 6) {
            return ResponseEntity.badRequest()
                    .body("Password must be at least 6 characters");
        }

        Boolean profileChange = (Boolean) session.getAttribute("profileChange");

        if (Boolean.TRUE.equals(profileChange)) {
            Integer userId = (Integer) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Session expired");
            }

            userService.updatePasswordById(userId, newPassword);
            session.removeAttribute("profileChange");

        } else {
            String email = (String) session.getAttribute("otpEmail");
            if (email == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Session expired");
            }

            userService.updatePassword(email, newPassword);
            session.removeAttribute("otpEmail");
            session.removeAttribute("resetFlow");
        }

        return ResponseEntity.ok("Password updated successfully");
    }

    @GetMapping("/change-password")
    public String changePassword(HttpSession session) {

        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        // mark this as profile-initiated password change
        session.setAttribute("profileChange", true);

        return "reset-password";
    }
}
