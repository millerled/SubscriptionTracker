# CLAUDE.md

## 产品信息
- **产品名称**: SubscriptionTracker
- **类型**: 原生 Android 应用
- **定位**: 订阅日期提醒器 + 生活 Deadline 追踪工具
- **当前版本**: v1.0 MVP
- **平台**: Android 8.0+ (API 26+)

## 技术栈
- **语言**: Kotlin 2.0.21
- **UI**: Jetpack Compose + Material3 (BOM 2024.12.01)
- **数据库**: Room 2.6.1 (本地离线存储)
- **架构**: MVVM + Repository Pattern
- **异步**: Kotlin Coroutines 1.9.0 + Flow
- **导航**: Navigation Compose 2.8.5
- **构建**: Gradle 8.11.1 + AGP 8.7.3

## 项目结构
```
app/src/main/java/com/subscriptiontracker/
├── MainActivity.kt               # 单 Activity 入口，NavGraph 定义
├── SubscriptionTrackerApp.kt     # Application，数据库单例持有
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt        # Room 数据库定义（单例）
│   │   ├── dao/SubscriptionDao.kt
│   │   ├── entity/SubscriptionEntity.kt
│   │   └── converter/Converters.kt
│   └── repository/SubscriptionRepository.kt
├── domain/model/
│   ├── Subscription.kt           # 领域模型
│   ├── SubscriptionStatus.kt     # 状态枚举
│   ├── BillingCycle.kt           # 周期枚举
│   └── SortOption.kt             # 排序选项枚举
├── ui/
│   ├── theme/                    # Color / Type / Shape / Theme
│   ├── home/                     # HomeScreen + HomeViewModel + SubscriptionCard + SortSelector
│   ├── addedit/                  # AddEditScreen + AddEditViewModel
│   └── components/               # FrostedGlassCard / StatusBadge / ConfirmDeleteDialog
└── util/
    ├── DateFormatter.kt          # LocalDate 扩展函数
    └── CurrencyFormatter.kt      # 金额格式化
```

## 核心设计决策
1. **无依赖注入框架**：MVP 阶段不使用 Hilt/Dagger，通过 Application 类持有数据库单例，ViewModel 通过 `AndroidViewModel` 获取 Application Context
2. **LocalDate 存储**：Room 中以 epoch days (Long) 存储，通过 TypeConverter 转换
3. **枚举存储**：以枚举 name 字符串存储，领域模型使用枚举类型
4. **导航**：单 NavHost，两个路由：`home` / `addEdit/{subscriptionId}`
5. **状态管理**：ViewModels 通过 StateFlow 暴露状态，Compose 通过 `collectAsState()` 订阅

## 5 种订阅状态 + 颜色
| 状态 | 卡片底色 | 标签颜色 | 首页展现 |
|------|---------|---------|---------|
| ACTIVE | 白色 | 绿色 | 正常显示 |
| ACTIVE + 即将到期 | 浅红 #FFF5F5 | 红色 #FF3B30 | 倒计时标红 |
| CONSIDERING | 橙色 #FFF3E0 | 橙色 #FF9500 | 橙色卡片 |
| PLANNED | 蓝色 #E3F2FD | 蓝色 #007AFF | 蓝色卡片 |
| PAUSED | 灰色 #F5F5F5 | 灰色 #C7C7CC | 文字透明度 60% 视觉降级 |
| UNDECIDED | 黄色 #FFFDE7 | 黄色 #FFCC00 | 黄色卡片 |

## 即将到期逻辑
- 仅 ACTIVE 状态触发
- 到期前 7 天进入预警
- 卡片背景变浅红，倒计时文本变红加粗，标签显示"即将到期"

## 排序规则
- **按状态**: ACTIVE → CONSIDERING → PLANNED → UNDECIDED → PAUSED，同状态按到期日升序
- **按到期日**: 最近到期在前
- **按加入时间**: 最近添加在前
- **自定义**: 按 sortOrder 升序（供后续拖拽排序）

## UI 设计关键字
Apple 极简主义 · 大圆角 (20dp) · 毛玻璃模糊 · 浅色系 · 卡片化布局 · Material3

## 版本记录
- **v1.0 MVP** (2026-05): 首页卡片列表 CRUD + 状态颜色 + 多排序 + PRD + CLAUDE.md
