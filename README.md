# 审批系统说明

## TODO

该版本使用模块化动态表单配置，目前功能基本实现，模版的数据处理放在后端进行。

总的操作流程为：在"**已发任务新**"发起任务，随后其他用户在"**待办任务**"进行审批，最后可以在"**已发任务**"页面生成签名文件（需要正确配置模版）。

部分复杂表格单出行列配置，并拆成多个模版。

![动态表单配置页面](./doc/image1.png)
*图表 1 - 动态表单配置页面*

![模版配置](./doc/image2.png)
*图表 2 - 模版配置*

![表单页面](./doc/image3.png)
*图表 3 - 表单页面*

![任务完成后的状态](./doc/image4.png)
*图表 4 - 任务完成后的状态*

---

## 部署教程

### 1. 环境准备

建议使用以下版本（与项目当前依赖保持一致）：

- JDK 1.8 或 17
- Maven 3.6+
- MySQL 5.7 或 8.0
- Redis 6.x（或兼容版本）

### 2. 初始化数据库

1. 创建数据库（示例）：

```sql
CREATE DATABASE `exam-flow` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

2. 执行项目内 SQL 初始化脚本（按你的业务需要选择 `doc/` 或 `xzit-core/src/main/resources/sql/` 下的脚本）。

### 3. 配置敏感参数（必须）

敏感参数支持两种注入方式：

1. 通过环境变量注入（推荐，尤其是 QA/生产环境）
2. 直接修改 `application-dev.yml` / `application-qa.yml` / `application.yml`

启动前请确保以下参数已正确配置（无论采用哪种方式）：

- `ALIYUN_ACCESS_KEY_ID`（必填）
- `ALIYUN_ACCESS_KEY_SECRET`（必填）
- `ALIYUN_OSS_ENDPOINT`（必填）
- `ALIYUN_OSS_BUCKET_NAME`（必填）
- `DB_MASTER_USERNAME`
- `DB_MASTER_PASSWORD`
- `DRUID_LOGIN_USERNAME`
- `DRUID_LOGIN_PASSWORD`
- `REDIS_PASSWORD`
- `TOKEN_SECRET`
- `WECHAT_APP_ID`
- `WECHAT_APP_SECRET`

其中 OSS 为系统文件上传/下载的必要组件，以上 `ALIYUN_OSS_*` 与阿里云密钥必须正确配置，否则文件相关功能不可用。

### 4. 本地开发环境启动

1. 使用 `dev` 配置启动（默认 `application.yml` 已激活 `dev`）：

```bash
mvn clean package -DskipTests
mvn spring-boot:run -pl xzit-starter -am
```

2. 或直接运行打包后的 jar（先打包再启动）：

```bash
java -jar xzit-starter/target/xzit-starter.jar --spring.profiles.active=dev
```

### 5. QA / 生产环境部署

1. 打包：

```bash
mvn clean package -DskipTests
```

2. 通过 profile 指定环境：

```bash
java -jar xzit-starter/target/xzit-starter.jar --spring.profiles.active=qa
```

3. 建议配合进程守护（如 systemd / supervisord）并配置日志滚动。

### 6. 端口与访问

- 默认端口：`8080`
- 默认上下文路径：`/`
- 若需修改，请调整 `xzit-starter/src/main/resources/application.yml` 中的 `server.port` 和 `server.servlet.context-path`

### 7. 常见问题排查

- 启动时报 `Could not resolve placeholder`：通常是缺少环境变量，请先补齐变量。
- 数据库连接失败：检查 `application-*.yml` 中数据源地址、账号密码和数据库权限。
- Redis 连接失败：确认 Redis 可访问、密码与 `REDIS_PASSWORD` 一致。
- 上传文件失败（OSS）：检查 `ALIYUN_*` 变量及 bucket 权限策略。

---

## feature/optimize（v3 新版本）

考虑审批的一些数据要从达成度进行调取，模块方案不好实现该功能，因为不同表单同一模块要获取的内容可能不同，所以将模块化表单改为单个 Vue 界面从而获得更高的自由度。

目前已经实现达成评价报告的部分内容从达成度系统中获取数据作为下拉列表中的内容给出。

- 毕业要求的下拉列表中的内容**来自达成度系统**
- 几个表格的行数是在选择完"**入学年级-专业-课程**"自动补全的，数据同样来自于达成度

模版还是复用的 v2 的那一套，数据构建全部放在前端，后端不需要再根据流程 id 分别进行复杂的数据解析操作。

目前 v3 只完成了**课程评价达成报告**的导出，其他流程暂未开发。

![v3 管理页面](./doc/image5.png)
*图表 5 - v3 管理页面*

![达成评价报告 1](./doc/image6.png)
*图表 6 - 达成评价报告 1*

![达成评价报告 2](./doc/image7.png)
*图表 7 - 达成评价报告 2*

![达成评价报告 3](./doc/image8.png)
*图表 8 - 达成评价报告 3*

---
