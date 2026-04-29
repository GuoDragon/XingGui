# 星轨 XingGui

星轨是面向孤独症儿童家校协同场景的 Android + 本地后端演示作品。当前项目按计算机设计大赛答辩展示口径整理为：Android App、Ktor 后端、本地 MySQL、Flyway 迁移、规则化安全校验与可观测性闭环。

## 项目结构

```text
XingGui/
├─ app/                         Android App，Kotlin + Jetpack Compose + MVP
├─ backend/                     Ktor REST API，Flyway + MySQL
├─ app/src/main/assets/data/    首次建库使用的演示种子数据
├─ 启动后端.bat                 Windows 本地演示启动脚本
└─ 大赛演示检查清单.md          现场演示与故障排查清单
```

## 一键启动后端

1. 确认本机 MySQL 已启动，并存在数据库 `xinggui`。
2. 默认演示配置为 `root / root`，端口 `8080`，无需云服务器。
3. 在项目根目录双击或运行：

```powershell
.\启动后端.bat
```

也可以直接运行：

```powershell
.\gradlew.bat :backend:run
```

> Gradle 停在 `83% EXECUTING` 是正常状态，表示 Ktor 服务正在前台运行。停止后端请在该窗口按 `Ctrl+C`。

默认配置项：

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `XINGGUI_MODE` | `local-demo` | 本机演示模式，允许默认账号和本地 HTTP |
| `XINGGUI_PORT` | `8080` | 后端端口 |
| `XINGGUI_DB_URL` | `jdbc:mysql://127.0.0.1:3306/xinggui?...` | 本机 MySQL 连接 |
| `XINGGUI_DB_USER` | `root` | MySQL 用户 |
| `XINGGUI_DB_PASSWORD` | `root` | MySQL 密码 |
| `XINGGUI_ADMIN_PASSWORD` | `1` | 本机演示管理员密码 |
| `XINGGUI_SEED_DIR` | `app/src/main/assets/data` | 种子数据目录 |
| `XINGGUI_UPLOAD_DIR` | `storage/iep-documents` | IEP 文件保存目录 |

非 `local-demo` 模式下，后端会拒绝 `root/root`、管理员默认密码 `1`、关闭 TLS 的数据库连接或明文公开地址，避免把演示配置误带到生产环境。

## 启动 Android App

- 模拟器默认读取 `app/src/main/assets/data/app_config.json`，后端地址为 `http://10.0.2.2:8080`。
- 真机联调时，把该文件中的 `backendBaseUrl` 改为电脑局域网 IP，例如 `http://192.168.1.10:8080`。
- 若登录页提示“无法连接本地服务”，先确认 `启动后端.bat` 正在运行，再点击登录页的“重试连接”。

## 演示账号

| 身份 | 账号 | 密码 | 说明 |
| --- | --- | --- | --- |
| 家长 | `parent001` | `parent001` | 绑定 `child001` |
| 教师 | `teacher001` | `teacher001` | 绑定 `child001`、`child002` |
| 家长 | `parent002` | `parent002` | 绑定 `child002` |
| 管理员 | `1` | `1` | 后端管理账号；登录 App 会提示无移动端入口 |

后端启动时会自检并修复缺失的演示账号、角色和儿童关系；不会删除用户后续新增的数据。旧版 `pbkdf2` 密码哈希在登录成功后会自动升级为 `pbkdf2_sha256`。

## 健康检查与展示指标

启动后可访问：

```powershell
curl http://127.0.0.1:8080/health
curl http://127.0.0.1:8080/metrics/basic
```

`/health` 会返回数据库状态、上传目录可写性、运行模式和演示账号状态。`/metrics/basic` 会返回请求量、延迟、登录失败数、限流命中数、上传数、审计日志数和数据库连接池状态，便于答辩展示“稳定性与可观测性”。

## 安全与隐私设计

- 账号密码使用 `pbkdf2_sha256`，兼容旧哈希并在成功登录后升级。
- 登录同时具备账号锁定、IP/设备维度限流、审计日志与友好错误提示。
- 新增 `POST /auth/logout-all`，用于撤销当前账号全部未过期会话。
- 注册在风险升高时触发算术验证码，App 会展示验证码题目并重试注册。
- IEP 上传保留大小、扩展名、MIME、文件魔数校验；通过规则校验后标记为 `PASSED_BY_RULES`，避免误表达为第三方杀毒通过。
- 文件保存、上传成功和失败都会进入审计链路；失败时回滚数据库并删除临时文件。
- App 登录/注册页和“我的”页提供“隐私与儿童数据说明”，明确儿童档案、成长报告和 IEP 文档仅用于授权家长/教师协同展示。

## 常用接口

- 认证：`POST /auth/register`、`POST /auth/login`、`GET /auth/me`、`POST /auth/logout`、`POST /auth/logout-all`
- 安全：`GET /security/captcha`
- 会话：`GET /session/roles`、`POST /session/active-role`、`POST /session/selected-child`
- 业务：`/children`、`/goals/{childId}`、`/archive/checkin`、`/reports/{childId}`、`/resources`
- 运维展示：`GET /health`、`GET /metrics/basic`

## 验证命令

```powershell
.\gradlew.bat :backend:test
.\gradlew.bat :app:compileDebugKotlin
.\gradlew.bat :app:testDebugUnitTest
```

如遇依赖下载失败，先确认网络或本机 Gradle 缓存；比赛现场建议提前完成一次完整构建。
