# 验证报告（v23）

## 结果
PASSED

## 统计
- 通过：857
- 失败：0

## 测试执行日志

```text
[[1;34mINFO[m] Scanning for projects...
[[1;34mINFO[m] 
[[1;34mINFO[m] [1m--------------------< [0;36massistant:java-ai-assistant[0;1m >---------------------[m
[[1;34mINFO[m] [1mBuilding java-ai-assistant 1.0.0-SNAPSHOT[m
[[1;34mINFO[m] [1m--------------------------------[ jar ]---------------------------------[m
[[1;34mINFO[m] 
[[1;34mINFO[m] [1m--- [0;32mjacoco-maven-plugin:0.8.13:prepare-agent[m [1m(prepare-agent)[m @ [36mjava-ai-assistant[0;1m ---[m
[[1;34mINFO[m] argLine set to -javaagent:/root/.m2/repository/org/jacoco/org.jacoco.agent/0.8.13/org.jacoco.agent-0.8.13-runtime.jar=destfile=/root/exp_SWAT/java-ai-assistant/target/jacoco.exec
[[1;34mINFO[m] 
[[1;34mINFO[m] [1m--- [0;32mmaven-resources-plugin:2.6:resources[m [1m(default-resources)[m @ [36mjava-ai-assistant[0;1m ---[m
[[1;34mINFO[m] Using 'UTF-8' encoding to copy filtered resources.
[[1;34mINFO[m] skip non existing resourceDirectory /root/exp_SWAT/java-ai-assistant/src/main/resources
[[1;34mINFO[m] 
[[1;34mINFO[m] [1m--- [0;32mmaven-compiler-plugin:3.14.0:compile[m [1m(default-compile)[m @ [36mjava-ai-assistant[0;1m ---[m
[[1;34mINFO[m] Nothing to compile - all classes are up to date.
[[1;34mINFO[m] 
[[1;34mINFO[m] [1m--- [0;32mmaven-resources-plugin:2.6:testResources[m [1m(default-testResources)[m @ [36mjava-ai-assistant[0;1m ---[m
[[1;34mINFO[m] Using 'UTF-8' encoding to copy filtered resources.
[[1;34mINFO[m] skip non existing resourceDirectory /root/exp_SWAT/java-ai-assistant/src/test/resources
[[1;34mINFO[m] 
[[1;34mINFO[m] [1m--- [0;32mmaven-compiler-plugin:3.14.0:testCompile[m [1m(default-testCompile)[m @ [36mjava-ai-assistant[0;1m ---[m
[[1;34mINFO[m] Nothing to compile - all classes are up to date.
[[1;34mINFO[m] 
[[1;34mINFO[m] [1m--- [0;32mmaven-surefire-plugin:3.5.6:test[m [1m(default-test)[m @ [36mjava-ai-assistant[0;1m ---[m
[[1;34mINFO[m] Using auto detected provider org.apache.maven.surefire.junitplatform.JUnitPlatformProvider
[[1;34mINFO[m] 
[[1;34mINFO[m] -------------------------------------------------------
[[1;34mINFO[m]  T E S T S
[[1;34mINFO[m] -------------------------------------------------------
[[1;34mINFO[m] Running assistant.app.[1mMainTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m5[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.679 s -- in assistant.app.[1mMainTest[m
[[1;34mINFO[m] Running assistant.app.[1mDemoDataFactoryTest[m
OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
[[1;34mINFO[m] [1;32mTests run: [0;1;32m4[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 5.688 s -- in assistant.app.[1mDemoDataFactoryTest[m
[[1;34mINFO[m] Running assistant.app.[1mApplicationFactoryTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m8[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.975 s -- in assistant.app.[1mApplicationFactoryTest[m
[[1;34mINFO[m] Running assistant.app.[1mConsoleApplicationTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m25[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.011 s -- in assistant.app.[1mConsoleApplicationTest[m
[[1;34mINFO[m] Running assistant.testability.[1mIncrementalIdGeneratorTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m8[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.122 s -- in assistant.testability.[1mIncrementalIdGeneratorTest[m
[[1;34mINFO[m] Running assistant.testability.[1mFixedTimeProviderTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m6[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.056 s -- in assistant.testability.[1mFixedTimeProviderTest[m
[[1;34mINFO[m] Running assistant.testability.[1mSystemTimeProviderTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m3[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.035 s -- in assistant.testability.[1mSystemTimeProviderTest[m
[[1;34mINFO[m] Running assistant.study.[1mStudyPlanAnalysisServiceTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m12[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.110 s -- in assistant.study.[1mStudyPlanAnalysisServiceTest[m
[[1;34mINFO[m] Running assistant.study.[1mStudyPlanStatusTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m9[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.111 s -- in assistant.study.[1mStudyPlanStatusTest[m
[[1;34mINFO[m] Running assistant.study.[1mStudyPlanQueryTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m11[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.223 s -- in assistant.study.[1mStudyPlanQueryTest[m
[[1;34mINFO[m] Running assistant.study.[1mInMemoryStudyPlanRepositoryTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m17[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.202 s -- in assistant.study.[1mInMemoryStudyPlanRepositoryTest[m
[[1;34mINFO[m] Running assistant.study.[1mStudyPlanViewTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m5[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.103 s -- in assistant.study.[1mStudyPlanViewTest[m
[[1;34mINFO[m] Running assistant.study.[1mStudyPlanServiceTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m37[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.331 s -- in assistant.study.[1mStudyPlanServiceTest[m
[[1;34mINFO[m] Running assistant.study.[1mStudyPlanTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m21[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.277 s -- in assistant.study.[1mStudyPlanTest[m
[[1;34mINFO[m] Running assistant.ai.[1mDraftImportServiceTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m8[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.504 s -- in assistant.ai.[1mDraftImportServiceTest[m
[[1;34mINFO[m] Running assistant.ai.[1mAiResponseTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m2[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.039 s -- in assistant.ai.[1mAiResponseTest[m
[[1;34mINFO[m] Running assistant.ai.[1mAiAssistantServiceTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m10[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.278 s -- in assistant.ai.[1mAiAssistantServiceTest[m
[[1;34mINFO[m] Running assistant.ai.[1mStudyPlanDraftContentTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m3[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.062 s -- in assistant.ai.[1mStudyPlanDraftContentTest[m
[[1;34mINFO[m] Running assistant.ai.[1mInMemorySuggestionDraftRepositoryTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m6[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.060 s -- in assistant.ai.[1mInMemorySuggestionDraftRepositoryTest[m
[[1;34mINFO[m] Running assistant.ai.[1mStructuredSuggestionParserTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m11[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.563 s -- in assistant.ai.[1mStructuredSuggestionParserTest[m
[[1;34mINFO[m] Running assistant.ai.[1mPromptBuilderTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m9[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.174 s -- in assistant.ai.[1mPromptBuilderTest[m
[[1;34mINFO[m] Running assistant.ai.[1mTaskDraftItemTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m3[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.022 s -- in assistant.ai.[1mTaskDraftItemTest[m
[[1;34mINFO[m] Running assistant.ai.[1mSuggestionDraftViewTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m3[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.041 s -- in assistant.ai.[1mSuggestionDraftViewTest[m
[[1;34mINFO[m] Running assistant.ai.[1mAiConfigurationLoaderTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m6[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.051 s -- in assistant.ai.[1mAiConfigurationLoaderTest[m
[[1;34mINFO[m] Running assistant.ai.[1mSuggestionDraftTypeTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m2[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.021 s -- in assistant.ai.[1mSuggestionDraftTypeTest[m
[[1;34mINFO[m] Running assistant.ai.[1mJdkAiHttpTransportTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m8[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.983 s -- in assistant.ai.[1mJdkAiHttpTransportTest[m
[[1;34mINFO[m] Running assistant.ai.[1mAiConfigurationTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m5[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.071 s -- in assistant.ai.[1mAiConfigurationTest[m
[[1;34mINFO[m] Running assistant.ai.[1mSuggestionDraftStatusTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m2[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.018 s -- in assistant.ai.[1mSuggestionDraftStatusTest[m
[[1;34mINFO[m] Running assistant.ai.[1mAiRequestTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m4[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.039 s -- in assistant.ai.[1mAiRequestTest[m
[[1;34mINFO[m] Running assistant.ai.[1mDeepSeekAiClientTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m10[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.503 s -- in assistant.ai.[1mDeepSeekAiClientTest[m
[[1;34mINFO[m] Running assistant.ai.[1mDraftLifecycleServiceTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m14[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.218 s -- in assistant.ai.[1mDraftLifecycleServiceTest[m
[[1;34mINFO[m] Running assistant.ai.[1mAiScenarioTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m3[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.024 s -- in assistant.ai.[1mAiScenarioTest[m
[[1;34mINFO[m] Running assistant.ai.[1mAiErrorMapperTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m4[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.037 s -- in assistant.ai.[1mAiErrorMapperTest[m
[[1;34mINFO[m] Running assistant.ai.[1mSuggestionDraftTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m6[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.043 s -- in assistant.ai.[1mSuggestionDraftTest[m
[[1;34mINFO[m] Running assistant.ai.[1mAiMessageTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m4[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.031 s -- in assistant.ai.[1mAiMessageTest[m
[[1;34mINFO[m] Running assistant.summary.[1mLocalContextTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m5[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.100 s -- in assistant.summary.[1mLocalContextTest[m
[[1;34mINFO[m] Running assistant.summary.[1mSummaryServiceTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m11[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.018 s -- in assistant.summary.[1mSummaryServiceTest[m
[[1;34mINFO[m] Running assistant.summary.[1mDashboardSummaryTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m4[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.071 s -- in assistant.summary.[1mDashboardSummaryTest[m
[[1;34mINFO[m] Running assistant.schedule.[1mScheduleServiceTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m36[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.246 s -- in assistant.schedule.[1mScheduleServiceTest[m
[[1;34mINFO[m] Running assistant.schedule.[1mInMemoryScheduleRepositoryTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m13[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.094 s -- in assistant.schedule.[1mInMemoryScheduleRepositoryTest[m
[[1;34mINFO[m] Running assistant.schedule.[1mScheduleViewTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m10[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.078 s -- in assistant.schedule.[1mScheduleViewTest[m
[[1;34mINFO[m] Running assistant.schedule.[1mScheduleItemTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m27[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.133 s -- in assistant.schedule.[1mScheduleItemTest[m
[[1;34mINFO[m] Running assistant.schedule.[1mScheduleConflictPolicyTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m17[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.106 s -- in assistant.schedule.[1mScheduleConflictPolicyTest[m
[[1;34mINFO[m] Running assistant.schedule.[1mScheduleStatusTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m8[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.026 s -- in assistant.schedule.[1mScheduleStatusTest[m
[[1;34mINFO[m] Running assistant.schedule.[1mScheduleQueryTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m10[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.047 s -- in assistant.schedule.[1mScheduleQueryTest[m
[[1;34mINFO[m] Running assistant.note.[1mNoteViewTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m8[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.048 s -- in assistant.note.[1mNoteViewTest[m
[[1;34mINFO[m] Running assistant.note.[1mInMemoryNoteRepositoryTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m13[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.053 s -- in assistant.note.[1mInMemoryNoteRepositoryTest[m
[[1;34mINFO[m] Running assistant.note.[1mNoteServiceTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m25[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.230 s -- in assistant.note.[1mNoteServiceTest[m
[[1;34mINFO[m] Running assistant.note.[1mNoteTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m19[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.103 s -- in assistant.note.[1mNoteTest[m
[[1;34mINFO[m] Running assistant.note.[1mNoteSearchPolicyTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m11[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.052 s -- in assistant.note.[1mNoteSearchPolicyTest[m
[[1;34mINFO[m] Running assistant.note.[1mNoteQueryTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m7[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.050 s -- in assistant.note.[1mNoteQueryTest[m
[[1;34mINFO[m] Running assistant.common.[1mTagTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m15[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.056 s -- in assistant.common.[1mTagTest[m
[[1;34mINFO[m] Running assistant.common.[1mEntityIdTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m9[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.041 s -- in assistant.common.[1mEntityIdTest[m
[[1;34mINFO[m] Running assistant.common.[1mDateTimeRangeTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m27[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.144 s -- in assistant.common.[1mDateTimeRangeTest[m
[[1;34mINFO[m] Running assistant.common.[1mTransactionAmountTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m15[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.098 s -- in assistant.common.[1mTransactionAmountTest[m
[[1;34mINFO[m] Running assistant.common.[1mBusinessExceptionTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m6[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.048 s -- in assistant.common.[1mBusinessExceptionTest[m
[[1;34mINFO[m] Running assistant.common.[1mMoneyValueTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m23[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.110 s -- in assistant.common.[1mMoneyValueTest[m
[[1;34mINFO[m] Running assistant.common.[1mErrorCodeTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m2[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.015 s -- in assistant.common.[1mErrorCodeTest[m
[[1;34mINFO[m] Running assistant.common.[1mOperationResultTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m9[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.044 s -- in assistant.common.[1mOperationResultTest[m
[[1;34mINFO[m] Running assistant.common.[1mDateRangeTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m20[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.117 s -- in assistant.common.[1mDateRangeTest[m
[[1;34mINFO[m] Running assistant.common.[1mProgressTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m10[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.044 s -- in assistant.common.[1mProgressTest[m
[[1;34mINFO[m] Running assistant.task.[1mTaskViewTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m8[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.052 s -- in assistant.task.[1mTaskViewTest[m
[[1;34mINFO[m] Running assistant.task.[1mInMemoryTaskRepositoryTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m13[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.067 s -- in assistant.task.[1mInMemoryTaskRepositoryTest[m
[[1;34mINFO[m] Running assistant.task.[1mTaskItemTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m29[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.121 s -- in assistant.task.[1mTaskItemTest[m
[[1;34mINFO[m] Running assistant.task.[1mTaskPriorityTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m5[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.035 s -- in assistant.task.[1mTaskPriorityTest[m
[[1;34mINFO[m] Running assistant.task.[1mTaskStatusTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m6[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.019 s -- in assistant.task.[1mTaskStatusTest[m
[[1;34mINFO[m] Running assistant.task.[1mTaskServiceTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m41[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.242 s -- in assistant.task.[1mTaskServiceTest[m
[[1;34mINFO[m] Running assistant.task.[1mTaskQueryTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m9[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.054 s -- in assistant.task.[1mTaskQueryTest[m
[[1;34mINFO[m] Running assistant.finance.[1mFinanceStatisticsServiceTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m8[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.047 s -- in assistant.finance.[1mFinanceStatisticsServiceTest[m
[[1;34mINFO[m] Running assistant.finance.[1mFinanceStatisticsTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m11[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.046 s -- in assistant.finance.[1mFinanceStatisticsTest[m
[[1;34mINFO[m] Running assistant.finance.[1mTransactionRecordTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m13[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.046 s -- in assistant.finance.[1mTransactionRecordTest[m
[[1;34mINFO[m] Running assistant.finance.[1mTransactionQueryTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m10[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.043 s -- in assistant.finance.[1mTransactionQueryTest[m
[[1;34mINFO[m] Running assistant.finance.[1mTransactionViewTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m6[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.023 s -- in assistant.finance.[1mTransactionViewTest[m
[[1;34mINFO[m] Running assistant.finance.[1mFinanceServiceTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m24[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.161 s -- in assistant.finance.[1mFinanceServiceTest[m
[[1;34mINFO[m] Running assistant.finance.[1mInMemoryTransactionRepositoryTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m15[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.063 s -- in assistant.finance.[1mInMemoryTransactionRepositoryTest[m
[[1;34mINFO[m] Running assistant.finance.[1mTransactionTypeTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m5[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.019 s -- in assistant.finance.[1mTransactionTypeTest[m
[[1;34mINFO[m] 
[[1;34mINFO[m] Results:
[[1;34mINFO[m] 
[[1;34mINFO[m] [1;32mTests run: 857, Failures: 0, Errors: 0, Skipped: 0[m
[[1;34mINFO[m] 
[[1;34mINFO[m] [1m------------------------------------------------------------------------[m
[[1;34mINFO[m] [1;32mBUILD SUCCESS[m
[[1;34mINFO[m] [1m------------------------------------------------------------------------[m
[[1;34mINFO[m] Total time:  31.239 s
[[1;34mINFO[m] Finished at: 2026-06-12T12:50:30Z
[[1;34mINFO[m] [1m------------------------------------------------------------------------[m

```
