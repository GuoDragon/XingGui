# 星轨 XingGUI

面向孤独症儿童家校协同场景的 Android 演示应用，当前采用 **Android App + 本地 Ktor API + MySQL** 的同仓结构：

- `:app`：Kotlin + Jetpack Compose + MVP 的移动端
- `:backend`：Kotlin + Ktor + Flyway + MySQL 的本地 REST API
- `app/src/main/assets/data/*.json`：仅作为首库初始化的种子数据来源，不再是运行期权威数据源

本轮重构重点：

- 登录、注册、角色选择、儿童/目标/报告/档案/资源全量改走后端 API
- MySQL 接管用户、角色、资源运行态、档案打卡、报告历史等核心业务数据
- 保留多角色账号模型，移动端仅消费 `PARENT` / `TEACHER`
- “星资源”首页、搜索、分类、详情页统一为浅蓝背景 + 圆角卡片 + 强标题层级的新视觉语言

## 模块结构

```text
XingGui/
├── app/
│   └── src/main/java/com/example/xinggui/
│       ├── data/
│       ├── navigation/
│       ├── presentation/
│       └── ui/
├── backend/
│   └── src/main/
│       ├── kotlin/com/example/xinggui/backend/
│       └── resources/db/migration/
└── app/src/main/assets/data/
```

## 数据架构

- Android 端仅保留轻量会话缓存：`session_state.json`
- 业务真实来源统一为 MySQL
- Flyway 启动时自动执行 `backend/src/main/resources/db/migration/V1__init.sql`
- 后端首次启动时会读取 `app/src/main/assets/data/*.json` 并灌入数据库
- 资源解锁、搜索历史、报告历史、档案打卡、目标完成状态均持久化到 MySQL

核心表包括：

- `users` / `roles` / `user_roles`
- `children` / `child_guardians` / `child_teachers`
- `goal_plans` / `weekly_checkins`
- `report_summaries` / `report_history_entries`
- `archive_checkin_records`
- `resources` / `resource_runtime_state`
- `sessions`

## 启动后端

1. 本地准备 MySQL，并创建数据库，例如 `xinggui`
2. 按需配置环境变量或 JVM 属性
3. 在项目根目录运行：

```powershell
./gradlew.bat :backend:run
```

后端默认配置如下：

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `XINGGUI_PORT` | `8080` | 本地 API 端口 |
| `XINGGUI_DB_URL` | `jdbc:mysql://127.0.0.1:3306/xinggui?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai` | MySQL 连接串 |
| `XINGGUI_DB_USER` | `root` | MySQL 用户名 |
| `XINGGUI_DB_PASSWORD` | `root` | MySQL 密码 |
| `XINGGUI_SEED_DIR` | 自动探测 `app/src/main/assets/data` | 首库初始化数据目录 |

常用验证命令：

```powershell
./gradlew.bat :backend:compileKotlin
./gradlew.bat :app:compileDebugKotlin
./gradlew.bat :app:testDebugUnitTest
```

## 启动 Android App

- 模拟器默认读取 `app/src/main/assets/data/app_config.json` 中的 `backendBaseUrl`
- 当前默认值为 `http://10.0.2.2:8080`，适用于 Android Emulator 访问宿主机本地后端
- 若使用真机调试，请改成宿主机局域网 IP

运行方式：

1. 先启动 `:backend`
2. 用 Android Studio 打开 `XingGui`
3. 运行 `app` 模块

## 账号与角色

种子数据默认提供以下账号：

- 管理员：`1 / 1`
- 家长演示账号：`parent001 / parent001`
- 教师演示账号：`teacher001 / teacher001`
- 家长演示账号：`parent002 / parent002`

说明：

- 普通注册可选择 `PARENT`、`TEACHER` 或多角色组合
- `ADMIN` 不允许移动端自注册
- 管理员账号可调用后端管理员接口，但登录 Android App 时会提示“该账号无移动端入口”
- 登录成功后，角色选择页展示的是数据库中真实授予的移动端角色

## 主要接口

- 认证：`POST /auth/register`、`POST /auth/login`、`GET /auth/me`
- 会话：`GET /session/roles`、`POST /session/active-role`、`POST /session/selected-child`
- 业务：`/children`、`/goals/{childId}`、`/archive/checkin`、`/reports/{childId}`、`/resources`
- 管理员：`/admin/users`、`/admin/users/{userId}/roles`

## 备注

- 后端首次启动时如果数据库非空，将跳过种子导入
- 阅读器页保留 PDF 阅读主流程，仅做轻量视觉对齐
- 当前仓库保留 `assets` 数据文件，便于空库初始化与演示回灌
