# 设计审查报告（v16 r1）

## 审查结果
APPROVED

## 发现
- **[轻微]** — `NoteService.createNote(...)` 的契约写作顺序为先转换标签，再调用 `idGenerator.nextId()` 和 `timeProvider.today()`，这能避免标签非法时消耗编号或日期调用；编码时应保持该顺序。当前设计已明确失败不得保存笔记，因此不构成阻塞。
- **[轻微]** — `NoteView` 规范构造器要求保存不可修改的 `LinkedHashSet` 副本。Java record 紧凑构造器中需要显式给参数变量重新赋值，例如 `tags = Collections.unmodifiableSet(new LinkedHashSet<>(tags));`，否则容易只校验不替换字段。该点属于实现注意事项，设计本身已给出正确契约。
- **[轻微]** — `NoteRepository` 接口只以“实现应拒绝空参数”描述空值行为，而 `InMemoryNoteRepository` 进一步固定为 `NullPointerException`。后续若新增其他实现应保持一致；本轮默认实现的契约已经足够编码和测试。

未发现严重或一般问题。设计覆盖了任务文件要求的查询条件、只读视图、仓储契约、内存仓储快照隔离、服务输入转换、错误映射、失败原子性、可控编号/日期和对应测试范围，并与现有 `Note`、`NoteSearchPolicy`、`OperationResult`、`IdGenerator`、`TimeProvider` 及相邻模块风格兼容。
