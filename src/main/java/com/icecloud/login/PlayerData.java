package com.icecloud.login;

import java.util.UUID;

public class PlayerData {
    private final UUID uuid;
    private final String username;
    private final String passwordHash;
    private final String email;
    private final String qq;
    private final long lastLogin;
    private final long registerTime;

    public PlayerData(UUID uuid, String username, String passwordHash, String email, String qq, long lastLogin, long registerTime) {
        this.uuid = uuid;
        this.username = username;
        this.passwordHash = passwordHash;
        this.email = email;
        this.qq = qq;
        this.lastLogin = lastLogin;
        this.registerTime = registerTime;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getEmail() {
        return email;
    }

    public String getQq() {
        return qq;
    }

    public long getLastLogin() {
        return lastLogin;
    }

    public long getRegisterTime() {
        return registerTime;
    }
}
