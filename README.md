# IceCloudLogin-Plugin
MinecraftJavaPlugin-IceCloud插件

## 概述

IceCloudLogin 是一款功能强大的 Spigot/Paper 服务器登录插件，提供安全、便捷的玩家认证系统。插件支持验证码注册、邮箱绑定、QQ绑定、密码重置等多种功能，旨在为服务器提供完善的账号管理解决方案。

**版本**: 0.5.1  
**作者**: ya_xzer21145  
**支持版本**: Spigot/Paper 1.20+  
**Java要求**: Java 8+

---

## 核心功能

### 1. 登录系统

#### 自动登录
- 玩家在2小时内使用密码登录过，再次进入服务器时自动登录
- 无需重复输入密码，提升玩家体验
- 自动登录不计入登录检测列表，2小时后需重新验证

#### 手动登录
- 未注册玩家需先完成注册流程
- 已注册玩家使用 `/login <密码>` 命令登录
- 支持别名 `/l` 快速登录

#### 登录限制
- 登录超时：默认120秒，超时后踢出服务器
- 密码错误限制：默认5次，超过后踢出
- 惩罚系统：被踢出3次后执行管理员配置的命令

### 2. 注册系统

#### 验证码验证
- 新玩家首次进入服务器需先完成验证码验证
- 系统生成6位数字验证码
- 使用 `/captcha <验证码>` 命令完成验证
- 验证码有效期：默认5分钟

#### 账号注册
- 验证码通过后使用 `/register <密码> <重复密码>` 注册
- 支持别名 `/reg` 快速注册
- 密码要求：
  - 长度：6-32位
  - 字符：0-9、A-Z、a-z、-、_
- 注册成功后自动登录

#### 注册限制
- 注册超时：默认120秒，超时后踢出服务器
- 防止重复注册：同一UUID只能注册一次

### 3. 邮箱绑定

#### 绑定流程
1. 玩家使用 `/mail <邮箱地址>` 发起绑定请求
2. 系统向指定邮箱发送验证码
3. 玩家使用 `/mailc <验证码>` 完成验证
4. 验证成功后邮箱绑定到账号

#### 功能特性
- 支持多种邮箱服务商（QQ邮箱、Gmail、163等）
- 验证码有效期：5分钟
- 发送冷却：60秒
- 防重复绑定：同一邮箱只能绑定一个账号

#### 邮箱配置
```yaml
email:
  enabled: true
  host: smtp.qq.com
  port: 587
  username: your-email@qq.com
  password: your-authorization-code
  from: "IceCloudLogin <your-email@qq.com>"
  ssl: false
  tls: true
  code-validity: 300
  cooldown: 60
```

### 4. QQ绑定

#### 绑定方式
插件使用邮件方式实现QQ绑定，无需复杂的OAuth2.0授权流程：
1. 玩家使用 `/qq <QQ号>` 发起绑定
2. 系统自动生成QQ邮箱（QQ号@qq.com）
3. 向该邮箱发送验证码
4. 玩家使用 `/qqc <验证码>` 完成绑定

#### 优势
- 配置简单，无需申请QQ开放平台应用
- 稳定可靠，依赖成熟的邮件服务
- 玩家操作便捷，无需跳转网页

#### QQ配置
```yaml
qq:
  enabled: true
  use-email: true
  email-domain: qq.com
  code-validity: 300
  cooldown: 60
```

### 5. 密码管理

#### 密码重置
玩家忘记密码时可通过绑定的邮箱或QQ重置：

**邮箱重置**：
1. 使用 `/mailzhpass` 命令
2. 系统向绑定邮箱发送验证码
3. 使用 `/mailc <验证码>` 验证
4. 验证成功后重新注册设置新密码

**QQ重置**：
1. 使用 `/qqzhpass` 命令
2. 系统向绑定QQ邮箱发送验证码
3. 使用 `/qqc <验证码>` 验证
4. 验证成功后重新注册设置新密码

#### 密码修改
已登录玩家可使用 `/changepass <原密码> <新密码>` 修改密码
- 支持别名：`/cpss`、`/changepassword`
- 需验证原密码
- 新密码需符合注册规则

### 6. 邮箱/QQ修改

#### 邮箱修改
使用 `/changemail <原邮箱> <新邮箱>` 修改绑定邮箱
- 支持别名：`/cmail`
- 需验证原邮箱
- 新邮箱需通过验证码验证

#### QQ修改
使用 `/changeqq <原QQ> <新QQ>` 修改绑定QQ
- 支持别名：`/cq`
- 需验证原QQ
- 新QQ需通过验证码验证

### 7. 登出功能

玩家可使用 `/logout` 命令主动登出
- 取消登录状态
- 重新进入服务器需再次登录
- 用于测试或安全需求

### 8. 管理员功能

#### 删除玩家注册
管理员使用 `/regdel <玩家名>` 删除玩家注册信息
- 需权限：`icecloudlogin.admin`
- 删除后玩家需重新注册
- 玩家在线时会踢出

#### 重载配置
管理员使用 `/icecloudlogin reload` 重载配置文件
- 需权限：`icecloudlogin.admin`
- 无需重启服务器
- 实时生效

#### 查看版本
使用 `/icecloudlogin version` 查看插件版本信息

#### 查看帮助
使用 `/icecloudlogin help` 查看所有命令帮助

---

## 玩家限制

未登录/未注册玩家受到以下限制：

| 功能 | 限制 |
|------|------|
| 聊天 | 无法发送聊天消息 |
| 命令 | 无法使用除登录/注册外的命令 |
| 移动 | 无法移动位置 |
| 交互 | 无法与方块/实体交互 |
| 破坏 | 无法破坏方块 |
| 放置 | 无法放置方块 |
| 伤害 | 无法造成/受到伤害 |
| 背包 | 无法打开背包 |

---

## UI提示

### Title标题显示
- 验证码阶段：显示"请输入验证码"
- 注册阶段：显示"请注册"
- 登录阶段：显示"请登录"
- 登录成功：显示"登录成功"（3秒）
- 注册成功：显示"注册成功"（3秒）

### Actionbar倒计时
实时显示剩余时间：
- 验证码："请在 X 秒内输入验证码！"
- 注册："请在 X 秒内完成注册！"
- 登录："请在 X 秒内完成登录！"

### 欢迎消息
登录/注册成功后显示欢迎信息
- 支持多行配置
- 支持颜色代码
- 支持玩家名称变量 `{player}`

---

## 数据库支持

### SQLite（默认）
- 无需额外配置
- 数据文件：`plugins/IceCloudLogin/data.db`
- 适合小型服务器

### MySQL
- 支持远程数据库
- 适合大型服务器
- 配置示例：
```yaml
database:
  type: mysql
  mysql:
    host: localhost
    port: 3306
    database: icecloudlogin
    username: root
    password: password
```

### 数据表结构
```sql
CREATE TABLE players (
    uuid VARCHAR(36) PRIMARY KEY,
    username VARCHAR(16) NOT NULL,
    password_hash VARCHAR(64) NOT NULL,
    email VARCHAR(100),
    qq VARCHAR(20),
    last_login BIGINT,
    register_time BIGINT
);
```

---

## 安全特性

### 密码加密
- 使用SHA-256哈希算法
- 密码不以明文存储
- 防止密码泄露

### 验证码保护
- 一次性验证码
- 有效期限制
- 防止暴力破解

### 登录保护
- 密码错误次数限制
- 踢出次数惩罚
- IP记录（可选）

---

## 配置文件

### config.yml
主配置文件，包含所有功能设置：
- 数据库配置
- 登录设置
- 注册设置
- 邮箱配置
- QQ配置
- Title配置
- Actionbar配置

### message.yml
消息配置文件，支持自定义所有提示信息：
- 前缀设置
- 登录消息
- 注册消息
- 邮箱消息
- QQ消息
- 错误消息
- 管理员消息

### welcome.txt
欢迎消息文件，登录/注册成功后显示：
- 支持多行
- 支持颜色代码
- 支持变量替换

---

## 命令列表

| 命令 | 别名 | 权限 | 描述 |
|--------|------|--------|------|
| /login | /l | 无 | 登录服务器 |
| /register | /reg | 无 | 注册账号 |
| /captcha | 无 | 无 | 验证验证码 |
| /mail | 无 | 无 | 绑定邮箱 |
| /mailc | 无 | 无 | 验证邮箱 |
| /qq | 无 | 无 | 绑定QQ |
| /qqc | 无 | 无 | 验证QQ |
| /logout | 无 | 无 | 登出服务器 |
| /changepass | /cpss, /changepassword | 无 | 修改密码 |
| /changemail | /cmail | 无 | 修改邮箱 |
| /changeqq | /cq | 无 | 修改QQ |
| /mailzhpass | 无 | 无 | 通过邮箱重置密码 |
| /qqzhpass | 无 | 无 | 通过QQ重置密码 |
| /regdel | 无 | icecloudlogin.admin | 删除玩家注册 |
| /icecloudlogin | 无 | 无 | 主命令 |

---

## 权限系统

| 权限 | 描述 | 默认 |
|--------|------|------|
| icecloudlogin.* | 所有权限 | - |
| icecloudlogin.admin | 管理员权限 | OP |

---

## 安装说明

### 1. 下载插件
将 `IceCloudLogin-0.5.1.jar` 放入服务器的 `plugins` 文件夹

### 2. 重启服务器
重启Minecraft服务器，插件将自动加载

### 3. 配置插件
编辑 `plugins/IceCloudLogin/config.yml` 配置各项功能

### 4. 配置邮箱（可选）
如需使用邮箱绑定功能，配置SMTP服务器信息

### 5. 重载配置
在游戏中执行 `/icecloudlogin reload` 或重启服务器

---

## 常见问题

### Q: 如何启用邮箱功能？
A: 在config.yml中设置 `email.enabled: true`，并配置SMTP服务器信息

### Q: 如何获取QQ邮箱授权码？
A: 登录QQ邮箱 → 设置 → 账户 → 开启POP3/SMTP服务 → 获取授权码

### Q: 玩家忘记密码怎么办？
A: 玩家可使用 `/mailzhpass` 或 `/qqzhpass` 通过绑定邮箱/QQ重置密码

### Q: 如何修改登录超时时间？
A: 在config.yml中修改 `login.timeout` 值（单位：秒）

### Q: 支持哪些Minecraft版本？
A: 支持 Spigot/Paper 1.20+ 版本

### Q: 数据存储在哪里？
A: 默认使用SQLite，数据文件在 `plugins/IceCloudLogin/data.db`

---

## 技术支持

如有问题或建议，请联系：
- 作者：ya_xzer21145
- 插件版本：0.5.1

---

## 许可证

本插件仅供学习和个人使用，请勿用于商业用途。
