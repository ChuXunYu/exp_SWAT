# 设计审查报告（v20 r2）

## 审查结果
APPROVED

## 发现

- **[轻微]** — `SuggestionDraftView` 将 `Optional<StudyPlanDraftContent>` 作为 record 字段暴露，虽然可实现且不影响正确性，但后续若需要序列化或控制台展示，调用方会比使用 nullable 字段或显式 `hasStudyPlan()` 稍多一层处理成本。本轮任务未要求序列化接口，该选择不阻碍编码。
- **[轻微]** — `InMemorySuggestionDraftRepository.findById(...)` 和 `findAll()` 返回聚合根引用以支持后续生命周期服务原地迁移状态，设计同时要求查询展示使用 `SuggestionDraftView` 快照。该边界需要编码者在后续服务层继续遵守，但本轮仓储作为领域持有边界的契约是自洽的。

