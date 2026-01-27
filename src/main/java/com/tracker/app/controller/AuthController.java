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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
            // ✅ Already registered
            redirect.addFlashAttribute("error", ex.getMessage());
            return "redirect:/register";

        } catch (RuntimeException ex) {
            // ✅ OTP already sent / retry flow
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
                    .body("Session expired. Please register again.");
        }

        String response = userService.verifyOtp(email, request.getOtp());

        if ("Email verified successfully".equals(response)) {
            session.removeAttribute("otpEmail");
            return ResponseEntity.ok(response);
        }

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



}
