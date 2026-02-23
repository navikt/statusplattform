package nav.statusplattform.core.entities;

import java.time.ZonedDateTime;
import java.util.UUID;

public class OtpVerificationEntity {

    private UUID id;
    private String email;
    private String otpCode;
    private ZonedDateTime expiresAt;
    private boolean verified;
    private int attempts;
    private ZonedDateTime createdAt;

    public OtpVerificationEntity() {
    }

    public UUID getId() {
        return id;
    }

    public OtpVerificationEntity setId(UUID id) {
        this.id = id;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public OtpVerificationEntity setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getOtpCode() {
        return otpCode;
    }

    public OtpVerificationEntity setOtpCode(String otpCode) {
        this.otpCode = otpCode;
        return this;
    }

    public ZonedDateTime getExpiresAt() {
        return expiresAt;
    }

    public OtpVerificationEntity setExpiresAt(ZonedDateTime expiresAt) {
        this.expiresAt = expiresAt;
        return this;
    }

    public boolean isVerified() {
        return verified;
    }

    public OtpVerificationEntity setVerified(boolean verified) {
        this.verified = verified;
        return this;
    }

    public int getAttempts() {
        return attempts;
    }

    public OtpVerificationEntity setAttempts(int attempts) {
        this.attempts = attempts;
        return this;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public OtpVerificationEntity setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }
}
