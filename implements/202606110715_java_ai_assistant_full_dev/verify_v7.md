# 验证报告（v7）

## 结果
PASSED

## 统计
- 通过：193
- 失败：0
- 错误：0
- 跳过：0
- 测试命令：`mvn test`

## 测试执行日志

```text
$ mvn test
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
[[1;34mINFO[m] Running assistant.testability.[1mIncrementalIdGeneratorTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m8[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.358 s -- in assistant.testability.[1mIncrementalIdGeneratorTest[m
[[1;34mINFO[m] Running assistant.testability.[1mFixedTimeProviderTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m6[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.108 s -- in assistant.testability.[1mFixedTimeProviderTest[m
[[1;34mINFO[m] Running assistant.testability.[1mSystemTimeProviderTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m3[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.091 s -- in assistant.testability.[1mSystemTimeProviderTest[m
[[1;34mINFO[m] Running assistant.common.[1mTagTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m15[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.201 s -- in assistant.common.[1mTagTest[m
[[1;34mINFO[m] Running assistant.common.[1mEntityIdTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m9[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.113 s -- in assistant.common.[1mEntityIdTest[m
[[1;34mINFO[m] Running assistant.common.[1mDateTimeRangeTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m27[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.216 s -- in assistant.common.[1mDateTimeRangeTest[m
[[1;34mINFO[m] Running assistant.common.[1mTransactionAmountTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m15[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.150 s -- in assistant.common.[1mTransactionAmountTest[m
[[1;34mINFO[m] Running assistant.common.[1mBusinessExceptionTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m6[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.058 s -- in assistant.common.[1mBusinessExceptionTest[m
[[1;34mINFO[m] Running assistant.common.[1mMoneyValueTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m23[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.118 s -- in assistant.common.[1mMoneyValueTest[m
[[1;34mINFO[m] Running assistant.common.[1mErrorCodeTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m2[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.020 s -- in assistant.common.[1mErrorCodeTest[m
[[1;34mINFO[m] Running assistant.common.[1mOperationResultTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m9[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.046 s -- in assistant.common.[1mOperationResultTest[m
[[1;34mINFO[m] Running assistant.common.[1mDateRangeTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m20[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.088 s -- in assistant.common.[1mDateRangeTest[m
[[1;34mINFO[m] Running assistant.common.[1mProgressTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m10[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.059 s -- in assistant.common.[1mProgressTest[m
[[1;34mINFO[m] Running assistant.task.[1mTaskItemTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m29[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.180 s -- in assistant.task.[1mTaskItemTest[m
[[1;34mINFO[m] Running assistant.task.[1mTaskPriorityTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m5[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.039 s -- in assistant.task.[1mTaskPriorityTest[m
[[1;34mINFO[m] Running assistant.task.[1mTaskStatusTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m6[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.037 s -- in assistant.task.[1mTaskStatusTest[m
[[1;34mINFO[m] 
[[1;34mINFO[m] Results:
[[1;34mINFO[m] 
[[1;34mINFO[m] [1;32mTests run: 193, Failures: 0, Errors: 0, Skipped: 0[m
[[1;34mINFO[m] 
[[1;34mINFO[m] [1m------------------------------------------------------------------------[m
[[1;34mINFO[m] [1;32mBUILD SUCCESS[m
[[1;34mINFO[m] [1m------------------------------------------------------------------------[m
[[1;34mINFO[m] Total time:  8.032 s
[[1;34mINFO[m] Finished at: 2026-06-11T13:33:14Z
[[1;34mINFO[m] [1m------------------------------------------------------------------------[m

[exit code] 0
```
