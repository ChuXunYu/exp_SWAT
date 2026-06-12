# 任务指令（v12）

## 动作
RETRY

## 任务描述
新增学习计划模块的查询条件、只读视图、仓储契约、内存仓储和应用服务，形成学习计划管理服务闭环。预期文件路径包括：

- `java-ai-assistant/src/main/java/assistant/study/StudyPlanQuery.java`
- `java-ai-assistant/src/main/java/assistant/study/StudyPlanView.java`
- `java-ai-assistant/src/main/java/assistant/study/StudyPlanRepository.java`
- `java-ai-assistant/src/main/java/assistant/study/InMemoryStudyPlanRepository.java`
- `java-ai-assistant/src/main/java/assistant/study/StudyPlanService.java`
- `java-ai-assistant/src/test/java/assistant/study/StudyPlanQueryTest.java`
- `java-ai-assistant/src/test/java/assistant/study/StudyPlanViewTest.java`
- `java-ai-assistant/src/test/java/assistant/study/InMemoryStudyPlanRepositoryTest.java`
- `java-ai-assistant/src/test/java/assistant/study/StudyPlanServiceTest.java`

本轮必须实现以下行为：

- `StudyPlanQuery` 表达可组合查询条件，至少支持按动态状态、计划周期重叠范围筛选；允许 `all()` 无条件查询，并提供清晰的 `has...Filter()` 与 `matches(...)` 行为。
- 学习计划动态状态筛选沿用 `schedule` 模块的显式时间上下文模式：`StudyPlanQuery.matches(...)` 必须显式接收 `StudyPlan plan`、`StudyPlanAnalysisService analysisService` 和 `LocalDate currentDate`（或等价顺序参数），不得在 query 内部直接调用 `LocalDate.now()`、`LocalDateTime.now()`、`System.currentTimeMillis()`，也不得复制状态推导逻辑。
- `StudyPlanView` 作为只读 DTO/record，包含 `EntityId id`、`String goalName`、`DateRange period`、`LocalDate startDate`、`LocalDate endDate`、`int expectedHours`、`Progress progress`、`StudyPlanStatus status`，并提供 `from(StudyPlan plan, StudyPlanAnalysisService analysisService, LocalDate currentDate)` 工厂方法。
- `StudyPlanRepository` 定义 `save`、`findById`、`findAll`、`findBy`、`deleteById`；其中 `findBy` 的职责必须与动态状态筛选契约对齐，显式接收 `StudyPlanQuery query`、`StudyPlanAnalysisService analysisService` 和 `LocalDate currentDate`（或等价上下文对象），仓储只负责基于调用方传入的上下文筛选，不得自行读取系统时间或复制状态推导逻辑。内存实现使用 `LinkedHashMap<EntityId, StudyPlan>` 保持插入顺序。
- 仓储层必须定义清晰的只读快照边界：`StudyPlanRepository.save(...)` 保存时不得把调用方传入的可变 `StudyPlan` 实体引用原样暴露为内部状态；`findById(...)`、`findAll()`、`findBy(...)` 返回值除了集合不可修改外，还必须保证调用方后续对返回 `StudyPlan` 实例执行 `updateDetails(...)` / `updateProgress(...)` 不会影响仓储内部状态。可通过复制构造、快照重建、内部 clone/memento 等等价策略实现，但不得把仓储内部实体直接泄露给外部调用方。
- `StudyPlanService` 注入 `StudyPlanRepository`、`IdGenerator`、`TimeProvider`、`StudyPlanAnalysisService`，实现创建、查看、列表、组合筛选、修改详情、更新进度、删除、统计已完成数量和未完成数量；服务层负责从 `timeProvider.today()` 取得当前日期，并将其传入 query / repository / view 所需的动态状态计算链路。
- `StudyPlanService` 只对“返回学习计划快照”的公开方法统一应用 `StudyPlanView` 规则：创建、查看、列表、组合筛选、修改详情、更新进度成功时，载荷必须返回 `StudyPlanView` 或不可修改的 `List<StudyPlanView>`，不得向调用方返回内部可变 `StudyPlan` 引用；删除固定返回 `OperationResult<Void>`，统计固定返回 `OperationResult<Integer>`，二者不纳入 `StudyPlanView` 统一返回规则。
- 动态状态必须通过 `timeProvider.today()` 和 `StudyPlanAnalysisService` 统一计算，不得在服务或视图中直接调用 `LocalDate.now()`、`LocalDateTime.now()`、`System.currentTimeMillis()`。
- 学习计划服务的写接口必须统一接收原始输入，而不是要求调用方先构造 `DateRange` 或 `Progress`。创建接口必须支持显式初始进度，以便后续 AI 学习计划草稿导入复用；本轮公开服务方法签名固定接收原始 `LocalDate startDate`、`LocalDate endDate`、原始整数初始进度参数（`int`/`Integer` 或仅参数命名等价的同义签名），并同时提供一个不传初始进度、默认 `0` 的便捷重载。修改详情公开接口同样固定接收原始 `startDate`、`endDate` 与目标名称、预期投入小时数，由服务内部负责构造 `DateRange`。不允许把“直接接收 `DateRange`”或“直接接收 `Progress`”继续保留为模糊选项，也不允许把原始日期校验责任推回调用方。
- `StudyPlanService.updateProgress(...)` 的公开接口必须与创建初始进度规则保持一致，固定接收原始整数进度值，由服务内部转换为 `Progress.zero()` / `Progress.of(...)` 后再进入领域对象或仓储边界，不允许继续暴露 `Progress` 作为服务层公开参数。
- 创建、修改详情和更新进度的非法输入应转换为 `OperationResult.failure(ErrorCode.VALIDATION_ERROR, ...)`；其中包括 `0` 和 `100` 进度成功、`-1` 和 `101` 进度失败、开始日期晚于截止日期失败、空日期失败、空必填字段失败、非正预期投入小时数失败。查看、修改、删除和更新进度不存在编号应返回 `NOT_FOUND`；任何非法输入失败后仓储状态保持不变。
- 学习计划统计接口在本轮必须固定为“全量计划统计”，不新增聚合 DTO，也不提供按查询条件或日期范围过滤的统计重载。公开方法签名应为 `OperationResult<Integer> countCompletedPlans()` 与 `OperationResult<Integer> countIncompletePlans()`（或仅参数命名等价的同义签名），二者统一基于 `timeProvider.today()` 和 `StudyPlanAnalysisService` 对仓储中的全部计划进行动态状态计算。完成数量统计以动态状态 `COMPLETED` 为准；未完成数量统计应统计除 `COMPLETED` 外的计划数量，包含 `NOT_STARTED`、`IN_PROGRESS` 和 `OVERDUE_INCOMPLETE`。
- 本轮测试必须覆盖固定 `currentDate` 下的动态状态筛选稳定性，验证 `StudyPlanQuery` / `StudyPlanRepository.findBy(...)` / `StudyPlanService.list...` 在相同显式日期输入下返回一致结果；同时覆盖创建学习计划时初始进度为 `0`、`100`、`-1`、`101` 的成功/失败路径，创建与修改详情在非法日期范围下的失败路径，以及失败后仓储状态不变。
- 本轮测试还必须覆盖仓储快照隔离边界：验证外部在 `save(...)` 后继续修改原始 `StudyPlan`、以及修改 `findById(...)` / `findAll()` / `findBy(...)` 返回的 `StudyPlan`，都不会影响仓储内部状态；并覆盖 `countCompletedPlans()` 与 `countIncompletePlans()` 在固定 `currentDate` 下的返回值与动态状态分类一致。
- 本轮测试还必须覆盖 `StudyPlanService.updateProgress(...)` 对原始整数 `0`、`100`、`-1`、`101` 的成功/失败路径，验证成功后状态快照正确刷新、失败后仓储状态不变，并明确服务层而非调用方承担非法原始进度到 `VALIDATION_ERROR` 的映射。

## 选择理由
v11 已完成学习计划实体 `StudyPlan`、状态枚举 `StudyPlanStatus` 和统一分析组件 `StudyPlanAnalysisService`。学习计划核心功能还缺少创建、查看、修改、删除、更新进度、查询筛选和完成/未完成数量统计的应用服务入口。

先补齐学习计划服务闭环，可以让后续汇总服务、AI 本地上下文、AI 学习计划草稿导入和控制台菜单通过稳定公开 API 协作学习计划数据，而不是直接操作可变实体或仓储集合。

## 任务上下文
完整需求要求“学习计划管理”支持创建学习目标、记录计划开始日期、截止日期、预期投入时间和完成进度，支持更新进度、判断是否逾期、统计已完成与未完成计划数量，并可将计划内容交给 AI 生成拆解建议；需要明确处理进度 0%、100%、小于 0、大于 100、截止日期早于开始日期等情况。

架构级 OOD 要求 `assistant.study` 负责学习目标创建、日期范围校验、进度更新、逾期判断、完成数量统计，以及为 AI 拆解建议提供计划上下文。学习计划状态由进度、日期范围和当前时间共同决定，应通过统一分析组件推导，避免不同服务给出不一致结论。AI 拆解建议只读取计划上下文，不直接修改计划。

技术方案要求：

- 学习计划实体持有目标名称、开始日期、截止日期、预期投入小时数和进度。
- 进度通过 `Progress` 值对象保证范围 0 到 100。
- 开始日期晚于截止日期时拒绝创建。
- 计划状态由 `StudyPlanAnalysisService` 推导为未开始、进行中、已完成、逾期未完成。
- 进度达到 100 视为已完成，优先级高于日期状态。
- 完成数量统计和本周统计都通过分析组件计算。
- 普通单元测试必须隔离真实当前时间、网络和 API Key。

## 已有代码上下文
当前 Maven 工程位于 `java-ai-assistant/`，已完成 Java 17、JUnit Jupiter、Mockito、Jackson、JaCoCo、Surefire/Failsafe 基线。

可复用通用类型：

- `assistant.common.EntityId`：正整数编号值对象。
- `assistant.testability.IdGenerator` / `IncrementalIdGenerator`：可替换编号生成。
- `assistant.testability.TimeProvider`、`FixedTimeProvider`、`SystemTimeProvider`：可替换当前日期与时间来源。
- `assistant.common.DateRange`：左右闭日期区间，支持 `contains(LocalDate)` 和 `overlaps(DateRange)`。
- `assistant.common.Progress`：0 到 100 的进度值对象，提供 `zero()`、`complete()`、`of(int)` 和 `isComplete()`。
- `assistant.common.OperationResult<T>` 与 `assistant.common.ErrorCode`：应用服务成功/失败返回语义。

已有学习计划类型：

- `assistant.study.StudyPlanStatus`：`NOT_STARTED`、`IN_PROGRESS`、`COMPLETED`、`OVERDUE_INCOMPLETE` 四种动态状态。
- `assistant.study.StudyPlan`：持有 `id`、`goalName`、`period`、`expectedHours`、`progress`，提供 `create(...)`、`updateDetails(...)`、`updateProgress(...)`、`isCompleted()` 等公开行为。
- `assistant.study.StudyPlanAnalysisService`：`analyzeStatus(StudyPlan, LocalDate)` 按完成优先、逾期、未开始、进行中顺序推导动态状态，并提供 `isCompleted(...)` 和 `isOverdueIncomplete(...)`。

已有模块风格：

- `TaskService` 和 `ScheduleService` 在服务边界捕获 `NullPointerException` / `IllegalArgumentException` 并转换为 `VALIDATION_ERROR`。
- `TaskView`、`ScheduleView` 作为只读 DTO，服务成功载荷不暴露内部可变实体。
- `InMemoryTaskRepository`、`InMemoryScheduleRepository` 使用 `LinkedHashMap<EntityId, ...>` 保持插入顺序，`findAll()` 返回 `List.copyOf(...)`，组合查询通过 query 的 `matches(...)` 实现。

本轮测试应覆盖查询条件、只读视图、仓储插入顺序和快照边界、服务创建/查看/列表/筛选/修改/删除/更新进度、状态动态计算、完成与未完成数量统计、非法输入错误分类、不存在编号错误分类，以及失败操作不改变仓储状态。

## RETRY 说明
上一轮计划审查指出两个缺口：一是学习计划动态状态筛选没有像既有 `schedule` 模块那样定义显式时间上下文，容易导致实现者在 query、仓储或服务中直接读取系统时间或复制状态判断逻辑；二是创建学习计划时的初始进度契约缺失，无法覆盖需求和后续 AI 草稿导入场景。

本轮修正方向是补齐唯一可执行的接口边界：明确动态状态筛选通过 `StudyPlanQuery.matches(..., StudyPlanAnalysisService, LocalDate)` 与 `StudyPlanRepository.findBy(..., StudyPlanAnalysisService, LocalDate)` 完成，服务层统一提供 `timeProvider.today()`；同时把服务创建接口固定为“公开层接收原始整数初始进度，内部转换为 `Progress`，并保留默认 0 重载”，再把 `0`、`100`、`-1`、`101` 纳入测试范围。

## 修订说明（v12 r1）
| 审查意见 | 修改措施 |
|---------|---------|
| 学习计划动态状态筛选契约未定义完整，可能导致直接读取系统时间或复制状态推导逻辑。 | 明确 `StudyPlanQuery.matches(...)` 与 `StudyPlanRepository.findBy(...)` 必须显式接收 `StudyPlanAnalysisService` 和 `LocalDate currentDate`，并指定由 `StudyPlanService` 统一从 `timeProvider.today()` 提供上下文，补充固定日期下的查询一致性测试要求。 |
| 创建学习计划时初始进度规则缺失，无法覆盖需求和 AI 草稿导入场景。 | 明确创建接口支持显式初始进度且保留默认 0 便捷入口，要求 `0`、`100` 成功，`-1`、`101` 映射为 `VALIDATION_ERROR`，并补充失败后仓储状态不变测试。 |

## 修订说明（v12 r2）
| 审查意见 | 修改措施 |
|---------|---------|
| 仓储“不可修改快照”边界未明确，仍可能暴露内部可变 `StudyPlan` 实体引用。 | 补充仓储快照隔离约束：`save(...)`、`findById(...)`、`findAll()`、`findBy(...)` 都不得让外部持有的 `StudyPlan` 引用影响仓储内部状态，并将保存后修改原对象、修改查询结果对象不影响仓储的测试纳入本轮范围。 |
| 学习计划完成/未完成统计接口形态未固定，可能导致实现分叉。 | 明确本轮只实现全量统计，公开方法固定为 `countCompletedPlans()` 与 `countIncompletePlans()` 返回 `OperationResult<Integer>`，不新增统计 DTO 或按条件统计重载，并补充固定日期下统计结果测试要求。 |

## 修订说明（v12 r3）
| 审查意见 | 修改措施 |
|---------|---------|
| `StudyPlanService` 的通用成功返回规则与固定统计签名并存，导致统计接口是否适用 `StudyPlanView` 规则不明确。 | 明确 `StudyPlanView` / `List<StudyPlanView>` 只适用于创建、查看、列表、组合筛选、修改详情、更新进度这些返回学习计划快照的方法；删除固定返回 `OperationResult<Void>`，统计固定返回 `OperationResult<Integer>`，从统一快照返回规则中排除。 |
| 创建接口同时要求接收 `Progress` 与映射非法原始进度，仍未形成唯一公开 API。 | 明确公开创建服务接口固定接收原始整数初始进度参数，并保留默认 `0` 的便捷重载；由服务内部转换为 `Progress` 并统一把 `-1`、`101` 等非法原始数值映射为 `VALIDATION_ERROR`，不再保留“`Progress` 或等价参数”的模糊描述。 |

## 修订说明（v12 r4）
| 审查意见 | 修改措施 |
|---------|---------|
| 服务边界没有把创建/修改详情的日期输入形态收束到唯一接口，导致“开始日期晚于截止日期”可能在调用方构造 `DateRange` 时提前失败，无法在服务层映射为 `VALIDATION_ERROR`。 | 明确 `StudyPlanService` 创建与修改详情公开接口必须接收原始 `startDate`/`endDate`（或唯一等价原始请求对象），由服务内部构造 `DateRange` 并统一映射非法日期、空日期、空字段和非正预期小时数为 `VALIDATION_ERROR`；同时补充非法日期范围与失败后仓储不变测试要求。 |
| `updateProgress(...)` 仍未固定接收原始整数，`-1`、`101` 等非法原始进度无法形成稳定服务层错误映射与白盒测试契约。 | 明确 `StudyPlanService.updateProgress(...)` 公开接口固定接收原始整数进度值，由服务内部转换为 `Progress` 并统一把非法原始值映射为 `VALIDATION_ERROR`；同时补充 `0`、`100`、`-1`、`101` 的更新进度成功/失败与失败后仓储不变测试。 |
