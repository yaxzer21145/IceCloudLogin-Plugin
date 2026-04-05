# PartyPixelLogin 邮箱发送与QQ绑定功能配置教程

## 目录
1. [邮箱发送功能配置](#邮箱发送功能配置)
2. [QQ账号绑定功能配置](#qq账号绑定功能配置)
3. [整体配置步骤](#整体配置步骤)
4. [常见问题与故障排除](#常见问题与故障排除)

---

## 邮箱发送功能配置

### 1.1 邮件服务器选择

#### 推荐的SMTP服务提供商

| 服务商 | SMTP地址 | 端口 | SSL | TLS | 特点 |
|---------|----------|------|-----|-----|------|
| QQ邮箱 | smtp.qq.com | 587 | 否 | 是 | 国内访问稳定，免费 |
| 163邮箱 | smtp.163.com | 465 | 是 | 否 | 国内访问稳定 |
| Gmail | smtp.gmail.com | 587 | 否 | 是 | 国际服务，需应用密码 |
| Outlook | smtp.office365.com | 587 | 否 | 是 | 企业级服务 |
| 自建服务器 | 自定义 | 自定义 | 可选 | 可选 | 完全控制 |

#### 选择建议
- **国内用户**：推荐使用QQ邮箱或163邮箱，访问速度快，稳定性高
- **国际用户**：推荐使用Gmail或Outlook
- **企业用户**：推荐使用企业邮箱或自建邮件服务器
- **测试环境**：可使用Mailtrap等测试邮件服务

### 1.2 相关参数设置

#### 1.2.1 QQ邮箱配置示例

```yaml
email:
  enabled: true
  host: smtp.qq.com
  port: 587
  username: your-email@qq.com
  password: your-authorization-code
  from: PartyPixelLogin <your-email@qq.com>
  ssl: false
  tls: true
  code-validity: 300
  cooldown: 60
```

#### 1.2.2 获取QQ邮箱授权码

1. 登录QQ邮箱网页版：https://mail.qq.com
2. 点击"设置" → "账户"
3. 找到"POP3/IMAP/SMTP/Exchange/CardDAV/CalDAV服务"
4. 开启"POP3/SMTP服务"或"IMAP/SMTP服务"
5. 按照提示发送短信验证
6. 获取16位授权码（不是QQ密码）

**重要提示**：
- 必须使用授权码，不能使用QQ密码
- 授权码只显示一次，请妥善保存
- 如忘记授权码，需要重新生成

#### 1.2.3 163邮箱配置示例

```yaml
email:
  enabled: true
  host: smtp.163.com
  port: 465
  username: your-email@163.com
  password: your-authorization-code
  from: PartyPixelLogin <your-email@163.com>
  ssl: true
  tls: false
  code-validity: 300
  cooldown: 60
```

#### 1.2.4 Gmail配置示例

```yaml
email:
  enabled: true
  host: smtp.gmail.com
  port: 587
  username: your-email@gmail.com
  password: your-app-password
  from: PartyPixelLogin <your-email@gmail.com>
  ssl: false
  tls: true
  code-validity: 300
  cooldown: 60
```

**Gmail应用密码获取步骤**：
1. 登录Google账户
2. 进入"安全性"设置
3. 启用"两步验证"
4. 在"应用密码"部分生成新密码
5. 使用生成的16位密码作为SMTP密码

### 1.3 安全配置

#### 1.3.1 SSL/TLS设置说明

**SSL (Secure Sockets Layer)**
- 使用465端口
- 提供端到端加密
- 适用于大多数现代邮件服务器

**TLS (Transport Layer Security)**
- 使用587端口
- STARTTLS协议，先建立明文连接再升级为加密
- 更灵活的加密方式

#### 1.3.2 安全配置最佳实践

```yaml
email:
  # 生产环境推荐配置
  ssl: true
  tls: true
  
  # 或者使用TLS（推荐）
  ssl: false
  tls: true
  
  # 避免的配置（不安全）
  ssl: false
  tls: false
```

#### 1.3.3 防火墙与网络配置

确保服务器可以访问以下端口：
- 25（SMTP，通常被ISP封锁）
- 465（SMTPS，SSL加密）
- 587（SMTP with STARTTLS，TLS加密）

**云服务器安全组配置**：
```bash
# 阿里云/腾讯云/华为云安全组规则
入站规则：
- 协议：TCP
- 端口：465, 587
- 来源：0.0.0.0/0
```

### 1.4 测试验证步骤

#### 1.4.1 配置文件测试

1. 编辑 `config.yml` 文件：
```bash
cd plugins/PartyPixelLogin
nano config.yml
```

2. 修改邮件配置：
```yaml
email:
  enabled: true
  host: smtp.qq.com
  port: 587
  username: your-email@qq.com
  password: your-authorization-code
  from: PartyPixelLogin <your-email@qq.com>
  ssl: false
  tls: true
```

3. 保存文件并重启服务器

#### 1.4.2 游戏内测试

1. 以管理员身份进入服务器
2. 执行测试命令：
```
/mail test@example.com
```

3. 检查控制台输出：
```
[PartyPixelLogin] 邮件已发送到 test@example.com
```

4. 检查收件箱是否收到验证码邮件

#### 1.4.3 日志验证

查看服务器日志文件：
```bash
tail -f logs/latest.log
```

查找邮件相关日志：
```
[PartyPixelLogin] 发送邮件到 user@example.com
[PartyPixelLogin] 邮件发送成功
```

#### 1.4.4 自动化测试脚本

创建测试脚本 `test_email.sh`：
```bash
#!/bin/bash

echo "开始测试邮件发送功能..."

# 检查配置文件
if [ ! -f "plugins/PartyPixelLogin/config.yml" ]; then
    echo "错误：配置文件不存在"
    exit 1
fi

# 解析配置
EMAIL_HOST=$(grep "host:" plugins/PartyPixelLogin/config.yml | awk '{print $2}')
EMAIL_PORT=$(grep "port:" plugins/PartyPixelLogin/config.yml | awk '{print $2}')
EMAIL_USER=$(grep "username:" plugins/PartyPixelLogin/config.yml | awk '{print $2}')

echo "邮件服务器：$EMAIL_HOST:$EMAIL_PORT"
echo "发件人：$EMAIL_USER"

echo "测试完成！请在游戏中执行 /mail 命令进行实际测试"
```

### 1.5 常见问题排查

#### 问题1：连接超时

**症状**：
```
[PartyPixelLogin] Failed to send email: Connection timed out
```

**解决方案**：
1. 检查网络连接：
```bash
ping smtp.qq.com
telnet smtp.qq.com 587
```

2. 检查防火墙规则：
```bash
# Linux
sudo iptables -L -n | grep 587
sudo firewall-cmd --list-ports

# Windows
netsh advfirewall firewall show rule name=all
```

3. 检查云服务器安全组配置

#### 问题2：认证失败

**症状**：
```
[PartyPixelLogin] Failed to send email: Authentication failed
```

**解决方案**：
1. 确认使用的是授权码而非密码
2. 检查用户名格式（通常需要完整邮箱地址）
3. 重新生成授权码：
   - QQ邮箱：设置 → 账户 → 重新生成授权码
   - Gmail：安全性 → 应用密码 → 生成新密码

#### 问题3：SSL/TLS错误

**症状**：
```
[PartyPixelLogin] Failed to send email: SSL handshake failed
```

**解决方案**：
1. 确认端口与加密方式匹配：
   - 465端口 → SSL: true, TLS: false
   - 587端口 → SSL: false, TLS: true

2. 检查Java版本（需要Java 8+）：
```bash
java -version
```

3. 更新Java安全策略（如需要）

#### 问题4：邮件被标记为垃圾邮件

**解决方案**：
1. 设置正确的发件人名称：
```yaml
email:
  from: "服务器名称 <your-email@qq.com>"
```

2. 添加SPF记录到DNS：
```
@ IN TXT "v=spf1 include:qq.com ~all"
```

3. 使用专业的邮件模板：
```java
String emailContent = String.format(
    "尊敬的玩家 %s：\n\n" +
    "您的验证码是：%s\n\n" +
    "该验证码在5分钟内有效，请勿泄露给他人。\n\n" +
    "如果这不是您本人的操作，请忽略此邮件。\n\n" +
    "------------------\n" +
    "PartyPixelLogin 登录系统",
    playerName, code
);
```

#### 问题5：发送频率限制

**症状**：
```
[PartyPixelLogin] Failed to send email: Rate limit exceeded
```

**解决方案**：
1. 调整冷却时间配置：
```yaml
email:
  cooldown: 120  # 增加到2分钟
```

2. 实现发送队列：
```java
public class EmailQueue {
    private Queue<EmailTask> queue = new LinkedList<>();
    
    public void addToQueue(EmailTask task) {
        queue.add(task);
        processQueue();
    }
    
    private void processQueue() {
        if (!queue.isEmpty()) {
            EmailTask task = queue.poll();
            sendEmailWithDelay(task, 2000); // 2秒间隔
        }
    }
}
```

---

## QQ账号绑定功能配置

### 2.1 当前实现方式

**重要说明**：PartyPixelLogin当前使用**邮件方式**实现QQ绑定，而非QQ开放平台API。这种方式更简单、稳定，不需要复杂的OAuth2.0授权流程。

#### 工作原理
1. 玩家输入QQ号
2. 插件自动生成QQ邮箱地址（QQ号@qq.com）
3. 发送验证码到该QQ邮箱
4. 玩家输入验证码完成绑定

#### 配置示例
```yaml
qq:
  enabled: true
  use-email: true
  email-domain: qq.com
  code-validity: 300
  cooldown: 60
```

### 2.2 邮件方式QQ绑定配置

#### 2.2.1 基本配置

编辑 `config.yml`：
```yaml
qq:
  # 启用QQ绑定功能
  enabled: true
  
  # 使用邮件方式（推荐）
  use-email: true
  
  # QQ邮箱域名
  email-domain: qq.com
  
  # 验证码有效期（秒）
  code-validity: 300
  
  # 发送冷却时间（秒）
  cooldown: 60
```

#### 2.2.2 使用步骤

**玩家操作流程**：

1. 绑定QQ号：
```
/qq 123456789
```

2. 系统发送验证码到 `123456789@qq.com`

3. 玩家在QQ邮箱中查看验证码

4. 验证QQ号：
```
/qqc 123456
```

5. 绑定成功

#### 2.2.3 数据库存储

QQ号存储在数据库的 `players` 表中：

```sql
CREATE TABLE players (
    uuid TEXT PRIMARY KEY,
    player_name TEXT NOT NULL,
    password TEXT NOT NULL,
    email TEXT,
    qq TEXT,           -- QQ号存储在这里
    last_login_time BIGINT DEFAULT 0,
    registration_time BIGINT DEFAULT 0
);
```

#### 2.2.4 绑定状态管理

```java
// 检查QQ是否已绑定
public boolean isQQBound(UUID uuid) {
    PlayerData data = databaseManager.getPlayerData(uuid);
    return data.getQq() != null && !data.getQq().isEmpty();
}

// 检查QQ号是否已被其他玩家绑定
public boolean isQQBound(String qq) {
    return databaseManager.isQQBound(qq);
}

// 更新QQ号
public boolean updateQQ(UUID uuid, String qq) {
    return databaseManager.updateQQ(uuid, qq);
}
```

### 2.3 高级：QQ开放平台API方式（可选）

如果您希望使用QQ开放平台API实现更高级的功能，可以参考以下实现方案。

#### 2.3.1 QQ开放平台应用创建

1. 访问QQ开放平台：https://connect.qq.com/

2. 注册开发者账号：
   - 使用QQ账号登录
   - 完善开发者信息
   - 实名认证

3. 创建应用：
   - 点击"创建应用"
   - 选择应用类型（网站应用/移动应用）
   - 填写应用信息：
     ```
     应用名称：PartyPixelLogin
     应用简介：Minecraft服务器登录插件
     应用官网：https://your-server.com
     应用回调地址：https://your-server.com/callback
     ```

4. 获取应用凭证：
   - App ID：应用标识
   - App Key：应用密钥
   - 回调地址：OAuth授权回调

5. 等待审核（通常1-3个工作日）

#### 2.3.2 OAuth2.0授权流程

**流程图**：
```
用户点击"QQ登录" 
    ↓
跳转到QQ授权页面
    ↓
用户授权应用
    ↓
QQ返回授权码
    ↓
服务器用授权码换取访问令牌
    ↓
获取用户QQ信息
    ↓
完成绑定
```

**实现代码示例**：

```java
public class QQOAuthHandler {
    private static final String AUTH_URL = "https://graph.qq.com/oauth2.0/authorize";
    private static final String TOKEN_URL = "https://graph.qq.com/oauth2.0/token";
    private static final String OPENID_URL = "https://graph.qq.com/oauth2.0/me";
    private static final String USERINFO_URL = "https://graph.qq.com/user/get_user_info";
    
    private final String appId;
    private final String appKey;
    private final String redirectUri;
    
    public QQOAuthHandler(String appId, String appKey, String redirectUri) {
        this.appId = appId;
        this.appKey = appKey;
        this.redirectUri = redirectUri;
    }
    
    // 生成授权URL
    public String getAuthorizationUrl() {
        return String.format(
            "%s?response_type=code&client_id=%s&redirect_uri=%s&state=test",
            AUTH_URL, appId, URLEncoder.encode(redirectUri)
        );
    }
    
    // 用授权码换取访问令牌
    public String getAccessToken(String code) throws IOException {
        String url = String.format(
            "%s?grant_type=authorization_code&client_id=%s&client_secret=%s&code=%s&redirect_uri=%s",
            TOKEN_URL, appId, appKey, code, URLEncoder.encode(redirectUri)
        );
        
        String response = HttpUtil.get(url);
        JSONObject json = new JSONObject(response);
        return json.getString("access_token");
    }
    
    // 获取用户OpenID
    public String getOpenId(String accessToken) throws IOException {
        String url = String.format("%s?access_token=%s", OPENID_URL, accessToken);
        String response = HttpUtil.get(url);
        
        // 响应格式：client_id=xxx&openid=xxx
        Map<String, String> params = parseResponse(response);
        return params.get("openid");
    }
    
    // 获取用户信息
    public QQUserInfo getUserInfo(String accessToken, String openId) throws IOException {
        String url = String.format(
            "%s?access_token=%s&oauth_consumer_key=%s&openid=%s",
            USERINFO_URL, accessToken, appId, openId
        );
        
        String response = HttpUtil.get(url);
        JSONObject json = new JSONObject(response);
        
        return new QQUserInfo(
            json.getString("nickname"),
            json.getString("figureurl_qq_2"),
            openId
        );
    }
}

public class QQUserInfo {
    private final String nickname;
    private final String avatar;
    private final String openId;
    
    // 构造函数、getter方法
}
```

#### 2.3.3 玩家绑定流程

```java
public class QQBindCommand implements CommandExecutor {
    private final QQOAuthHandler oauthHandler;
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("此命令只能由玩家执行");
            return true;
        }
        
        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();
        
        // 生成授权URL
        String authUrl = oauthHandler.getAuthorizationUrl();
        
        // 发送URL给玩家
        player.sendMessage(ChatColor.GREEN + "请点击以下链接绑定QQ账号：");
        player.sendMessage(ChatColor.AQUA + authUrl);
        player.sendMessage(ChatColor.YELLOW + "链接将在5分钟后失效");
        
        // 存储待绑定状态
        pendingBindings.put(uuid, new PendingBinding(System.currentTimeMillis()));
        
        // 设置超时
        new BukkitRunnable() {
            @Override
            public void run() {
                pendingBindings.remove(uuid);
            }
        }.runTaskLater(plugin, 300 * 20L); // 5分钟
        
        return true;
    }
}

// 回调处理（需要Web服务器）
@WebServlet("/qq/callback")
public class QQCallbackServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        String code = req.getParameter("code");
        String state = req.getParameter("state");
        
        try {
            // 获取访问令牌
            String accessToken = oauthHandler.getAccessToken(code);
            
            // 获取OpenID
            String openId = oauthHandler.getOpenId(accessToken);
            
            // 获取用户信息
            QQUserInfo userInfo = oauthHandler.getUserInfo(accessToken, openId);
            
            // 绑定到玩家
            bindQQToPlayer(openId, userInfo);
            
            resp.getWriter().write("绑定成功！请返回游戏。");
            
        } catch (Exception e) {
            resp.getWriter().write("绑定失败：" + e.getMessage());
        }
    }
}
```

#### 2.3.4 安全验证机制

**1. 防CSRF攻击**
```java
// 生成随机state参数
public String generateState() {
    return UUID.randomUUID().toString().replace("-", "");
}

// 验证state参数
public boolean validateState(String state) {
    return pendingStates.containsKey(state);
}
```

**2. 防重放攻击**
```java
// 使用一次性授权码
public boolean isCodeUsed(String code) {
    return usedCodes.contains(code);
}

public void markCodeAsUsed(String code) {
    usedCodes.add(code);
}
```

**3. 数据加密存储**
```java
// 加密QQ OpenID
public String encryptOpenId(String openId) {
    try {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encrypted = cipher.doFinal(openId.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    } catch (Exception e) {
        throw new RuntimeException("加密失败", e);
    }
}

// 解密QQ OpenID
public String decryptOpenId(String encrypted) {
    try {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encrypted));
        return new String(decrypted);
    } catch (Exception e) {
        throw new RuntimeException("解密失败", e);
    }
}
```

#### 2.3.5 Web服务器配置

如果使用OAuth2.0方式，需要配置Web服务器：

**Nginx配置示例**：
```nginx
server {
    listen 80;
    server_name your-server.com;
    
    location /qq/callback {
        proxy_pass http://localhost:8080/qq/callback;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

**Spring Boot配置示例**：
```java
@SpringBootApplication
public class CallbackServer {
    public static void main(String[] args) {
        SpringApplication.run(CallbackServer.class, args);
    }
    
    @Bean
    public ServletRegistrationBean<QQCallbackServlet> qqCallbackServlet() {
        return new ServletRegistrationBean<>(new QQCallbackServlet(), "/qq/callback");
    }
}
```

---

## 整体配置步骤

### 3.1 环境要求

#### 3.1.1 服务器要求

| 组件 | 最低要求 | 推荐配置 |
|-------|---------|----------|
| Java版本 | Java 8 | Java 17 |
| 服务器内存 | 2GB | 4GB+ |
| 磁盘空间 | 1GB | 10GB+ |
| 网络带宽 | 1Mbps | 10Mbps+ |
| 操作系统 | Linux/Windows | Linux (Ubuntu 20.04+) |

#### 3.1.2 软件依赖

**Minecraft服务器**：
- Spigot 1.20+
- Paper 1.20+
- 其他兼容服务端

**数据库**（可选）：
- SQLite（默认，无需额外配置）
- MySQL 5.7+ / MariaDB 10.3+

**邮件服务**：
- 有效的SMTP服务器账号
- 稳定的网络连接

### 3.2 前置条件

#### 3.2.1 准备工作清单

- [ ] 已安装Minecraft服务器
- [ ] 已安装Java 8或更高版本
- [ ] 已获取有效的邮箱账号（QQ邮箱/Gmail等）
- [ ] 已获取邮箱授权码/应用密码
- [ ] 已测试服务器网络连接
- [ ] 已备份现有数据（如适用）

#### 3.2.2 权限检查

确保服务器有以下权限：
```bash
# 文件读写权限
chmod -R 755 plugins/PartyPixelLogin/

# 网络访问权限
# 检查防火墙
sudo ufw status
sudo ufw allow 587/tcp
sudo ufw allow 465/tcp

# 检查DNS解析
nslookup smtp.qq.com
```

### 3.3 详细操作步骤

#### 步骤1：安装插件

1. 下载最新版本的PartyPixelLogin插件：
```bash
cd plugins
wget https://github.com/your-repo/PartyPixelLogin/releases/latest/download/PartyPixelLogin-1.0.0.jar
```

2. 重启服务器：
```bash
# 使用服务管理器
systemctl restart minecraft

# 或手动重启
stop_server.sh
start_server.sh
```

3. 确认插件加载：
```
[PartyPixelLogin] PartyPixelLogin has been enabled!
```

#### 步骤2：配置数据库

**使用SQLite（默认）**：
```yaml
database:
  type: sqlite
  sqlite:
    path: plugins/PartyPixelLogin/data.db
```

**使用MySQL**：

1. 创建数据库：
```sql
CREATE DATABASE partypixellogin CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 创建用户并授权：
```sql
CREATE USER 'partypixellogin'@'localhost' IDENTIFIED BY 'your-password';
GRANT ALL PRIVILEGES ON partypixellogin.* TO 'partypixellogin'@'localhost';
FLUSH PRIVILEGES;
```

3. 配置插件：
```yaml
database:
  type: mysql
  mysql:
    host: localhost
    port: 3306
    database: partypixellogin
    username: partypixellogin
    password: your-password
```

#### 步骤3：配置邮箱功能

1. 获取邮箱授权码：
   - QQ邮箱：设置 → 账户 → 生成授权码
   - Gmail：安全性 → 应用密码 → 生成密码

2. 编辑配置文件：
```bash
nano plugins/PartyPixelLogin/config.yml
```

3. 配置邮件参数：
```yaml
email:
  enabled: true
  host: smtp.qq.com
  port: 587
  username: your-email@qq.com
  password: your-authorization-code
  from: PartyPixelLogin <your-email@qq.com>
  ssl: false
  tls: true
  code-validity: 300
  cooldown: 60
```

4. 保存配置并重载：
```bash
# 在游戏中执行
/ppl reload

# 或重启服务器
```

#### 步骤4：配置QQ绑定功能

1. 编辑配置文件：
```yaml
qq:
  enabled: true
  use-email: true
  email-domain: qq.com
  code-validity: 300
  cooldown: 60
```

2. 保存配置并重载

#### 步骤5：配置消息与欢迎信息

1. 编辑消息配置：
```bash
nano plugins/PartyPixelLogin/message.yml
```

2. 自定义消息：
```yaml
prefix: "&6[登录系统] &r"

login:
  success: "&a欢迎回来，{player}！"
  need-login: "&e请使用 /login <密码> 进行登录"
```

3. 编辑欢迎信息：
```bash
nano plugins/PartyPixelLogin/welcome.txt
```

4. 添加欢迎内容：
```
&6========================================
&e欢迎 &a{player} &e来到服务器！
&e祝你游戏愉快！
&6========================================
```

#### 步骤6：配置安全限制

1. 配置登录限制：
```yaml
login:
  timeout: 120              # 登录超时（秒）
  auto-login-duration: 7200  # 自动登录时长（秒）
  max-wrong-password: 5      # 最大错误次数
  max-kick-count: 3         # 最大踢出次数
  kick-commands:
    - "ban {player} 多次登录失败"
```

2. 配置标题显示：
```yaml
title:
  update-interval: 20
  captcha:
    enabled: true
    title: "&6请输入验证码"
    subtitle: "&e使用 /captcha <验证码>"
  register:
    enabled: true
    title: "&6请注册"
    subtitle: "&e使用 /register <密码> <重复密码>"
  login:
    enabled: true
    title: "&6请登录"
    subtitle: "&e使用 /login <密码>"
```

3. 配置动作栏倒计时：
```yaml
actionbar:
  enabled: true
  captcha: "&e请在 {time} 秒内输入验证码！"
  register: "&e请在 {time} 秒内完成注册！"
  login: "&e请在 {time} 秒内完成登录！"
```

### 3.4 配置验证方法

#### 3.4.1 功能测试清单

**邮箱功能测试**：
- [ ] 玩家可以发送邮箱绑定请求
- [ ] 系统成功发送验证码邮件
- [ ] 玩家可以验证邮箱
- [ ] 邮箱绑定成功保存到数据库

**QQ绑定功能测试**：
- [ ] 玩家可以发送QQ绑定请求
- [ ] 系统成功发送验证码到QQ邮箱
- [ ] 玩家可以验证QQ号
- [ ] QQ号绑定成功保存到数据库

**登录功能测试**：
- [ ] 新玩家需要先验证码验证
- [ ] 新玩家可以成功注册
- [ ] 老玩家可以成功登录
- [ ] 自动登录功能正常工作
- [ ] 密码错误次数限制生效
- [ ] 踢出次数限制生效

**UI显示测试**：
- [ ] 标题正常显示
- [ ] 动作栏倒计时正常工作
- [ ] 欢迎信息正常显示
- [ ] 颜色代码正确渲染

#### 3.4.2 自动化测试脚本

创建测试脚本 `test_config.sh`：
```bash
#!/bin/bash

echo "========================================="
echo "PartyPixelLogin 配置测试脚本"
echo "========================================="

# 检查插件文件
if [ ! -f "plugins/PartyPixelLogin.jar" ]; then
    echo "❌ 插件文件不存在"
    exit 1
else
    echo "✅ 插件文件存在"
fi

# 检查配置文件
if [ ! -f "plugins/PartyPixelLogin/config.yml" ]; then
    echo "❌ 配置文件不存在"
    exit 1
else
    echo "✅ 配置文件存在"
fi

# 检查数据库
if [ ! -f "plugins/PartyPixelLogin/data.db" ]; then
    echo "⚠️  数据库文件不存在（首次运行正常）"
else
    echo "✅ 数据库文件存在"
fi

# 检查邮件配置
if grep -q "enabled: true" plugins/PartyPixelLogin/config.yml; then
    echo "✅ 邮箱功能已启用"
else
    echo "⚠️  邮箱功能未启用"
fi

# 检查QQ绑定配置
if grep -q "enabled: true" plugins/PartyPixelLogin/config.yml; then
    echo "✅ QQ绑定功能已启用"
else
    echo "⚠️  QQ绑定功能未启用"
fi

# 检查网络连接
if ping -c 1 smtp.qq.com &> /dev/null; then
    echo "✅ 可以连接到邮件服务器"
else
    echo "❌ 无法连接到邮件服务器"
fi

echo "========================================="
echo "测试完成！"
echo "========================================="
```

#### 3.4.3 日志分析

查看关键日志：
```bash
# 查看插件启动日志
grep "PartyPixelLogin" logs/latest.log | head -20

# 查看邮件发送日志
grep "发送邮件" logs/latest.log

# 查看错误日志
grep "ERROR" logs/latest.log | grep "PartyPixelLogin"
```

### 3.5 成功标准

#### 3.5.1 功能完整性

所有核心功能正常工作：
- ✅ 玩家注册流程完整
- ✅ 玩家登录流程完整
- ✅ 邮箱绑定功能正常
- ✅ QQ绑定功能正常
- ✅ 自动登录功能正常
- ✅ 安全限制功能正常

#### 3.5.2 性能标准

- 插件启动时间 < 5秒
- 邮件发送时间 < 10秒
- 数据库查询时间 < 100ms
- 服务器无明显性能影响

#### 3.5.3 稳定性标准

- 连续运行24小时无崩溃
- 处理100+玩家无异常
- 内存使用稳定（无内存泄漏）
- 日志无严重错误

---

## 常见问题与故障排除

### 4.1 邮箱相关问题

#### Q1: 邮件发送失败，提示"Connection refused"

**原因分析**：
- 端口被防火墙阻止
- 邮件服务器地址错误
- 网络连接问题

**解决方案**：
```bash
# 1. 检查端口是否开放
telnet smtp.qq.com 587

# 2. 检查防火墙
sudo iptables -L -n | grep 587

# 3. 检查DNS解析
nslookup smtp.qq.com

# 4. 测试网络连接
ping smtp.qq.com
```

#### Q2: 邮件发送成功但玩家收不到

**原因分析**：
- 邮件被误判为垃圾邮件
- 玩家邮箱地址错误
- 邮件服务器延迟

**解决方案**：
1. 检查垃圾邮件文件夹
2. 验证邮箱地址格式
3. 添加发件人到白名单
4. 检查邮件服务器日志

#### Q3: 验证码过期太快

**解决方案**：
```yaml
# 增加验证码有效期
email:
  code-validity: 600  # 10分钟

qq:
  code-validity: 600  # 10分钟
```

### 4.2 QQ绑定相关问题

#### Q1: QQ邮箱收不到验证码

**原因分析**：
- QQ邮箱未激活
- 邮件被拦截
- QQ号格式错误

**解决方案**：
1. 确认QQ邮箱已激活
2. 检查QQ号格式（5-11位数字）
3. 检查垃圾邮件文件夹
4. 测试QQ邮箱是否正常工作

#### Q2: QQ号格式验证失败

**解决方案**：
```java
// 验证QQ号格式
public boolean isValidQQ(String qq) {
    // QQ号：5-11位数字，首位不为0
    return qq != null && qq.matches("^[1-9][0-9]{4,10}$");
}

// 测试用例
assert isValidQQ("12345") == false;   // 太短
assert isValidQQ("0123456789") == false; // 首位为0
assert isValidQQ("123456789") == true;   // 正确
assert isValidQQ("12345678901") == true; // 正确
```

### 4.3 数据库相关问题

#### Q1: 数据库连接失败

**解决方案**：
```yaml
# 检查数据库配置
database:
  type: mysql
  mysql:
    host: localhost
    port: 3306
    database: partypixellogin
    username: partypixellogin
    password: your-password

# 测试数据库连接
mysql -h localhost -u partypixellogin -p partypixellogin
```

#### Q2: 数据丢失

**预防措施**：
```bash
# 定期备份数据库
crontab -e

# 添加备份任务
0 2 * * * /usr/bin/mysqldump -u root -p partypixellogin > /backup/partypixellogin_$(date +\%Y\%m\%d).sql

# SQLite备份
0 2 * * * cp plugins/PartyPixelLogin/data.db /backup/data_$(date +\%Y\%m\%d).db
```

### 4.4 性能优化建议

#### 4.4.1 数据库优化

```sql
-- 添加索引
CREATE INDEX idx_last_login ON players(last_login_time);
CREATE INDEX idx_email ON players(email);
CREATE INDEX idx_qq ON players(qq);

-- 定期清理过期数据
DELETE FROM players WHERE last_login_time < UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 1 YEAR));
```

#### 4.4.2 邮件发送优化

```java
// 使用连接池
public class EmailConnectionPool {
    private final Queue<EmailConnection> pool = new LinkedList<>();
    private final int maxSize = 10;
    
    public EmailConnection getConnection() {
        if (pool.isEmpty()) {
            return new EmailConnection();
        }
        return pool.poll();
    }
    
    public void releaseConnection(EmailConnection conn) {
        if (pool.size() < maxSize) {
            pool.offer(conn);
        }
    }
}

// 异步发送邮件
public void sendEmailAsync(String to, String subject, String content) {
    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
        emailManager.sendEmail(to, subject, content);
    });
}
```

#### 4.4.3 缓存优化

```java
// 使用缓存减少数据库查询
public class PlayerDataCache {
    private final Map<UUID, PlayerData> cache = new ConcurrentHashMap<>();
    private final long cacheTimeout = 300000; // 5分钟
    
    public PlayerData getPlayerData(UUID uuid) {
        PlayerData cached = cache.get(uuid);
        if (cached != null && !isExpired(cached)) {
            return cached;
        }
        
        PlayerData data = databaseManager.getPlayerData(uuid);
        if (data != null) {
            cache.put(uuid, data);
        }
        return data;
    }
    
    private boolean isExpired(PlayerData data) {
        return System.currentTimeMillis() - data.getCacheTime() > cacheTimeout;
    }
}
```

---

## 附录

### A. 配置文件完整示例

```yaml
# config.yml
database:
  type: sqlite
  sqlite:
    path: plugins/PartyPixelLogin/data.db
  mysql:
    host: localhost
    port: 3306
    database: partypixellogin
    username: partypixellogin
    password: your-password

login:
  timeout: 120
  auto-login-duration: 7200
  max-wrong-password: 5
  max-kick-count: 3
  kick-commands:
    - "ban {player} 多次登录失败"

registration:
  timeout: 120
  max-password-length: 32
  min-password-length: 6
  password-pattern: "^[a-zA-Z0-9_-]{6,32}$"

captcha:
  length: 6
  validity-time: 300

email:
  enabled: true
  host: smtp.qq.com
  port: 587
  username: your-email@qq.com
  password: your-authorization-code
  from: PartyPixelLogin <your-email@qq.com>
  ssl: false
  tls: true
  code-validity: 300
  cooldown: 60

qq:
  enabled: true
  use-email: true
  email-domain: qq.com
  code-validity: 300
  cooldown: 60

title:
  update-interval: 20
  captcha:
    enabled: true
    title: "&6请输入验证码"
    subtitle: "&e使用 /captcha <验证码>"
  register:
    enabled: true
    title: "&6请注册"
    subtitle: "&e使用 /register <密码> <重复密码>"
  login:
    enabled: true
    title: "&6请登录"
    subtitle: "&e使用 /login <密码>"

actionbar:
  enabled: true
  captcha: "&e请在 {time} 秒内输入验证码！"
  register: "&e请在 {time} 秒内完成注册！"
  login: "&e请在 {time} 秒内完成登录！"
```

### B. 命令参考

| 命令 | 用法 | 权限 | 描述 |
|-------|------|-------|------|
| /login | /login <密码> | 无 | 登录服务器 |
| /register | /register <密码> <重复密码> | 无 | 注册账号 |
| /captcha | /captcha <验证码> | 无 | 验证验证码 |
| /mail | /mail <邮箱> | 无 | 绑定邮箱 |
| /mailc | /mailc <验证码> | 无 | 验证邮箱 |
| /qq | /qq <QQ号> | 无 | 绑定QQ |
| /qqc | /qqc <验证码> | 无 | 验证QQ |
| /logout | /logout | 无 | 登出服务器 |
| /regdel | /regdel <玩家> | partypixellogin.admin | 删除玩家注册 |

### C. 技术支持

- **GitHub Issues**: https://github.com/your-repo/PartyPixelLogin/issues
- **文档**: https://github.com/your-repo/PartyPixelLogin/wiki
- **Discord**: https://discord.gg/your-server
- **邮箱**: support@your-server.com

---

**文档版本**: 1.0.0  
**最后更新**: 2026-03-08  
**适用插件版本**: PartyPixelLogin 1.0.0+
