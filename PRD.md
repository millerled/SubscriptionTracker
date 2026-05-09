# SubscriptionTracker — 产品需求文档 (PRD)

## 1. 产品概述

### 1.1 产品定位
SubscriptionTracker 是一款**订阅日期提醒器 + 生活 Deadline 追踪工具**，帮助用户管理各类周期性订阅服务和一次性付费事项，清晰掌握每笔支出的到期时间、续费周期和订阅状态。

### 1.2 目标用户
- 订阅了大量数字服务（视频、音乐、工具、云存储等）的用户
- 需要追踪金钱支出周期的个人
- 对生活各类 Deadline 有管理需求的人

### 1.3 核心价值
- **终结意外扣费**：到期前主动提醒，避免忘记取消而自动续费
- **支出可视化**：统计卡片展示本月支出、日均、已扣/待扣比例
- **决策辅助**：通过意向标记帮助用户管理"该不该续"的纠结

---

## 2. 功能范围

### 2.1 v2.0 (当前版本)
- 首页卡片列表，薄渐变边框标状态
- 完整 CRUD 操作（创建/查看/编辑/删除）
- 4 种订阅状态 + 3 种意向
- 统计卡片（本月支出/日均/已扣费/待扣费比例条）
- 状态筛选器 Chips
- 扣费确认弹窗（到期前 7 天触发）
- 二级详情页（壁纸区 + 信息摘要 + 历史扣费记录）
- 编辑页完整表单（名称/金额/周期/日期/状态/意向/自动续费开关）
- 3 种排序 + 按状态筛选

### 2.2 后续迭代
| 优先级 | 功能 | 说明 |
|--------|------|------|
| P0 | 系统通知提醒 | 到期前 N 天推送通知 |
| P1 | 拖拽自定义排序 | 长按拖动调整卡片顺序 |
| P1 | 数据统计增强 | 月度/年度趋势图表、分类占比 |
| P2 | 数据导出 | CSV/PDF 导出 |
| P2 | 深色模式 | 跟随系统或手动切换 |
| P2 | 预设 App Logo | 音乐/影视/游戏/AI/工具/云存储/学习/生活 图标库 |
| P2 | 自定义壁纸 | 详情页壁纸上传与切换 |

---

## 3. 订阅状态定义

### 3.1 4 个主状态（卡片边框薄渐变颜色）

| 状态 | 英文标识 | 边框色 | 含义 | 触发条件 |
|------|----------|--------|------|---------|
| 生效中 | ACTIVE | `#2ECC71` 绿 | 正常使用中 | 距到期 > 7 天 |
| 即将续订 | RENEWING | `#E67E22` 橙红 | 自动续费，用户确认续 | 距到期 ≤ 7 天 + 自动续费 + 用户选"是" |
| 即将到期 | EXPIRING | `#FF6B6B` 红 | 不续/一次性到期 | 距到期 ≤ 7 天 + 非自动续费，或用户选"否" |
| 暂停/已失效 | PAUSED | `#94A3B8` 灰 | 停用 | 用户手动设置 |

### 3.2 状态自动流转
- 生效中 + 距到期 ≤ 7 天 → 触发扣费确认弹窗
  - 选"是" → 即将续订，扣费日更新为下一周期
  - 选"否" → 即将到期
  - 选"不确定" → 保持生效中，扣费日后意向圆点自动变深
- 非自动续费 + 到期过后 + 无操作 → 即将到期
- 即将到期状态 → 用户可进入详情页重新设定到期日，回到生效中

### 3.3 3 个意向（标题旁小圆点，深色）

| 意向 | 英文标识 | 圆点色 | 说明 |
|------|----------|--------|------|
| 考虑续订 | CONSIDERING | `#D97706` 深琥珀 | 用户有意续订 |
| 预计退订 | QUITTING | `#B91C1C` 深酒红 | 用户打算退订 |
| 暂不确定 | UNDECIDED | `#64748B` 石板灰 | 默认意向 |

**圆点变深逻辑**：扣费日过后且意向仍为"暂不确定" → 圆点色加深至 `#374151` 以示过期未处理。

---

## 4. 用户流程

### 4.1 主要流程
```
                         ┌──────────────┐
                         │   启动 App    │
                         └──────┬───────┘
                                │
                    ┌───────────▼───────────┐
                    │   首页（卡片列表）      │
                    │   - 统计卡片           │
                    │   - 状态筛选 Chips     │
                    │   - FAB 新增按钮       │
                    │   - 扣费确认弹窗       │
                    └──┬──────┬──────┬──────┘
                       │      │      │
            点击 FAB   │      │ 点击卡片     │ 长按卡片
                       │      │      │      │
              ┌────────▼┐    ┌▼──────▼─┐   ┌▼──────────┐
              │新增订阅页│    │ 详情页   │   │删除确认弹窗│
              │- 填写表单│    │- 壁纸区  │   │- 确认/取消│
              │- 保存    │    │- 信息摘要│   └──────────┘
              └───┬─────┘    │- 编辑展开│
                  │          │- 历史记录│
                  │          └────┬─────┘
                  │               │ 编辑按钮
                  │          ┌────▼─────┐
                  │          │编辑订阅页│
                  │          │- 修改表单│
                  │          │- 保存    │
                  │          └────┬─────┘
                  └───────────────┘
                         保存成功
```

### 4.2 扣费确认流程
```
启动 App → 首页加载 → 检测到期≤7天且状态为"生效中"的订阅
  ├─ 0 条: 不弹窗
  ├─ 1-7 条: 逐条弹窗确认（续/不续/不确定）
  └─ >7 条: 提示"您有 N 条订阅待处理，请到对应卡片中手动确认"
```

---

## 5. 数据模型

### 5.1 订阅 (Subscription)

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 自动 | 主键 |
| name | String | 是 | 订阅名称 |
| amount | Double | 是 | 金额 |
| billingCycle | BillingCycle | 是 | 续费周期 |
| deadlineDate | LocalDate | 是 | 到期日期 |
| category | String | 是 | 分类 |
| status | SubscriptionStatus | 是 | 订阅状态 |
| autoRenew | Boolean | 是 | 是否自动续费 |
| intention | Intention | 是 | 意向（默认 UNDECIDED） |
| logoUri | String? | 否 | 品牌 Logo 路径 |
| wallpaperUri | String? | 否 | 壁纸路径 |
| notes | String? | 否 | 备注 |
| createdAt | Long | 自动 | 创建时间戳 |
| sortOrder | Int | 自动 | 自定义排序权重 |

### 5.2 扣费记录 (PaymentHistory)

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 自动 | 主键 |
| subscriptionId | Long | 是 | 关联订阅 FK |
| paymentDate | Long | 是 | 扣费日期 (epoch days) |
| amount | Double | 是 | 金额 |
| action | String | 是 | RENEWED / CANCELLED / UNCERTAIN |
| createdAt | Long | 自动 | 记录时间戳 |

### 5.3 枚举定义

**SubscriptionStatus**: ACTIVE(生效中) / RENEWING(即将续订) / EXPIRING(即将到期) / PAUSED(暂停/已失效)

**Intention**: CONSIDERING(考虑续订) / QUITTING(预计退订) / UNDECIDED(暂不确定)

**BillingCycle**: MONTHLY(月付) / QUARTERLY(季付) / YEARLY(年付) / ONE_TIME(一次性)

**Category（预设）**: 娱乐 / 工具 / 健康 / 学习 / 生活 / 其他

---

## 6. UI 设计规范

### 6.1 视觉风格
- **设计语言**: 现代极简卡片风格
- **布局**: 卡片化 LazyColumn 列表
- **圆角**: 卡片 20dp，Logo 12dp，Chip 30dp，按钮 14dp
- **效果**: 薄渐变边框 (1.5dp) + 白色背景 + 微阴影
- **色系**: 浅灰底 `#F5F7FA`，白色卡片，深色文字

### 6.2 调色板
| 用途 | 色值 | 说明 |
|------|------|------|
| PageBackground | `#F5F7FA` | 页面浅灰背景 |
| CardWhite | `#FFFFFF` | 卡片白色 |
| TextMain | `#1A2C3E` | 主文字 |
| TextSecondary | `#6C7A89` | 辅助文字 |
| TextMuted | `#94A3B8` | 弱化文字 |
| Primary | `#1C4E80` | 主题深蓝 |
| StatusBorderActive | `#2ECC71` | 生效中边框绿 |
| StatusBorderRenewing | `#E67E22` | 即将续订边框橙 |
| StatusBorderExpiring | `#FF6B6B` | 即将到期边框红 |
| StatusBorderPaused | `#94A3B8` | 已失效边框灰 |
| IntentionConsidering | `#D97706` | 考虑续订圆点 |
| IntentionQuitting | `#B91C1C` | 预计退订圆点 |
| IntentionUndecided | `#64748B` | 暂不确定圆点 |
| IntentionUndecidedStale | `#374151` | 过期未处理圆点 |
| PaidGreen | `#2ECC71` | 已扣费绿 |
| PendingOrange | `#E67E22` | 待扣费橙 |

### 6.3 首页布局
```
┌─────────────────────────────────┐
│  SubscriptionTracker      ⚙     │ ← TopAppBar
├─────────────────────────────────┤
│  ┌─────────────────────────┐    │
│  │ 本月订阅支出             │    │ ← 统计卡片 (白底+阴影+20dp圆角)
│  │ ¥ 1,175.4              │    │   主金额 36sp Bold
│  │ 日均支出 ¥39.18         │    │
│  │ █████░░░░░              │    │ ← 已扣/待扣比例条
│  │ 已扣 ¥57.6  待扣 ¥1117.8│    │
│  └─────────────────────────┘    │
│                                  │
│  生效中 18  已失效 5             │ ← Filter Chips
│                                  │
│  ┌───────────────────────────┐  │
│  │ ┌──┐ Spotify     ●  ¥15  │  │ ← 订阅行 (薄渐变边框+白底)
│  │ │ S│ 3天后续订  · 每月   │  │   左: Logo 40dp 首字母
│  │ └──┘                     │  │   中: 名称+意向圆点
│  └───────────────────────────┘  │   右: 金额
│                                  │   底: 剩余天数 + 周期
│  ┌───────────────────────────┐  │
│  │ ┌──┐ Cursor      ●  ¥150 │  │
│  │ │ C│ 今日续订   · 每月   │  │
│  │ └──┘                     │  │
│  └───────────────────────────┘  │
│                                  │
│            [+] FAB               │
└─────────────────────────────────┘
```

### 6.4 详情页布局
```
┌─────────────────────────────────┐
│  ← 返回           订阅名称      │ ← TopAppBar
├─────────────────────────────────┤
│  ┌─────────────────────────┐    │
│  │     壁纸区 (160dp)      │    │ ← 状态色半透明背景
│  └─────────────────────────┘    │
│                                  │
│  名称 · ¥金额 / 周期             │ ← 信息摘要
│  状态 · 到期日                  │
│  ──────────                      │
│  展开编辑 ▼                      │ ← 点击展开编辑按钮
│  ┌─────────────────────────┐    │
│  │  [编辑订阅信息]          │    │
│  └─────────────────────────┘    │
│                                  │
│  历史扣费记录                    │
│  2026-05-01  ¥15  已续           │
│  2026-04-01  ¥15  已续           │
└─────────────────────────────────┘
```

---

## 7. 技术规格

### 7.1 技术栈
- **语言**: Kotlin 2.0.21
- **UI 框架**: Jetpack Compose (Material3, BOM 2024.12.01)
- **数据库**: Room 2.6.1 (本地持久化)
- **架构**: MVVM + Repository Pattern
- **异步**: Kotlin Coroutines 1.9.0 + Flow
- **导航**: Navigation Compose 2.8.5
- **构建**: Gradle 8.11.1 + AGP 8.7.3
- **最低 API**: 26 (Android 8.0)

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

### 7.3 项目结构
```
app/src/main/java/com/subscriptiontracker/
├── MainActivity.kt
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt
│   │   ├── dao/SubscriptionDao.kt / PaymentHistoryDao.kt
│   │   ├── entity/SubscriptionEntity.kt / PaymentHistoryEntity.kt
│   │   └── converter/Converters.kt
│   └── repository/SubscriptionRepository.kt
├── domain/model/
│   ├── Subscription.kt / SubscriptionStatus.kt
│   ├── BillingCycle.kt / Intention.kt / SortOption.kt
├── ui/
│   ├── theme/     (Color / Type / Shape / Theme)
│   ├── home/      (HomeScreen / HomeViewModel / SubscriptionCard / StatsCard / FilterChips / PaymentConfirmationDialog)
│   ├── addedit/   (AddEditScreen / AddEditViewModel)
│   ├── detail/    (DetailScreen / DetailViewModel)
│   └── components/ (FrostedGlassCard / AppLogoIcon)
└── util/ (DateFormatter / CurrencyFormatter)
```

---

## 8. 非功能性需求

- 应用启动速度 < 1 秒
- 列表滚动 60fps 无卡顿
- 所有数据仅存储在本地，无需网络权限
- 支持 Android 8.0 及以上系统
- Room 数据库使用 destructive migration（开发阶段）

---

## 9. 版本记录

- **v1.0 MVP** (2026-05): 首页卡片 CRUD + 5 状态颜色 + 多排序 + Apple 极简风格
- **v2.0** (2026-05): 全新 UI 重构 — 4 状态薄边框 + 3 意向圆点 + 统计卡片 + 筛选器 + 扣费确认弹窗 + 详情页 + 扣费记录
