package assistant.docs;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class DocumentationDeliveryTest {
    private static final Path DOCS = Path.of("docs");
    private static final Path TEST_SOURCES = Path.of("src/test/java");
    private static final Pattern TEST_METHOD_PATTERN = Pattern.compile(
            "@Test\\s+(?:void\\s+)?([A-Za-z_$][A-Za-z\\d_$]*)\\s*\\(");

    @Test
    void testPlanDocumentsRequiredScopeToolsCommandsAndBoundaries() {
        String plan = read(DOCS.resolve("test-plan.md"));

        assertAll(
                () -> assertContainsAll(plan, List.of(
                        "# 实验 1 测试计划",
                        "## 测试目标",
                        "## 测试范围",
                        "## 测试环境",
                        "## 测试工具",
                        "## 测试分层",
                        "## 测试策略",
                        "## 核心功能覆盖计划",
                        "## 运行命令",
                        "## 集成测试边界",
                        "## 通过准则")),
                () -> assertContainsAll(plan, coreFeatureNames()),
                () -> assertContainsAll(plan, List.of(
                        "Java | 17",
                        "JUnit Jupiter | 5.14.4",
                        "Mockito | 5.18.0",
                        "Maven Surefire Plugin | 3.5.6",
                        "Maven Failsafe Plugin | 3.5.6",
                        "JaCoCo Maven Plugin | 0.8.13")),
                () -> assertContainsAll(plan, List.of(
                        "mvn clean test",
                        "mvn clean verify",
                        "mvn jacoco:report",
                        "mvn -Pintegration verify",
                        "通过 944 个测试，失败 0 个",
                        "不存在 `*IT.java`",
                        "普通单元测试不得访问真实 DeepSeek、网络、API Key、用户文件或真实当前时间")));
    }

    @Test
    void testCasesMapEveryCoreFeatureToRepresentativeWhiteBoxRows() {
        String cases = read(DOCS.resolve("test-cases.md"));

        assertAll(
                () -> assertContainsAll(cases, List.of(
                        "# 白盒测试用例",
                        "## 用例编号规则",
                        "## 测试方法说明",
                        "## 用例总览",
                        "## 跨模块场景链路",
                        "## 执行结果摘要")),
                () -> assertContainsAll(cases, List.of(
                        "编号 | 测试层级 | 测试方法 | 被测类/方法 | JUnit 测试类/方法 | 输入或前置条件 | 预期结果 | 实际结果",
                        "AI-01",
                        "DRAFT-01",
                        "TASK-01",
                        "SCHEDULE-01",
                        "STUDY-01",
                        "FINANCE-01",
                        "NOTE-01",
                        "SUMMARY-01",
                        "通过 944 个测试，失败 0 个")),
                () -> assertContainsAll(cases, List.of(
                        "AI 任务草稿确认导入到任务服务",
                        "AI 学习计划草稿确认导入到学习计划服务",
                        "AI 草稿导入失败回滚",
                        "任务变化后汇总同步",
                        "收支变化后汇总同步",
                        "笔记变化后汇总和 AI 本地上下文同步")));
    }

    @Test
    void defectRegressionRecordsConcreteRegressionEvidenceAndResidualRisks() {
        String regression = read(DOCS.resolve("defect-regression.md"));

        assertAll(
                () -> assertContainsAll(regression, List.of(
                        "# 缺陷修复与回归测试记录",
                        "## 记录口径",
                        "## 缺陷记录",
                        "## 核心回归测试集",
                        "## 回归执行结论",
                        "## 残余风险",
                        "缺陷表现 | 触发输入/前置状态 | 预期结果 | 实际结果 | 可能原因 | 修复方式 | 复现/回归用例 | 重跑范围 | 结论")),
                () -> assertContainsAll(regression, List.of(
                        "BUG-01",
                        "BUG-05",
                        "BUG-10",
                        "通过 944 个测试，失败 0 个",
                        "真实 DeepSeek 集成测试未作为默认回归执行",
                        "当前 `src/test/java` 下没有 `*IT.java` 集成测试类",
                        "不伪造覆盖率数字")));
    }

    @Test
    void coverageReadmeDocumentsJacocoAndPathMappingsWithoutFakePercentages() {
        String coverage = read(DOCS.resolve("coverage/README.md"));

        assertAll(
                () -> assertContainsAll(coverage, List.of(
                        "# 覆盖证据说明",
                        "## JaCoCo 报告生成",
                        "## 覆盖目标",
                        "## 重点方法路径分析",
                        "## 覆盖证据与用例映射",
                        "## 结果记录方式",
                        "target/site/jacoco/index.html")),
                () -> assertContainsAll(coverage, List.of(
                        "assistant.finance.FinanceStatisticsService.calculate(List<TransactionRecord>)",
                        "assistant.ai.DraftImportService.importDraft(SuggestionDraft)",
                        "assistant.summary.SummaryService.getDashboardSummary()",
                        "圈复杂度估算",
                        "F-P1",
                        "D-P1",
                        "S-P1")),
                () -> assertFalse(coverage.contains("%"), "coverage README must not claim a concrete percentage"));
    }

    @Test
    void readmeDocumentsTestDeliverablesCoverageCommandIntegrationBoundaryAndBaseline() {
        String readme = read(Path.of("README.md"));

        assertAll(
                () -> assertContainsAll(readme, List.of(
                        "## Unit Tests",
                        "mvn clean test",
                        "passed 944 tests with 0 failures",
                        "## Integration Tests",
                        "mvn -Pintegration verify",
                        "does not contain `*IT.java` classes",
                        "does not mean that a real DeepSeek connectivity test already exists",
                        "DEEPSEEK_API_KEY")),
                () -> assertContainsAll(readme, List.of(
                        "## Test Documentation",
                        "[Test plan](docs/test-plan.md)",
                        "[White-box test cases](docs/test-cases.md)",
                        "[Defect and regression record](docs/defect-regression.md)",
                        "[Coverage evidence notes](docs/coverage/README.md)",
                        "[Environment](docs/environment.md)")),
                () -> assertContainsAll(readme, List.of(
                        "## Coverage",
                        "mvn clean verify",
                        "mvn jacoco:report",
                        "target/site/jacoco/index.html")));
    }

    @Test
    void environmentDocumentsTestPluginsIsolationDeliverablesCoverageOutputAndBaseline() {
        String environment = read(DOCS.resolve("environment.md"));

        assertAll(
                () -> assertContainsAll(environment, List.of(
                        "Java 17 LTS",
                        "JUnit Jupiter 5.14.4",
                        "Mockito 5.18.0",
                        "Maven Surefire Plugin 3.5.6",
                        "Maven Failsafe Plugin 3.5.6",
                        "JaCoCo Maven Plugin 0.8.13")),
                () -> assertContainsAll(environment, List.of(
                        "mvn clean test",
                        "mvn clean verify",
                        "mvn -Pintegration verify",
                        "mvn jacoco:report",
                        "target/site/jacoco/")),
                () -> assertContainsAll(environment, List.of(
                        "Ordinary unit tests use fixed time, in-memory repositories, and mock or fake AI dependencies",
                        "do not access the real DeepSeek service, network resources, a real API key, user files, or the real current time",
                        "Future tests that make real external calls belong in the `integration` profile")),
                () -> assertContainsAll(environment, List.of(
                        "[Test plan](test-plan.md)",
                        "[White-box test cases](test-cases.md)",
                        "[Defect and regression record](defect-regression.md)",
                        "[Coverage evidence notes](coverage/README.md)",
                        "passing 944 tests with 0 failures",
                        "does not contain `*IT.java` integration test classes")));
    }

    @Test
    void documentedWhiteBoxAndCoverageTestReferencesPointToExistingTestAssets() {
        String cases = read(DOCS.resolve("test-cases.md"));
        String coverage = read(DOCS.resolve("coverage/README.md"));
        Map<String, Set<String>> testMethodsByClass = loadTestMethodsByClass();

        assertAll(
                () -> assertReferencesExist(cases, testMethodsByClass, List.of(
                        reference("AiAssistantServiceTest", "askSendsBuiltRequestToClientAndReturnsContent"),
                        reference("DeepSeekAiClientTest", "chatMapsTransportExceptions"),
                        reference("DraftImportServiceTest", "importsAllTaskDraftItems"),
                        reference("DraftImportServiceTest", "rollsBackCreatedTasksWhenTaskCreationFails"),
                        reference("ConsoleApplicationTest", "draftMenuRejectsInvalidIdBeforeCallingDraftLifecycleService"),
                        reference("TaskServiceTest", "createTaskStoresTodoTaskAndReturnsTaskView"),
                        reference("ScheduleServiceTest", "createScheduleAllowsTouchingTimeRanges"),
                        reference("StudyPlanServiceTest", "listStudyPlansComputesStatusesWithInjectedCurrentDate"),
                        reference("FinanceStatisticsServiceTest", "calculateAllowsNegativeBalanceWhenExpenseExceedsIncome"),
                        reference("NoteServiceTest", "deleteNoteReturnsNotFoundForMissingId"),
                        reference("SummaryServiceTest", "getDashboardSummaryUsesSingleStableTodaySnapshotForAllDateBoundariesAndQueries"))),
                () -> assertReferencesExist(coverage, testMethodsByClass, List.of(
                        reference("FinanceStatisticsServiceTest", "calculateReturnsZeroForEmptyRecords"),
                        reference("FinanceStatisticsServiceTest", "calculateAccumulatesIncomeAndExpenseSeparately"),
                        reference("FinanceStatisticsServiceTest", "calculateAllowsNegativeBalanceWhenExpenseExceedsIncome"),
                        reference("DraftImportServiceTest", "importDraftRejectsNullDraft"),
                        reference("DraftImportServiceTest", "rollsBackCreatedTasksWhenTaskCreationThrowsRuntimeException"),
                        reference("DraftImportServiceTest", "propagatesStudyPlanCreationFailure"),
                        reference("SummaryServiceTest", "getDashboardSummaryPropagatesFirstDependencyFailure"),
                        reference("SummaryServiceTest", "buildLocalContextReturnsLocalContextFromSuccessfulSummary"),
                        reference("LocalContextTest", "fromBuildsLinesInSourceOrderForMultiModuleData"))),
                () -> assertDocumentedClassExists(cases, testMethodsByClass, "StudyPlanAnalysisServiceTest"),
                () -> assertDocumentedClassExists(cases, testMethodsByClass, "MoneyValueTest"),
                () -> assertDocumentedClassExists(cases, testMethodsByClass, "TransactionAmountTest"),
                () -> assertDocumentedClassExists(coverage, testMethodsByClass, "DraftImportServiceTest"),
                () -> assertDocumentedClassExists(coverage, testMethodsByClass, "SummaryServiceTest"));
    }

    @Test
    void documentedIntegrationBoundaryMatchesCurrentTestTree() {
        boolean hasIntegrationTest = assertDoesNotThrow(() -> {
            try (Stream<Path> paths = Files.walk(TEST_SOURCES)) {
                return paths.anyMatch(path -> path.getFileName().toString().endsWith("IT.java"));
            }
        });
        String plan = read(DOCS.resolve("test-plan.md"));
        String regression = read(DOCS.resolve("defect-regression.md"));

        assertAll(
                () -> assertFalse(hasIntegrationTest),
                () -> assertTrue(plan.contains("当前 `src/test/java` 下不存在 `*IT.java` 集成测试类")),
                () -> assertTrue(regression.contains("当前 `src/test/java` 下没有 `*IT.java` 集成测试类")));
    }

    private static String read(Path path) {
        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new AssertionError("Unable to read " + path, exception);
        }
    }

    private static void assertContainsAll(String text, List<String> expectedFragments) {
        for (String expected : expectedFragments) {
            assertTrue(text.contains(expected), () -> "Missing fragment: " + expected);
        }
    }

    private static Map<String, Set<String>> loadTestMethodsByClass() {
        Map<String, Set<String>> methodsByClass = new HashMap<>();
        try (Stream<Path> paths = Files.walk(TEST_SOURCES)) {
            paths.filter(path -> path.getFileName().toString().endsWith("Test.java"))
                    .forEach(path -> methodsByClass.put(testClassName(path), testMethodNames(path)));
        } catch (IOException exception) {
            throw new AssertionError("Unable to scan test sources", exception);
        }
        return methodsByClass;
    }

    private static Set<String> testMethodNames(Path testSource) {
        String source = read(testSource);
        Set<String> methods = new HashSet<>();
        Matcher matcher = TEST_METHOD_PATTERN.matcher(source);
        while (matcher.find()) {
            methods.add(matcher.group(1));
        }
        return methods;
    }

    private static String testClassName(Path testSource) {
        String fileName = testSource.getFileName().toString();
        return fileName.substring(0, fileName.length() - ".java".length());
    }

    private static void assertReferencesExist(
            String document,
            Map<String, Set<String>> testMethodsByClass,
            List<TestReference> references
    ) {
        for (TestReference reference : references) {
            assertDocumentedClassExists(document, testMethodsByClass, reference.className());
            assertTrue(document.contains(reference.className() + "." + reference.methodName()),
                    () -> "Missing documented test method reference: " + reference);
            assertTrue(testMethodsByClass.get(reference.className()).contains(reference.methodName()),
                    () -> "Documented test method does not exist in source tree: " + reference);
        }
    }

    private static void assertDocumentedClassExists(
            String document,
            Map<String, Set<String>> testMethodsByClass,
            String className
    ) {
        assertTrue(document.contains(className), () -> "Missing documented test class reference: " + className);
        assertTrue(testMethodsByClass.containsKey(className),
                () -> "Documented test class does not exist in source tree: " + className);
    }

    private static TestReference reference(String className, String methodName) {
        return new TestReference(className, methodName);
    }

    private static List<String> coreFeatureNames() {
        return List.of(
                "AI 问答与学习生活建议。",
                "AI 结构化建议确认导入。",
                "任务待办管理。",
                "日程提醒管理。",
                "学习计划管理。",
                "收支记录管理。",
                "个人笔记或日记管理。",
                "数据查询与汇总统计。");
    }

    private record TestReference(String className, String methodName) {
        @Override
        public String toString() {
            return className + "." + methodName;
        }
    }
}
