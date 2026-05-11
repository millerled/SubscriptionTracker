# SubscriptionTracker — 产品需求文档 (PRD)

## 1. 产品概述

### 1.1 产品定位
SubscriptionTracker 是一款**订阅日期提醒器 + 生活 Deadline 追踪工具**，帮助用户管理各类周期性订阅服务和一次性付费事项，清晰掌握每笔支出的到期时间、续费周期、订阅状态和续订意向。

### 1.2 目标用户
- 订阅了大量数字服务（视频、音乐、AI 工具、云存储、学习平台等）的用户
- 需要追踪周期性扣费和一次性 Deadline 的个人
- 希望在续费前做决策、避免意外扣费的人

### 1.3 核心价值
- **避免意外扣费**：到期前通过首页弹窗、系统通知和桌面小组件提示用户处理。
- **支出可视化**：首页统计本月订阅支出、日均支出、已扣/待扣金额比例。
- **决策辅助**：用主状态 + 意向圆点表达“是否继续”的判断过程。
- **本地优先**：数据保存在本机 Room 数据库，无账号、无服务端依赖。

---

## 2. 当前版本范围

### 2.1 当前代码口径
- Android `versionName` 仍为 `1.0`，但功能已经接近 v2.0 设计。
- 当前实现以 **4 个主状态 + 3 个续订意向** 为核心模型。
- 代码中已包含详情页、扣费历史、通知 Worker、通知权限请求、桌面 Widget、图标/壁纸选择、热力图、排序和置顶能力。

### 2.2 已实现功能
- 首页卡片列表，未筛选时支持按状态、按到期日、按加入时间、自定义 4 种排序。
- 订阅 CRUD：新增、查看详情、编辑、删除。
- 首页统计卡片：展示“本月订阅支出”、日均支出、已扣费/待扣费比例条；本月支出会按周期折算（月付全额、季付 1/3、年付 1/12、一次性仅到期当月计入），并排除 PAUSED。
- 活跃度热力图：展示过去 12 周内创建/修改订阅的活跃情况。
- 状态筛选：全部、生效中、已失效；并展示即将续订/即将到期数量。
- 首页视觉：大标题、胶囊分段、快捷筛选/排序、品牌色订阅卡片，参考用户收集的小红书订阅类 App UI。
- 订阅卡片：常见软件 Logo/预设图标、品牌色渐变背景、右侧淡化品牌水印、续费进度条、意向圆点、金额、剩余天数、周期文案、下次付款日期、自动续费/状态标签。
- 扣费确认弹窗：启动首页后扫描 7 天内到期的 ACTIVE 订阅，逐条确认续/不续/不确定。
- 扣费记录：确认操作会写入 `payment_history`，详情页展示历史记录。
- 详情页：顶部品牌色 Hero 或自定义壁纸区域、信息摘要、展开编辑入口、历史扣费记录。
- 新增/编辑表单：名称、金额、周期、开始日期、到期日期、状态、意向、自动续费、图标、壁纸、备注；日期支持输入和系统 DatePicker。
- 到期日推算：默认按开始日期 + 周期天数计算，用户可手动覆盖；一次性周期会禁用到期日编辑。
- 图片能力：支持常见软件预设 Logo、名称自动匹配品牌色、自定义 Logo、自定义详情壁纸；自定义图片复制到应用内部目录。
- 长按卡片操作：置顶/取消置顶、删除。
- 系统通知：Android 13+ 启动时请求通知权限；每日 WorkManager 扫描明天到期的 ACTIVE 订阅并发通知。
- 桌面 Widget：展示未来 7 天内即将到期的 ACTIVE 订阅，最多 5 条。
- 深链：通知和 Widget 使用 `subscriptiontracker://detail/{id}` 打开详情页。

### 2.3 当前限制
- 扣费确认弹窗只在 `HomeViewModel` 初始化时检查一次；从详情/编辑页返回首页不会重新触发。
- `autoRenew` 当前主要影响周期文案，扣费确认逻辑尚未按自动续费与否区分处理。
- 选中状态筛选后，列表固定按到期日升序，不再使用 SortSelector 的当前排序。
- 替换图片时旧图片文件不一定被清理；删除订阅会清理当前字段指向的本地图片。
- Widget 使用简单 RemoteViews + `runBlocking` 读取数据库，适合当前阶段但仍偏 MVP。
- 当前没有自动化测试目录。

---

## 3. 订阅状态定义

### 3.1 4 个主状态
| 状态 | 英文标识 | 边框色 | 含义 | 当前触发/设置方式 |
|------|----------|--------|------|------------------|
| 生效中 | ACTIVE | `#34A853` 绿 | 正常使用中 | 新增默认值；用户也可手动选择 |
| 即将续订 | RENEWING | `#FBBC05` 黄 | 已确认续订但仍临近下一次到期 | 扣费确认选择“是，续了”后推进到期日；若新到期日仍在 7 天内则保留 RENEWING |
| 即将到期 | EXPIRING | `#EA4335` 红 | 用户不续或即将失效 | 扣费确认选择“否，没续”后写入；也可手动选择 |
| 暂停/已失效 | PAUSED | `#94A3B8` 灰 | 停用/失效 | 用户手动选择 |

### 3.2 3 个续订意向
| 意向 | 英文标识 | 圆点色 | 说明 |
|------|----------|--------|------|
| 考虑续订 | CONSIDERING | `#D97706` 深琥珀 | 用户倾向继续使用 |
| 预计退订 | QUITTING | `#B91C1C` 深酒红 | 用户倾向取消 |
| 暂不确定 | UNDECIDED | `#64748B` 石板灰 | 默认意向 |

**圆点变深逻辑**：到期日已过且意向仍为 UNDECIDED 时，首页卡片圆点使用 `#374151`，表示过期未处理。

### 3.3 当前到期确认逻辑
```
首页 ViewModel 初始化
  → 先将已过期、非自动续费、仍为 ACTIVE 的订阅标记为 EXPIRING
  → 查询未来 7 天内到期的订阅
  → 过滤 status == ACTIVE
  → 如果数量为 1-7 条，逐条弹出扣费确认
```

用户选择后的行为：
- **是，续了**：写入 `RENEWED` 扣费记录，到期日加上当前周期天数；若新到期日超过 7 天，状态回到 ACTIVE，否则状态为 RENEWING。
- **否，没续**：写入 `CANCELLED` 扣费记录，状态改为 EXPIRING。
- **不确定**：写入 `UNCERTAIN` 扣费记录，状态保持 ACTIVE。
- **稍后再说**：关闭当前确认队列，不写入记录。
- **超过 7 条待确认**：当前代码不弹窗，也没有批量提示。
- **过期自动流转**：不写入扣费记录，只更新状态为 EXPIRING。

---

## 4. 用户流程

### 4.1 主流程
```
启动 App
  ↓
首页
  ├─ 查看统计卡片、热力图、筛选器、订阅列表
  ├─ 点击 FAB → 新增订阅 → 保存后返回首页
  ├─ 点击卡片 → 详情页 → 展开编辑 → 编辑订阅 → 保存后返回
  ├─ 长按卡片 → 底部操作面板 → 置顶/取消置顶/删除
  └─ 命中 7 天内到期 ACTIVE 订阅 → 扣费确认弹窗
```

### 4.2 通知/Widget 流程
```
每日 WorkManager 扫描
  → 找到明天到期且 ACTIVE 的订阅
  → 发送系统通知
  → 点击通知 deep link 到详情页

桌面 Widget 更新
  → 查询未来 7 天到期且 ACTIVE 的订阅
  → 展示最多 5 条
  → 点击条目 deep link 到详情页
```

### 4.3 新增/编辑表单流程
```
填写名称、金额、周期、开始日期
  → 默认根据周期推算到期日期
  → 用户可手动覆盖到期日期
  → 日期可输入 2026-05-11 / 2026/05/11 / 20260511 / 2026年5月11日 / 5月11日
  → 选择状态、自动续费、续订意向
  → 选择常见软件预设图标或自定义 Logo
  → 可选择详情壁纸与备注
  → 保存到 Room
```

---

## 5. 数据模型

### 5.1 Subscription
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 自动 | 主键 |
| name | String | 是 | 订阅名称 |
| amount | Double | 是 | 金额 |
| billingCycle | BillingCycle | 是 | 续费周期 |
| startDate | LocalDate | 是 | 开始日期 |
| deadlineDate | LocalDate | 是 | 到期日期 |
| category | String | 是 | 分类 |
| status | SubscriptionStatus | 是 | 主状态 |
| autoRenew | Boolean | 是 | 是否自动续费 |
| intention | Intention | 是 | 续订意向，默认 UNDECIDED |
| logoUri | String? | 否 | `preset:<id>` 或本地图片路径 |
| wallpaperUri | String? | 否 | 本地壁纸路径 |
| notes | String? | 否 | 备注 |
| createdAt | Long | 自动 | 创建时间戳，毫秒 |
| modifiedAt | Long | 自动 | 修改时间戳，毫秒 |
| sortOrder | Int | 自动 | 自定义排序/置顶权重 |

### 5.2 PaymentHistory
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 自动 | 主键 |
| subscriptionId | Long | 是 | 关联订阅 FK，删除订阅时级联删除 |
| paymentDate | Long | 是 | 扣费日期 epoch days |
| amount | Double | 是 | 金额 |
| action | String | 是 | RENEWED / CANCELLED / UNCERTAIN |
| createdAt | Long | 自动 | 记录时间戳，毫秒 |

### 5.3 枚举定义
**SubscriptionStatus**: ACTIVE(生效中) / RENEWING(即将续订) / EXPIRING(即将到期) / PAUSED(暂停/已失效)

**Intention**: CONSIDERING(考虑续订) / QUITTING(预计退订) / UNDECIDED(暂不确定)

**BillingCycle**: MONTHLY(月付, 30 天) / QUARTERLY(季付, 91 天) / YEARLY(年付, 365 天) / ONE_TIME(一次性, 0 天)

**SortOption**: BY_STATUS(按状态) / BY_DEADLINE(按到期日) / BY_DATE_ADDED(按加入时间) / CUSTOM(自定义)

### 5.4 数据库
- Room 数据库名：`subscription_tracker_db`
- 当前版本：`3`
- 表：`subscriptions`、`payment_history`
- 迁移：
  - `MIGRATION_1_2`：重建 `subscriptions` 表以修正旧库 `intention` 可空问题，同时增加 `autoRenew`、`wallpaperUri` 列，并新建 `payment_history` 表（含外键级联删除）
  - `MIGRATION_2_3`：`subscriptions` 增加 `startDate`、`modifiedAt` 列
- 日期字段：`deadlineDate/startDate/paymentDate` 使用 epoch days
- 时间戳字段：`createdAt/modifiedAt` 使用 epoch millis
- 旧库从 v1 迁移到 v2 时，`autoRenew` 默认 0（false）；从 v2 迁移到 v3 时，`startDate` 默认 0（1970-01-01）

---

## 6. UI 设计规范

### 6.1 视觉风格
- **设计语言**：Google 四色品牌感 + 小红书参考订阅管理界面，保持轻量 Apple/Material3 基础。
- **布局**：浅灰页面背景 + 大标题区域 + 胶囊筛选/排序 + 品牌色订阅卡片 + LazyColumn。
- **圆角**：订阅卡片 24dp，Logo 12dp，胶囊控件 16-30dp，详情 Hero 26dp。
- **效果**：订阅卡片使用品牌色柔和渐变背景和右侧淡化水印图形；统计卡片使用白到淡蓝轻渐变。
- **文字**：深色主文字 + 灰色辅助文字，金额使用品牌强调色，状态/自动续费以 pill 标签展示。
- **Logo 策略**：不直接内置第三方真实商标图片；通过品牌色、字母/符号、别名匹配和统一圆角图标形成常见软件预设库。

### 6.2 调色板
| 用途 | 色值 | 说明 |
|------|------|------|
| PageBackground | `#F7F9FC` | 页面浅灰背景 |
| CardWhite | `#FFFFFF` | 卡片白色 |
| DividerColor | `#EDF2F7` | 分隔线 |
| TextMain | `#111827` | 主文字 |
| TextSecondary | `#6B7280` | 辅助文字 |
| TextMuted | `#94A3B8` | 弱化文字 |
| Primary | `#4285F4` | Google 风格主题蓝 |
| StatusBorderActive | `#34A853` | 生效中绿 |
| StatusBorderRenewing | `#FBBC05` | 即将续订黄 |
| StatusBorderExpiring | `#EA4335` | 即将到期红 |
| StatusBorderPaused | `#94A3B8` | 已失效边框灰 |
| IntentionConsidering | `#D97706` | 考虑续订圆点 |
| IntentionQuitting | `#B91C1C` | 预计退订圆点 |
| IntentionUndecided | `#64748B` | 暂不确定圆点 |
| IntentionUndecidedStale | `#374151` | 过期未处理圆点 |
| PaidGreen | `#34A853` | 已扣费绿 |
| PendingOrange | `#FBBC05` | 待扣费黄 |

### 6.3 首页信息架构
```
大标题区域
  - 标题：订阅
  - 副标题：管理你的所有订阅服务
  - 胶囊分段：订阅 / 暂停
  - 快捷工具：全部 / 生效 / 当前排序
  ↓
统计卡片
  - 本月订阅支出
  - 日均支出
  - 已扣费/待扣费比例条
  ↓
活跃度热力图（有数据时显示）
  ↓
快捷到期筛选
  - 即将续订
  - 即将到期
  - 全部
  ↓
排序胶囊
  - 按状态
  - 按到期日
  - 按加入时间
  - 自定义
  ↓
品牌订阅卡片列表
  - 左侧大 Logo
  - 续费进度条/剩余天数
  - 名称 + 意向圆点
  - 金额 + 周期
  - 下次付款日期
  - 状态/自动续费标签
  - 右侧淡化品牌水印
  ↓
FAB 新增订阅
```

### 6.4 详情页信息架构
```
TopAppBar: 返回 + 订阅名称
  ↓
品牌 Hero
  - 有壁纸时显示图片
  - 无壁纸时显示品牌色渐变和 Logo 水印
  - App Logo、名称、金额/周期、状态/到期日
  ↓
展开编辑
  - 编辑订阅信息按钮
  ↓
历史扣费记录
  - 日期、金额、动作
```

---

## 7. 技术规格

### 7.1 技术栈
- **语言**: Kotlin 2.0.21
- **UI 框架**: Jetpack Compose + Material3
- **数据库**: Room 2.6.1
- **架构**: MVVM + Repository Pattern
- **异步**: Kotlin Coroutines + Flow/StateFlow
- **导航**: Navigation Compose
- **图片加载**: Coil Compose
- **后台任务**: WorkManager
- **最低 API**: 26
- **目标 API**: 35

### 7.2 架构层级
```
UI Layer (Compose Screens + ViewModels)
    ↕ StateFlow
Domain Layer (Models, Enums)
    ↕
Data Layer (Repository, DAO, Entity)
    ↕
Room Database (SQLite)
```

### 7.3 关键实现
- `SubscriptionTrackerApp.onCreate()` 调度每日通知 Worker。
- `MainActivity` 请求 Android 13+ 通知权限，并处理 `subscriptiontracker://detail/{id}` 深链。
- `HomeViewModel` 聚合首页列表、排序、统计、筛选、确认弹窗、过期自动流转、热力图、删除和置顶状态。
- `AddEditViewModel` 负责表单状态、日期解析、到期日自动推算、保存校验。
- `DetailViewModel` 加载订阅详情并收集扣费历史 Flow。
- `SubscriptionRepository` 负责 DAO 封装、Entity/Domain 转换、扣费记录、置顶、热力图数据。

---

## 8. 非功能性需求

- 应用数据本地存储，无服务端依赖。
- 无网络权限要求。
- 支持 Android 8.0+。
- 首页列表滚动应保持流畅，卡片计算逻辑保持轻量。
- 通知和 Widget 应能 deep link 到对应详情页。
- 图片文件存储在应用内部目录，避免依赖外部 URI 长期授权。

---

## 9. 后续迭代

| 优先级 | 功能 | 说明 |
|--------|------|------|
| P1 | Widget 优化 | 支持更稳健的刷新、空态、样式和手动刷新 |
| P1 | 图片生命周期 | 替换 Logo/壁纸时清理旧文件，减少孤儿图片 |
| P1 | 拖拽自定义排序 | 在 CUSTOM 排序下支持长按拖动 |
| P1 | 测试覆盖 | 增加 Repository/ViewModel 单元测试和关键 UI 测试 |
| P2 | 数据统计增强 | 月度/年度趋势图表、分类占比 |
| P2 | 数据导出 | CSV/PDF 导出 |
| P2 | 深色模式 | 跟随系统或手动切换 |
| P2 | 预设 Logo 深化 | 当前为品牌色 + 字母/符号抽象预设；后续可在确认授权后引入统一矢量资产或用户本地图标包 |
| P2 | 扣费确认刷新 | 从详情/编辑页返回首页后重新扫描到期确认 |

---

## 10. 版本记录

- **v1.0 MVP** (2026-05): 首页卡片 CRUD、状态颜色、多排序枚举、基础本地 Room 存储。
- **v2.0 当前代码口径** (2026-05): 4 主状态 + 3 意向、统计卡片、筛选器、扣费确认弹窗、详情页、扣费历史、Logo/壁纸、热力图、通知 Worker、桌面 Widget、深链详情、置顶/删除操作面板。
- **v2.1 UI/Logo 优化** (2026-05): 常见软件预设 Logo 库、名称自动匹配品牌色、Google 风格 Launcher 图标、首页品牌色订阅卡片、详情页品牌 Hero、胶囊筛选/排序视觉升级。
