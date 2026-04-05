package com.icecloud.login;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class DatabaseManager {
    private final IceCloudLogin plugin;
    private Connection connection;

    public DatabaseManager(IceCloudLogin plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        String type = plugin.getConfigManager().getDatabaseType();
        try {
            if (type.equalsIgnoreCase("mysql")) {
                initializeMySQL();
            } else {
                initializeSQLite();
            }
            createTables();
            plugin.getLogger().info("数据库连接成功！");
        } catch (SQLException e) {
            plugin.getLogger().severe("数据库连接失败: " + e.getMessage());
        }
    }

    private void initializeMySQL() throws SQLException {
        String host = plugin.getConfigManager().getMysqlHost();
        int port = plugin.getConfigManager().getMysqlPort();
        String database = plugin.getConfigManager().getMysqlDatabase();
        String username = plugin.getConfigManager().getMysqlUsername();
        String password = plugin.getConfigManager().getMysqlPassword();

        String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&autoReconnect=true";
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            plugin.getLogger().severe("MySQL驱动未找到: " + e.getMessage());
            throw new SQLException("MySQL driver not found");
        }
        connection = java.sql.DriverManager.getConnection(url, username, password);
    }

    private void initializeSQLite() throws SQLException {
        String path = plugin.getConfigManager().getSqlitePath();
        java.io.File file = new java.io.File(path);
        file.getParentFile().mkdirs();
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            plugin.getLogger().severe("SQLite驱动未找到: " + e.getMessage());
            throw new SQLException("SQLite driver not found");
        }
        String url = "jdbc:sqlite:" + path;
        connection = java.sql.DriverManager.getConnection(url);
    }

    private void createTables() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS players (" +
                "uuid VARCHAR(36) PRIMARY KEY," +
                "username VARCHAR(16) NOT NULL," +
                "password_hash VARCHAR(64) NOT NULL," +
                "email VARCHAR(100)," +
                "qq VARCHAR(20)," +
                "last_login BIGINT," +
                "register_time BIGINT" +
                ")";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.execute();
        }
    }

    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                plugin.getLogger().severe("关闭数据库连接失败: " + e.getMessage());
            }
        }
    }

    public boolean isPlayerRegistered(UUID uuid) {
        String sql = "SELECT uuid FROM players WHERE uuid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            plugin.getLogger().severe("查询玩家注册状态失败: " + e.getMessage());
            return false;
        }
    }

    public void registerPlayer(UUID uuid, String username, String passwordHash) {
        String sql = "INSERT INTO players (uuid, username, password_hash, register_time) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, username);
            stmt.setString(3, passwordHash);
            stmt.setLong(4, System.currentTimeMillis());
            stmt.execute();
        } catch (SQLException e) {
            plugin.getLogger().severe("注册玩家失败: " + e.getMessage());
        }
    }

    public PlayerData getPlayerData(UUID uuid) {
        String sql = "SELECT * FROM players WHERE uuid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new PlayerData(
                        UUID.fromString(rs.getString("uuid")),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("email"),
                        rs.getString("qq"),
                        rs.getLong("last_login"),
                        rs.getLong("register_time")
                );
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("获取玩家数据失败: " + e.getMessage());
        }
        return null;
    }

    public void updateLastLogin(UUID uuid) {
        String sql = "UPDATE players SET last_login = ? WHERE uuid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, System.currentTimeMillis());
            stmt.setString(2, uuid.toString());
            stmt.execute();
        } catch (SQLException e) {
            plugin.getLogger().severe("更新最后登录时间失败: " + e.getMessage());
        }
    }

    public boolean updateEmail(UUID uuid, String email) {
        String sql = "UPDATE players SET email = ? WHERE uuid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, uuid.toString());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            plugin.getLogger().severe("更新邮箱失败: " + e.getMessage());
            return false;
        }
    }

    public boolean updateQQ(UUID uuid, String qq) {
        String sql = "UPDATE players SET qq = ? WHERE uuid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, qq);
            stmt.setString(2, uuid.toString());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            plugin.getLogger().severe("更新QQ失败: " + e.getMessage());
            return false;
        }
    }

    public boolean updatePassword(UUID uuid, String passwordHash) {
        String sql = "UPDATE players SET password_hash = ? WHERE uuid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, passwordHash);
            stmt.setString(2, uuid.toString());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            plugin.getLogger().severe("更新密码失败: " + e.getMessage());
            return false;
        }
    }

    public boolean deletePlayer(UUID uuid) {
        String sql = "DELETE FROM players WHERE uuid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            plugin.getLogger().severe("删除玩家失败: " + e.getMessage());
            return false;
        }
    }

    public void clearSession(UUID uuid) {
        plugin.removePlayerSession(uuid);
    }
}
