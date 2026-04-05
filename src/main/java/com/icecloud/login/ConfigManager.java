package com.icecloud.login;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ConfigManager {
    private final IceCloudLogin plugin;
    private FileConfiguration config;
    private FileConfiguration messageConfig;
    private File messageFile;
    private File welcomeFile;
    private List<String> welcomeMessages;

    public ConfigManager(IceCloudLogin plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();
        
        loadMessageConfig();
        loadWelcomeFile();
    }
    
    private void loadMessageConfig() {
        messageFile = new File(plugin.getDataFolder(), "message.yml");
        if (!messageFile.exists()) {
            plugin.saveResource("message.yml", false);
        }
        messageConfig = YamlConfiguration.loadConfiguration(messageFile);
    }
    
    private void loadWelcomeFile() {
        welcomeFile = new File(plugin.getDataFolder(), "welcome.txt");
        if (!welcomeFile.exists()) {
            plugin.saveResource("welcome.txt", false);
        }
        loadWelcomeMessages();
    }
    
    private void loadWelcomeMessages() {
        welcomeMessages = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(Files.newInputStream(welcomeFile.toPath()), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().startsWith("#") && !line.trim().isEmpty()) {
                    welcomeMessages.add(translateColorCodes(line));
                }
            }
        } catch (IOException e) {
            plugin.getLogger().severe("无法加载 welcome.txt: " + e.getMessage());
        }
    }
    
    public List<String> getWelcomeMessages(String playerName) {
        List<String> messages = new ArrayList<>();
        for (String line : welcomeMessages) {
            messages.add(line.replace("{player}", playerName));
        }
        return messages;
    }
    
    public void reloadMessageConfig() {
        loadMessageConfig();
    }
    
    public void saveMessageConfig() {
        try {
            messageConfig.save(messageFile);
        } catch (IOException e) {
            plugin.getLogger().severe("无法保存 message.yml: " + e.getMessage());
        }
    }

    public FileConfiguration getConfig() {
        return config;
    }
    
    public FileConfiguration getMessageConfig() {
        return messageConfig;
    }

    public String getDatabaseType() {
        return config.getString("database.type", "sqlite");
    }

    public String getSqlitePath() {
        return config.getString("database.sqlite.path", "plugins/IceCloudLogin/data.db");
    }

    public String getMysqlHost() {
        return config.getString("database.mysql.host", "localhost");
    }

    public int getMysqlPort() {
        return config.getInt("database.mysql.port", 3306);
    }

    public String getMysqlDatabase() {
        return config.getString("database.mysql.database", "icecloudlogin");
    }

    public String getMysqlUsername() {
        return config.getString("database.mysql.username", "root");
    }

    public String getMysqlPassword() {
        return config.getString("database.mysql.password", "password");
    }

    public int getLoginTimeout() {
        return config.getInt("login.timeout", 120);
    }

    public int getAutoLoginDuration() {
        return config.getInt("login.auto-login-duration", 7200);
    }

    public int getMaxWrongPassword() {
        return config.getInt("login.max-wrong-password", 5);
    }

    public int getMaxKickCount() {
        return config.getInt("login.max-kick-count", 3);
    }

    public List<String> getKickCommands() {
        return config.getStringList("login.kick-commands");
    }

    public int getRegistrationTimeout() {
        return config.getInt("registration.timeout", 120);
    }

    public int getMaxPasswordLength() {
        return config.getInt("registration.max-password-length", 32);
    }

    public int getMinPasswordLength() {
        return config.getInt("registration.min-password-length", 6);
    }

    public Pattern getPasswordPattern() {
        return Pattern.compile(config.getString("registration.password-pattern", "^[a-zA-Z0-9_-]{6,32}$"));
    }

    public int getCaptchaLength() {
        return config.getInt("captcha.length", 6);
    }

    public int getCaptchaValidityTime() {
        return config.getInt("captcha.validity-time", 300);
    }

    public boolean isEmailEnabled() {
        return config.getBoolean("email.enabled", true);
    }

    public String getEmailHost() {
        return config.getString("email.host", "smtp.qq.com");
    }

    public int getEmailPort() {
        return config.getInt("email.port", 587);
    }

    public String getEmailUsername() {
        return config.getString("email.username", "your-email@qq.com");
    }

    public String getEmailPassword() {
        return config.getString("email.password", "your-email-password");
    }

    public String getEmailFrom() {
        return config.getString("email.from", "IceCloudLogin <your-email@qq.com>");
    }

    public boolean isEmailSSL() {
        return config.getBoolean("email.ssl", false);
    }

    public boolean isEmailTLS() {
        return config.getBoolean("email.tls", true);
    }

    public int getEmailCodeValidity() {
        return config.getInt("email.code-validity", 300);
    }

    public int getEmailCooldown() {
        return config.getInt("email.cooldown", 60);
    }

    public boolean isQQEnabled() {
        return config.getBoolean("qq.enabled", true);
    }

    public boolean isQQUseEmail() {
        return config.getBoolean("qq.use-email", true);
    }

    public String getQQEmailDomain() {
        return config.getString("qq.email-domain", "qq.com");
    }

    public int getQQCodeValidity() {
        return config.getInt("qq.code-validity", 300);
    }

    public int getQQCooldown() {
        return config.getInt("qq.cooldown", 60);
    }

    public int getTitleUpdateInterval() {
        return config.getInt("title.update-interval", 20);
    }

    public boolean isCaptchaTitleEnabled() {
        return config.getBoolean("title.captcha.enabled", true);
    }

    public String getCaptchaTitle() {
        return translateColorCodes(config.getString("title.captcha.title", "&6请输入验证码"));
    }

    public String getCaptchaSubtitle() {
        return translateColorCodes(config.getString("title.captcha.subtitle", "&e使用 /captcha <验证码>"));
    }

    public boolean isRegisterTitleEnabled() {
        return config.getBoolean("title.register.enabled", true);
    }

    public String getRegisterTitle() {
        return translateColorCodes(config.getString("title.register.title", "&6请注册"));
    }

    public String getRegisterSubtitle() {
        return translateColorCodes(config.getString("title.register.subtitle", "&e使用 /register <密码> <重复密码>"));
    }

    public boolean isLoginTitleEnabled() {
        return config.getBoolean("title.login.enabled", true);
    }

    public String getLoginTitle() {
        return translateColorCodes(config.getString("title.login.title", "&6请登录"));
    }

    public String getLoginSubtitle() {
        return translateColorCodes(config.getString("title.login.subtitle", "&e使用 /login <密码>"));
    }

    public boolean isLoginSuccessTitleEnabled() {
        return config.getBoolean("title.login-success.enabled", true);
    }

    public String getLoginSuccessTitle() {
        return translateColorCodes(config.getString("title.login-success.title", "&a登录成功"));
    }

    public String getLoginSuccessSubtitle() {
        return translateColorCodes(config.getString("title.login-success.subtitle", "&e欢迎回来！"));
    }

    public int getLoginSuccessTitleDuration() {
        return config.getInt("title.login-success.duration", 3);
    }

    public boolean isRegisterSuccessTitleEnabled() {
        return config.getBoolean("title.register-success.enabled", true);
    }

    public String getRegisterSuccessTitle() {
        return translateColorCodes(config.getString("title.register-success.title", "&a注册成功"));
    }

    public String getRegisterSuccessSubtitle() {
        return translateColorCodes(config.getString("title.register-success.subtitle", "&e欢迎加入服务器！"));
    }

    public int getRegisterSuccessTitleDuration() {
        return config.getInt("title.register-success.duration", 3);
    }

    public boolean isActionbarEnabled() {
        return config.getBoolean("actionbar.enabled", true);
    }

    public String getCaptchaActionbar(int time) {
        return translateColorCodes(config.getString("actionbar.captcha", "&e请在 {time} 秒内输入验证码！").replace("{time}", String.valueOf(time)));
    }

    public String getRegisterActionbar(int time) {
        return translateColorCodes(config.getString("actionbar.register", "&e请在 {time} 秒内完成注册！").replace("{time}", String.valueOf(time)));
    }

    public String getLoginActionbar(int time) {
        return translateColorCodes(config.getString("actionbar.login", "&e请在 {time} 秒内完成登录！").replace("{time}", String.valueOf(time)));
    }

    public String getMessagePrefix() {
        return translateColorCodes(messageConfig.getString("prefix", "&6[IceCloudLogin] &r"));
    }

    public String getLoginSuccessMessage() {
        return translateColorCodes(messageConfig.getString("login.success", "&a登录成功！"));
    }

    public String getLoginWrongPasswordMessage(int remaining) {
        return translateColorCodes(messageConfig.getString("login.wrong-password", "&c密码错误！还有 {remaining} 次机会！").replace("{remaining}", String.valueOf(remaining)));
    }

    public String getLoginNotRegisteredMessage() {
        return translateColorCodes(messageConfig.getString("login.not-registered", "&c您还没有注册，请先注册。"));
    }

    public String getLoginAlreadyLoggedInMessage() {
        return translateColorCodes(messageConfig.getString("login.already-logged-in", "&a您已经登录了。"));
    }

    public String getLoginAutoLoginMessage() {
        return translateColorCodes(messageConfig.getString("login.auto-login", "&a自动登录成功！"));
    }

    public String getLoginNeedLoginMessage() {
        return translateColorCodes(messageConfig.getString("login.need-login", "&e请使用 /login <密码> 进行登录。"));
    }

    public String getLoginTimeoutKickMessage() {
        return translateColorCodes(messageConfig.getString("login.timeout-kick", "&c登录超时！请在限定时间内完成登录。"));
    }

    public String getLoginMaxWrongKickMessage() {
        return translateColorCodes(messageConfig.getString("login.max-wrong-kick", "&c密码错误次数过多！"));
    }

    public String getLogoutSuccessMessage() {
        return translateColorCodes(messageConfig.getString("login.logout-success", "&a已成功登出！请重新登录。"));
    }

    public String getRegisterSuccessMessage() {
        return translateColorCodes(messageConfig.getString("register.success", "&a注册成功！已自动登录。"));
    }

    public String getRegisterPasswordMismatchMessage() {
        return translateColorCodes(messageConfig.getString("register.password-mismatch", "&c两次输入的密码不一致！"));
    }

    public String getRegisterPasswordTooShortMessage() {
        return translateColorCodes(messageConfig.getString("register.password-too-short", "&c密码长度不能少于 {min} 位！").replace("{min}", String.valueOf(getMinPasswordLength())));
    }

    public String getRegisterPasswordTooLongMessage() {
        return translateColorCodes(messageConfig.getString("register.password-too-long", "&c密码长度不能超过 {max} 位！").replace("{max}", String.valueOf(getMaxPasswordLength())));
    }

    public String getRegisterPasswordInvalidMessage() {
        return translateColorCodes(messageConfig.getString("register.password-invalid", "&c密码只能包含 0-9, A-Z, a-z, -, _！"));
    }

    public String getRegisterAlreadyRegisteredMessage() {
        return translateColorCodes(messageConfig.getString("register.already-registered", "&c您已经注册过了！"));
    }

    public String getRegisterNeedCaptchaMessage() {
        return translateColorCodes(messageConfig.getString("register.need-captcha", "&e请先使用 /captcha <验证码> 验证验证码！"));
    }

    public String getRegisterCaptchaWrongMessage() {
        return translateColorCodes(messageConfig.getString("register.captcha-wrong", "&c验证码错误！"));
    }

    public String getRegisterCaptchaExpiredMessage() {
        return translateColorCodes(messageConfig.getString("register.captcha-expired", "&c验证码已过期！"));
    }

    public String getRegistrationTimeoutKickMessage() {
        return translateColorCodes(messageConfig.getString("register.timeout-kick", "&c注册超时！请在限定时间内完成注册。"));
    }

    public String getCaptchaMessage(String code) {
        return translateColorCodes(messageConfig.getString("captcha.message", "&e请输入验证码: &6{code}").replace("{code}", code));
    }

    public String getCaptchaPromptMessage() {
        return translateColorCodes(messageConfig.getString("captcha.prompt", "&e请使用 /captcha <验证码> 进行验证。"));
    }

    public String getCaptchaSuccessMessage() {
        return translateColorCodes(messageConfig.getString("captcha.success", "&a验证码验证成功！请使用 /register <密码> <重复密码> 进行注册。"));
    }

    public String getEmailBindSuccessMessage() {
        return translateColorCodes(messageConfig.getString("email.bind-success", "&a邮箱绑定成功！"));
    }

    public String getEmailBindFailedMessage() {
        return translateColorCodes(messageConfig.getString("email.bind-failed", "&c邮箱绑定失败！"));
    }

    public String getEmailAlreadyBoundMessage() {
        return translateColorCodes(messageConfig.getString("email.already-bound", "&c该邮箱已经绑定了！"));
    }

    public String getEmailCodeSentMessage() {
        return translateColorCodes(messageConfig.getString("email.code-sent", "&a验证码已发送到您的邮箱，请在5分钟内完成验证。"));
    }

    public String getEmailCodeWrongMessage() {
        return translateColorCodes(messageConfig.getString("email.code-wrong", "&c验证码错误！"));
    }

    public String getEmailCodeExpiredMessage() {
        return translateColorCodes(messageConfig.getString("email.code-expired", "&c验证码已过期！"));
    }

    public String getEmailCooldownMessage(int time) {
        return translateColorCodes(messageConfig.getString("email.cooldown", "&c请等待 {time} 秒后再发送验证码。").replace("{time}", String.valueOf(time)));
    }

    public String getEmailNeedVerifyMessage() {
        return translateColorCodes(messageConfig.getString("email.need-verify", "&e请使用 /mailc <验证码> 验证邮箱。"));
    }

    public String getEmailNotBoundMessage() {
        return translateColorCodes(messageConfig.getString("email.not-bound", "&c您还没有绑定邮箱！"));
    }

    public String getEmailInvalidMessage() {
        return translateColorCodes(messageConfig.getString("email.invalid", "&c请输入有效的邮箱地址。"));
    }

    public String getEmailNoPendingMessage() {
        return translateColorCodes(messageConfig.getString("email.no-pending", "&c您没有待验证的邮箱绑定请求。"));
    }

    public String getQQBindSuccessMessage() {
        return translateColorCodes(messageConfig.getString("qq.bind-success", "&aQQ绑定成功！"));
    }

    public String getQQBindFailedMessage() {
        return translateColorCodes(messageConfig.getString("qq.bind-failed", "&cQQ绑定失败！"));
    }

    public String getQQAlreadyBoundMessage() {
        return translateColorCodes(messageConfig.getString("qq.already-bound", "&c该QQ已经绑定了！"));
    }

    public String getQQCodeSentMessage() {
        return translateColorCodes(messageConfig.getString("qq.code-sent", "&a验证码已发送到您的QQ邮箱，请在5分钟内完成验证。"));
    }

    public String getQQCodeWrongMessage() {
        return translateColorCodes(messageConfig.getString("qq.code-wrong", "&c验证码错误！"));
    }

    public String getQQCodeExpiredMessage() {
        return translateColorCodes(messageConfig.getString("qq.code-expired", "&c验证码已过期！"));
    }

    public String getQQCooldownMessage(int time) {
        return translateColorCodes(messageConfig.getString("qq.cooldown", "&c请等待 {time} 秒后再发送验证码。").replace("{time}", String.valueOf(time)));
    }

    public String getQQNeedVerifyMessage() {
        return translateColorCodes(messageConfig.getString("qq.need-verify", "&e请使用 /qqc <验证码> 验证QQ。"));
    }

    public String getQQNotBoundMessage() {
        return translateColorCodes(messageConfig.getString("qq.not-bound", "&c您还没有绑定QQ！"));
    }

    public String getQQInvalidMessage() {
        return translateColorCodes(messageConfig.getString("qq.invalid", "&c请输入有效的QQ号！"));
    }

    public String getQQNoPendingMessage() {
        return translateColorCodes(messageConfig.getString("qq.no-pending", "&c您没有待验证的QQ绑定请求。"));
    }

    public String getChatDeniedMessage() {
        return translateColorCodes(messageConfig.getString("restrictions.chat-denied", "&c请先登录后再发送消息！"));
    }

    public String getCommandDeniedMessage() {
        return translateColorCodes(messageConfig.getString("restrictions.command-denied", "&c请先登录后再使用指令！"));
    }

    public String getMoveDeniedMessage() {
        return translateColorCodes(messageConfig.getString("restrictions.move-denied", "&c请先登录后再移动！"));
    }

    public String getInteractDeniedMessage() {
        return translateColorCodes(messageConfig.getString("restrictions.interact-denied", "&c请先登录后再进行交互！"));
    }

    public String getBreakDeniedMessage() {
        return translateColorCodes(messageConfig.getString("restrictions.break-denied", "&c请先登录后再破坏方块！"));
    }

    public String getPlaceDeniedMessage() {
        return translateColorCodes(messageConfig.getString("restrictions.place-denied", "&c请先登录后再放置方块！"));
    }

    public String getDamageDeniedMessage() {
        return translateColorCodes(messageConfig.getString("restrictions.damage-denied", "&c请先登录！"));
    }

    public String getInventoryDeniedMessage() {
        return translateColorCodes(messageConfig.getString("restrictions.inventory-denied", "&c请先登录后再打开背包！"));
    }

    public String getErrorDatabaseErrorMessage() {
        return translateColorCodes(messageConfig.getString("error.database-error", "&c数据库错误，请联系管理员。"));
    }

    public String getErrorEmailErrorMessage() {
        return translateColorCodes(messageConfig.getString("error.email-error", "&c邮件发送失败，请联系管理员。"));
    }

    public String getErrorUnknownErrorMessage() {
        return translateColorCodes(messageConfig.getString("error.unknown-error", "&c发生未知错误，请联系管理员。"));
    }

    public String getErrorPlayerOnlyMessage() {
        return translateColorCodes(messageConfig.getString("error.player-only", "&c此命令只能由玩家执行。"));
    }

    public String getErrorNeedLoginFirstMessage() {
        return translateColorCodes(messageConfig.getString("error.need-login-first", "&c请先完成登录或注册。"));
    }

    public String getErrorNoPermissionMessage() {
        return translateColorCodes(messageConfig.getString("error.no-permission", "&c您没有权限执行此命令！"));
    }

    public String getErrorPlayerNotFoundMessage() {
        return translateColorCodes(messageConfig.getString("error.player-not-found", "&c未找到该玩家！"));
    }

    public String getPasswordResetEmailCodeSentMessage() {
        return translateColorCodes(messageConfig.getString("password-reset.email-code-sent", "&a验证码已发送到您的绑定邮箱，请在5分钟内完成验证。"));
    }

    public String getPasswordResetQQCodeSentMessage() {
        return translateColorCodes(messageConfig.getString("password-reset.qq-code-sent", "&a验证码已发送到您的绑定QQ邮箱，请在5分钟内完成验证。"));
    }

    public String getPasswordResetVerifySuccessMessage() {
        return translateColorCodes(messageConfig.getString("password-reset.verify-success", "&a验证成功！请使用 /register <密码> <重复密码> 重新设置密码。"));
    }

    public String getPasswordResetNoBindingMessage(String method) {
        return translateColorCodes(messageConfig.getString("password-reset.no-binding", "&c您没有绑定{method}，无法使用此功能！").replace("{method}", method));
    }

    public String getPasswordResetInvalidMethodMessage() {
        return translateColorCodes(messageConfig.getString("password-reset.invalid-method", "&c无效的验证方式！"));
    }

    public String getChangePasswordSuccessMessage() {
        return translateColorCodes(messageConfig.getString("change-password.success", "&a密码修改成功！请使用新密码重新登录。"));
    }

    public String getChangePasswordWrongOldMessage() {
        return translateColorCodes(messageConfig.getString("change-password.wrong-old-password", "&c原密码错误！"));
    }

    public String getChangePasswordSameMessage() {
        return translateColorCodes(messageConfig.getString("change-password.same-password", "&c新密码不能与旧密码相同！"));
    }

    public String getChangePasswordNeedLoginMessage() {
        return translateColorCodes(messageConfig.getString("change-password.need-login", "&c请先登录后再修改密码！"));
    }

    public String getChangeEmailSuccessMessage() {
        return translateColorCodes(messageConfig.getString("change-email.success", "&a邮箱修改成功！"));
    }

    public String getChangeEmailWrongOldMessage() {
        return translateColorCodes(messageConfig.getString("change-email.wrong-old-email", "&c原邮箱错误！"));
    }

    public String getChangeEmailCodeSentMessage() {
        return translateColorCodes(messageConfig.getString("change-email.code-sent", "&a验证码已发送到新邮箱，请在5分钟内完成验证。"));
    }

    public String getChangeEmailVerifySuccessMessage(String email) {
        return translateColorCodes(messageConfig.getString("change-email.verify-success", "&a验证成功！邮箱已更改为 {email}").replace("{email}", email));
    }

    public String getChangeEmailSameMessage() {
        return translateColorCodes(messageConfig.getString("change-email.same-email", "&c新邮箱不能与旧邮箱相同！"));
    }

    public String getChangeEmailNeedLoginMessage() {
        return translateColorCodes(messageConfig.getString("change-email.need-login", "&c请先登录后再修改邮箱！"));
    }

    public String getChangeEmailNeedVerifyFirstMessage() {
        return translateColorCodes(messageConfig.getString("change-email.need-verify-first", "&c请先验证新邮箱！"));
    }

    public String getChangeQQSuccessMessage() {
        return translateColorCodes(messageConfig.getString("change-qq.success", "&aQQ修改成功！"));
    }

    public String getChangeQQWrongOldMessage() {
        return translateColorCodes(messageConfig.getString("change-qq.wrong-old-qq", "&c原QQ号错误！"));
    }

    public String getChangeQQCodeSentMessage() {
        return translateColorCodes(messageConfig.getString("change-qq.code-sent", "&a验证码已发送到新QQ邮箱，请在5分钟内完成验证。"));
    }

    public String getChangeQQVerifySuccessMessage(String qq) {
        return translateColorCodes(messageConfig.getString("change-qq.verify-success", "&a验证成功！QQ已更改为 {qq}").replace("{qq}", qq));
    }

    public String getChangeQQSameMessage() {
        return translateColorCodes(messageConfig.getString("change-qq.same-qq", "&c新QQ号不能与旧QQ号相同！"));
    }

    public String getChangeQQNeedLoginMessage() {
        return translateColorCodes(messageConfig.getString("change-qq.need-login", "&c请先登录后再修改QQ！"));
    }

    public String getChangeQQNeedVerifyFirstMessage() {
        return translateColorCodes(messageConfig.getString("change-qq.need-verify-first", "&c请先验证新QQ！"));
    }

    public String getMainCommandHelpHeader() {
        return translateColorCodes(messageConfig.getString("main-command.help-header", "&6========== IceCloudLogin 帮助 =========="));
    }

    public String getMainCommandHelpFooter() {
        return translateColorCodes(messageConfig.getString("main-command.help-footer", "&6========================================="));
    }

    public String getMainCommandHelpLogin() {
        return translateColorCodes(messageConfig.getString("main-command.help-login", "&e/login <密码> &7- 登录服务器"));
    }

    public String getMainCommandHelpRegister() {
        return translateColorCodes(messageConfig.getString("main-command.help-register", "&e/register <密码> <重复密码> &7- 注册账号"));
    }

    public String getMainCommandHelpCaptcha() {
        return translateColorCodes(messageConfig.getString("main-command.help-captcha", "&e/captcha <验证码> &7- 验证验证码"));
    }

    public String getMainCommandHelpMail() {
        return translateColorCodes(messageConfig.getString("main-command.help-mail", "&e/mail <邮箱> &7- 绑定邮箱"));
    }

    public String getMainCommandHelpMailc() {
        return translateColorCodes(messageConfig.getString("main-command.help-mailc", "&e/mailc <验证码> &7- 验证邮箱"));
    }

    public String getMainCommandHelpQQ() {
        return translateColorCodes(messageConfig.getString("main-command.help-qq", "&e/qq <QQ号> &7- 绑定QQ"));
    }

    public String getMainCommandHelpQQc() {
        return translateColorCodes(messageConfig.getString("main-command.help-qqc", "&e/qqc <验证码> &7- 验证QQ"));
    }

    public String getMainCommandHelpLogout() {
        return translateColorCodes(messageConfig.getString("main-command.help-logout", "&e/logout &7- 登出服务器"));
    }

    public String getMainCommandHelpChangepass() {
        return translateColorCodes(messageConfig.getString("main-command.help-changepass", "&e/changepass <原密码> <新密码> &7- 修改密码"));
    }

    public String getMainCommandHelpChangemail() {
        return translateColorCodes(messageConfig.getString("main-command.help-changemail", "&e/changemail <原邮箱> <新邮箱> &7- 修改邮箱"));
    }

    public String getMainCommandHelpChangeqq() {
        return translateColorCodes(messageConfig.getString("main-command.help-changeqq", "&e/changeqq <原QQ> <新QQ> &7- 修改QQ"));
    }

    public String getMainCommandHelpMailzhpass() {
        return translateColorCodes(messageConfig.getString("main-command.help-mailzhpass", "&e/mailzhpass &7- 通过邮箱重置密码"));
    }

    public String getMainCommandHelpQqzhpass() {
        return translateColorCodes(messageConfig.getString("main-command.help-qqzhpass", "&e/qqzhpass &7- 通过QQ重置密码"));
    }

    public String getMainCommandHelpRegdel() {
        return translateColorCodes(messageConfig.getString("main-command.help-regdel", "&e/regdel <玩家> &7- 删除玩家注册(管理员)"));
    }

    public String getMainCommandHelpReload() {
        return translateColorCodes(messageConfig.getString("main-command.help-reload", "&e/icecloudlogin reload &7- 重载配置(管理员)"));
    }

    public String getMainCommandHelpVersion() {
        return translateColorCodes(messageConfig.getString("main-command.help-version", "&e/icecloudlogin version &7- 查看版本"));
    }

    public String getMainCommandReloadSuccess() {
        return translateColorCodes(messageConfig.getString("main-command.reload-success", "&a配置重载成功！"));
    }

    public String getMainCommandVersion() {
        return translateColorCodes(messageConfig.getString("main-command.version", "&6IceCloudLogin &ev{version} &7- 作者: ya_xzer21145"));
    }

    public String getUsageLoginMessage() {
        return translateColorCodes(messageConfig.getString("misc.usage-login", "&e用法: /login <密码>"));
    }

    public String getUsageRegisterMessage() {
        return translateColorCodes(messageConfig.getString("misc.usage-register", "&e用法: /register <密码> <重复密码>"));
    }

    public String getUsageCaptchaMessage() {
        return translateColorCodes(messageConfig.getString("misc.usage-captcha", "&e用法: /captcha <验证码>"));
    }

    public String getUsageMailMessage() {
        return translateColorCodes(messageConfig.getString("misc.usage-mail", "&e用法: /mail <邮箱>"));
    }

    public String getUsageMailcMessage() {
        return translateColorCodes(messageConfig.getString("misc.usage-mailc", "&e用法: /mailc <验证码>"));
    }

    public String getUsageQQMessage() {
        return translateColorCodes(messageConfig.getString("misc.usage-qq", "&e用法: /qq <QQ号>"));
    }

    public String getUsageQQcMessage() {
        return translateColorCodes(messageConfig.getString("misc.usage-qqc", "&e用法: /qqc <验证码>"));
    }

    public String getUsageLogoutMessage() {
        return translateColorCodes(messageConfig.getString("misc.usage-logout", "&e用法: /logout"));
    }

    public String getUsageRegdelMessage() {
        return translateColorCodes(messageConfig.getString("misc.usage-regdel", "&e用法: /regdel <玩家>"));
    }

    public String getUsageMailzhpassMessage() {
        return translateColorCodes(messageConfig.getString("misc.usage-mailzhpass", "&e用法: /mailzhpass"));
    }

    public String getUsageQqzhpassMessage() {
        return translateColorCodes(messageConfig.getString("misc.usage-qqzhpass", "&e用法: /qqzhpass"));
    }

    public String getUsageChangepassMessage() {
        return translateColorCodes(messageConfig.getString("misc.usage-changepass", "&e用法: /changepass <原密码> <新密码>"));
    }

    public String getUsageChangemailMessage() {
        return translateColorCodes(messageConfig.getString("misc.usage-changemail", "&e用法: /changemail <原邮箱> <新邮箱>"));
    }

    public String getUsageChangeqqMessage() {
        return translateColorCodes(messageConfig.getString("misc.usage-changeqq", "&e用法: /changeqq <原QQ> <新QQ>"));
    }

    public String getAdminRegdelSuccessMessage(String player) {
        return translateColorCodes(messageConfig.getString("admin.regdel-success", "&a已成功删除玩家 {player} 的注册信息！").replace("{player}", player));
    }

    public String getAdminRegdelFailedMessage() {
        return translateColorCodes(messageConfig.getString("admin.regdel-failed", "&c删除玩家注册信息失败！"));
    }

    private String translateColorCodes(String message) {
        if (message == null) return "";
        return message.replace("&", "\u00A7");
    }
}
