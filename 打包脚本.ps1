# 星轨 XingGui 项目打包脚本
# 用于计算机设计大赛参赛作品打包

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  星轨 XingGui - 项目打包脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$sourceDir = "D:\AndroidStudio\app\android_template\XingGui"
$targetDir = "D:\计算机设计\大三\星轨\XingGui"

Write-Host "[1/6] 准备工作..." -ForegroundColor Yellow
Write-Host "  源目录: $sourceDir"
Write-Host "  目标目录: $targetDir"
Write-Host ""

# 确保目标目录存在
if (-not (Test-Path $targetDir)) {
    New-Item -ItemType Directory -Path $targetDir -Force | Out-Null
    Write-Host "  目标目录已创建" -ForegroundColor Green
}

Write-Host "[2/6] 复制项目核心文件..." -ForegroundColor Yellow

# 需要复制的文件和目录列表
$itemsToCopy = @(
    # 项目根目录文件
    "gradlew",
    "gradlew.bat",
    "gradle.properties",
    "settings.gradle.kts",
    "build.gradle.kts",
    "README.md",
    "启动后端.bat",
    "大赛演示检查清单.md",
    
    # Gradle Wrapper
    "gradle",
    
    # App模块
    "app",
    
    # Backend模块
    "backend"
)

# 需要排除的文件和目录
$excludeItems = @(
    "build",
    ".gradle",
    ".idea",
    "app/build",
    "backend/build",
    "*.iml",
    "local.properties",
    ".DS_Store",
    ".tmp",
    "UIReferences",
    "PDF_RESOURCES_README.md",
    "add_new_resources.sql",
    ".claude"
)

Write-Host "  正在复制文件..." -ForegroundColor Gray

foreach ($item in $itemsToCopy) {
    $sourcePath = Join-Path $sourceDir $item
    $targetPath = Join-Path $targetDir $item
    
    if (Test-Path $sourcePath) {
        Write-Host "  复制: $item" -ForegroundColor Gray
        
        if ((Get-Item $sourcePath) -is [System.IO.DirectoryInfo]) {
            # 复制目录，排除指定项
            robocopy $sourcePath $targetPath /E /XD $excludeItems /XF $excludeItems /NFL /NDL /NJH /NJS | Out-Null
        } else {
            # 复制文件
            Copy-Item $sourcePath $targetPath -Force
        }
    } else {
        Write-Host "  警告: 未找到 $item" -ForegroundColor Yellow
    }
}

Write-Host "  核心文件复制完成" -ForegroundColor Green
Write-Host ""

Write-Host "[3/6] 清理不必要的文件..." -ForegroundColor Yellow

# 删除排除的文件（robocopy可能没有完全排除）
foreach ($exclude in $excludeItems) {
    $excludePath = Join-Path $targetDir $exclude
    if (Test-Path $excludePath) {
        Write-Host "  删除: $exclude" -ForegroundColor Gray
        Remove-Item $excludePath -Recurse -Force -ErrorAction SilentlyContinue
    }
}

Write-Host "  清理完成" -ForegroundColor Green
Write-Host ""

Write-Host "[4/6] 生成打包文档..." -ForegroundColor Yellow

$readmeContent = @"
# 星轨 XingGui - 参赛作品打包说明

## 项目信息
- 项目名称: 星轨 XingGui
- 打包时间: $(Get-Date -Format "yyyy-MM-dd HH:mm:ss")
- 参赛类型: 计算机设计大赛 - 软件开发赛道

## 一、打包过程执行步骤

### 步骤1: 分析项目结构
- 确定核心源代码位置
- 识别必要的构建文件
- 筛选需要排除的无关文件

### 步骤2: 创建目标目录
- 路径: D:\计算机设计\大三\星轨\XingGui
- 准备打包环境

### 步骤3: 复制核心文件
复制以下内容：
- Gradle Wrapper (gradlew, gradlew.bat, gradle/)
- 项目配置文件 (settings.gradle.kts, build.gradle.kts, gradle.properties)
- Android App 模块 (app/) - 仅源代码和资源，排除 build/
- Backend 模块 (backend/) - 仅源代码和资源，排除 build/
- 必要的文档 (README.md, 启动后端.bat, 大赛演示检查清单.md)

### 步骤4: 清理不必要文件
- 删除 build/ 目录
- 删除 .gradle/ 目录
- 删除 .idea/ 目录
- 删除临时文件
- 删除 UI 参考文件
- 保留核心运行代码

### 步骤5: 生成打包文档
- 创建本 README.md 文件
- 记录打包过程和说明

## 二、打包结果文件结构

```
XingGui/
├── gradlew                          # Gradle Wrapper (Unix)
├── gradlew.bat                      # Gradle Wrapper (Windows)
├── gradle.properties                # Gradle 配置
├── settings.gradle.kts              # 项目设置
├── build.gradle.kts                 # 根构建脚本
├── README.md                        # 项目说明
├── 启动后端.bat                     # 后端启动脚本
├── 大赛演示检查清单.md              # 演示检查清单
├── gradle/                          # Gradle Wrapper 目录
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── app/                             # Android App 模块
│   ├── build.gradle.kts             # App 构建脚本
│   ├── proguard-rules.pro           # ProGuard 规则
│   └── src/                         # App 源代码
│       ├── main/                    # 主代码
│       │   ├── AndroidManifest.xml  # Android 清单
│       │   ├── java/                # Kotlin/Java 代码
│       │   ├── res/                 # 资源文件
│       │   └── assets/              # 资产文件 (PDF资源等)
│       └── test/                    # 测试代码
└── backend/                         # Backend 模块
    ├── build.gradle.kts             # Backend 构建脚本
    └── src/                         # Backend 源代码
        ├── main/                    # 主代码
        │   ├── kotlin/              # Kotlin 代码
        │   └── resources/           # 资源文件
        └── test/                    # 测试代码
```

## 三、已完成的工作内容总结

✅ 项目核心源代码完整打包
✅ Android App 模块（包含所有功能代码和资源）
✅ Backend 模块（包含所有服务端代码）
✅ Gradle 构建系统完整保留
✅ 必要的文档和启动脚本
✅ 清理了构建缓存和临时文件
✅ 确保项目可在新环境中构建

## 四、需要用户后续完成的任务清单

### 任务1: 构建 Android APK
1. 使用 Android Studio 打开项目
2. 等待 Gradle 同步完成
3. 菜单: Build → Generate Signed Bundle / APK
4. 选择 APK → Next
5. 配置签名密钥
6. 选择 release 构建变体 → Finish
7. APK 输出位置: app/build/outputs/apk/release/

**或者使用命令行:**
```bash
.\gradlew.bat :app:assembleRelease
```

### 任务2: 构建 Backend JAR (可选)
如果需要独立的可执行 JAR，需要：
1. 在 backend/build.gradle.kts 中添加 Shadow 插件
2. 执行: `.\gradlew.bat :backend:shadowJar`
3. JAR 输出位置: backend/build/libs/

### 任务3: 准备数据库
1. 安装 MySQL 8.0+
2. 创建数据库: `CREATE DATABASE xinggui;`
3. 配置用户名和密码（默认: root/root）
4. 数据库会在首次启动时自动通过 Flyway 初始化

### 任务4: 测试运行
1. 启动后端: 双击运行 `启动后端.bat`
2. 打开浏览器访问: http://localhost:8080/health
3. 使用 Android Studio 运行 App 或安装 APK

### 任务5: 准备参赛文档
1. 撰写作品说明书
2. 录制演示视频（可选但推荐）
3. 整理技术架构文档
4. 准备演示 PPT（如需要）

## 五、注意事项和潜在问题

### 1. Gradle 依赖下载
- 首次构建需要下载依赖，需要网络连接
- 建议提前在稳定网络环境下完成一次构建
- 如遇下载失败，可以配置阿里云 Maven 镜像（项目已配置）

### 2. Android SDK
- 确保 Android Studio 已安装 Android SDK
- 项目要求: minSdk 24, targetSdk 35
- 可通过 Android Studio SDK Manager 安装

### 3. Java 版本
- Android: JDK 11 或更高（Android Studio 自带）
- Backend: JDK 21（重要！）
- 请确保 JAVA_HOME 环境变量正确设置

### 4. 数据库配置
- 默认数据库: MySQL 8.0+
- 默认地址: localhost:3306
- 默认账号: root / root
- 如需修改，可通过环境变量配置：
  - XINGGUI_DB_URL
  - XINGGUI_DB_USER
  - XINGGUI_DB_PASSWORD

### 5. 端口占用
- 后端默认端口: 8080
- 如端口被占用，可通过环境变量 XINGGUI_PORT 修改
- 同时需要修改 App 的 app_config.json 文件中的 backendBaseUrl

### 6. 存储空间
- PDF 资源文件较多，确保有足够磁盘空间
- 建议至少预留 500MB

### 7. 真机调试
- 真机连接电脑时，需要将 app_config.json 中的地址改为电脑局域网 IP
- 例如: http://192.168.1.100:8080
- 确保手机和电脑在同一网络
- 关闭电脑防火墙或允许 8080 端口

## 六、评委如何在另一台设备上运行项目

### 环境准备（评委设备）

#### 必需软件
1. **Android Studio** (最新版本)
   - 用于打开项目、构建 APK、运行模拟器
   - 下载: https://developer.android.com/studio

2. **JDK 21** (用于后端)
   - 下载: https://adoptium.net/
   - 安装后配置 JAVA_HOME 环境变量

3. **MySQL 8.0+** (数据库)
   - 下载: https://dev.mysql.com/downloads/mysql/
   - 或使用便携版 MySQL（推荐用于演示）

4. **Git** (可选，用于版本管理)

#### 步骤1: 解压项目
- 将 XingGui 文件夹解压到任意位置
- 例如: C:\XingGui\

#### 步骤2: 配置数据库
1. 启动 MySQL 服务
2. 创建数据库:
```sql
CREATE DATABASE xinggui CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```
3. 记住用户名和密码（默认用 root/root）

#### 步骤3: 启动后端服务
1. 打开命令行/终端
2. 进入项目目录:
```cmd
cd C:\XingGui
```
3. 运行启动脚本（Windows）:
```cmd
启动后端.bat
```
或使用 Gradle:
```cmd
.\gradlew.bat :backend:run
```
4. 等待看到 "XingGui backend started" 提示
5. 测试后端: 浏览器访问 http://localhost:8080/health
   - 应返回: `{"database":"UP","..."}`

#### 步骤4: 运行 Android App

**方式 A: 使用 Android Studio（推荐用于查看代码）**
1. 打开 Android Studio
2. 选择 "Open an Existing Project"
3. 选择 XingGui 文件夹
4. 等待 Gradle 同步完成（首次可能需要几分钟）
5. 创建 Android 模拟器或连接真机
6. 点击运行按钮（绿色三角形）

**方式 B: 使用预构建 APK（如果已构建）**
1. 将 APK 文件安装到 Android 设备/模拟器
2. 打开应用
3. 测试各功能

#### 步骤5: 使用演示账号测试

| 角色 | 账号 | 密码 | 说明 |
|------|------|------|------|
| 家长 | parent001 | parent001 | 绑定 child001 (晨晨) |
| 教师 | teacher001 | teacher001 | 可切换多个儿童 |
| 家长 | parent002 | parent002 | 绑定 child002 (朵朵) |

**测试流程建议:**
1. 使用 parent001 登录
2. 查看星报告 - 成长雷达图
3. 进入星档案 - 完成一次打卡
4. 查看星资源 - 打开一个 PDF
5. 进入星目标 - 查看 IEP 计划
6. 进入我的 - 查看个人资料

#### 步骤6: 测试后端功能（可选）

健康检查:
```bash
curl http://localhost:8080/health
```

指标查看:
```bash
curl http://localhost:8080/metrics/basic
```

## 七、项目功能模块概览

### 星报告
- 儿童成长多维度评估
- 雷达图可视化展示
- 报告历史记录
- AI 智能分析总结

### 星档案
- 发展里程碑打卡
- 周统计和趋势
- CDC 官方检查表
- 图片和笔记记录

### 星目标
- IEP 个别化教育计划
- 学期/月度/周目标
- 文件上传和管理
- 目标进度跟踪

### 星资源
- 专业教育资源库
- PDF 在线阅读
- 资源分类浏览
- 学习进度记录

### 个人中心
- 个人资料管理
- 儿童档案编辑
- 多角色切换
- 安全退出登录

## 八、技术架构

### Android 端
- 语言: Kotlin
- UI: Jetpack Compose
- 架构: MVP (Model-View-Presenter)
- 网络: HttpURLConnection + Gson
- 最低版本: Android 7.0 (API 24)

### 后端
- 语言: Kotlin
- 框架: Ktor
- 数据库: MySQL + Flyway
- 连接池: HikariCP
- Java 版本: JDK 21

### 安全特性
- 密码加密: PBKDF2 + SHA-256
- 登录限流: IP/设备维度
- 验证码: 算术验证码
- 文件校验: 魔数检查 + 审计日志

## 九、快速故障排查

### 问题: 后端启动失败
**检查:**
- MySQL 是否启动
- 数据库 xinggui 是否存在
- 端口 8080 是否被占用
- JDK 版本是否为 21

### 问题: App 无法连接后端
**检查:**
- 后端是否正在运行
- 模拟器: 地址应为 http://10.0.2.2:8080
- 真机: 地址应为电脑局域网 IP
- 电脑防火墙是否阻止 8080 端口

### 问题: Gradle 同步失败
**检查:**
- 网络连接是否正常
- 是否配置了代理（如需要）
- 尝试: File → Invalidate Caches / Restart

### 问题: PDF 资源无法打开
**检查:**
- assets/resources/pdfs/ 目录下 PDF 文件是否完整
- 应用是否有存储权限

## 十、联系方式（如需要）

如有问题，请参考:
- 项目 README.md
- 大赛演示检查清单.md
- 或联系开发团队

---

**打包完成时间:** $(Get-Date -Format "yyyy-MM-dd HH:mm:ss")
**打包工具:** PowerShell Script
**项目版本:** 1.0
"@

$readmePath = Join-Path $targetDir "打包说明.md"
$readmeContent | Out-File -FilePath $readmePath -Encoding UTF8

Write-Host "  打包说明文档已生成" -ForegroundColor Green
Write-Host ""

Write-Host "[5/6] 生成演示账号说明..." -ForegroundColor Yellow

$accountsContent = @"
# 星轨 XingGui - 演示账号说明

## 测试账号列表

### 家长账号 1
- 账号: parent001
- 密码: parent001
- 绑定儿童: child001 (晨晨)
- 适用场景: 家长端功能完整测试

### 教师账号
- 账号: teacher001
- 密码: teacher001
- 绑定儿童: child001, child002
- 适用场景: 教师端多儿童管理测试

### 家长账号 2
- 账号: parent002
- 密码: parent002
- 绑定儿童: child002 (朵朵)
- 适用场景: 第二个家长端测试

### 管理员账号
- 账号: 1
- 密码: 1
- 说明: 后端管理账号，App 登录会提示无移动端入口

## 测试流程建议

### 基础测试
1. 使用 parent001 登录
2. 查看星报告 - 确认雷达图显示正常
3. 进入星档案 - 尝试完成一次打卡
4. 查看星资源 - 打开一个 PDF 资源
5. 进入星目标 - 查看 IEP 计划
6. 进入我的 - 查看和编辑个人资料

### 角色切换测试
1. 使用 teacher001 登录
2. 测试多儿童切换功能
3. 验证不同儿童的数据隔离

### 教师-家长协作测试
1. 用 teacher001 为 child001 更新数据
2. 用 parent001 登录查看是否同步

## 注意事项

⚠️ 首次启动后端时，会自动创建演示账号和数据
⚠️ 数据库中的用户数据会保留，不会被覆盖
⚠️ 忘记密码可直接在数据库中重置，或删除数据库重新初始化
"@

$accountsPath = Join-Path $targetDir "演示账号.md"
$accountsContent | Out-File -FilePath $accountsPath -Encoding UTF8

Write-Host "  演示账号文档已生成" -ForegroundColor Green
Write-Host ""

Write-Host "[6/6] 验证打包结果..." -ForegroundColor Yellow

# 统计文件数量
$fileCount = (Get-ChildItem -Path $targetDir -Recurse -File | Measure-Object).Count
$dirCount = (Get-ChildItem -Path $targetDir -Recurse -Directory | Measure-Object).Count

Write-Host "  文件数量: $fileCount"
Write-Host "  目录数量: $dirCount"
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  ✅ 打包完成！" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "打包位置: $targetDir" -ForegroundColor Yellow
Write-Host ""
Write-Host "已生成的文档:" -ForegroundColor Gray
Write-Host "  - 打包说明.md"
Write-Host "  - 演示账号.md"
Write-Host ""
Write-Host "后续任务请参考 打包说明.md 文档" -ForegroundColor Cyan
Write-Host ""
"@
