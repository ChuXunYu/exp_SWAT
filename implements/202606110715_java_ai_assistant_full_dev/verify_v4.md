# 验证报告（v4）

## 结果
PASSED

## 统计
- 通过：90
- 失败：0

## 测试执行日志

```text
$ cd /root/exp_SWAT/java-ai-assistant && mvn test
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
[[1;34mINFO[m] [1;32mTests run: [0;1;32m8[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.383 s -- in assistant.testability.[1mIncrementalIdGeneratorTest[m
[[1;34mINFO[m] Running assistant.testability.[1mFixedTimeProviderTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m6[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.113 s -- in assistant.testability.[1mFixedTimeProviderTest[m
[[1;34mINFO[m] Running assistant.testability.[1mSystemTimeProviderTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m3[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.105 s -- in assistant.testability.[1mSystemTimeProviderTest[m
[[1;34mINFO[m] Running assistant.common.[1mEntityIdTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m9[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.142 s -- in assistant.common.[1mEntityIdTest[m
[[1;34mINFO[m] Running assistant.common.[1mDateTimeRangeTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m27[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.284 s -- in assistant.common.[1mDateTimeRangeTest[m
[[1;34mINFO[m] Running assistant.common.[1mBusinessExceptionTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m6[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.073 s -- in assistant.common.[1mBusinessExceptionTest[m
[[1;34mINFO[m] Running assistant.common.[1mErrorCodeTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m2[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.023 s -- in assistant.common.[1mErrorCodeTest[m
[[1;34mINFO[m] Running assistant.common.[1mOperationResultTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m9[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.079 s -- in assistant.common.[1mOperationResultTest[m
[[1;34mINFO[m] Running assistant.common.[1mDateRangeTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m20[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.128 s -- in assistant.common.[1mDateRangeTest[m
[[1;34mINFO[m] 
[[1;34mINFO[m] Results:
[[1;34mINFO[m] 
[[1;34mINFO[m] [1;32mTests run: 90, Failures: 0, Errors: 0, Skipped: 0[m
[[1;34mINFO[m] 
[[1;34mINFO[m] [1m------------------------------------------------------------------------[m
[[1;34mINFO[m] [1;32mBUILD SUCCESS[m
[[1;34mINFO[m] [1m------------------------------------------------------------------------[m
[[1;34mINFO[m] Total time:  7.352 s
[[1;34mINFO[m] Finished at: 2026-06-11T10:59:04Z
[[1;34mINFO[m] [1m------------------------------------------------------------------------[m

```

## 推送记录

```text
$ git push origin 202606110715_java_ai_assistant_full_dev
To https://github.com/ChuXunYu/exp_SWAT.git
   8cbfc5e..452d321  202606110715_java_ai_assistant_full_dev -> 202606110715_java_ai_assistant_full_dev

```
