package com.tracker.app.controller;

import com.tracker.app.dto.UpdateProfileRequest;
import com.tracker.app.entity.User;
import com.tracker.app.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
public class ProfileController {

    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    public String viewProfile(Model model, HttpSession session) {

        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        User user = userService.getUserById(userId);
        model.addAttribute("user", user);

        return "profile";
    }


    @GetMapping("/profile/edit")
    public String editProfile(HttpSession session, Model model) {

        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UpdateProfileRequest profile = new UpdateProfileRequest();
        profile.setName(user.getName());
        profile.setEmail(user.getEmail());
        profile.setMobile(user.getMobile());

        model.addAttribute("user", user);       // for image display
        model.addAttribute("profile", profile); // 🔥 REQUIRED for th:object

        return "profile-edit";
    }



    @PostMapping("/profile/update")
    public String updateProfile(
            @ModelAttribute("profile") UpdateProfileRequest dto,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        //  get updated user
        User updatedUser = userService.updateProfile(userId, dto);

        // update session so header updates
        session.setAttribute("name", updatedUser.getName());
        session.setAttribute("profileImage", updatedUser.getProfileImage());

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Profile updated successfully"
        );

        return "redirect:/profile";
    }

    @GetMapping("/profile/image/{fileName}")
    public ResponseEntity<Resource> serveImage(@PathVariable String fileName) throws IOException {

        Path imagePath = Paths.get("uploads/profile-images").resolve(fileName);

        if (!Files.exists(imagePath)) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new UrlResource(imagePath.toUri());

        String contentType = Files.probeContentType(imagePath);
        if (contentType == null) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }




}
