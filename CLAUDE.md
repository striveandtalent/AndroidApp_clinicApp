# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

---

## 项目概述

这是一个**诊所管理系统** Android 应用，用于管理患者信息、就诊记录、治疗记录和附件。项目最初 fork 自 [Dimezis/BlurView](https://github.com/Dimezis/BlurView)（iOS 风格模糊效果库），包名仍保留原始名称 `com.eightbitlab.blurview_sample`，但应用模块已被完全改造为诊所管理系统，`library/` 模块保持 BlurView 原样。

## 构建与运行

```bash
# 构建应用（Windows）
./gradlew assembleDebug

# 构建并安装到设备
./gradlew installDebug

# 运行所有测试（仅 library 模块有测试）
./gradlew test

# 运行单个测试类
./gradlew :library:test --tests "com.eightbitlab.blurview.SizeScalerTest"

# 清理构建产物
./gradlew clean
```

## 技术栈

| 层面 | 技术 |
|------|------|
| 语言 | **Java 11**（整个项目无一 Kotlin） |
| 构建 | Gradle (Groovy DSL)，AGP 8.13.0 |
| UI | 传统 XML 布局 + Material3，**无 Jetpack Compose** |
| 网络 | Retrofit 2.11 + OkHttp 4.12 + Gson |
| 图片加载 | Glide 4.16（集成 OkHttp） |
| 视频播放 | AndroidX Media3 (ExoPlayer) 1.4.1 + 300MB LRU 缓存 |
| 模糊效果 | 本地 `:library` 模块（BlurView v3.2.0，支持 API 31+ RenderNode 硬件加速） |
| 本地存储 | 仅 SharedPreferences + ExoPlayer VideoCache，**无 Room/SQLite** |

## 架构概览

```
┌─────────────────────────────────────────────┐
│                  UI 层                       │
│  Activity/Fragment 直接处理一切               │
│  - 调用 ApiClient.api() 发起网络请求         │
│  - 在 onResponse/onFailure 回调中更新 UI     │
│  - 无 ViewModel / DataBinding / DI          │
├─────────────────────────────────────────────┤
│              网络层 (net/)                    │
│  ApiClient: Retrofit 单例 + OkHttp 配置      │
│  ApiService: 所有 REST 接口定义              │
│  TokenManager: Token 内存+SP 双存储          │
│  AppSettings: 服务器地址等配置 SP            │
├─────────────────────────────────────────────┤
│              模型层 (各子包)                  │
│  简单 POJO，Gson 反序列化                    │
│  @SerializedName 支持大小写别名              │
└─────────────────────────────────────────────┘
```

**核心模式：无正式 MVVM/MVP 架构。** 每个 Activity/Fragment 直接持有业务逻辑，通过 `ApiClient.api().xxx().enqueue(callback)` 模式进行异步网络调用。没有依赖注入、没有仓库模式、没有协程。

## 关键文件与职责

### 应用入口与全局配置

- **[MyApp.java](app/src/main/java/com/eightbitlab/blurview_sample/MyApp.java)** — Application 类，初始化 Glide 和全局异常捕获
- **[ApiClient.java](app/src/main/java/com/eightbitlab/blurview_sample/net/ApiClient.java)** — Retrofit 单例，配置不安全 SSL（信任所有证书）、Auth 拦截器（自动注入 `Authorization: Bearer <token>`）、日志拦截器
- **[ApiService.java](app/src/main/java/com/eightbitlab/blurview_sample/net/ApiService.java)** — 所有 API 端点定义，返回 `Call<ReturnInfo<T>>`
- **[TokenManager.java](app/src/main/java/com/eightbitlab/blurview_sample/Login/TokenManager.java)** — Token 双层存储：先内存后 SP；支持 `saveMemoryToken()`（不记住密码）和 `saveToken()`（记住密码）
- **[AppSettings.java](app/src/main/java/com/eightbitlab/blurview_sample/net/AppSettings.java)** — SharedPreferences 配置（服务器地址、环境切换）
- **[ReturnInfo.java](app/src/main/java/com/eightbitlab/blurview_sample/ReturnInfo.java)** — 通用 API 响应包装：`status`, `code`, `message`, `traceId`, `data`

### 登录认证

- **[LoginActivity.java](app/src/main/java/com/eightbitlab/blurview_sample/Login/LoginActivity.java)** — 登录页，支持「记住登录」、环境切换（本地/线上服务器两种 URL 配置）。已登录用户自动跳转到 MainActivity
- **[LoginRequest.java](app/src/main/java/com/eightbitlab/blurview_sample/Login/LoginRequest.java)** — `{userName, password}`
- **[LoginResponse.java](app/src/main/java/com/eightbitlab/blurview_sample/Login/LoginResponse.java)** — `{token, userName, realName, role}`

### 患者管理

- **[PatientSearch 片段 (CaseFragment.java)](app/src/main/java/com/eightbitlab/blurview_sample/PatientDetail/CaseFragment.java)** — 患者列表/搜索，显示在 MainActivity 的 ViewPager 中
- **[PatientDetailActivity.java](app/src/main/java/com/eightbitlab/blurview_sample/PatientDetail/PatientDetailActivity.java)** — 患者详情（含就诊历史列表）
- **[PatientEditActivity.java](app/src/main/java/com/eightbitlab/blurview_sample/PatientDetail/PatientEditActivity.java)** — 编辑患者信息
- **[CreatePatientActivity.java](app/src/main/java/com/eightbitlab/blurview_sample/Patient_Create/CreatePatientActivity.java)** — 创建新患者（姓名、性别、年龄、生日、电话、身份证、地址、过敏史、病史、主治疗方案）

### 就诊与治疗记录

- **[VisitDetailActivity.java](app/src/main/java/com/eightbitlab/blurview_sample/VisitDetail/VisitDetailActivity.java)** — 就诊详情聚合页：主诉、现病史、体征、诊断、报告附件、处方附件、其他附件、初诊记录、复诊记录、治疗效果评分(1-4)、总费用、医嘱、备注
- **[VisitCreateActivity.java](app/src/main/java/com/eightbitlab/blurview_sample/VisitDetail/VisitCreateActivity.java)** — 创建新就诊
- **[VisitEditActivity.java](app/src/main/java/com/eightbitlab/blurview_sample/VisitDetail/VisitEditActivity.java)** — 分节编辑就诊（病情信息 / 治疗过程 / 医嘱 / 备注）
- **[TreatmentRecordEditActivity.java](app/src/main/java/com/eightbitlab/blurview_sample/VisitDetail/TreatmentRecordEditActivity.java)** — 创建/编辑初诊或复诊记录（时间、内容、费用）

### 附件管理

- **[AttachmentPreviewActivity.java](app/src/main/java/com/eightbitlab/blurview_sample/VisitDetail/AttachmentPreviewActivity.java)** — ViewPager 全屏图片预览，支持删除
- **[VideoPreviewActivity.java](app/src/main/java/com/eightbitlab/blurview_sample/VisitDetail/VideoPreviewActivity.java)** — 视频播放，使用 ExoPlayer + 300MB LRU 缓存
- **附件上传**通过 `MultipartBody.Part` 调用 `POST /api/Storage/upload`

### 设置

- **[SettingFragment.java](app/src/main/java/com/eightbitlab/blurview_sample/SettingFragment.java)** — 设置页，显示当前服务器地址、连接测试、登录/登出

### 主界面

- **[MainActivity.java](app/src/main/java/com/eightbitlab/blurview_sample/MainActivity.java)** — 主界面，ViewPager2 承载患者列表和设置两个 Tab，底部毛玻璃导航栏（BlurView）

## API 响应格式

所有接口返回统一格式 `ReturnInfo<T>`：

```json
{
  "status": 200,      // HTTP 状态码
  "code": "0",        // 业务状态码，"0" 表示成功
  "message": "ok",
  "traceId": "xxx",
  "data": { ... }     // 具体业务数据
}
```

## 环境配置

应用支持两套服务器环境，通过 `AppSettings.getBaseUrl()` 获取当前活跃的 BaseUrl：

| 环境 | 默认地址 |
|------|----------|
| 本地开发 | `http://192.168.0.110:6123/` |
| 线上生产 | `https://frp-gap.com:63807/` |

BaseUrl 可通过设置页自由编辑。

## 重要注意事项

1. **不安全 SSL**：`ApiClient` 信任所有 HTTPS 证书，仅用于开发环境，切勿在生产环境使用
2. **包名未改**：包名仍为 `com.eightbitlab.blurview_sample`，如果上架应用商店需修改
3. **完全依赖服务端**：没有本地数据库，所有数据通过网络获取，离线不可用
4. **Gson 大小写兼容**：`ReturnInfo` 等模型的字段使用 `@SerializedName(value = "...", alternate = {...})` 同时支持 PascalCase 和 camelCase 两种 JSON key 风格
5. **Activity 直接启动**：不使用 Navigation Component，所有页面跳转通过 `Intent` + `startActivity()` / `startActivityForResult()`
6. **library 模块独立**：`library/` 是 BlurView 开源库原代码，有独立的 maven-publish 配置和 JUnit5 测试，修改时注意不要破坏其原有 API
