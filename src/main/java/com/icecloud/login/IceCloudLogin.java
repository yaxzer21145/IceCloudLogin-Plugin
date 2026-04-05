package com.icecloud.login;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class IceCloudLogin extends JavaPlugin {
    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private CaptchaManager captchaManager;
    private EmailManager emailManager;
    private PlayerListener playerListener;
    private CommandHandler commandHandler;
    
    private Map<UUID, PlayerSession> playerSessions;
    private Map<UUID, PendingBinding> pendingEmailBindings;
    private Map<UUID, PendingBinding> pendingQQBindings;
    private Map<UUID, Integer> wrongPasswordCount;
    private Map<UUID, Integer> kickCount;
    private Map<UUID, PasswordResetVerification> pendingPasswordResets;

    @Override
    public void onEnable() {
        playerSessions = new ConcurrentHashMap<>();
        pendingEmailBindings = new HashMap<>();
        pendingQQBindings = new HashMap<>();
        wrongPasswordCount = new ConcurrentHashMap<>();
        kickCount = new ConcurrentHashMap<>();
        pendingPasswordResets = new HashMap<>();
        
        saveDefaultConfig();
        
        configManager = new ConfigManager(this);
        configManager.loadConfig();
        
        databaseManager = new DatabaseManager(this);
        databaseManager.initialize();
        
        captchaManager = new CaptchaManager(this);
        emailManager = new EmailManager(this);
        
        playerListener = new PlayerListener(this);
        getServer().getPluginManager().registerEvents(playerListener, this);
        
        commandHandler = new CommandHandler(this);
        commandHandler.registerCommands();
        
        getLogger().info("IceCloudLogin has been enabled!");
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.close();
        }
        
        playerSessions.clear();
        pendingEmailBindings.clear();
        pendingQQBindings.clear();
        wrongPasswordCount.clear();
        kickCount.clear();
        pendingPasswordResets.clear();
        
        getLogger().info("IceCloudLogin has been disabled!");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public CaptchaManager getCaptchaManager() {
        return captchaManager;
    }

    public EmailManager getEmailManager() {
        return emailManager;
    }

    public PlayerSession getPlayerSession(UUID uuid) {
        return playerSessions.get(uuid);
    }

    public void createPlayerSession(UUID uuid, PlayerSession session) {
        playerSessions.put(uuid, session);
    }

    public void removePlayerSession(UUID uuid) {
        playerSessions.remove(uuid);
    }

    public PendingBinding getPendingEmailBinding(UUID uuid) {
        return pendingEmailBindings.get(uuid);
    }

    public void createPendingEmailBinding(UUID uuid, PendingBinding binding) {
        pendingEmailBindings.put(uuid, binding);
    }

    public void removePendingEmailBinding(UUID uuid) {
        pendingEmailBindings.remove(uuid);
    }

    public PendingBinding getPendingQQBinding(UUID uuid) {
        return pendingQQBindings.get(uuid);
    }

    public void createPendingQQBinding(UUID uuid, PendingBinding binding) {
        pendingQQBindings.put(uuid, binding);
    }

    public void removePendingQQBinding(UUID uuid) {
        pendingQQBindings.remove(uuid);
    }

    public int getWrongPasswordCount(UUID uuid) {
        return wrongPasswordCount.getOrDefault(uuid, 0);
    }

    public void incrementWrongPasswordCount(UUID uuid) {
        int count = getWrongPasswordCount(uuid) + 1;
        wrongPasswordCount.put(uuid, count);
    }

    public void resetWrongPasswordCount(UUID uuid) {
        wrongPasswordCount.remove(uuid);
    }

    public int getKickCount(UUID uuid) {
        return kickCount.getOrDefault(uuid, 0);
    }

    public void incrementKickCount(UUID uuid) {
        int count = getKickCount(uuid) + 1;
        kickCount.put(uuid, count);
    }

    public void resetKickCount(UUID uuid) {
        kickCount.remove(uuid);
    }

    public void executeKickCommands(String playerName) {
        for (String cmd : configManager.getKickCommands()) {
            String formattedCmd = cmd.replace("{player}", playerName);
            getServer().dispatchCommand(getServer().getConsoleSender(), formattedCmd);
        }
    }

    public PasswordResetVerification getPendingPasswordReset(UUID uuid) {
        return pendingPasswordResets.get(uuid);
    }

    public void createPendingPasswordReset(UUID uuid, PasswordResetVerification verification) {
        pendingPasswordResets.put(uuid, verification);
    }

    public void removePendingPasswordReset(UUID uuid) {
        pendingPasswordResets.remove(uuid);
    }

    public static class PlayerSession {
        private final UUID uuid;
        private final String playerName;
        private final long joinTime;
        private boolean loggedIn;
        private boolean captchaVerified;
        private SessionState state;
        private BukkitRunnable titleTask;
        private BukkitRunnable actionbarTask;
        private int remainingTime;

        public PlayerSession(UUID uuid, String playerName) {
            this.uuid = uuid;
            this.playerName = playerName;
            this.joinTime = System.currentTimeMillis();
            this.loggedIn = false;
            this.captchaVerified = false;
            this.state = SessionState.CAPTCHA;
            this.remainingTime = 0;
        }

        public UUID getUuid() {
            return uuid;
        }

        public String getPlayerName() {
            return playerName;
        }

        public long getJoinTime() {
            return joinTime;
        }

        public boolean isLoggedIn() {
            return loggedIn;
        }

        public void setLoggedIn(boolean loggedIn) {
            this.loggedIn = loggedIn;
        }

        public boolean isCaptchaVerified() {
            return captchaVerified;
        }

        public void setCaptchaVerified(boolean captchaVerified) {
            this.captchaVerified = captchaVerified;
        }

        public SessionState getState() {
            return state;
        }

        public void setState(SessionState state) {
            this.state = state;
        }

        public BukkitRunnable getTitleTask() {
            return titleTask;
        }

        public void setTitleTask(BukkitRunnable titleTask) {
            this.titleTask = titleTask;
        }

        public BukkitRunnable getActionbarTask() {
            return actionbarTask;
        }

        public void setActionbarTask(BukkitRunnable actionbarTask) {
            this.actionbarTask = actionbarTask;
        }

        public int getRemainingTime() {
            return remainingTime;
        }

        public void setRemainingTime(int remainingTime) {
            this.remainingTime = remainingTime;
        }

        public void cancelTasks() {
            if (titleTask != null) {
                titleTask.cancel();
                titleTask = null;
            }
            if (actionbarTask != null) {
                actionbarTask.cancel();
                actionbarTask = null;
            }
        }
    }

    public static class PendingBinding {
        private final String value;
        private final String code;
        private final long timestamp;

        public PendingBinding(String value, String code) {
            this.value = value;
            this.code = code;
            this.timestamp = System.currentTimeMillis();
        }

        public String getValue() {
            return value;
        }

        public String getCode() {
            return code;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }

    public enum SessionState {
        CAPTCHA,
        REGISTER,
        LOGIN
    }

    public static class PasswordResetVerification {
        private final String type;
        private final String value;
        private final String code;
        private final long timestamp;

        public PasswordResetVerification(String type, String value, String code) {
            this.type = type;
            this.value = value;
            this.code = code;
            this.timestamp = System.currentTimeMillis();
        }

        public String getType() {
            return type;
        }

        public String getValue() {
            return value;
        }

        public String getCode() {
            return code;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public boolean isExpired(long validityTime) {
            return System.currentTimeMillis() - timestamp > validityTime * 1000;
        }
    }
}
