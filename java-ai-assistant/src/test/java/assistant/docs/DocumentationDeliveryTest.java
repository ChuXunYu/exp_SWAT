package assistant.docs;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
    private static final Path LAB_GUIDE = Path.of("..", "软件质量保证与测试实验指导书.md");
    private static final Path TEST_SOURCES = Path.of("src/test/java");
    private static final Pattern TEST_METHOD_PATTERN = Pattern.compile(
            "@Test\\s+(?:void\\s+)?([A-Za-z_$][A-Za-z\\d_$]*)\\s*\\(");
    private static final Pattern TEST_REFERENCE_PATTERN = Pattern.compile(
            "\\b([A-Za-z_$][A-Za-z\\d_$]*Test)\\.([A-Za-z_$][A-Za-z\\d_$]*)\\b");
    private static final Pattern SCORE_ROW_PATTERN = Pattern.compile("\\| [^|]+ \\| (\\d+) \\| [^|]+ \\| [^|]+ \\|");

    @Test
    void labGuideDocumentsRequiredCourseStructureAndProjectScope() {
        String guide = read(LAB_GUIDE);

        assertAll(
                () -> assertContainsAll(guide, List.of(
                        "课程编号：A0801050230",
                        "# 软件质量保证与测试",
                        "# java-ai-assistant 实验指导书",
                        "东北大学软件学院")),
                () -> assertContainsAll(guide, List.of(
                        "## 一、实验基本信息",
                        "## 二、实验目的",
                        "## 三、实验环境",
                        "## 四、项目简介与被测对象",
                        "## 五、被测范围与不测范围",
                        "## 六、质量目标与质量属性",
                        "## 七、测试策略",
                        "## 八、测试计划",
                        "## 九、测试用例设计方法",
                        "## 十、单元测试实验",
                        "## 十一、集成测试实验",
                        "## 十二、系统测试与验收测试实验",
                        "## 十三、自动化测试与 CI 实验",
                        "## 十四、缺陷记录与分析",
                        "## 十五、质量度量",
                        "## 十六、实验报告提交要求",
                        "## 十七、评分标准",
                        "## 附录 A：推荐命令",
                        "## 附录 B：关键目录结构",
                        "## 附录 C：关键测试类索引",
                        "## 附录 D：参考文档清单")),
                () -> assertContainsAll(guide, List.of(
                        "`/root/exp_SWAT/java-ai-assistant`",
                        "Java 17",
                        "Maven 单模块",
                        "控制台个人学习生活助手",
                        "`assistant.app.Main`",
                        "默认测试不访问真实 DeepSeek、真实网络、真实 API Key、用户文件或真实当前时间",
                        "内存仓储",
                        "不包含数据库、文件导出、系统通知、账号、多用户、图形界面或后台提醒服务")));
    }

    @Test
    void labGuideMapsRealModulesCommandsCiAndIntegrationBoundaries() {
        String guide = read(LAB_GUIDE);

        assertAll(
                () -> assertContainsAll(guide, List.of(
                        "`AiAssistantService`", "`PromptBuilder`", "`DeepSeekAiClient`",
                        "`AiConfigurationLoader`", "`StructuredSuggestionParser`",
                        "`DraftLifecycleService`", "`DraftImportService`",
                        "`Main`", "`ApplicationFactory`", "`ConsoleApplication`", "`DemoDataFactory`",
                        "`TaskService`", "`TaskItem`", "`TaskQuery`", "`TaskStatus`", "`TaskPriority`",
                        "`ScheduleService`", "`ScheduleItem`", "`ScheduleConflictPolicy`",
                        "`ScheduleStatus`", "`ScheduleQuery`",
                        "`StudyPlanService`", "`StudyPlan`", "`StudyPlanAnalysisService`",
                        "`StudyPlanStatus`", "`StudyPlanQuery`",
                        "`FinanceService`", "`FinanceStatisticsService`", "`TransactionRecord`",
                        "`TransactionQuery`", "`TransactionType`",
                        "`NoteService`", "`Note`", "`NoteQuery`", "`NoteSearchPolicy`",
                        "`SummaryService`", "`DashboardSummary`", "`LocalContext`",
                        "`TimeProvider`", "`FixedTimeProvider`", "`IdGenerator`", "`IncrementalIdGenerator`")),
                () -> assertContainsAll(guide, List.of(
                        "mvn clean test",
                        "mvn clean verify",
                        "mvn jacoco:report",
                        "mvn -Pintegration verify",
                        "mvn -q -DskipTests dependency:build-classpath -Dmdep.outputFile=target/classpath.txt",
                        "mvn -q -DskipTests compile",
                        "java -cp \"target/classes:$(cat target/classpath.txt)\" assistant.app.Main",
                        "printf 'q\\n' | java -cp \"target/classes:$(cat target/classpath.txt)\" assistant.app.Main")),
                () -> assertContainsAll(guide, List.of(
                        "`push`",
                        "`pull_request`",
                        "`java-ai-assistant`",
                        "mvn -B -DskipTests package",
                        "mvn -B test")),
                () -> assertContainsAll(guide, List.of(
                        "当前仓库没有 `*IT.java` 文件",
                        "Failsafe 输出 `No tests to run`",
                        "不等价于真实 DeepSeek 连通性已验证",
                        "未来可新增 `*IT.java`",
                        "`DEEPSEEK_API_KEY`")));
    }

    @Test
    void labGuideDocumentsHistoricalAcceptanceAsEvidenceAndRequiresCurrentExecutionResults() {
        String guide = read(LAB_GUIDE);

        assertAll(
                () -> assertContainsAll(guide, List.of(
                        "/root/exp_SWAT/acceptance/20260613_full_acceptance.md",
                        "2026-06-13 验收记录",
                        "`mvn clean test` 952 个测试通过",
                        "`mvn clean verify` 通过",
                        "`mvn -Pintegration verify` 通过但无集成测试可运行",
                        "JaCoCo 指令覆盖 96.78%",
                        "分支覆盖 86.65%",
                        "行覆盖 94.55%",
                        "/root/exp_SWAT/implements/202606131017_qa-risk-fixes/verify_v4.md",
                        "`mvn clean verify` 通过，989 个测试通过、失败 0、错误 0、跳过 0",
                        "指令覆盖 96.79%",
                        "分支覆盖 86.61%",
                        "行覆盖 94.48%")),
                () -> assertContainsAll(guide, List.of(
                        "课程复核时必须以当前执行输出为准",
                        "不得伪造新的执行结果",
                        "测试数量、失败数、覆盖率和 CI 状态必须来自当前真实执行输出",
                        "明确标注为“来自 2026-06-13 验收记录”")));
    }

    @Test
    void labGuideDefinesDefectMetricsReportRequirementsAndOneHundredPointRubric() {
        String guide = read(LAB_GUIDE);

        assertAll(
                () -> assertContainsAll(guide, List.of(
                        "缺陷编号",
                        "严重级别",
                        "复现步骤",
                        "预期结果",
                        "实际结果",
                        "根因分析",
                        "修复措施",
                        "回归测试",
                        "阻塞",
                        "严重",
                        "一般",
                        "轻微")),
                () -> assertContainsAll(guide, List.of(
                        "测试通过率",
                        "模块用例覆盖范围",
                        "覆盖率",
                        "缺陷分布",
                        "自动化执行结果",
                        "风险残留")),
                () -> assertContainsAll(guide, List.of(
                        "/root/exp_SWAT/实验说明.md",
                        "/root/exp_SWAT/实验报告模板.md",
                        "实验报告",
                        "答辩视频",
                        "待测程序源代码",
                        "单元测试脚本")),
                () -> assertEquals(100, scoreTotal(guide)));
    }

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
                        "具体测试数量以当前 Maven/Surefire 输出为准",
                        "QA 风险修复 v4 执行 `mvn clean verify` 时 989 个测试通过、失败 0 个、错误 0 个、跳过 0 个",
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
                        "执行结果数量以当前 Maven/Surefire 输出为准",
                        "QA 风险修复 v4 执行 `mvn clean verify` 时 989 个测试通过、失败 0 个、错误 0 个、跳过 0 个")),
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
                        "通过 989 个测试、失败 0 个、错误 0 个、跳过 0 个",
                        "BUG-11",
                        "BUG-15",
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
                        "## Project Overview",
                        "## Features",
                        "## Requirements",
                        "## Build",
                        "## Unit Tests",
                        "mvn clean test",
                        "passed 989 tests with 0 failures",
                        "## Integration Tests",
                        "mvn -Pintegration verify",
                        "does not contain `*IT.java` classes",
                        "does not mean that a real DeepSeek connectivity test already exists",
                        "DEEPSEEK_API_KEY",
                        "## Run",
                        "## Configuration",
                        "## Common Workflows",
                        "## Known Limitations",
                        "assistant.app.Main",
                        "ASSISTANT_DEMO_DATA")),
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
    void readmeDocumentsRunnableEntryPointCommandsWithoutUnsupportedCliOrJarClaims() {
        String readme = read(Path.of("README.md"));

        assertAll(
                () -> assertContainsAll(readme, List.of(
                        "mvn -q -DskipTests dependency:build-classpath -Dmdep.outputFile=target/classpath.txt",
                        "mvn -q -DskipTests compile",
                        "java -cp \"target/classes:$(cat target/classpath.txt)\" assistant.app.Main",
                        "printf 'q\\n' | java -cp \"target/classes:$(cat target/classpath.txt)\" assistant.app.Main",
                        "ASSISTANT_DEMO_DATA=false java -cp \"target/classes:$(cat target/classpath.txt)\" assistant.app.Main",
                        "java -DASSISTANT_DEMO_DATA=false -cp \"target/classes:$(cat target/classpath.txt)\" assistant.app.Main")),
                () -> assertFalse(readme.contains("--demo-data"), "README must not document unsupported demo-data CLI flags"),
                () -> assertFalse(readme.contains("--api-key"), "README must not document unsupported API key CLI flags"),
                () -> assertFalse(readme.contains("--model"), "README must not document unsupported model CLI flags"),
                () -> assertFalse(readme.contains("--help"), "README must not document unsupported help CLI flags"),
                () -> assertFalse(readme.contains("Main-Class"), "README must not claim a configured executable jar"));
    }

    @Test
    void readmeDocumentsAiAndDemoDataConfigurationContracts() {
        String readme = read(Path.of("README.md"));

        assertAll(
                () -> assertContainsAll(readme, List.of(
                        "`DEEPSEEK_API_KEY`",
                        "`DEEPSEEK_BASE_URL`",
                        "`https://api.deepseek.com`",
                        "`DEEPSEEK_MODEL`",
                        "`deepseek-v4-flash`",
                        "`DEEPSEEK_TIMEOUT_SECONDS`",
                        "`/chat/completions`",
                        "`ASSISTANT_DEMO_DATA`",
                        "`false`, `0`, or `no` disables demo data")),
                () -> assertContainsAll(readme, List.of(
                        "when no API key is configured, it returns an unconfigured error instead of calling the real service",
                        "The default test lifecycle is isolated from real DeepSeek calls",
                        "Do not commit API keys to source code, tests, or documentation examples")));
    }

    @Test
    void readmeDocumentsCurrentFeatureSurfaceAndKnownLimitationsOnly() {
        String readme = read(Path.of("README.md"));
        String lowerCaseReadme = readme.toLowerCase();

        assertAll(
                () -> assertContainsAll(readme, List.of(
                        "Summary: shows today's tasks, overdue incomplete tasks, upcoming high-priority tasks in the next 7 days",
                        "Tasks: list, add, view, filter, update, complete, reopen, and delete tasks.",
                        "Schedules: list, add, view, filter, update, and delete schedule items",
                        "Study plans: list, add, view, filter, update, update progress, and delete plans.",
                        "Finance: list, add, view, filter, update, delete, and calculate transaction statistics.",
                        "Notes: list, add, view, filter, update, and delete notes.",
                        "AI drafts: generate, view, confirm, or cancel structured suggestion drafts",
                        "The main menu contains summary, tasks, schedules, study plans, finance, notes, AI Q&A, AI drafts, help, and exit.")),
                () -> assertContainsAll(readme, List.of(
                        "single-user console application",
                        "stored in memory and is not persisted after the process exits",
                        "There is no database, file export, real system notification, or background reminder service.",
                        "This README does not claim a concrete coverage percentage.")),
                () -> assertFalse(lowerCaseReadme.contains("account"), "README must not claim account management"),
                () -> assertFalse(lowerCaseReadme.contains("contact"), "README must not claim contact management"),
                () -> assertFalse(lowerCaseReadme.contains("health"), "README must not claim health management"));
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
                        "passing 989 tests with 0 failures",
                        "does not contain `*IT.java` integration test classes")));
    }

    @Test
    void documentedWhiteBoxAndCoverageTestReferencesPointToExistingTestAssets() {
        String cases = read(DOCS.resolve("test-cases.md"));
        String coverage = read(DOCS.resolve("coverage/README.md"));
        Map<String, Set<String>> testMethodsByClass = loadTestMethodsByClass();

        assertAll(
                () -> assertDocumentedTestMethodReferencesExist(cases, testMethodsByClass),
                () -> assertDocumentedTestMethodReferencesExist(coverage, testMethodsByClass),
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

    private static int scoreTotal(String guide) {
        int rubricStart = guide.indexOf("## 十七、评分标准");
        int appendixStart = guide.indexOf("## 附录 A：推荐命令");
        assertTrue(rubricStart >= 0, "Missing rubric section");
        assertTrue(appendixStart > rubricStart, "Missing appendix after rubric");

        String rubric = guide.substring(rubricStart, appendixStart);
        int total = 0;
        Matcher matcher = SCORE_ROW_PATTERN.matcher(rubric);
        while (matcher.find()) {
            total += Integer.parseInt(matcher.group(1));
        }
        return total;
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

    private static void assertDocumentedTestMethodReferencesExist(
            String document,
            Map<String, Set<String>> testMethodsByClass
    ) {
        Matcher matcher = TEST_REFERENCE_PATTERN.matcher(document);
        Set<TestReference> references = new HashSet<>();
        while (matcher.find()) {
            references.add(reference(matcher.group(1), matcher.group(2)));
        }
        assertFalse(references.isEmpty(), "Document must reference at least one concrete JUnit test method");
        for (TestReference reference : references) {
            assertTrue(testMethodsByClass.containsKey(reference.className()),
                    () -> "Documented test class does not exist in source tree: " + reference.className());
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
