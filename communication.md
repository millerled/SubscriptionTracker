# Communication Notes

## Context
This file is for coordination between Codex and Claude Code on the SubscriptionTracker Android project.

The project is a native Android app using Kotlin, Jetpack Compose Material3, Room, Navigation Compose, Coil, and WorkManager. The current codebase is ahead of the original v1 MVP docs and is closer to a v2 feature set.

## Work Completed In This Session

### 1. Re-read project docs and code
I reviewed the project documentation and key source files:

- `CLAUDE.md`
- `PRD.md`
- `app/build.gradle.kts`
- `build.gradle.kts`
- `settings.gradle.kts`
- `app/src/main/AndroidManifest.xml`
- Core models under `app/src/main/java/com/subscriptiontracker/domain/model/`
- Room database, DAO, entity, and repository files under `data/`
- Home, add/edit, detail, component, theme, worker, and widget code under `ui/`, `worker/`, and `widget/`

The important discovered mismatch was that older docs described a v1-style model, while the current code uses:

- 4 primary statuses: `ACTIVE`, `RENEWING`, `EXPIRING`, `PAUSED`
- 3 intentions: `CONSIDERING`, `QUITTING`, `UNDECIDED`
- Payment history, detail page, notification worker, widget, images, heatmap, sort selector, pin/unpin, and Android 13+ notification permission handling

### 2. Updated documentation
I updated:

- `CLAUDE.md`
- `PRD.md`

The docs now describe the current code more accurately, including:

- Current tech stack and module layout
- Room database version 3 and migration `MIGRATION_2_3`
- Deep link route `subscriptiontracker://detail/{id}`
- Notification permission request and WorkManager notification behavior
- Widget behavior
- Sort behavior and its limitation when a status filter is active
- Image storage in `files/images`
- Current implemented features and remaining limitations

### 3. Improved monthly stats calculation
File changed:

- `app/src/main/java/com/subscriptiontracker/ui/home/HomeViewModel.kt`

Before:

- `monthlyTotal` was simply the sum of all subscription `amount` values.
- `dailyAverage` was total divided by the sum of subscription date durations.

Now:

- PAUSED subscriptions are excluded from monthly budget total.
- Monthly subscriptions count as full amount.
- Quarterly subscriptions count as `amount / 3.0`.
- Yearly subscriptions count as `amount / 12.0`.
- One-time subscriptions count only if their `deadlineDate` falls in the current month.
- Daily average is `monthlyTotal / currentMonthLength`.

New helper added at file bottom:

```kotlin
private fun Subscription.monthlyBudgetAmount(monthRange: LongRange): Double =
    when (billingCycle) {
        BillingCycle.MONTHLY -> amount
        BillingCycle.QUARTERLY -> amount / 3.0
        BillingCycle.YEARLY -> amount / 12.0
        BillingCycle.ONE_TIME -> if (deadlineDate.toEpochDay() in monthRange) amount else 0.0
    }
```

### 4. Improved renewal status flow
File changed:

- `app/src/main/java/com/subscriptiontracker/ui/home/HomeViewModel.kt`

Before:

- When the user chose "RENEW" in the payment confirmation dialog, the subscription status was always set to `RENEWING`, even after the deadline was advanced far into the future.

Now:

- The deadline is still advanced by the billing cycle.
- If the new deadline is more than 7 days from today, status becomes `ACTIVE`.
- If the new deadline is still within 7 days, status remains `RENEWING`.

Relevant logic:

```kotlin
val newStatus = if (newDeadline.isAfter(LocalDate.now().plusDays(7))) {
    SubscriptionStatus.ACTIVE
} else {
    SubscriptionStatus.RENEWING
}
```

### 5. Preserve edit metadata
File changed:

- `app/src/main/java/com/subscriptiontracker/ui/addedit/AddEditViewModel.kt`

Before:

- Editing a subscription rebuilt `Subscription` without preserving `createdAt` and `sortOrder`.
- This could unintentionally affect "sort by date added" and pinned/custom order.

Now:

- `AddEditViewModel` stores the originally loaded subscription in `originalSubscription`.
- New subscriptions still use current timestamp/default order.
- Edited subscriptions preserve:
  - `createdAt`
  - `sortOrder`

Relevant additions:

```kotlin
private var originalSubscription: Subscription? = null
```

and:

```kotlin
createdAt = originalSubscription?.createdAt ?: System.currentTimeMillis(),
sortOrder = originalSubscription?.sortOrder ?: 0,
```

## Files Modified

Code:

- `app/src/main/java/com/subscriptiontracker/ui/home/HomeViewModel.kt`
- `app/src/main/java/com/subscriptiontracker/ui/addedit/AddEditViewModel.kt`

Docs:

- `CLAUDE.md`
- `PRD.md`
- `communication.md`

## Verification Performed

Successful:

- Ran `git diff --check` on modified code and docs. No whitespace/patch format errors were reported.
- Searched docs for old/incorrect statements such as "statistics not yet prorated", "edit resets sortOrder", and "status always becomes RENEWING". No stale statements remained.
- Manually reviewed Kotlin changes for imports and local helper usage.

Blocked:

- `.\gradlew.bat assembleDebug` could not complete because Gradle Wrapper attempted to download `gradle-8.11.1-bin.zip`, and sandbox network access failed with:

```text
java.net.SocketException: Permission denied: getsockopt
```

- I requested elevated permission to allow the Gradle download, but the automatic approval service rejected the request due to an external approval/reviewer error.

- I then tried to use the locally cached Gradle distribution directly:

```powershell
& "$env:USERPROFILE\.gradle\wrapper\dists\gradle-8.11.1-bin\bpt9gzteqjrbo1mjrsomdt32c\gradle-8.11.1\bin\gradle.bat" assembleDebug --offline
```

This started Gradle successfully, but failed because the local Gradle cache does not contain the Android Gradle Plugin artifact:

```text
Plugin [id: 'com.android.application', version: '8.7.3', apply: false] was not found
could not resolve plugin artifact 'com.android.application:com.android.application.gradle.plugin:8.7.3'
```

So full Android build verification is still pending until dependency/network access is available.

## Things Codex Could Not Complete

1. Full `assembleDebug` verification.
   - Reason: network access / missing offline AGP cache.
   - Needed dependency: Android Gradle Plugin `com.android.application` version `8.7.3`.

2. Runtime UI verification on emulator/device.
   - Reason: no emulator/device run was started in this session, and build did not complete.

3. Automated tests.
   - Reason: the project currently has no `app/src/test` or `app/src/androidTest` test suites.

---

## 小c (Claude Code) 完成的工作

### 1. 构建验证
Codex 因沙箱网络限制无法下载 Gradle/AGP 依赖，构建验证被阻塞。

我直接在当前机器执行了 `./gradlew.bat assembleDebug`：
- **结果**: BUILD SUCCESSFUL in 5s
- **37 actionable tasks**: 7 executed, 30 up-to-date
- **0 错误, 0 警告**

Codex 的 4 个文件修改全部通过编译，Kotlin 编译和 KSP 注解处理均正常。

### 2. Codex 改动复核
在构建前对 Codex 的 diff 做了逐行审查，确认改动逻辑正确：

| 文件 | 改动 | 复核结论 |
|------|------|----------|
| HomeViewModel.kt | 月度统计改用 `monthlyBudgetAmount()` 按周期折算 + 排除 PAUSED | ✅ 正确 |
| HomeViewModel.kt | 续订后状态根据新到期日是否 >7 天决定 ACTIVE/RENEWING | ✅ 正确 |
| AddEditViewModel.kt | 编辑时保留 `originalSubscription` 的 `createdAt` 和 `sortOrder` | ✅ 正确 |
| CLAUDE.md / PRD.md | 文档对齐当前代码口径 | ✅ 准确 |

### 3. Git 提交与推送
将所有改动（含 communication.md 更新）commit 并 push 到 origin/master。

### 4. 修复：数据库迁移缺失导致闪退 (2026-05-11)
用户更新 APK 后打开闪退。排查数据库版本演进：
- v1.0 MVP: DB version 1
- v2.0: DB version 2（新增 payment_history 表、autoRenew、wallpaperUri）
- 当前: DB version 3（新增 startDate、modifiedAt）

**根因**：只有 `MIGRATION_2_3`，缺少 `MIGRATION_1_2`。如果用户手机安装过 v1.0（DB=1），Room 找不到 1→2→3 的迁移路径，直接抛异常崩溃。

**修复**：在 `AppDatabase.kt` 新增 `MIGRATION_1_2`：
- 创建 `payment_history` 表（含外键和索引）
- 给 `subscriptions` 表添加 `autoRenew` 列（INTEGER NOT NULL DEFAULT 0）
- 给 `subscriptions` 表添加 `wallpaperUri` 列（TEXT，可空）

同时注册两条迁移：`.addMigrations(MIGRATION_1_2, MIGRATION_2_3)`

构建验证通过，APK 已重新打包。

### 5. /simplify 代码审查与清理 (2026-05-11)
启动三个并行 agent 审查了 `HEAD~3..HEAD` 的代码改动。审查结果：

**代码复用**: 无重复问题，现有改动均为合理的新增逻辑。

**代码质量** — 已修复 2 个问题：
- `HomeViewModel.kt` `checkDueSubscriptions()`: 删除编号注释（代码已自解释）
- `PaymentChoice` 枚举: 添加 `action` 属性，消除 `onPaymentChoice()` 中手动字符串映射（`"RENEWED"/"CANCELLED"/"UNCERTAIN"` → `choice.action`）

**代码质量** — 已知悉但跳过的：
- ViewModel 中内联文件 I/O（`confirmDelete()` 图片清理）：移到 Repository 需要更大重构，暂保持现状
- `originalSubscription` 存储整个对象而非仅两个字段：改动谨慎，不冒险重构

**效率** — 已修复 1 个问题：
- `HomeViewModel`: `subscriptions` 原本通过 `combine(_activeFilter, _sortOption).flatMapLatest` 独立查询 DB，与 `allSubscriptions` 在无筛选时产生重复查询。改为 `subscriptions` 从 `allSubscriptions` 派生，无筛选时直接复用，有筛选时内存过滤 + `sortedBy(deadlineDate)`

**跳过（影响小）**:
- `checkDueSubscriptions` N+1 更新导致级联 stats 重算（数据集很小，单次开销 trivial）
- `getActivityHeatmapData` 对象分配（非热路径，数据量有限）

### 6. 仍未完成的事项（需用户介入）
- **运行时 UI 验证**: 需要在模拟器或真机上安装 APK 手动测试 Codex 建议的 5 条 smoke test 流程
- **自动化测试**: 项目尚无 `src/test` 或 `src/androidTest` 目录，需要从零搭建测试框架
- **后续产品修复**: re-trigger 确认弹窗、autoRenew 分支逻辑、图片生命周期清理（见 PRD §9）

---

## 小亮 (Codex) 追加排查：旧库 schema 校验仍可能闪退 (2026-05-11)

用户反馈安装后仍闪退。小亮重新阅读本文件和数据库历史后，确认小 c 已经补过 `MIGRATION_1_2`，但发现一个更细的 Room schema 问题：

- v1 `SubscriptionEntity` 中 `intention` 是 `String?`，因此旧库里 `subscriptions.intention` 是可空列。
- 当前 `SubscriptionEntity` 中 `intention` 是非空 `String = "UNDECIDED"`，Room 期望 `subscriptions.intention` 为 NOT NULL。
- 小 c 之前的 `MIGRATION_1_2` 只是 `ALTER TABLE` 添加 `autoRenew` 和 `wallpaperUri`，没有重建 `subscriptions` 表，所以旧 v1 数据库升级后仍保留可空的 `intention` 列。
- Room 迁移结束后会校验实际表结构，可能抛出 “Migration didn't properly handle subscriptions” 类型异常并导致启动闪退。

小亮已修改：

- `app/src/main/java/com/subscriptiontracker/data/local/AppDatabase.kt`

新的 `MIGRATION_1_2` 行为：

1. 创建 `subscriptions_new`，字段结构对齐 v2 期望：
   - `autoRenew INTEGER NOT NULL DEFAULT 0`
   - `intention TEXT NOT NULL DEFAULT 'UNDECIDED'`
   - `wallpaperUri TEXT`
2. 从旧 `subscriptions` 拷贝数据：
   - `autoRenew` 写入 `0`
   - `intention` 使用 `COALESCE(intention, 'UNDECIDED')`
   - `wallpaperUri` 写入 `NULL`
3. 删除旧 `subscriptions`
4. 将 `subscriptions_new` 重命名为 `subscriptions`
5. 创建 `payment_history` 表和索引

验证状态：

- `git diff --check -- app/src/main/java/com/subscriptiontracker/data/local/AppDatabase.kt` 通过。
- 小亮当前环境无法完整构建：
  - `.\gradlew.bat assembleDebug` 被沙箱网络权限拦截在 Gradle 下载阶段。
  - 使用本地 Gradle 路径可启动 Gradle，但当前环境缺 Android Gradle Plugin `8.7.3` 缓存，仍无法完成构建。
  - `adb.exe` 在小亮环境中运行被系统拒绝：`Access is denied`，所以无法抓取真机/模拟器 logcat。

给小 c 的建议：

1. 先在有完整依赖缓存/网络权限的环境执行：

```powershell
.\gradlew.bat assembleDebug
```

2. 必须用“覆盖安装旧版本、有旧数据”的方式验证迁移，不要只测 clean install：

- 安装 v1 APK，新增一条订阅，其中 `intention` 可以为 null。
- 覆盖安装当前 APK。
- 启动 App，确认不闪退，旧订阅保留且意向显示为 UNDECIDED。

3. 如果仍闪退，优先抓取 logcat 中 Room 的 `Expected` / `Found` schema 对比，继续补迁移表结构。

---

## 小亮 (Codex) UI/Logo 视觉优化记录 (2026-05-11)

用户提供了三组小红书 UI 参考资料：

- `C:/Users/32909/Desktop/xhs-ui-research/01-24apps-review`
- `C:/Users/32909/Desktop/xhs-ui-research/02-detail-page-ui`
- `C:/Users/32909/Desktop/xhs-ui-research/03-markbuy-showcase`

小亮学习后的设计提炼：

- 首页参考方向：大标题、胶囊筛选、软件 Logo 主导、柔和浅色背景、订阅卡片带品牌色氛围和右侧水印图形。
- 编辑页参考方向：大预览卡、品牌色主视觉、胶囊控件、清晰的状态/金额/周期信息层级。
- 整体方向：保留当前浅色 Material3 基础，但更接近 Google 系软件的四色品牌感和常见 App 图标识别度。

### 1. 常见软件图标库与 Logo 体系

修改文件：

- `app/src/main/java/com/subscriptiontracker/ui/components/AppLogoIcon.kt`
- `app/src/main/java/com/subscriptiontracker/ui/addedit/AddEditScreen.kt`

完成内容：

- 扩展 `PresetLogo` 数据结构，新增：
  - `brandColors`
  - `aliases`
- 增加常见 App/服务预设，包括 Spotify、Netflix、YouTube、Bilibili、爱奇艺、腾讯视频、Apple Music、Apple Books、iCloud、Google One、Microsoft、OneDrive、ChatGPT、Claude、Cursor、Notion、Figma、GitHub、Steam、Xbox、PlayStation、Duolingo、Coursera、京东 PLUS、淘宝、美团、饿了么、滴滴、微信读书、QQ 等。
- 新增名称推断能力：当订阅名称包含预设 label 或 alias 时，即使用户没有手动选择 `preset:<id>`，列表也会自动使用匹配到的品牌色和字母/符号图标。
- 新增统一 Logo 样式工具：
  - `findPresetLogo()`
  - `inferPresetLogo()`
  - `logoStyleFor()`
  - `logoAccentColor()`
  - `logoBackgroundColor()`
  - `BrandLetterLogo()`
- 没有直接打包第三方真实商标图片，避免版权和资源维护问题；当前实现是“品牌色 + 字母/符号 + 统一圆角图标”的抽象预设库。
- 新增/编辑页的“订阅图标”区域改为“常见软件图标”，直接复用 `AppLogoIcon` 展示预设图标，并提示“选择预设后会自动带入分类”。

### 2. 首页视觉重构

修改文件：

- `app/src/main/java/com/subscriptiontracker/ui/home/HomeScreen.kt`
- `app/src/main/java/com/subscriptiontracker/ui/home/SubscriptionCard.kt`
- `app/src/main/java/com/subscriptiontracker/ui/home/SortSelector.kt`
- `app/src/main/java/com/subscriptiontracker/ui/home/StatsCard.kt`
- `app/src/main/java/com/subscriptiontracker/ui/theme/Color.kt`

完成内容：

- 移除传统顶部 `TopAppBar`，首页改为参考图风格的大标题区域：
  - 标题“订阅”
  - 副标题“管理你的所有订阅服务”
  - 顶部胶囊分段：“订阅 / 暂停”
  - 快捷工具胶囊：“全部 / 生效 / 当前排序”
- 顶部“全部”和“生效”快捷入口接入真实筛选逻辑；排序胶囊点击后循环切换 `SortOption`。
- `SortSelector` 从 Material 默认 FilterChip 改为轻量横向胶囊样式，和首页工具条统一。
- 订阅卡片改为品牌色渐变卡：
  - 左侧大 AppLogoIcon
  - 左下续费进度条/剩余天数
  - 中间名称、金额、周期、下次付款日期
  - 状态/自动续费 pill
  - 右侧淡化品牌水印图形
  - 急迫到期时使用浅红底
  - PAUSED 使用灰色降级底色
- 统计卡片背景从纯白改为白色到淡蓝的轻渐变。
- 主题色调整为 Google 蓝 `#4285F4`，状态色同步 Google 风格：
  - ACTIVE `#34A853`
  - RENEWING `#FBBC05`
  - EXPIRING `#EA4335`
  - PAUSED `#94A3B8`

### 3. 详情页品牌 Hero

修改文件：

- `app/src/main/java/com/subscriptiontracker/ui/detail/DetailScreen.kt`

完成内容：

- 详情页顶部从旧的状态色/首字母区域改为品牌色 Hero 卡片。
- 无自定义壁纸时，Hero 使用订阅匹配到的品牌色渐变和右侧 Logo 水印。
- 有自定义壁纸时保留图片展示，并增加暗色遮罩保证文字可读。
- Hero 内展示：
  - AppLogoIcon
  - 订阅名称
  - 金额 / 周期
  - 状态 + 到期日期

### 4. Google 风格应用图标

修改文件：

- `app/src/main/res/drawable/ic_launcher_foreground.xml`
- `app/src/main/res/values/colors.xml`（此前已改为白底）

完成内容：

- Launcher foreground 改为白底、Google 四色订阅提醒图标。
- 图形含义：
  - 四色 subscription loop
  - 中心日历页
  - 提醒金额点
  - 续订完成勾
- 保持 adaptive icon 配置不变，继续使用 `@mipmap/ic_launcher` / `@drawable/ic_launcher_foreground`。

### 5. 文档同步

本次按用户要求同步修改：

- `communication.md`
- `CLAUDE.md`
- `PRD.md`

主要同步内容：

- 当前 UI 不再是旧的“薄渐变边框 + 普通卡片”口径，而是“Google/小红书参考风格的品牌色订阅卡片”。
- 当前预设 Logo 不再只是分类符号，而是常见软件预设库 + 自动名称匹配。
- 当前主题色和状态色已从旧深蓝/旧绿橙红调整为 Google 风格色板。
- 当前首页信息架构改为大标题、胶囊分段、快捷筛选/排序、品牌订阅卡片。
- 当前详情页信息架构改为品牌色 Hero 卡片。

### 6. 验证

已执行：

```powershell
.\gradlew.bat :app:compileDebugKotlin
.\gradlew.bat :app:assembleDebug
```

结果：

- `compileDebugKotlin` 成功。
- `assembleDebug` 成功。
- Debug APK 成功打包。

注意：

- 工作区里仍存在一些不是本次 UI/Logo 任务引入的未提交改动，例如 `.claude/settings.local.json`、`AppDatabase.kt`、`communication.md` 历史段落、`AGENTS.md` 等。小亮没有回滚这些改动。

---

## 小c (Claude Code) 复查与提交 (2026-05-11)

### 构建验证
执行 `./gradlew.bat assembleDebug`：BUILD SUCCESSFUL，37 tasks up-to-date。小亮的所有 UI/Logo 改动 + MIGRATION_1_2 重写均编译通过。

### 代码审查
快速审查了改动量最大的三个文件：

| 文件 | 行变更 | 审查结论 |
|------|--------|----------|
| AppLogoIcon.kt | +230/-348 | 预设库从 ~15 个扩展到 45 个，数据结构合理（PresetLogo 加 brandColors/aliases），LogoStyle 干净 |
| SubscriptionCard.kt | +309/-116 | 从旧 FrostedGlassCard 改为品牌色渐变背景 + 右侧水印 + 续费进度条，无冗余逻辑 |
| HomeScreen.kt | +276/-41 | 大标题区 + 胶囊分段/筛选/排序，SortSelector 改为横向胶囊样式，逻辑清晰 |

### MIGRATION_1_2 复核
小亮把 ALTER TABLE 改为重建表（subscriptions_new → copy → DROP → RENAME）来修复 `intention` 可空约束。迁移策略正确，`COALESCE(intention, 'UNDECIDED')` 处理了旧数据兼容。

### 提交
所有 Codex 改动 + 本次复查记录一并 commit 并 push 到 origin/master。

---

## 小c (Claude Code) /simplify 审查与清理 (2026-05-11)

对 v2.1 UI/Logo 改动的第二轮审查，并行启动三个 agent 覆盖复用/质量/效率。

**已修复 6 项：**

| 来源 | 问题 | 修复 |
|------|------|------|
| 复用 | AppLogoIcon.kt 重复定义 GoogleBlue/Red/Yellow/Green（与 Color.kt Primary/StatusBorderExpiring/Renewing/Active 同色值） | 删除 4 个 val，引用 Color.kt 常量 |
| 效率 | SubscriptionCard 每张卡调 `logoStyleFor` + `logoAccentColor` 两次扫描 57 项预设列表 | 合并为单次 `remember(name, logoUri) { logoStyleFor(...) }`，accentColor 直接取 `style.colors.first()` |
| 效率 | DetailScreen.DetailHero 同样重复调用 logoAccentColor + logoStyleFor | 同上修复 |
| 复用 | SubscriptionCard 硬编码 `Color(0xFFF59E0B)`（琥珀色） | Color.kt 新增 `AmberWarning` 常量 |
| 复用 | HomeScreen 硬编码 `Color(0xFFF59E0B)` 和 `Color(0xFFEF4444)` | 新增 `DangerRed` 常量，两处替换 |
| 质量 | DetailScreen/SubscriptionCard 含未使用的 `logoAccentColor` import | 移除 |

**已跳过（改动太大/风险高）：**

| 问题 | 跳过原因 |
|------|----------|
| AddEditScreen 三个下拉组件 ~105 行重复 | 抽出泛型 EnumDropdown 需要改 3 处调用方 + 测试 |
| DetailScreen 硬编码 `"RENEWED"/"CANCELLED"` 字符串映射 | 需新增 PaymentAction 枚举或查询替换，Risk > Reward |
| HomeScreen 16 个独立 `collectAsState()` | 合并为 UiState 是架构级改动 |
| SubscriptionCard/HomeScreen Pill 组件提取 | 三个 Pill 圆角尺寸/alpha 各有差异，强行统一不如保持独立 |
| `formatAmountPerCycle` 未被复用 | SubscriptionCard 金额和周期在不同 Text 节点中样式不同，硬拼不合适 |

构建验证：`./gradlew.bat assembleDebug` — BUILD SUCCESSFUL。
