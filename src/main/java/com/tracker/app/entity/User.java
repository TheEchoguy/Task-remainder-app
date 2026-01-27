package com.tracker.app.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    @Column( unique = true)
    private String email;


    private String password;

    @Column(nullable = false)
    private Boolean verified ;

    private String otp;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime otpExpiry = LocalDateTime.now();
    private String mobile;
    @Column(name = "profile_image")
    private String profileImage;


    public User() {
    }

    public User(String name, String email, String password, Boolean verified, LocalDateTime createdAt) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.verified = verified;
        this.createdAt = createdAt;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean isVerified() {
        return verified;
    }

    public void setVerified(Boolean verified) {
        this.verified = verified;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public String getOtp() {
        return otp;
    }
    public void setOtp(String otp) {
        this.otp = otp;
    }
    public LocalDateTime getOtpExpiry() {
        return otpExpiry;
    }

    public void setOtpExpiry(LocalDateTime otpExpiry) {
        this.otpExpiry = otpExpiry;
    }

    public String getMobile(){
        return mobile;
    }
    public void setMobile(String mobile) {
        this.mobile=mobile;

    }
    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

}
