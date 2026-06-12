# 测试报告（v14）

## 概述

本轮围绕 `assistant.finance` 收支记录应用模块补齐/确认 JUnit Jupiter 单元测试。测试依据为 `detail_v14.md` 的行为契约，并对照 `code_v14.md` 确认实现报告未声明设计偏差。

本轮测试覆盖查询条件、只读视图、内存仓储、统计服务和应用服务，重点验证公开接口行为，不依赖实现细节。

## 测试文件

| 文件路径 | 覆盖对象 | 说明 |
|---------|----------|------|
| `java-ai-assistant/src/test/java/assistant/finance/TransactionQueryTest.java` | `TransactionQuery` | 覆盖无条件查询、类型/类别/日期范围筛选、组合匹配、通配条件、过滤标志和非法输入。 |
| `java-ai-assistant/src/test/java/assistant/finance/TransactionViewTest.java` | `TransactionView` | 覆盖实体字段投影、构造校验、文本规范化和视图与实体脱钩。 |
| `java-ai-assistant/src/test/java/assistant/finance/InMemoryTransactionRepositoryTest.java` | `InMemoryTransactionRepository` | 覆盖 CRUD、插入顺序、组合筛选、不可修改列表、保存/读取快照隔离和空参数拒绝。 |
| `java-ai-assistant/src/test/java/assistant/finance/FinanceStatisticsServiceTest.java` | `FinanceStatisticsService` | 覆盖空集合、收入支出累计、负结余、金额精度、单侧类型记录和空参数/空元素拒绝。 |
| `java-ai-assistant/src/test/java/assistant/finance/FinanceServiceTest.java` | `FinanceService` | 覆盖创建、查看、列表、筛选、修改、删除、统计、错误映射、状态不变和只读返回值。 |

## 覆盖明细

### TransactionQuery

- `allQueryMatchesEveryTransaction()`：验证无过滤条件匹配收入和支出记录。
- `typeQueryMatchesOnlySameType()`：验证类型筛选只命中相同 `TransactionType`。
- `categoryQueryNormalizesAndMatchesExactCategory()`：验证查询类别执行 `strip()`，大小写不折叠并精确匹配。
- `dateRangeQueryUsesInclusiveBounds()`：验证日期范围左右闭，开始日和结束日均命中。
- `combinedQueryRequiresEveryProvidedFilterToMatch()`：验证类型、类别和日期范围必须同时满足。
- `ofAllowsNullComponentsAsWildcards()`：验证 `of(...)` 的空组件作为通配条件。
- `exposesFilterPresenceFlags()`：验证三个 `has*Filter()` 方法与字段状态一致。
- `singleCriterionFactoriesRejectNullCriterion()`：验证单条件工厂拒绝空条件。
- `rejectsBlankCategoryFilter()`：验证空字符串、普通空白和 Unicode 空白类别被拒绝。
- `matchesRejectsNullRecord()`：验证空记录匹配请求抛出 `NullPointerException`。

### TransactionView

- `fromProjectsTransactionRecordFields()`：验证 `from(...)` 投影编号、类型、金额、类别、日期和备注。
- `fromRejectsNullRecord()`：验证空实体被拒绝。
- `constructorRejectsNullRequiredFields()`：验证编号、类型、金额、类别、日期、备注均为必填字段。
- `constructorNormalizesCategoryAndNote()`：验证类别和备注执行 `strip()`。
- `constructorRejectsBlankCategory()`：验证空白类别被拒绝。
- `viewIsDetachedFromLaterRecordMutation()`：验证实体后续修改不影响已创建视图。

### InMemoryTransactionRepository

- `saveAndFindByIdReturnsDetachedSnapshot()`：验证保存后按编号读取返回不同实体实例且字段一致。
- `saveCopiesInputRecordSoLaterCallerMutationsDoNotAffectRepository()`：验证保存时复制输入实体。
- `findByIdReturnsEmptyWhenRecordDoesNotExist()`：验证不存在编号返回空 `Optional`。
- `saveReplacesRecordWithSameIdAndKeepsInsertionOrder()`：验证同编号替换不改变首次插入顺序。
- `findAllReturnsRecordsInInsertionOrder()`：验证全量读取保持插入顺序。
- `findAllReturnsUnmodifiableDetachedSnapshotList()`：验证全量列表不可修改，且返回实体为快照。
- `findByFiltersByTypeCategoryAndDateRange()`：验证按类型、类别和日期范围分别筛选。
- `findByAppliesCombinedQueryInInsertionOrder()`：验证组合查询返回插入顺序中的匹配子序列。
- `findByReturnsUnmodifiableDetachedSnapshotList()`：验证查询列表不可修改，且返回实体为快照。
- `mutatingRecordReturnedFromFindByIdDoesNotAffectStoredState()`：验证单条读取快照隔离。
- `mutatingRecordReturnedFromFindAllDoesNotAffectStoredState()`：验证全量读取快照隔离。
- `mutatingRecordReturnedFromFindByDoesNotAffectStoredState()`：验证筛选读取快照隔离。
- `deleteByIdRemovesExistingRecord()`：验证删除存在记录后不可再读取。
- `deleteByIdReturnsFalseWhenRecordDoesNotExist()`：验证删除不存在编号返回 `false`。
- `methodsRejectNullArguments()`：验证 `save`、`findById`、`findBy`、`deleteById` 拒绝空参数。

### FinanceStatisticsService

- `calculateReturnsZeroForEmptyRecords()`：验证空集合返回零统计。
- `calculateAccumulatesIncomeAndExpenseSeparately()`：验证收入和支出分别累计。
- `calculateAllowsNegativeBalanceWhenExpenseExceedsIncome()`：验证支出大于收入时允许负结余。
- `calculateKeepsTwoDecimalMoneyPrecision()`：验证金额累计保持两位小数语义。
- `calculateHandlesOnlyIncomeRecords()`：验证只有收入记录时支出为零、结余为收入。
- `calculateHandlesOnlyExpenseRecords()`：验证只有支出记录时收入为零、结余为负支出。
- `calculateRejectsNullRecordList()`：验证空记录列表引用被拒绝。
- `calculateRejectsNullRecordElement()`：验证集合中空记录元素被拒绝。

### FinanceService

- `constructorRejectsNullDependencies()`：验证构造器拒绝空仓储、编号生成器和统计服务。
- `recordIncomeCreatesRecordAndReturnsView()`：验证创建收入成功，编号可预测，返回视图并保存。
- `recordExpenseCreatesRecordAndReturnsView()`：验证创建支出成功并返回支出视图。
- `recordTransactionNormalizesCategoryAndNote()`：验证创建路径类别和备注规范化。
- `recordTransactionRejectsInvalidAmountCategoryAndDateAndKeepsRepositoryUnchanged()`：验证非法金额、空白类别、空日期、空金额文本返回 `VALIDATION_ERROR` 且不保存。
- `getTransactionReturnsViewForExistingRecord()`：验证查看存在记录返回 `TransactionView`。
- `getTransactionReturnsNotFoundForMissingRecord()`：验证查看不存在编号返回 `NOT_FOUND`。
- `getTransactionRejectsNullId()`：验证查看空编号返回 `VALIDATION_ERROR`。
- `listTransactionsReturnsUnmodifiableViewsInInsertionOrder()`：验证全量列表保持插入顺序且不可修改。
- `listTransactionsWithQueryFiltersByTypeCategoryDateRangeAndCombination()`：验证服务层按类型、类别、日期范围和组合条件筛选。
- `listTransactionsRejectsNullQuery()`：验证空查询返回 `VALIDATION_ERROR`。
- `updateTransactionChangesEditableFieldsAndReturnsView()`：验证修改成功更新可编辑字段且编号不变。
- `updateTransactionRejectsNullId()`：验证修改空编号返回 `VALIDATION_ERROR`。
- `updateTransactionRejectsMissingId()`：验证修改不存在编号返回 `NOT_FOUND`。
- `updateTransactionRejectsNullTypeAndKeepsRecordUnchanged()`：验证空类型返回 `VALIDATION_ERROR` 且原记录不变。
- `updateTransactionRejectsInvalidInputAndKeepsRecordUnchanged()`：验证非法金额、空白类别、空日期返回 `VALIDATION_ERROR` 且原记录不变。
- `deleteTransactionRemovesExistingRecord()`：验证删除存在记录成功，后续查询返回 `NOT_FOUND`。
- `deleteTransactionRejectsNullId()`：验证删除空编号返回 `VALIDATION_ERROR`。
- `deleteTransactionReturnsNotFoundForMissingRecord()`：验证删除不存在编号返回 `NOT_FOUND`。
- `calculateStatisticsReturnsTotalsForAllCurrentRecords()`：验证全量统计基于当前仓储状态。
- `calculateStatisticsWithQueryReturnsTotalsForMatchingRecords()`：验证按查询条件统计匹配记录。
- `calculateStatisticsRejectsNullQuery()`：验证空查询统计返回 `VALIDATION_ERROR` 且不读取仓储。
- `statisticsReflectCreateUpdateAndDeleteChanges()`：验证新增、修改、删除后统计即时反映当前状态。
- `serviceNeverReturnsMutableTransactionRecordReferences()`：验证服务只返回只读视图和不可修改列表，调用方无法持有可变实体引用。

## 执行说明

按照 verifier 指令，本环节只负责编写/确认测试，不负责运行测试；因此未在本轮执行 Maven 测试命令。`code_v14.md` 中记录编码阶段已执行 `mvn test` 且通过，但该结果不作为本测试编写环节的重新执行结果。

## 结论

`detail_v14.md` 要求的五个测试文件均已存在，并覆盖本轮公开行为契约的正常路径、边界条件、错误路径和状态交互。本轮未修改生产源码。
