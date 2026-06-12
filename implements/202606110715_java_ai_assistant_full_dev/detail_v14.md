# 详细设计（v14）

## 概述

本轮设计目标是在既有 `assistant.finance` 领域基础类型之上，补齐收支记录模块的查询、只读视图、仓储、应用服务和统计服务，使收支记录具备创建、查看、列表、组合筛选、修改、删除和即时统计能力。

本轮实现范围：

- `TransactionQuery`：表达按收支类型、规范化类别和左右闭日期范围组合筛选。
- `TransactionView`：从 `TransactionRecord` 投影只读 DTO，避免服务层向调用方暴露可变实体。
- `TransactionRepository` 与 `InMemoryTransactionRepository`：提供 CRUD 和组合查询，并对保存与读取均执行实体快照隔离。
- `FinanceStatisticsService`：从记录集合计算收入总额、支出总额和结余。
- `FinanceService`：对外提供收支记录创建、读取、筛选、修改、删除和统计入口，统一将非法输入映射为 `OperationResult.failure(ErrorCode.VALIDATION_ERROR, ...)`。
- 对上述类型新增 JUnit Jupiter 单元测试。

本轮不修改 v13 已有 `TransactionType`、`TransactionRecord`、`FinanceStatistics` 的公开接口。

## 文件规划

| 文件路径 | 操作 | 职责 |
|---------|------|------|
| `java-ai-assistant/src/main/java/assistant/finance/TransactionQuery.java` | 新建 | 定义收支记录组合查询条件和匹配逻辑。 |
| `java-ai-assistant/src/main/java/assistant/finance/TransactionView.java` | 新建 | 定义收支记录只读视图，从实体投影字段。 |
| `java-ai-assistant/src/main/java/assistant/finance/TransactionRepository.java` | 新建 | 定义收支记录仓储接口。 |
| `java-ai-assistant/src/main/java/assistant/finance/InMemoryTransactionRepository.java` | 新建 | 基于 `LinkedHashMap` 的内存仓储，保持插入顺序并做快照隔离。 |
| `java-ai-assistant/src/main/java/assistant/finance/FinanceStatisticsService.java` | 新建 | 基于记录集合计算收支统计。 |
| `java-ai-assistant/src/main/java/assistant/finance/FinanceService.java` | 新建 | 提供收支记录应用服务 API，封装金额转换、实体创建/更新和错误映射。 |
| `java-ai-assistant/src/test/java/assistant/finance/TransactionQueryTest.java` | 新建 | 覆盖查询条件、类别规范化、组合匹配和空参数拒绝。 |
| `java-ai-assistant/src/test/java/assistant/finance/TransactionViewTest.java` | 新建 | 覆盖视图投影、字段校验和与实体脱钩。 |
| `java-ai-assistant/src/test/java/assistant/finance/InMemoryTransactionRepositoryTest.java` | 新建 | 覆盖 CRUD、插入顺序、组合筛选、空参数和快照隔离。 |
| `java-ai-assistant/src/test/java/assistant/finance/FinanceStatisticsServiceTest.java` | 新建 | 覆盖空集合、收入支出累计、负结余、精度和空参数。 |
| `java-ai-assistant/src/test/java/assistant/finance/FinanceServiceTest.java` | 新建 | 覆盖服务创建、查询、修改、删除、统计、错误映射和失败后状态不变。 |

## 类型定义

### `TransactionQuery`

**形态**：`record`

**包路径**：`assistant.finance`

**职责**：表达收支记录查询条件，按 `TransactionType`、规范化类别和 `DateRange` 对 `TransactionRecord` 做组合匹配；任一条件为空表示该维度不过滤，全部为空表示匹配所有记录。

**类型签名定义**：`public record TransactionQuery(TransactionType type, String category, DateRange dateRange)`

**字段定义**：

| 字段签名 | 约束 |
|----------|------|
| `TransactionType type` | 可为 `null`；非空时记录类型必须完全相等。 |
| `String category` | 可为 `null`；非空时构造器保存 `strip()` 后文本，规范化后不得为空。 |
| `DateRange dateRange` | 可为 `null`；非空时使用 `DateRange.contains(record.getDate())`，左右闭筛选。 |

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public TransactionQuery` 紧凑构造器 | `TransactionQuery` | 当 `category != null` 时执行 `strip()`；规范化后为空抛出 `IllegalArgumentException("category must not be blank")`。 |
| `public static TransactionQuery all()` | `TransactionQuery` | 返回无过滤条件查询。 |
| `public static TransactionQuery byType(TransactionType type)` | `TransactionQuery` | `type == null` 抛出 `NullPointerException("type")`；返回仅按类型过滤查询。 |
| `public static TransactionQuery byCategory(String category)` | `TransactionQuery` | `category == null` 抛出 `NullPointerException("category")`；返回仅按规范化类别过滤查询。 |
| `public static TransactionQuery byDateRange(DateRange dateRange)` | `TransactionQuery` | `dateRange == null` 抛出 `NullPointerException("dateRange")`；返回仅按日期范围过滤查询。 |
| `public static TransactionQuery of(TransactionType type, String category, DateRange dateRange)` | `TransactionQuery` | 允许任一组件为 `null`，非空类别按构造器规范化。 |
| `public boolean hasTypeFilter()` | `boolean` | `type != null` 时返回 `true`。 |
| `public boolean hasCategoryFilter()` | `boolean` | `category != null` 时返回 `true`。 |
| `public boolean hasDateRangeFilter()` | `boolean` | `dateRange != null` 时返回 `true`。 |
| `public boolean matches(TransactionRecord record)` | `boolean` | `record == null` 抛出 `NullPointerException("record")`；所有非空过滤条件均匹配时返回 `true`。 |

**构造方式**：

- 外部优先使用静态工厂表达意图。
- `of(null, null, null)` 与 `all()` 等价。

**类型关系**：

- 依赖 `assistant.common.DateRange`。
- 被 `TransactionRepository.findBy(...)` 和 `FinanceService.listTransactions(TransactionQuery)` 使用。

### `TransactionView`

**形态**：`record`

**包路径**：`assistant.finance`

**职责**：作为服务层返回给调用方的只读 DTO，投影收支记录编号、类型、金额、类别、日期和备注，防止调用方持有可变 `TransactionRecord` 引用。

**类型签名定义**：`public record TransactionView(EntityId id, TransactionType type, TransactionAmount amount, String category, LocalDate date, String note)`

**字段定义**：

| 字段签名 | 约束 |
|----------|------|
| `EntityId id` | 非空。 |
| `TransactionType type` | 非空。 |
| `TransactionAmount amount` | 非空。 |
| `String category` | 非空；规范化后不得为空。 |
| `LocalDate date` | 非空。 |
| `String note` | 非空；允许空字符串。 |

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public TransactionView` 紧凑构造器 | `TransactionView` | 校验字段非空；类别执行 `strip()` 并拒绝空白；备注执行 `strip()`，但不允许 `null`。 |
| `public static TransactionView from(TransactionRecord record)` | `TransactionView` | `record == null` 抛出 `NullPointerException("record")`；从记录 getter 投影并构造视图。 |

**构造方式**：

- 服务层只通过 `TransactionView.from(record)` 将实体转换为视图。
- 测试可直接调用 record 构造器验证视图不变量。

**类型关系**：

- 依赖 `assistant.common.EntityId`、`assistant.common.TransactionAmount`、`java.time.LocalDate`。
- `FinanceService` 所有成功查询和写操作返回 `TransactionView` 或 `List<TransactionView>`，不得返回 `TransactionRecord`。

### `TransactionRepository`

**形态**：`interface`

**包路径**：`assistant.finance`

**职责**：定义收支记录持久化抽象，为服务层提供按编号、全量和组合查询能力。

**类型签名定义**：`public interface TransactionRepository`

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `void save(TransactionRecord record)` | `void` | 保存或替换同编号记录；实现必须拒绝 `null`。 |
| `Optional<TransactionRecord> findById(EntityId id)` | `Optional<TransactionRecord>` | `id == null` 时实现应抛出 `NullPointerException("id")`；未找到返回 `Optional.empty()`。 |
| `List<TransactionRecord> findAll()` | `List<TransactionRecord>` | 返回当前记录快照，保持插入顺序；返回列表不可修改。 |
| `List<TransactionRecord> findBy(TransactionQuery query)` | `List<TransactionRecord>` | `query == null` 时实现应抛出 `NullPointerException("query")`；返回匹配记录快照，保持插入顺序；返回列表不可修改。 |
| `boolean deleteById(EntityId id)` | `boolean` | `id == null` 时实现应抛出 `NullPointerException("id")`；删除存在记录返回 `true`，不存在返回 `false`。 |

**构造方式**：

- 接口无构造器。

**类型关系**：

- 被 `FinanceService` 依赖。
- 本轮唯一实现为 `InMemoryTransactionRepository`。

### `InMemoryTransactionRepository`

**形态**：`final class`

**包路径**：`assistant.finance`

**职责**：提供基于内存 `LinkedHashMap<EntityId, TransactionRecord>` 的收支记录仓储，并确保调用方无法通过保存后仍持有的实体或仓储读取返回值修改内部状态。

**类型签名定义**：`public final class InMemoryTransactionRepository implements TransactionRepository`

**字段定义**：

| 字段签名 | 约束 |
|----------|------|
| `private final Map<EntityId, TransactionRecord> records = new LinkedHashMap<>();` | key 为记录编号；value 始终保存实体副本。 |

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public void save(TransactionRecord record)` | `void` | 拒绝 `null`；保存 `copyOf(record)`；同编号替换值但保持 `LinkedHashMap` 已有 key 的原插入位置。 |
| `public Optional<TransactionRecord> findById(EntityId id)` | `Optional<TransactionRecord>` | 拒绝 `null`；找到时返回 `copyOf(stored)`。 |
| `public List<TransactionRecord> findAll()` | `List<TransactionRecord>` | 对 `records.values()` 逐条 `copyOf` 后返回 `stream().toList()` 结果。 |
| `public List<TransactionRecord> findBy(TransactionQuery query)` | `List<TransactionRecord>` | 拒绝 `null`；按 `query.matches(stored)` 过滤，逐条 `copyOf` 后返回不可修改列表。 |
| `public boolean deleteById(EntityId id)` | `boolean` | 拒绝 `null`；从 map 删除并返回是否存在。 |

**私有辅助方法设计**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `private static TransactionRecord copyOf(TransactionRecord source)` | `TransactionRecord` | `source == null` 抛出 `NullPointerException("source")`；通过 `new TransactionRecord(source.getId(), source.getType(), source.getAmount(), source.getCategory(), source.getDate(), source.getNote())` 重建副本。 |

**构造方式**：

- 使用默认公开无参构造器。

**类型关系**：

- 依赖 `assistant.common.EntityId`、`java.util.LinkedHashMap`、`java.util.Map`、`java.util.Optional`、`java.util.List`、`java.util.Objects`。
- 快照隔离与 `InMemoryStudyPlanRepository` 风格一致。

### `FinanceStatisticsService`

**形态**：`final class`

**包路径**：`assistant.finance`

**职责**：从收支记录集合计算不可变 `FinanceStatistics`，集中覆盖金额累计、收入支出分类、空集合和负结余语义。

**类型签名定义**：`public final class FinanceStatisticsService`

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public FinanceStatistics calculate(List<TransactionRecord> records)` | `FinanceStatistics` | `records == null` 抛出 `NullPointerException("records")`；集合为空返回 `FinanceStatistics.zero()`；任一元素为 `null` 抛出 `NullPointerException("record")`；按类型累计收入与支出并返回 `FinanceStatistics.of(totalIncome, totalExpense)`。 |

**私有辅助方法设计**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `private static MoneyValue amountAsMoney(TransactionRecord record)` | `MoneyValue` | 使用 `MoneyValue.from(record.getAmount())` 转换单笔金额。 |

**构造方式**：

- 使用默认公开无参构造器。

**类型关系**：

- 依赖 `assistant.common.MoneyValue`。
- 统计过程中只能使用 `MoneyValue.zero()`、`MoneyValue.from(TransactionAmount)`、`MoneyValue.add(...)` 和 `FinanceStatistics.of(...)`；禁止使用 `double` 或 `Double`。

### `FinanceService`

**形态**：`final class`

**包路径**：`assistant.finance`

**职责**：作为收支记录应用服务，对外提供创建收入、创建支出、查看、列表、组合筛选、修改、删除和统计入口；将值对象/实体异常收束为稳定 `OperationResult`。

**类型签名定义**：`public final class FinanceService`

**字段定义**：

| 字段签名 | 约束 |
|----------|------|
| `private final TransactionRepository repository;` | 构造时非空。 |
| `private final IdGenerator idGenerator;` | 构造时非空；创建记录时生成编号。 |
| `private final FinanceStatisticsService statisticsService;` | 构造时非空；统计时委托计算。 |

**构造器**：

| 方法签名 | 契约 |
|----------|------|
| `public FinanceService(TransactionRepository repository, IdGenerator idGenerator, FinanceStatisticsService statisticsService)` | 任一依赖为空抛出 `NullPointerException`，参数名分别为 `repository`、`idGenerator`、`statisticsService`。 |

**公开创建接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public OperationResult<TransactionView> recordIncome(String amountText, String category, LocalDate date, String note)` | `OperationResult<TransactionView>` | 等价于以 `TransactionType.INCOME` 创建记录；金额文本由服务内部调用 `TransactionAmount.of(amountText)` 转换；非法金额、空类别、空日期统一返回 `VALIDATION_ERROR`；成功后保存记录并返回视图。 |
| `public OperationResult<TransactionView> recordExpense(String amountText, String category, LocalDate date, String note)` | `OperationResult<TransactionView>` | 等价于以 `TransactionType.EXPENSE` 创建记录；错误映射与收入一致。 |

**公开查询接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public OperationResult<TransactionView> getTransaction(EntityId id)` | `OperationResult<TransactionView>` | `id == null` 返回 `VALIDATION_ERROR`；不存在返回 `NOT_FOUND`；存在时返回 `TransactionView`。 |
| `public OperationResult<List<TransactionView>> listTransactions()` | `OperationResult<List<TransactionView>>` | 返回当前全部记录视图，保持插入顺序；成功 payload 不可修改。 |
| `public OperationResult<List<TransactionView>> listTransactions(TransactionQuery query)` | `OperationResult<List<TransactionView>>` | `query == null` 返回 `VALIDATION_ERROR`；否则返回匹配视图，保持插入顺序；成功 payload 不可修改。 |

**公开修改/删除接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public OperationResult<TransactionView> updateTransaction(EntityId id, TransactionType type, String amountText, String category, LocalDate date, String note)` | `OperationResult<TransactionView>` | `id == null` 返回 `VALIDATION_ERROR`；`type == null` 返回 `VALIDATION_ERROR`，不得向调用方泄漏 `NullPointerException` 或 `IllegalArgumentException`；不存在编号返回 `NOT_FOUND`；金额、类别、日期非法返回 `VALIDATION_ERROR`；成功时调用实体 `updateDetails(...)` 后保存并返回视图；任一非法输入失败后仓储状态不变。 |
| `public OperationResult<Void> deleteTransaction(EntityId id)` | `OperationResult<Void>` | `id == null` 返回 `VALIDATION_ERROR`；不存在返回 `NOT_FOUND`；存在时删除并返回 `OperationResult.success()`。 |

**公开统计接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public OperationResult<FinanceStatistics> calculateStatistics()` | `OperationResult<FinanceStatistics>` | 基于 `repository.findAll()` 计算当前全量统计；不修改仓储。 |
| `public OperationResult<FinanceStatistics> calculateStatistics(TransactionQuery query)` | `OperationResult<FinanceStatistics>` | `query == null` 返回 `VALIDATION_ERROR`；否则基于 `repository.findBy(query)` 计算统计；不修改仓储。 |

**私有辅助方法设计**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `private OperationResult<TransactionView> recordTransaction(TransactionType type, String amountText, String category, LocalDate date, String note)` | `OperationResult<TransactionView>` | 捕获创建路径中的 `NullPointerException`、`IllegalArgumentException` 和 `NumberFormatException`，映射为 `VALIDATION_ERROR`；成功时保存。 |
| `private TransactionAmount toAmount(String amountText)` | `TransactionAmount` | 调用 `TransactionAmount.of(amountText)`；允许其抛出运行时异常供调用方捕获。 |
| `private static TransactionView toView(TransactionRecord record)` | `TransactionView` | 调用 `TransactionView.from(record)`。 |
| `private static List<TransactionView> toUnmodifiableViews(List<TransactionRecord> records)` | `List<TransactionView>` | 对记录列表逐条投影视图并返回 `stream().toList()`。 |
| `private OperationResult<TransactionView> validationFailure(String message)` | `OperationResult<TransactionView>` | 返回 `OperationResult.failure(ErrorCode.VALIDATION_ERROR, stableMessage(message))`。 |
| `private OperationResult<List<TransactionView>> validationFailureList(String message)` | `OperationResult<List<TransactionView>>` | 返回列表类型校验失败。 |
| `private OperationResult<FinanceStatistics> validationFailureStatistics(String message)` | `OperationResult<FinanceStatistics>` | 返回统计类型校验失败。 |
| `private OperationResult<Void> validationFailureVoid(String message)` | `OperationResult<Void>` | 返回空 payload 校验失败。 |
| `private OperationResult<TransactionView> notFound(EntityId id)` | `OperationResult<TransactionView>` | 返回 `OperationResult.failure(ErrorCode.NOT_FOUND, "transaction not found: " + id.value())`。 |
| `private OperationResult<Void> notFoundVoid(EntityId id)` | `OperationResult<Void>` | 返回删除路径的未找到失败。 |
| `private static String stableMessage(String message)` | `String` | 当异常 message 为空或空白时返回 `"invalid transaction input"`，避免 `OperationResult.failure(...)` 因空消息二次抛异常。 |

**构造方式**：

- 生产/测试均显式注入 `TransactionRepository`、`IdGenerator`、`FinanceStatisticsService`。
- 本轮不引入 `TimeProvider`，因为收支创建和统计均使用调用方传入日期或已有记录日期。

**类型关系**：

- 依赖 `assistant.common.OperationResult`、`assistant.common.ErrorCode`、`assistant.common.EntityId`、`assistant.common.TransactionAmount`。
- 依赖 `assistant.testability.IdGenerator`。
- 只向调用方暴露 `TransactionView` 和 `FinanceStatistics`，不暴露 `TransactionRecord`。

## 错误处理

- `TransactionQuery`、`TransactionView`、`InMemoryTransactionRepository`、`FinanceStatisticsService` 作为领域/基础组件，沿用既有模块风格：空引用抛出 `NullPointerException`，语义非法抛出 `IllegalArgumentException`。
- `FinanceService` 作为应用服务，必须将调用方输入错误转换为 `OperationResult.failure(ErrorCode.VALIDATION_ERROR, ...)`。
- `TransactionAmount.of(amountText)` 可能抛出 `NullPointerException`、`NumberFormatException` 或 `IllegalArgumentException`；服务创建和修改路径均捕获并返回 `VALIDATION_ERROR`。
- `TransactionRecord.create(...)` 和 `TransactionRecord.updateDetails(...)` 可能因空类型、空金额、空类别、空日期或空白类别抛出异常；服务层捕获并返回 `VALIDATION_ERROR`。
- `updateTransaction(...)` 必须在读取并修改实体前显式检查 `type == null`，返回 `VALIDATION_ERROR`，确保未知或空收支类型不泄漏为运行时异常。
- `id == null` 在查看、修改和删除中返回 `VALIDATION_ERROR`。
- 查询和按条件统计的 `query == null` 返回 `VALIDATION_ERROR`。
- 未找到编号在查看、修改和删除中返回 `ErrorCode.NOT_FOUND`。
- 删除成功返回 `OperationResult.success()`，payload 为 `null`。
- 统计路径不捕获仓储内部编程错误；正常服务调用只处理用户输入导致的失败。

## 行为契约

### 查询契约

1. `TransactionQuery.all()` 匹配任意合法 `TransactionRecord`。
2. 类型筛选使用枚举引用相等判断。
3. 类别筛选使用记录中已规范化类别与查询中已规范化类别做精确相等判断；大小写不折叠。
4. 日期范围筛选使用 `DateRange.contains(record.getDate())`，开始日期和结束日期当天均命中。
5. 组合查询必须同时满足所有非空条件。
6. 构造查询时，开始日期晚于结束日期由 `DateRange` 自身拒绝，本类型不重复提供开始/结束日期构造器。

### 视图契约

1. `TransactionView.from(record)` 返回记录字段快照。
2. 视图为 record，不提供任何修改实体的方法。
3. 修改原始 `TransactionRecord` 后，既有 `TransactionView` 字段不变化。
4. 服务层成功返回的单条结果均为 `TransactionView`，列表结果均为不可修改 `List<TransactionView>`。

### 仓储契约

1. `save(record)` 保存实体副本；保存后调用方继续修改原实体，不影响仓储状态。
2. `findById(...)`、`findAll()`、`findBy(...)` 均返回实体副本；调用方修改返回实体，不影响仓储状态。
3. `findAll()` 与 `findBy(...)` 返回不可修改列表。
4. `LinkedHashMap` 保持首次插入顺序；同编号替换不会把记录移动到末尾。
5. `findBy(...)` 的返回顺序为仓储插入顺序中的匹配子序列。
6. `deleteById(...)` 删除后，后续 `findAll()`、`findBy(...)` 和统计均基于当前剩余记录。

### 统计契约

1. 空集合返回 `FinanceStatistics.zero()`。
2. `TransactionType.INCOME` 记录金额累计到收入总额。
3. `TransactionType.EXPENSE` 记录金额累计到支出总额。
4. 结余由 `FinanceStatistics.of(totalIncome, totalExpense)` 统一计算，收入小于支出时允许负结余。
5. 统计计算只能使用 `MoneyValue` 和 `BigDecimal` 语义，禁止使用 `double`。
6. 统计服务不修改传入记录，不保存任何缓存。

### 服务契约

1. `recordIncome(...)` 和 `recordExpense(...)` 成功时使用 `idGenerator.nextId()` 生成编号，创建记录，保存到仓储，并返回视图。
2. 创建路径中金额非法、类别为空白、日期为空、金额文本为空或不可解析时，返回 `VALIDATION_ERROR` 且不保存记录。
3. `getTransaction(id)` 只读查询仓储并返回视图，不修改仓储。
4. `listTransactions()` 和 `listTransactions(query)` 返回不可修改视图列表，调用方无法通过列表或视图修改内部实体。
5. `updateTransaction(...)` 成功时只修改目标记录的类型、金额、类别、日期和备注，不改变编号。
6. `updateTransaction(...)` 在 `id == null`、`type == null`、金额非法、类别空白或日期为空时返回 `VALIDATION_ERROR`，并保持原记录不变。
7. `updateTransaction(...)` 在编号不存在时返回 `NOT_FOUND`，不创建新记录。
8. `deleteTransaction(id)` 只删除存在记录；不存在返回 `NOT_FOUND`。
9. `calculateStatistics()` 和 `calculateStatistics(query)` 均按调用时仓储当前快照即时计算；新增、修改、删除后下一次统计必须反映最新状态。
10. `calculateStatistics(query)` 在 `query == null` 时返回 `VALIDATION_ERROR` 且不读取/修改仓储。

## 依赖关系

- 新增 `assistant.finance` 类型依赖既有 `assistant.common` 值对象和 `assistant.testability.IdGenerator`。
- `TransactionQuery` 依赖：
  - `assistant.common.DateRange`
  - `java.util.Objects`
- `TransactionView` 依赖：
  - `assistant.common.EntityId`
  - `assistant.common.TransactionAmount`
  - `java.time.LocalDate`
  - `java.util.Objects`
- `TransactionRepository` 依赖：
  - `assistant.common.EntityId`
  - `java.util.List`
  - `java.util.Optional`
- `InMemoryTransactionRepository` 依赖：
  - `java.util.LinkedHashMap`
  - `java.util.Map`
  - `java.util.List`
  - `java.util.Optional`
  - `java.util.Objects`
- `FinanceStatisticsService` 依赖：
  - `assistant.common.MoneyValue`
  - `java.util.List`
  - `java.util.Objects`
- `FinanceService` 依赖：
  - `assistant.common.EntityId`
  - `assistant.common.ErrorCode`
  - `assistant.common.OperationResult`
  - `assistant.common.TransactionAmount`
  - `assistant.testability.IdGenerator`
  - `java.time.LocalDate`
  - `java.util.List`
  - `java.util.Objects`

## 测试规格

### `TransactionQueryTest`

| 测试方法 | 覆盖点 |
|----------|--------|
| `allQueryMatchesEveryTransaction()` | 无条件查询匹配收入和支出记录。 |
| `typeQueryMatchesOnlySameType()` | 类型过滤分别匹配收入/支出。 |
| `categoryQueryNormalizesAndMatchesExactCategory()` | 查询类别执行 `strip()`，与记录规范化类别精确匹配。 |
| `dateRangeQueryUsesInclusiveBounds()` | 日期范围开始日、结束日命中，范围外不命中。 |
| `combinedQueryRequiresEveryProvidedFilterToMatch()` | 类型、类别、日期范围组合条件必须全部满足。 |
| `ofAllowsNullComponentsAsWildcards()` | `of(...)` 中空组件作为通配条件。 |
| `exposesFilterPresenceFlags()` | 三个 `has*Filter()` 与字段非空状态一致。 |
| `singleCriterionFactoriesRejectNullCriterion()` | 单条件工厂拒绝空条件。 |
| `rejectsBlankCategoryFilter()` | 空字符串、普通空白和 Unicode 空白类别抛出 `IllegalArgumentException`。 |
| `matchesRejectsNullRecord()` | `matches(null)` 抛出 `NullPointerException`。 |

### `TransactionViewTest`

| 测试方法 | 覆盖点 |
|----------|--------|
| `fromProjectsTransactionRecordFields()` | `from(...)` 投影编号、类型、金额、类别、日期和备注。 |
| `fromRejectsNullRecord()` | 空实体抛出 `NullPointerException`。 |
| `constructorRejectsNullRequiredFields()` | 空编号、类型、金额、类别、日期、备注均抛出 `NullPointerException`。 |
| `constructorNormalizesCategoryAndNote()` | 类别和备注执行 `strip()`。 |
| `constructorRejectsBlankCategory()` | 空白类别抛出 `IllegalArgumentException`。 |
| `viewIsDetachedFromLaterRecordMutation()` | 投影视图后修改实体，既有视图字段保持原值。 |

### `InMemoryTransactionRepositoryTest`

| 测试方法 | 覆盖点 |
|----------|--------|
| `saveAndFindByIdReturnsDetachedSnapshot()` | 保存后按编号读取返回不同实体实例且字段一致。 |
| `saveCopiesInputRecordSoLaterCallerMutationsDoNotAffectRepository()` | 保存后修改调用方持有实体不影响仓储。 |
| `findByIdReturnsEmptyWhenRecordDoesNotExist()` | 未找到返回空 `Optional`。 |
| `saveReplacesRecordWithSameIdAndKeepsInsertionOrder()` | 同编号替换且顺序不移动。 |
| `findAllReturnsRecordsInInsertionOrder()` | 全量读取保持插入顺序。 |
| `findAllReturnsUnmodifiableDetachedSnapshotList()` | 全量列表不可修改，修改返回实体不影响仓储。 |
| `findByFiltersByTypeCategoryAndDateRange()` | 分别覆盖类型、类别、日期范围筛选。 |
| `findByAppliesCombinedQueryInInsertionOrder()` | 组合查询结果为插入顺序匹配子序列。 |
| `findByReturnsUnmodifiableDetachedSnapshotList()` | 查询列表不可修改，修改返回实体不影响仓储。 |
| `mutatingRecordReturnedFromFindByIdDoesNotAffectStoredState()` | 单条读取快照隔离。 |
| `mutatingRecordReturnedFromFindAllDoesNotAffectStoredState()` | 全量读取快照隔离。 |
| `mutatingRecordReturnedFromFindByDoesNotAffectStoredState()` | 查询读取快照隔离。 |
| `deleteByIdRemovesExistingRecord()` | 删除存在记录成功且后续不可读取。 |
| `deleteByIdReturnsFalseWhenRecordDoesNotExist()` | 删除不存在编号返回 `false`。 |
| `methodsRejectNullArguments()` | `save`、`findById`、`findBy`、`deleteById` 拒绝空参数。 |

### `FinanceStatisticsServiceTest`

| 测试方法 | 覆盖点 |
|----------|--------|
| `calculateReturnsZeroForEmptyRecords()` | 空集合返回三个零值。 |
| `calculateAccumulatesIncomeAndExpenseSeparately()` | 多笔收入和支出分别累计。 |
| `calculateAllowsNegativeBalanceWhenExpenseExceedsIncome()` | 支出大于收入时结余为负。 |
| `calculateKeepsTwoDecimalMoneyPrecision()` | 金额累计保持两位小数语义。 |
| `calculateHandlesOnlyIncomeRecords()` | 只有收入时支出为零、结余为收入。 |
| `calculateHandlesOnlyExpenseRecords()` | 只有支出时收入为零、结余为负支出。 |
| `calculateRejectsNullRecordList()` | 空列表引用抛出 `NullPointerException`。 |
| `calculateRejectsNullRecordElement()` | 集合中空元素抛出 `NullPointerException`。 |

### `FinanceServiceTest`

| 测试方法 | 覆盖点 |
|----------|--------|
| `constructorRejectsNullDependencies()` | 构造器拒绝空仓储、编号生成器和统计服务。 |
| `recordIncomeCreatesRecordAndReturnsView()` | 创建收入成功，编号可预测，返回视图并保存。 |
| `recordExpenseCreatesRecordAndReturnsView()` | 创建支出成功，返回支出视图并保存。 |
| `recordTransactionNormalizesCategoryAndNote()` | 创建路径类别和备注规范化。 |
| `recordTransactionRejectsInvalidAmountCategoryAndDateAndKeepsRepositoryUnchanged()` | 非法金额、空白类别、空日期均返回 `VALIDATION_ERROR` 且不保存。 |
| `getTransactionReturnsViewForExistingRecord()` | 查看存在记录返回视图。 |
| `getTransactionReturnsNotFoundForMissingRecord()` | 查看不存在编号返回 `NOT_FOUND`。 |
| `getTransactionRejectsNullId()` | 查看空编号返回 `VALIDATION_ERROR`。 |
| `listTransactionsReturnsUnmodifiableViewsInInsertionOrder()` | 全量列表保持顺序且不可修改。 |
| `listTransactionsWithQueryFiltersByTypeCategoryDateRangeAndCombination()` | 服务组合筛选返回正确视图。 |
| `listTransactionsRejectsNullQuery()` | 空查询返回 `VALIDATION_ERROR`。 |
| `updateTransactionChangesEditableFieldsAndReturnsView()` | 修改成功更新类型、金额、类别、日期、备注且编号不变。 |
| `updateTransactionRejectsNullId()` | 修改空编号返回 `VALIDATION_ERROR`。 |
| `updateTransactionRejectsMissingId()` | 修改不存在编号返回 `NOT_FOUND`。 |
| `updateTransactionRejectsNullTypeAndKeepsRecordUnchanged()` | `type == null` 返回 `VALIDATION_ERROR` 且原记录不变。 |
| `updateTransactionRejectsInvalidInputAndKeepsRecordUnchanged()` | 金额非法、类别空白、日期为空返回 `VALIDATION_ERROR` 且原记录不变。 |
| `deleteTransactionRemovesExistingRecord()` | 删除存在记录成功，后续查询为 `NOT_FOUND`。 |
| `deleteTransactionRejectsNullId()` | 删除空编号返回 `VALIDATION_ERROR`。 |
| `deleteTransactionReturnsNotFoundForMissingRecord()` | 删除不存在编号返回 `NOT_FOUND`。 |
| `calculateStatisticsReturnsTotalsForAllCurrentRecords()` | 全量统计基于当前仓储状态。 |
| `calculateStatisticsWithQueryReturnsTotalsForMatchingRecords()` | 按查询条件统计匹配记录。 |
| `calculateStatisticsRejectsNullQuery()` | 空查询统计返回 `VALIDATION_ERROR`。 |
| `statisticsReflectCreateUpdateAndDeleteChanges()` | 新增、修改、删除后统计即时反映当前状态。 |
| `serviceNeverReturnsMutableTransactionRecordReferences()` | 通过服务结果无法绕过服务修改仓储内部记录。 |
