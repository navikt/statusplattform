package nav.statusplattform.core.repositories;

import nav.statusplattform.core.entities.OtpVerificationEntity;
import nav.statusplattform.core.exceptionHandling.ExceptionUtil;
import org.fluentjdbc.DbContext;
import org.fluentjdbc.DbContextTable;
import org.fluentjdbc.DatabaseRow;

import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

public class OtpRepository {

    private final DbContextTable otpTable;
    private final DbContextTable rateLimitTable;

    public OtpRepository(DbContext dbContext) {
        otpTable = dbContext.table("otp_verification");
        rateLimitTable = dbContext.table("otp_rate_limit");
    }

    public UUID save(OtpVerificationEntity entity) {
        return otpTable.newSaveBuilderWithUUID("id", entity.getId())
                .setField("email", entity.getEmail())
                .setField("otp_code", entity.getOtpCode())
                .setField("expires_at", entity.getExpiresAt())
                .setField("verified", entity.isVerified())
                .setField("attempts", entity.getAttempts())
                .execute()
                .getId();
    }

    public Optional<OtpVerificationEntity> findValidOtp(String email, String otpCode) {
        return otpTable.where("email", email)
                .where("otp_code", otpCode)
                .where("verified", false)
                .whereExpression("expires_at > ?", ZonedDateTime.now())
                .whereExpression("attempts < ?", 3)
                .orderBy("created_at DESC")
                .limit(1)
                .singleObject(OtpRepository::toOtp);
    }

    public void markVerified(UUID id) {
        otpTable.where("id", id)
                .update()
                .setField("verified", true)
                .execute();
    }

    public void incrementAttempts(UUID id) {
        otpTable.where("id", id)
                .update()
                .setFieldExpression("attempts", "attempts + 1")
                .execute();
    }

    public int countRecentSends(String email, ZonedDateTime since) {
        return otpTable.where("email", email)
                .whereExpression("created_at > ?", since)
                .getCount();
    }

    public void cleanupExpired() {
        otpTable.whereExpression("expires_at < ?", ZonedDateTime.now())
                .executeDelete();
    }

    public void cleanupOldRateLimits() {
        rateLimitTable.whereExpression("window_start < ?", ZonedDateTime.now().minusHours(24))
                .executeDelete();
    }

    public int getRateLimitCount(String email) {
        ZonedDateTime windowCutoff = ZonedDateTime.now().minusMinutes(15);
        return rateLimitTable.where("email", email)
                .whereExpression("window_start > ?", windowCutoff)
                .singleObject(row -> {
                    try {
                        return row.getInt("send_count");
                    } catch (SQLException e) {
                        throw ExceptionUtil.soften(e);
                    }
                })
                .orElse(0);
    }

    public void incrementRateLimit(String email) {
        int updated = rateLimitTable.where("email", email)
                .whereExpression("window_start > ?", ZonedDateTime.now().minusMinutes(15))
                .update()
                .setFieldExpression("send_count", "send_count + 1")
                .execute();

        if (updated == 0) {
            // Delete old entry if exists and create new window
            rateLimitTable.where("email", email).executeDelete();
            rateLimitTable.insert()
                    .setField("email", email)
                    .setField("send_count", 1)
                    .setField("window_start", ZonedDateTime.now())
                    .execute();
        }
    }

    static OtpVerificationEntity toOtp(DatabaseRow row) {
        try {
            return new OtpVerificationEntity()
                    .setId(row.getUUID("id"))
                    .setEmail(row.getString("email"))
                    .setOtpCode(row.getString("otp_code"))
                    .setExpiresAt(row.getZonedDateTime("expires_at"))
                    .setVerified(row.getBoolean("verified"))
                    .setAttempts(row.getInt("attempts"))
                    .setCreatedAt(row.getZonedDateTime("created_at"));
        } catch (SQLException e) {
            throw ExceptionUtil.soften(e);
        }
    }
}
