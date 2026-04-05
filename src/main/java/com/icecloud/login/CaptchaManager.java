package com.icecloud.login;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class CaptchaManager {
    private final IceCloudLogin plugin;
    private final Map<UUID, String> captchas;
    private final Map<UUID, Long> captchaTimestamps;
    private final Random random;

    public CaptchaManager(IceCloudLogin plugin) {
        this.plugin = plugin;
        this.captchas = new HashMap<>();
        this.captchaTimestamps = new HashMap<>();
        this.random = new Random();
    }

    public String generateCaptcha(UUID uuid) {
        int length = plugin.getConfigManager().getCaptchaLength();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < length; i++) {
            code.append(random.nextInt(10));
        }
        String captcha = code.toString();
        captchas.put(uuid, captcha);
        captchaTimestamps.put(uuid, System.currentTimeMillis());
        return captcha;
    }

    public boolean validateCaptcha(UUID uuid, String input) {
        String storedCaptcha = captchas.get(uuid);
        if (storedCaptcha == null) {
            return false;
        }

        Long timestamp = captchaTimestamps.get(uuid);
        if (timestamp == null) {
            return false;
        }

        int validityTime = plugin.getConfigManager().getCaptchaValidityTime();
        if (System.currentTimeMillis() - timestamp > validityTime * 1000) {
            captchas.remove(uuid);
            captchaTimestamps.remove(uuid);
            return false;
        }

        boolean valid = storedCaptcha.equals(input);
        if (valid) {
            captchas.remove(uuid);
            captchaTimestamps.remove(uuid);
        }
        return valid;
    }

    public void removeCaptcha(UUID uuid) {
        captchas.remove(uuid);
        captchaTimestamps.remove(uuid);
    }

    public String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            plugin.getLogger().severe("无法哈希密码: " + e.getMessage());
            return password;
        }
    }

    public boolean verifyPassword(String inputPassword, String storedHash) {
        return hashPassword(inputPassword).equals(storedHash);
    }
}
