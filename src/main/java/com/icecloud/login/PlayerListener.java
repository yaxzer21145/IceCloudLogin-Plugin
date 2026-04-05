package com.icecloud.login;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class PlayerListener implements Listener {
    private final IceCloudLogin plugin;

    public PlayerListener(IceCloudLogin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        IceCloudLogin.PlayerSession session = new IceCloudLogin.PlayerSession(uuid, player.getName());
        plugin.createPlayerSession(uuid, session);

        if (plugin.getDatabaseManager().isPlayerRegistered(uuid)) {
            PlayerData playerData = plugin.getDatabaseManager().getPlayerData(uuid);
            if (playerData != null) {
                long lastLogin = playerData.getLastLogin();
                long autoLoginDuration = plugin.getConfigManager().getAutoLoginDuration() * 1000L;
                
                if (lastLogin > 0 && System.currentTimeMillis() - lastLogin < autoLoginDuration) {
                    session.setLoggedIn(true);
                    session.cancelTasks();
                    plugin.getDatabaseManager().updateLastLogin(uuid);
                    player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getLoginAutoLoginMessage());
                    sendLoginSuccessTitle(player);
                    sendWelcomeMessage(player);
                    return;
                }
            }
            
            session.setState(IceCloudLogin.SessionState.LOGIN);
            session.setRemainingTime(plugin.getConfigManager().getLoginTimeout());
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getLoginNeedLoginMessage());
        } else {
            String captcha = plugin.getCaptchaManager().generateCaptcha(uuid);
            session.setRemainingTime(plugin.getConfigManager().getRegistrationTimeout());
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getCaptchaMessage(captcha));
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getCaptchaPromptMessage());
        }

        startTitleTask(player, session, session.getState());
        startActionbarTask(player, session, session.getState());
        startTimeoutTask(player, session, session.getState());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        IceCloudLogin.PlayerSession session = plugin.getPlayerSession(uuid);
        if (session != null) {
            session.cancelTasks();
            plugin.removePlayerSession(uuid);
        }
        
        plugin.removePendingEmailBinding(uuid);
        plugin.removePendingQQBinding(uuid);
        plugin.removePendingPasswordReset(uuid);
        plugin.resetWrongPasswordCount(uuid);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        IceCloudLogin.PlayerSession session = plugin.getPlayerSession(uuid);
        if (session != null && !session.isLoggedIn()) {
            event.setCancelled(true);
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getChatDeniedMessage());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        IceCloudLogin.PlayerSession session = plugin.getPlayerSession(uuid);
        if (session != null && !session.isLoggedIn()) {
            String cmd = event.getMessage().toLowerCase().split(" ")[0];
            if (cmd.startsWith("/login") || cmd.startsWith("/l") || 
                cmd.startsWith("/register") || cmd.startsWith("/reg") ||
                cmd.startsWith("/captcha") || cmd.startsWith("/mailzhpass") ||
                cmd.startsWith("/qqzhpass")) {
                return;
            }
            event.setCancelled(true);
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getCommandDeniedMessage());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        IceCloudLogin.PlayerSession session = plugin.getPlayerSession(uuid);
        if (session != null && !session.isLoggedIn()) {
            if (event.getFrom().getX() != event.getTo().getX() || 
                event.getFrom().getZ() != event.getTo().getZ() ||
                event.getFrom().getY() != event.getTo().getY()) {
                event.setTo(event.getFrom());
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        IceCloudLogin.PlayerSession session = plugin.getPlayerSession(uuid);
        if (session != null && !session.isLoggedIn()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        IceCloudLogin.PlayerSession session = plugin.getPlayerSession(uuid);
        if (session != null && !session.isLoggedIn()) {
            event.setCancelled(true);
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getBreakDeniedMessage());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        IceCloudLogin.PlayerSession session = plugin.getPlayerSession(uuid);
        if (session != null && !session.isLoggedIn()) {
            event.setCancelled(true);
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getPlaceDeniedMessage());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            UUID uuid = player.getUniqueId();
            
            IceCloudLogin.PlayerSession session = plugin.getPlayerSession(uuid);
            if (session != null && !session.isLoggedIn()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            UUID uuid = player.getUniqueId();
            
            IceCloudLogin.PlayerSession session = plugin.getPlayerSession(uuid);
            if (session != null && !session.isLoggedIn()) {
                event.setCancelled(true);
                player.sendMessage(plugin.getConfigManager().getMessagePrefix() + plugin.getConfigManager().getInventoryDeniedMessage());
            }
        }
    }

    private void startTitleTask(Player player, IceCloudLogin.PlayerSession session, IceCloudLogin.SessionState state) {
        if (!plugin.getConfigManager().isCaptchaTitleEnabled() && state == IceCloudLogin.SessionState.CAPTCHA) return;
        if (!plugin.getConfigManager().isRegisterTitleEnabled() && state == IceCloudLogin.SessionState.REGISTER) return;
        if (!plugin.getConfigManager().isLoginTitleEnabled() && state == IceCloudLogin.SessionState.LOGIN) return;

        BukkitRunnable task = new BukkitRunnable() {
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

        BukkitRunnable task = new BukkitRunnable() {
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

    private void startTimeoutTask(Player player, IceCloudLogin.PlayerSession session, IceCloudLogin.SessionState state) {
        int timeout;
        switch (state) {
            case CAPTCHA:
                timeout = plugin.getConfigManager().getRegistrationTimeout();
                break;
            case REGISTER:
                timeout = plugin.getConfigManager().getRegistrationTimeout();
                break;
            case LOGIN:
                timeout = plugin.getConfigManager().getLoginTimeout();
                break;
            default:
                timeout = 120;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) return;
                if (session.isLoggedIn()) return;

                String kickMessage = "";
                switch (state) {
                    case CAPTCHA:
                    case REGISTER:
                        kickMessage = plugin.getConfigManager().getRegistrationTimeoutKickMessage();
                        break;
                    case LOGIN:
                        kickMessage = plugin.getConfigManager().getLoginTimeoutKickMessage();
                        break;
                }

                player.kickPlayer(kickMessage);
            }
        }.runTaskLater(plugin, timeout * 20L);
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
