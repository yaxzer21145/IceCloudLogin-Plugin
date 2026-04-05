package com.icecloud.login;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;
import java.util.regex.Pattern;

public class CommandHandler implements CommandExecutor {
    private final IceCloudLogin plugin;

    public CommandHandler(IceCloudLogin plugin) {
        this.plugin = plugin;
    }

    public void registerCommands() {
        plugin.getCommand("login").setExecutor(this);
        plugin.getCommand("l").setExecutor(this);
        plugin.getCommand("register").setExecutor(this);
        plugin.getCommand("reg").setExecutor(this);
        plugin.getCommand("captcha").setExecutor(this);
        plugin.getCommand("mail").setExecutor(this);
        plugin.getCommand("mailc").setExecutor(this);
        plugin.getCommand("qq").setExecutor(this);
        plugin.getCommand("qqc").setExecutor(this);
        plugin.getCommand("logout").setExecutor(this);
        plugin.getCommand("regdel").setExecutor(this);
        plugin.getCommand("mailzhpass").setExecutor(this);
        plugin.getCommand("qqzhpass").setExecutor(this);
        plugin.getCommand("changepass").setExecutor(this);
        plugin.getCommand("cpss").setExecutor(this);
        plugin.getCommand("changepassword").setExecutor(this);
        plugin.getCommand("changemail").setExecutor(this);
        plugin.getCommand("cmail").setExecutor(this);
        plugin.getCommand("changeqq").setExecutor(this);
        plugin.getCommand("cq").setExecutor(this);
        plugin.getCommand("icecloudlogin").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String cmdName = command.getName().toLowerCase();

        switch (cmdName) {
            case "login":
            case "l":
                return handleLogin(sender, args);
            case "register":
            case "reg":
                return handleRegister(sender, args);
            case "captcha":
                return handleCaptcha(sender, args);
            case "mail":
                return handleMail(sender, args);
            case "mailc":
                return handleMailConfirm(sender, args);
            case "qq":
                return handleQQ(sender, args);
            case "qqc":
                return handleQQConfirm(sender, args);
            case "logout":
                return handleLogout(sender, args);
            case "regdel":
                return handleRegdel(sender, args);
            case "mailzhpass":
                return handleMailResetPassword(sender, args);
            case "qqzhpass":
                return handleQQResetPassword(sender, args);
            case "changepass":
            case "cpss":
            case "changepassword":
                return handleChangePassword(sender, args);
            case "changemail":
            case "cmail":
                return handleChangeEmail(sender, args);
            case "changeqq":
            case "cq":
                return handleChangeQQ(sender, args);
            case "icecloudlogin":
                return handleMainCommand(sender, args);
            default:
                return false;
        }
    }

    private boolean handleLogin(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getErrorPlayerOnlyMessage());
            return true;
        }

        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();

        IceCloudLogin.PlayerSession session = plugin.getPlayerSession(uuid);
        if (session == null) {
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getErrorUnknownErrorMessage());
            return true;
        }

        if (session.isLoggedIn()) {
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getLoginAlreadyLoggedInMessage());
            return true;
        }

        if (!plugin.getDatabaseManager().isPlayerRegistered(uuid)) {
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getLoginNotRegisteredMessage());
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getUsageLoginMessage());
            return true;
        }

        String password = args[0];
        PlayerData playerData = plugin.getDatabaseManager().getPlayerData(uuid);

        if (!verifyPassword(password, playerData.getPasswordHash())) {
            int wrongCount = plugin.getWrongPasswordCount(uuid) + 1;
            int maxWrong = plugin.getConfigManager().getMaxWrongPassword();
            int remaining = maxWrong - wrongCount;
            
            plugin.incrementWrongPasswordCount(uuid);
            
            if (remaining <= 0) {
                player.kickPlayer(plugin.getConfigManager().getLoginMaxWrongKickMessage());
                plugin.incrementKickCount(uuid);
                
                int kickCount = plugin.getKickCount(uuid);
                int maxKick = plugin.getConfigManager().getMaxKickCount();
                
                if (kickCount >= maxKick) {
                    plugin.executeKickCommands(player.getName());
                    plugin.resetKickCount(uuid);
                }
            } else {
                player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getLoginWrongPasswordMessage(remaining));
            }
            return true;
        }

        plugin.resetWrongPasswordCount(uuid);
        session.setLoggedIn(true);
        session.cancelTasks();
        plugin.getDatabaseManager().updateLastLogin(uuid);
        player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getLoginSuccessMessage());
        sendLoginSuccessTitle(player);
        sendWelcomeMessage(player);
        return true;
    }

    private boolean handleRegister(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getErrorPlayerOnlyMessage());
            return true;
        }

        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();

        IceCloudLogin.PlayerSession session = plugin.getPlayerSession(uuid);
        if (session == null) {
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getErrorUnknownErrorMessage());
            return true;
        }

        if (plugin.getDatabaseManager().isPlayerRegistered(uuid)) {
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getRegisterAlreadyRegisteredMessage());
            return true;
        }

        if (!session.isCaptchaVerified()) {
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getRegisterNeedCaptchaMessage());
            return true;
        }

        if (args.length != 2) {
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getUsageRegisterMessage());
            return true;
        }

        String password = args[0];
        String confirmPassword = args[1];

        if (!password.equals(confirmPassword)) {
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getRegisterPasswordMismatchMessage());
            return true;
        }

        if (password.length() < plugin.getConfigManager().getMinPasswordLength()) {
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getRegisterPasswordTooShortMessage());
            return true;
        }

        if (password.length() > plugin.getConfigManager().getMaxPasswordLength()) {
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getRegisterPasswordTooLongMessage());
            return true;
        }

        Pattern pattern = plugin.getConfigManager().getPasswordPattern();
        if (!pattern.matcher(password).matches()) {
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getRegisterPasswordInvalidMessage());
            return true;
        }

        String passwordHash = hashPassword(password);
        plugin.getDatabaseManager().registerPlayer(uuid, player.getName(), passwordHash);

        session.setLoggedIn(true);
        session.cancelTasks();
        plugin.getDatabaseManager().updateLastLogin(uuid);
        player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getRegisterSuccessMessage());
        sendRegisterSuccessTitle(player);
        sendWelcomeMessage(player);
        return true;
    }

    private boolean handleCaptcha(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getErrorPlayerOnlyMessage());
            return true;
        }

        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();

        IceCloudLogin.PlayerSession session = plugin.getPlayerSession(uuid);
        if (session == null) {
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getErrorUnknownErrorMessage());
            return true;
        }

        if (session.isCaptchaVerified()) {
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getCaptchaSuccessMessage());
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getUsageCaptchaMessage());
            return true;
        }

        String inputCode = args[0];

        if (!plugin.getCaptchaManager().validateCaptcha(uuid, inputCode)) {
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getRegisterCaptchaWrongMessage());
            return true;
        }

        session.setCaptchaVerified(true);
        session.setState(IceCloudLogin.SessionState.REGISTER);
        session.setRemainingTime(plugin.getConfigManager().getRegistrationTimeout());
        
        session.cancelTasks();
        startTitleTask(player, session, IceCloudLogin.SessionState.REGISTER);
        startActionbarTask(player, session, IceCloudLogin.SessionState.REGISTER);
        
        player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getCaptchaSuccessMessage());
        return true;
    }

    private boolean handleMail(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getErrorPlayerOnlyMessage());
            return true;
        }

        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();

        IceCloudLogin.PlayerSession session = plugin.getPlayerSession(uuid);
        if (session == null || !session.isLoggedIn()) {
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getErrorNeedLoginFirstMessage());
            return true;
        }

        if (!plugin.getConfigManager().isEmailEnabled()) {
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getErrorUnknownErrorMessage());
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getUsageMailMessage());
            return true;
        }

        String email = args[0];
        if (!isValidEmail(email)) {
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getEmailInvalidMessage());
            return true;
        }

        PlayerData playerData = plugin.getDatabaseManager().getPlayerData(uuid);
        if (playerData.getEmail() != null && !playerData.getEmail().isEmpty()) {
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getEmailAlreadyBoundMessage());
            return true;
        }

        IceCloudLogin.PendingBinding existingBinding = plugin.getPendingEmailBinding(uuid);
        if (existingBinding != null) {
            long elapsed = System.currentTimeMillis() - existingBinding.getTimestamp();
            int cooldown = plugin.getConfigManager().getEmailCooldown() * 1000;
            if (elapsed < cooldown) {
                int remaining = (int) ((cooldown - elapsed) / 1000);
                player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getEmailCooldownMessage(remaining));
                return true;
            }
        }

        String code = generateCode();
        boolean sent = plugin.getEmailManager().sendEmail(email, "IceCloudLogin 邮箱验证",
            "您的验证码是: " + code + "\n验证码有效期为5分钟。");

        if (!sent) {
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getErrorEmailErrorMessage());
            return true;
        }

        plugin.createPendingEmailBinding(uuid, new IceCloudLogin.PendingBinding(email, code));
        player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getEmailCodeSentMessage());
        player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getEmailNeedVerifyMessage());
        return true;
    }

    private boolean handleMailConfirm(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getErrorPlayerOnlyMessage());
            return true;
        }

        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();

        IceCloudLogin.PlayerSession session = plugin.getPlayerSession(uuid);
        if (session == null || !session.isLoggedIn()) {
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getErrorNeedLoginFirstMessage());
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getUsageMailcMessage());
            return true;
        }

        IceCloudLogin.PendingBinding binding = plugin.getPendingEmailBinding(uuid);
        if (binding == null) {
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getEmailNoPendingMessage());
            return true;
        }

        long elapsed = System.currentTimeMillis() - binding.getTimestamp();
        int validity = plugin.getConfigManager().getEmailCodeValidity() * 1000;
        if (elapsed > validity) {
            plugin.removePendingEmailBinding(uuid);
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getEmailCodeExpiredMessage());
            return true;
        }

        String inputCode = args[0];
        if (!binding.getCode().equals(inputCode)) {
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getEmailCodeWrongMessage());
            return true;
        }

        boolean success = plugin.getDatabaseManager().updateEmail(uuid, binding.getValue());
        if (success) {
            plugin.removePendingEmailBinding(uuid);
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getEmailBindSuccessMessage());
        } else {
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getEmailBindFailedMessage());
        }
        return true;
    }

    private boolean handleQQ(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getErrorPlayerOnlyMessage());
            return true;
        }

        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();

        IceCloudLogin.PlayerSession session = plugin.getPlayerSession(uuid);
        if (session == null || !session.isLoggedIn()) {
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getErrorNeedLoginFirstMessage());
            return true;
        }

        if (!plugin.getConfigManager().isQQEnabled()) {
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getErrorUnknownErrorMessage());
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getUsageQQMessage());
            return true;
        }

        String qq = args[0];
        if (!isValidQQ(qq)) {
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getQQInvalidMessage());
            return true;
        }

        PlayerData playerData = plugin.getDatabaseManager().getPlayerData(uuid);
        if (playerData.getQq() != null && !playerData.getQq().isEmpty()) {
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getQQAlreadyBoundMessage());
            return true;
        }

        IceCloudLogin.PendingBinding existingBinding = plugin.getPendingQQBinding(uuid);
        if (existingBinding != null) {
            long elapsed = System.currentTimeMillis() - existingBinding.getTimestamp();
            int cooldown = plugin.getConfigManager().getQQCooldown() * 1000;
            if (elapsed < cooldown) {
                int remaining = (int) ((cooldown - elapsed) / 1000);
                player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getQQCooldownMessage(remaining));
                return true;
            }
        }

        String code = generateCode();
        String qqEmail = qq + "@" + plugin.getConfigManager().getQQEmailDomain();
        boolean sent = plugin.getEmailManager().sendEmail(qqEmail, "IceCloudLogin QQ验证", 
            "您的验证码是: " + code + "\n验证码有效期为5分钟。");

        if (!sent) {
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getErrorEmailErrorMessage());
            return true;
        }

        plugin.createPendingQQBinding(uuid, new IceCloudLogin.PendingBinding(qq, code));
        player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getQQCodeSentMessage());
        player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getQQNeedVerifyMessage());
        return true;
    }

    private boolean handleQQConfirm(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getErrorPlayerOnlyMessage());
            return true;
        }

        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();

        IceCloudLogin.PlayerSession session = plugin.getPlayerSession(uuid);
        if (session == null || !session.isLoggedIn()) {
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getErrorNeedLoginFirstMessage());
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getUsageQQcMessage());
            return true;
        }

        IceCloudLogin.PendingBinding binding = plugin.getPendingQQBinding(uuid);
        if (binding == null) {
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getQQNoPendingMessage());
            return true;
        }

        long elapsed = System.currentTimeMillis() - binding.getTimestamp();
        int validity = plugin.getConfigManager().getQQCodeValidity() * 1000;
        if (elapsed > validity) {
            plugin.removePendingQQBinding(uuid);
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getQQCodeExpiredMessage());
            return true;
        }

        String inputCode = args[0];
        if (!binding.getCode().equals(inputCode)) {
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getQQCodeWrongMessage());
            return true;
        }

        boolean success = plugin.getDatabaseManager().updateQQ(uuid, binding.getValue());
        if (success) {
            plugin.removePendingQQBinding(uuid);
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getQQBindSuccessMessage());
        } else {
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getQQBindFailedMessage());
        }
        return true;
    }

    private boolean handleLogout(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getErrorPlayerOnlyMessage());
            return true;
        }

        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();

        IceCloudLogin.PlayerSession session = plugin.getPlayerSession(uuid);
        if (session == null) {
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getErrorUnknownErrorMessage());
            return true;
        }

        if (!session.isLoggedIn()) {
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getErrorNeedLoginFirstMessage());
            return true;
        }

        session.setLoggedIn(false);
        session.setState(IceCloudLogin.SessionState.LOGIN);
        session.setRemainingTime(plugin.getConfigManager().getLoginTimeout());
        
        startTitleTask(player, session, IceCloudLogin.SessionState.LOGIN);
        startActionbarTask(player, session, IceCloudLogin.SessionState.LOGIN);
        
        player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getLogoutSuccessMessage());
        return true;
    }

    private boolean handleRegdel(CommandSender sender, String[] args) {
        if (!sender.hasPermission("icecloudlogin.admin")) {
            sender.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getErrorNoPermissionMessage());
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getUsageRegdelMessage());
            return true;
        }

        String playerName = args[0];
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
        UUID uuid = offlinePlayer.getUniqueId();

        if (!plugin.getDatabaseManager().isPlayerRegistered(uuid)) {
            sender.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getErrorPlayerNotFoundMessage());
            return true;
        }

        boolean success = plugin.getDatabaseManager().deletePlayer(uuid);
        if (success) {
            if (offlinePlayer.isOnline()) {
                Player onlinePlayer = offlinePlayer.getPlayer();
                if (onlinePlayer != null) {
                    onlinePlayer.kickPlayer("您的账号已被删除！");
                }
            }
            sender.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getAdminRegdelSuccessMessage(playerName));
        } else {
            sender.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getAdminRegdelFailedMessage());
        }
        return true;
    }

    private boolean handleMailResetPassword(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getErrorPlayerOnlyMessage());
            return true;
        }

        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();

        if (!plugin.getDatabaseManager().isPlayerRegistered(uuid)) {
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getLoginNotRegisteredMessage());
            return true;
        }

        PlayerData playerData = plugin.getDatabaseManager().getPlayerData(uuid);
        if (playerData.getEmail() == null || playerData.getEmail().isEmpty()) {
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getPasswordResetNoBindingMessage("邮箱"));
            return true;
        }

        String code = generateCode();
        boolean sent = plugin.getEmailManager().sendEmail(playerData.getEmail(), "IceCloudLogin 密码重置",
            "您的验证码是: " + code + "\n验证码有效期为5分钟。");

        if (!sent) {
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getErrorEmailErrorMessage());
            return true;
        }

        plugin.createPendingPasswordReset(uuid, new IceCloudLogin.PasswordResetVerification("email", playerData.getEmail(), code));
        player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getPasswordResetEmailCodeSentMessage());
        return true;
    }

    private boolean handleQQResetPassword(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getErrorPlayerOnlyMessage());
            return true;
        }

        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();

        if (!plugin.getDatabaseManager().isPlayerRegistered(uuid)) {
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getLoginNotRegisteredMessage());
            return true;
        }

        PlayerData playerData = plugin.getDatabaseManager().getPlayerData(uuid);
        if (playerData.getQq() == null || playerData.getQq().isEmpty()) {
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getPasswordResetNoBindingMessage("QQ"));
            return true;
        }

        String code = generateCode();
        String qqEmail = playerData.getQq() + "@" + plugin.getConfigManager().getQQEmailDomain();
        boolean sent = plugin.getEmailManager().sendEmail(qqEmail, "IceCloudLogin 密码重置",
            "您的验证码是: " + code + "\n验证码有效期为5分钟。");

        if (!sent) {
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getErrorEmailErrorMessage());
            return true;
        }

        plugin.createPendingPasswordReset(uuid, new IceCloudLogin.PasswordResetVerification("qq", playerData.getQq(), code));
        player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getPasswordResetQQCodeSentMessage());
        return true;
    }

    private boolean handleChangePassword(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getErrorPlayerOnlyMessage());
            return true;
        }

        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();

        IceCloudLogin.PlayerSession session = plugin.getPlayerSession(uuid);
        if (session == null || !session.isLoggedIn()) {
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getChangePasswordNeedLoginMessage());
            return true;
        }

        if (args.length != 2) {
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getUsageChangepassMessage());
            return true;
        }

        String oldPassword = args[0];
        String newPassword = args[1];

        PlayerData playerData = plugin.getDatabaseManager().getPlayerData(uuid);
        if (!verifyPassword(oldPassword, playerData.getPasswordHash())) {
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getChangePasswordWrongOldMessage());
            return true;
        }

        if (oldPassword.equals(newPassword)) {
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getChangePasswordSameMessage());
            return true;
        }

        if (newPassword.length() < plugin.getConfigManager().getMinPasswordLength()) {
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getRegisterPasswordTooShortMessage());
            return true;
        }

        if (newPassword.length() > plugin.getConfigManager().getMaxPasswordLength()) {
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getRegisterPasswordTooLongMessage());
            return true;
        }

        Pattern pattern = plugin.getConfigManager().getPasswordPattern();
        if (!pattern.matcher(newPassword).matches()) {
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getRegisterPasswordInvalidMessage());
            return true;
        }

        String newPasswordHash = hashPassword(newPassword);
        boolean success = plugin.getDatabaseManager().updatePassword(uuid, newPasswordHash);
        if (success) {
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getChangePasswordSuccessMessage());
        } else {
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getErrorDatabaseErrorMessage());
        }
        return true;
    }

    private boolean handleChangeEmail(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getErrorPlayerOnlyMessage());
            return true;
        }

        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();

        IceCloudLogin.PlayerSession session = plugin.getPlayerSession(uuid);
        if (session == null || !session.isLoggedIn()) {
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getChangeEmailNeedLoginMessage());
            return true;
        }

        if (args.length != 2) {
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getUsageChangemailMessage());
            return true;
        }

        String oldEmail = args[0];
        String newEmail = args[1];

        PlayerData playerData = plugin.getDatabaseManager().getPlayerData(uuid);
        if (playerData.getEmail() == null || !playerData.getEmail().equalsIgnoreCase(oldEmail)) {
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getChangeEmailWrongOldMessage());
            return true;
        }

        if (oldEmail.equalsIgnoreCase(newEmail)) {
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getChangeEmailSameMessage());
            return true;
        }

        if (!isValidEmail(newEmail)) {
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getEmailInvalidMessage());
            return true;
        }

        String code = generateCode();
        boolean sent = plugin.getEmailManager().sendEmail(newEmail, "IceCloudLogin 邮箱修改验证",
            "您的验证码是: " + code + "\n验证码有效期为5分钟。");

        if (!sent) {
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getErrorEmailErrorMessage());
            return true;
        }

        plugin.createPendingEmailBinding(uuid, new IceCloudLogin.PendingBinding(newEmail, code));
        player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getChangeEmailCodeSentMessage());
        return true;
    }

    private boolean handleChangeQQ(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getErrorPlayerOnlyMessage());
            return true;
        }

        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();

        IceCloudLogin.PlayerSession session = plugin.getPlayerSession(uuid);
        if (session == null || !session.isLoggedIn()) {
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getChangeQQNeedLoginMessage());
            return true;
        }

        if (args.length != 2) {
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getUsageChangeqqMessage());
            return true;
        }

        String oldQQ = args[0];
        String newQQ = args[1];

        PlayerData playerData = plugin.getDatabaseManager().getPlayerData(uuid);
        if (playerData.getQq() == null || !playerData.getQq().equals(oldQQ)) {
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getChangeQQWrongOldMessage());
            return true;
        }

        if (oldQQ.equals(newQQ)) {
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getChangeQQSameMessage());
            return true;
        }

        if (!isValidQQ(newQQ)) {
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getQQInvalidMessage());
            return true;
        }

        String code = generateCode();
        String qqEmail = newQQ + "@" + plugin.getConfigManager().getQQEmailDomain();
        boolean sent = plugin.getEmailManager().sendEmail(qqEmail, "IceCloudLogin QQ修改验证",
            "您的验证码是: " + code + "\n验证码有效期为5分钟。");

        if (!sent) {
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getErrorEmailErrorMessage());
            return true;
        }

        plugin.createPendingQQBinding(uuid, new IceCloudLogin.PendingBinding(newQQ, code));
        player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getChangeQQCodeSentMessage());
        return true;
    }

    private boolean handleMainCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(plugin.getConfigManager().getMessagePrefix() + translateColorCodes("&6IceCloudLogin &ev0.5.1"));
            sender.sendMessage(plugin.getConfigManager().getMessagePrefix() + translateColorCodes("&e作者: &aya_xzer21145"));
            sender.sendMessage(plugin.getConfigManager().getMessagePrefix() + translateColorCodes("&e使用 &a/icecloudlogin help &e查看帮助"));
            return true;
        }

        String subCmd = args[0].toLowerCase();

        switch (subCmd) {
            case "help":
                sender.sendMessage(plugin.getConfigManager().getMainCommandHelpHeader());
                sender.sendMessage(translateColorCodes("&e登录相关:"));
                sender.sendMessage(translateColorCodes("  &a/login <密码> &7- 登录服务器"));
                sender.sendMessage(translateColorCodes("  &a/register <密码> <重复密码> &7- 注册账号"));
                sender.sendMessage(translateColorCodes("  &a/captcha <验证码> &7- 验证验证码"));
                sender.sendMessage(translateColorCodes("  &a/logout &7- 登出服务器"));
                sender.sendMessage(translateColorCodes("&e绑定相关:"));
                sender.sendMessage(translateColorCodes("  &a/mail <邮箱> &7- 绑定邮箱"));
                sender.sendMessage(translateColorCodes("  &a/mailc <验证码> &7- 验证邮箱"));
                sender.sendMessage(translateColorCodes("  &a/qq <QQ号> &7- 绑定QQ"));
                sender.sendMessage(translateColorCodes("  &a/qqc <验证码> &7- 验证QQ"));
                sender.sendMessage(translateColorCodes("&e修改相关:"));
                sender.sendMessage(translateColorCodes("  &a/changepass <原密码> <新密码> &7- 修改密码"));
                sender.sendMessage(translateColorCodes("  &a/changemail <原邮箱> <新邮箱> &7- 修改邮箱"));
                sender.sendMessage(translateColorCodes("  &a/changeqq <原QQ> <新QQ> &7- 修改QQ"));
                sender.sendMessage(translateColorCodes("&e密码重置:"));
                sender.sendMessage(translateColorCodes("  &a/mailzhpass &7- 通过邮箱重置密码"));
                sender.sendMessage(translateColorCodes("  &a/qqzhpass &7- 通过QQ重置密码"));
                if (sender.hasPermission("icecloudlogin.admin")) {
                    sender.sendMessage(translateColorCodes("&e管理员:"));
                    sender.sendMessage(translateColorCodes("  &a/regdel <玩家> &7- 删除玩家注册"));
                    sender.sendMessage(translateColorCodes("  &a/icecloudlogin reload &7- 重载配置"));
                }
                sender.sendMessage(plugin.getConfigManager().getMainCommandHelpFooter());
                return true;
            case "reload":
                if (!sender.hasPermission("icecloudlogin.admin")) {
                    sender.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getErrorNoPermissionMessage());
                    return true;
                }
                plugin.getConfigManager().loadConfig();
                sender.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getMainCommandReloadSuccess());
                return true;
            case "version":
                sender.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getMainCommandVersion().replace("{version}", "0.5.1"));
                return true;
            default:
                sender.sendMessage(plugin.getConfigManager().getMessagePrefix() + translateColorCodes("&c未知的子命令！使用 /icecloudlogin help 查看帮助。"));
                return true;
        }
    }

    private String translateColorCodes(String message) {
        if (message == null) return "";
        return message.replace("&", "\u00A7");
    }

    private String hashPassword(String password) {
        return plugin.getCaptchaManager().hashPassword(password);
    }

    private boolean verifyPassword(String inputPassword, String storedHash) {
        return plugin.getCaptchaManager().verifyPassword(inputPassword, storedHash);
    }

    private String generateCode() {
        java.util.Random random = new java.util.Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    }

    private boolean isValidQQ(String qq) {
        return qq != null && qq.matches("^[1-9][0-9]{4,10}$");
    }

    private void startTitleTask(Player player, IceCloudLogin.PlayerSession session, IceCloudLogin.SessionState state) {
        org.bukkit.scheduler.BukkitRunnable task = new org.bukkit.scheduler.BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || session.isLoggedIn()) {
                    cancel();
                    return;
                }

                String title = "";
                String subtitle = "";

                switch (state) {
                    case CAPTCHA:
                        title = plugin.getConfigManager().getCaptchaTitle();
                        subtitle = plugin.getConfigManager().getCaptchaSubtitle();
                        break;
                    case REGISTER:
                        title = plugin.getConfigManager().getRegisterTitle();
                        subtitle = plugin.getConfigManager().getRegisterSubtitle();
                        break;
                    case LOGIN:
                        title = plugin.getConfigManager().getLoginTitle();
                        subtitle = plugin.getConfigManager().getLoginSubtitle();
                        break;
                }

                player.sendTitle(title, subtitle, 0, 40, 0);
            }
        };
        task.runTaskTimer(plugin, 0, plugin.getConfigManager().getTitleUpdateInterval());
        session.setTitleTask(task);
    }

    private void startActionbarTask(Player player, IceCloudLogin.PlayerSession session, IceCloudLogin.SessionState state) {
        if (!plugin.getConfigManager().isActionbarEnabled()) return;

        org.bukkit.scheduler.BukkitRunnable task = new org.bukkit.scheduler.BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || session.isLoggedIn()) {
                    cancel();
                    return;
                }

                int remaining = session.getRemainingTime();
                String message = "";

                switch (state) {
                    case CAPTCHA:
                        message = plugin.getConfigManager().getCaptchaActionbar(remaining);
                        break;
                    case REGISTER:
                        message = plugin.getConfigManager().getRegisterActionbar(remaining);
                        break;
                    case LOGIN:
                        message = plugin.getConfigManager().getLoginActionbar(remaining);
                        break;
                }

                player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, 
                    new net.md_5.bungee.api.chat.TextComponent(message));
                
                session.setRemainingTime(remaining - 1);
            }
        };
        task.runTaskTimer(plugin, 0, 20);
        session.setActionbarTask(task);
    }

    private void sendLoginSuccessTitle(Player player) {
        if (!plugin.getConfigManager().isLoginSuccessTitleEnabled()) return;
        
        String title = plugin.getConfigManager().getLoginSuccessTitle();
        String subtitle = plugin.getConfigManager().getLoginSuccessSubtitle();
        int duration = plugin.getConfigManager().getLoginSuccessTitleDuration();
        
        player.sendTitle(title, subtitle, 10, duration * 20, 10);
    }

    private void sendRegisterSuccessTitle(Player player) {
        if (!plugin.getConfigManager().isRegisterSuccessTitleEnabled()) return;
        
        String title = plugin.getConfigManager().getRegisterSuccessTitle();
        String subtitle = plugin.getConfigManager().getRegisterSuccessSubtitle();
        int duration = plugin.getConfigManager().getRegisterSuccessTitleDuration();
        
        player.sendTitle(title, subtitle, 10, duration * 20, 10);
    }

    private void sendWelcomeMessage(Player player) {
        for (String line : plugin.getConfigManager().getWelcomeMessages(player.getName())) {
            player.sendMessage(line);
        }
    }
}
