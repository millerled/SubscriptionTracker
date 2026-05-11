# CLAUDE.md

## 产品信息
- **产品名称**: SubscriptionTracker
- **类型**: 原生 Android 应用
- **定位**: 订阅日期提醒器 + 生活 Deadline 追踪工具
- **代码状态**: v2.0 功能口径已落地一部分，Android `versionName` 仍为 `1.0`
- **平台**: Android 8.0+ (API 26+)，`compileSdk/targetSdk` 为 35

## 技术栈
- **语言**: Kotlin 2.0.21，JVM 17
- **UI**: Jetpack Compose + Material3，Compose BOM 2024.12.01
- **数据库**: Room 2.6.1，本地离线存储，数据库版本 `3`
- **架构**: MVVM + Repository Pattern
- **异步**: Kotlin Coroutines 1.9.0 + Flow/StateFlow
- **导航**: Navigation Compose 2.8.5
- **图片加载**: Coil Compose 2.7.0
- **后台任务**: WorkManager 2.10.0
- **构建**: Gradle 8.11.1 + AGP 8.7.3 + KSP 2.0.21-1.0.27

## 当前项目结构
```
app/src/main/java/com/subscriptiontracker/
├── MainActivity.kt                    # 单 Activity 入口，NavHost 与 deep link 处理
├── SubscriptionTrackerApp.kt          # Application，数据库单例与通知 Worker 调度
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt             # Room 数据库，subscriptions/payment_history，version 3
│   │   ├── dao/
│   │   │   ├── SubscriptionDao.kt
│   │   │   └── PaymentHistoryDao.kt
│   │   ├── entity/
│   │   │   ├── SubscriptionEntity.kt
│   │   │   └── PaymentHistoryEntity.kt
│   │   └── converter/Converters.kt
│   └── repository/SubscriptionRepository.kt
├── domain/model/
│   ├── Subscription.kt
│   ├── SubscriptionStatus.kt          # ACTIVE/RENEWING/EXPIRING/PAUSED
│   ├── Intention.kt                   # CONSIDERING/QUITTING/UNDECIDED
│   ├── BillingCycle.kt
│   └── SortOption.kt
├── ui/
│   ├── theme/                         # Color / Type / Shape / Theme
│   ├── home/                          # 首页、统计卡、筛选、扣费确认、卡片
│   ├── addedit/                       # 新增/编辑表单、图标/壁纸选择
│   ├── detail/                        # 详情页、历史扣费记录
│   └── components/                    # 卡片、Logo、热力图、底部操作面板等
├── util/
│   ├── DateFormatter.kt
│   └── CurrencyFormatter.kt
├── worker/NotificationWorker.kt       # 每日到期提醒 Worker
└── widget/SubscriptionWidgetProvider.kt # 首页桌面小组件
```

## 核心设计决策
1. **无依赖注入框架**：当前不使用 Hilt/Dagger。`SubscriptionTrackerApp` 持有数据库单例，ViewModel 通过 `AndroidViewModel` 获取 Application Context 并创建 Repository。
2. **Room 本地存储**：`subscriptions` 与 `payment_history` 两张表，`PaymentHistoryEntity` 通过外键关联订阅并在订阅删除时级联删除。
3. **日期存储**：业务层使用 `LocalDate`，数据库中日期字段以 epoch days (`Long`) 存储；创建/修改时间使用 epoch millis。
4. **枚举存储**：状态、意向、周期在数据库中以 enum `name` 字符串存储，Repository 负责 Entity/Domain 转换。
5. **导航**：单 NavHost，当前路由为 `home`、`addEdit/{subscriptionId}`、`detail/{subscriptionId}`；通知与 Widget 使用 `subscriptiontracker://detail/{id}` 深链进入详情页。
6. **状态管理**：ViewModel 通过 StateFlow 暴露 UI 状态，Compose 使用 `collectAsState()` 订阅。
7. **图片处理**：自定义 Logo/壁纸通过系统图片选择器读取，并复制到应用内部 `files/images` 目录；数据库保存本地文件路径或 `preset:<id>`。
8. **Logo 预设与颜色**：预设库定义在 `AppLogoIcon.kt`，包含常见软件的品牌色和别名；Google 四色（蓝/红/黄/绿）引用自 `Color.kt` 的 `Primary`/`StatusBorderExpiring`/`StatusBorderRenewing`/`StatusBorderActive`，不在两个文件中重复定义。Logo 查找结果在 Composable 中通过 `remember(name, logoUri)` 缓存，避免每次重组都 O(n) 扫描预设列表。
9. **通知权限**：Manifest 声明 `POST_NOTIFICATIONS`，`MainActivity` 在 Android 13+ 首次启动时用 Activity Result API 请求运行时权限；用户拒绝时应用继续运行。
10. **数据库迁移**：维护两条迁移 — `MIGRATION_1_2`（重建 subscriptions 表以修正 `intention` 非空约束，同时新增 autoRenew/wallpaperUri，并创建 payment_history 表）和 `MIGRATION_2_3`（新增 startDate/modifiedAt 列）；未启用 destructive migration。旧数据迁移后的 `startDate` 默认值为 0，对应 1970-01-01。

## 数据模型

### Subscription
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键，自增 |
| name | String | 订阅名称 |
| amount | Double | 金额 |
| billingCycle | BillingCycle | MONTHLY / QUARTERLY / YEARLY / ONE_TIME |
| startDate | LocalDate | 开始日期，用于自动推算到期日和日均统计 |
| deadlineDate | LocalDate | 到期日期 |
| category | String | 分类 |
| status | SubscriptionStatus | ACTIVE / RENEWING / EXPIRING / PAUSED |
| autoRenew | Boolean | 是否自动续费 |
| intention | Intention | CONSIDERING / QUITTING / UNDECIDED |
| logoUri | String? | 预设图标 ID 或本地图片路径 |
| wallpaperUri | String? | 本地壁纸路径 |
| notes | String? | 备注 |
| createdAt | Long | 创建时间戳，毫秒 |
| modifiedAt | Long | 修改时间戳，毫秒 |
| sortOrder | Int | 自定义排序/置顶权重，负数表示置顶 |

### PaymentHistory
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键，自增 |
| subscriptionId | Long | 关联订阅 ID，级联删除 |
| paymentDate | Long | 扣费日期 epoch days |
| amount | Double | 扣费金额 |
| action | String | `RENEWED` / `CANCELLED` / `UNCERTAIN` |
| createdAt | Long | 记录创建时间戳，毫秒 |

## 订阅状态与意向

### 4 个主状态
| 状态 | 显示名 | 边框色 | 当前含义 |
|------|--------|--------|----------|
| ACTIVE | 生效中 | `#34A853` | 正常使用中，也会被到期前 7 天确认弹窗扫描 |
| RENEWING | 即将续订 | `#FBBC05` | 用户确认续订后，如果推进后的到期日仍在 7 天内，则保留即将续订 |
| EXPIRING | 即将到期 | `#EA4335` | 用户选择不续，或手动设置为即将到期 |
| PAUSED | 暂停/已失效 | `#94A3B8` | 用户手动停用/失效 |

### 3 个意向
| 意向 | 显示名 | 圆点色 | 说明 |
|------|--------|--------|------|
| CONSIDERING | 考虑续订 | `#D97706` | 有续订倾向 |
| QUITTING | 预计退订 | `#B91C1C` | 打算取消 |
| UNDECIDED | 暂不确定 | `#64748B` | 默认意向；如果已过期仍未处理，圆点加深为 `#374151` |

## 当前已实现功能
- 首页订阅列表，未筛选时支持 4 种排序方式切换（按状态 / 按到期日 / 按加入时间 / 自定义）。
- 统计卡片：UI 文案为“本月订阅支出”；当前实现排除 PAUSED，月付按原金额、季付按 1/3、年付按 1/12 折算，一次性仅在到期当月计入；日均为本月折算总额除以当月天数，已扣费来自本月 `RENEWED` 记录，待扣费来自本月内到期的 ACTIVE 订阅。
- 过去 12 周订阅活跃度热力图，基于创建和修改时间生成。
- 状态筛选：全部、生效中、已失效；首页额外展示即将续订/即将到期数量。
- 首页视觉：大标题“订阅”、胶囊分段（订阅/暂停）、快捷筛选与排序胶囊，整体参考小红书收集图中的软件订阅管理界面。
- 订阅卡片：品牌色柔和渐变背景、左侧大 Logo、续费进度条、金额/周期、下次付款日期、状态/自动续费 pill、右侧淡化品牌水印图形；急迫到期使用浅红底，PAUSED 使用灰色降级底色。
- 新增/编辑表单：名称、金额、周期、开始日期、到期日期、状态、自动续费、意向、图标、壁纸、备注；日期字段支持日历选择，也支持 `yyyy-MM-dd`、`yyyy/MM/dd`、`yyyyMMdd`、`yyyy年M月d日`、`M月d日` 输入。
- 到期日自动推算：开始日期 + 周期天数；用户手动修改后标记为已覆盖。选择 ONE_TIME 时周期天数为 0，到期日字段在 UI 中禁用。
- 预设 Logo 与自定义图片：预设库包含常见软件/服务（Spotify、Netflix、YouTube、Bilibili、爱奇艺、Apple Music、Apple Books、iCloud、Google One、Microsoft、ChatGPT、Claude、Notion、Figma、GitHub、Steam、京东 PLUS、淘宝、美团、微信读书等）以及通用分类图标；支持根据订阅名称自动推断品牌色与图标；自定义图片复制到内部存储确保持久化。
- 详情页：品牌色 Hero 卡片（有壁纸时显示壁纸并加遮罩，无壁纸时显示品牌色渐变和 Logo 水印）、摘要信息、展开编辑入口、历史扣费记录。
- 到期前 7 天扣费确认弹窗：1-7 条 ACTIVE 到期项会逐条询问续/不续/不确定，并写入扣费记录；选择续订会推进到期日，若新到期日超过 7 天则状态回到 ACTIVE，否则为 RENEWING；超过 7 条时当前不会弹窗或展示批量提示。
- 过期自动流转：非自动续费 + 已过期 + ACTIVE 状态的订阅，进入首页时自动标记为 EXPIRING，不写入扣费记录。
- 通知权限：Android 13+ 首次启动时自动弹出 `POST_NOTIFICATIONS` 权限请求。
- 长按卡片打开底部操作面板：置顶/取消置顶、删除。
- 删除确认弹窗；删除订阅时会清理当前 `logoUri`/`wallpaperUri` 指向的本地图片文件，并在 `files/images` 为空时删除目录。
- 每日 WorkManager 到期提醒：扫描明天到期且 ACTIVE 的订阅，发送系统通知并深链到详情页。
- 桌面小组件：展示 7 天内即将到期的 ACTIVE 订阅，最多 5 条，可点击进入详情页。

## 当前实现限制 / 待处理
- 扣费确认弹窗只在 `HomeViewModel` 初始化时扫描一次；从详情/编辑页返回首页不会重新触发，可通过 `LaunchedEffect` 监听导航返回事件改进。
- `autoRenew` 当前主要影响周期文案（每月/单月等），扣费确认逻辑尚未按自动续费与否分支。
- 替换自定义 Logo/壁纸时旧图片文件不一定被清理；壁纸清除按钮会删除当前壁纸文件，自定义 Logo 清除只清空表单状态。
- Widget 使用同步 `runBlocking` 读取数据库，适合当前 MVP，但复杂数据量下需要迁移到 RemoteViewsService 或更稳健的刷新机制。
- 当前没有测试目录，尚未建立单元测试或 UI 测试。

## 排序与置顶规则
- **首页排序选择**: `SortSelector` 已接入首页，用户可在 4 种排序间切换（按状态 / 按到期日 / 按加入时间 / 自定义）。该排序只作用于未筛选列表。
- **按状态排序**: ACTIVE → RENEWING → EXPIRING → PAUSED，同状态按到期日升序。
- **按到期日**: `deadlineDate ASC`。
- **按加入时间**: `createdAt DESC`。
- **自定义排序**: `sortOrder ASC, createdAt DESC`；长按卡片置顶把 `sortOrder` 设为当前最小值减一，取消置顶重置为 0。
- **状态筛选叠加**: 当选中 ACTIVE 或 PAUSED 筛选时，列表调用 `getByStatus()`，结果固定按到期日升序，不使用当前 SortSelector 选项。

## UI 设计关键字
Google 四色品牌感 · 小红书参考订阅管理界面 · 浅灰背景 `#F7F9FC` · 品牌色渐变订阅卡片 · 大 Logo + 淡化水印 · 胶囊筛选/排序 · 24dp 大圆角 · Material3 · 轻量 Apple/Google 混合风格

## 版本记录
- **v1.0 MVP** (2026-05): 首页卡片列表 CRUD、状态颜色、多排序枚举、基础 PRD/CLAUDE 文档。
- **v2.0 当前代码口径** (2026-05): 4 主状态 + 3 意向、统计卡片、筛选、扣费确认、详情页、扣费记录、图片 Logo/壁纸、热力图、通知 Worker、桌面 Widget、深链详情。
- **v2.1 UI/Logo 优化** (2026-05): 常见软件预设 Logo 库、名称自动匹配品牌色、Google 风格 Launcher 图标、首页品牌色订阅卡片、详情页品牌 Hero、胶囊筛选/排序视觉升级。
