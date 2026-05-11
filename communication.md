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

### 4. 仍未完成的事项（需用户介入）
- **运行时 UI 验证**: 需要在模拟器或真机上安装 APK 手动测试 Codex 建议的 5 条 smoke test 流程
- **自动化测试**: 项目尚无 `src/test` 或 `src/androidTest` 目录，需要从零搭建测试框架
- **后续产品修复**: re-trigger 确认弹窗、autoRenew 分支逻辑、图片生命周期清理（见 PRD §9）
