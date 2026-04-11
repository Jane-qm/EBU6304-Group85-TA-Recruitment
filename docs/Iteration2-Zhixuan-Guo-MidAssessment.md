# Iteration 2 中期整理（Zhixuan Guo / `Zhixuan_GUO`）

> 本文档面向中期验收：从**需求理解**、**团队分工与个人贡献**、**技术栈与对系统的理解**三方面整理。  
> 分支说明：远程分支名为 **`Zhixuan_GUO`**（与手写 `Zhixuan_Guo` 为同一分支）。  
> 基线：已将 **`origin/main` 最新代码合并进 `Zhixuan_GUO`**，并与 **`iteration2-release` 标签**对齐。

---

## 1. 软件需求理解（Software requirements）

### 1.1 业务目标

本项目为 **BUPT International School TA Recruitment** 的桌面端原型：支持 **TA 申请**、**MO 发布岗位与处理申请/发 Offer**、**Admin 监管与配置**，数据以 **本地文件（JSON 等）** 持久化，**不使用数据库**（课程约束）。

### 1.2 角色与核心用例（Iteration 2 视角）

| 角色 | 典型需求 | 当前实现方向（与代码/README 对齐） |
|------|----------|--------------------------------------|
| **TA** | 维护资料与 CV、浏览岗位、提交/跟踪申请、接收通知与 Offer | TA 门户多面板（Dashboard / Catalog / Applications / Profile / Workload 等）、申请控制器、通知弹窗 |
| **MO** | 发布与管理岗位、审阅申请人、发 Offer、与 TA 侧状态协同 | MO Dashboard 与岗位/申请人相关面板（随 `main` 演进） |
| **Admin** | 账号审批与禁用、全站数据查看、CSV 导出、系统级配置（如申请周期） | `AdminHomeFrame` 多 Tab；与 `ta.*` DAO 数据源对齐 |
| **全体** | 登录注册、权限与账号状态、数据落盘可恢复 | `LoginFrame` + `PermissionService` + `JsonPersistenceManager` 初始化 `data/` |

### 1.3 非功能需求（我们答辩时常强调）

- **可运行性**：`mvn compile` + `mvn exec:java`，从仓库根运行以保证 `./data` 路径正确。  
- **可维护性**：JSON + Gson 统一工具；状态/通知类型逐步从魔法字符串收敛到常量（随迭代演进）。  
- **协作性**：GitHub PR 合并、`main` 为集成基线；个人分支合并 `main` 后再做 release。

---

## 2. 团队分工与个人贡献（Team roles & individual contribution）

### 2.1 分工原则（口头可讲）

- **横向**：按角色域（TA / MO / Admin / Auth / Common）拆分界面与业务。  
- **纵向**：公共能力（持久化、实体、跨角色服务）放 **`common.*`**；TA 专属交互放 **`ta.*`**；MO 放 **`mo.*`**。  
- **集成**：以 `main` 为唯一可信集成线；个人分支定期 merge / rebase，减少冲突窗口。

### 2.2 本人（Zhixuan Guo / GitHub `Jane-qm`）主要个人贡献（对照 `git log`）

> 下列为历史提交中可代表个人主线的摘要（具体 hash 以仓库为准）。

1. **基础设施与 JSON 持久化**
   - 引入并维护 **Gson + `JsonPersistenceManager`** 思路：`data/*.json` 初始化、`data/cvs/` 等目录准备、与 `Main` 启动流程衔接。  
   - 相关提交主题示例：`feat: implement Gson persistence foundation...`、`feat(iter2): extend JSON bootstrap...`。

2. **跨角色共享领域与服务**
   - 在 Iteration 2 阶段推动 **`ApplicationStatus` / `NotificationKind`** 等共享常量，统一申请状态与通知类型表达，降低 TA/MO/通知逻辑分叉成本。  
   - 扩展 **申请 / Offer / 通知** 相关服务层能力（如待审队列、waitlist hook、Offer 接受后对 MO 的通知等，以当时合入版本为准）。

3. **工程协作与分支健康**
   - 多次 **merge PR、解决合并冲突**（例如 `UserFileDAO` 与持久化构造器在合并后损坏的修复类提交）。  
   - 在团队调整范围时做过 **revert / scope control**（例如收缩不适合当前里程碑的 TA UI 实验），保证 `main` 可演示。

### 2.3 其他成员（答辩时一句话带过，避免抢功）

- **Can Chen**：TA 申请/交互与数据侧改进等（以 `Can_Chen/*` PR 为准）。  
- **Yiping Zheng**：登录 UI、迭代一文档与部分集成。  
- **Yanwen Chen / Jiayi Lou / Jiaze Wang / …**：Admin、MO 面板、测试用例文档、配置与导出等（以各自 PR 描述为准）。

---

## 3. 软件技术栈与开发者对系统的理解（Tech stack & mental model）

### 3.1 技术栈一览

| 层级 | 技术 | 说明 |
|------|------|------|
| 语言 / 构建 | **Java 17** + **Maven** | `pom.xml` 指定编译级别；`mvn exec:java` 启动 Swing。 |
| UI | **Java Swing** | `JFrame` / `BaseFrame`、表格与自定义渲染、卡片布局等。 |
| 持久化 | **JSON + Gson** | `common.util.GsonUtils`（若存在）与 `JsonPersistenceManager` 管理文件清单。 |
| 数据 | **本地 `data/` 目录** | 用户、岗位、申请、Offer、通知、TA 资料与 CV 元数据等 JSON 列表。 |
| 协作 | **Git + GitHub PR** | `main` 集成；标签用于里程碑（见下节 release）。 |

### 3.2 我对系统结构的「心智模型」（可对着目录讲）

1. **入口**：`Main` → 初始化 JSON → `LoginFrame`。  
2. **认证与路由**：`auth.*` + `PermissionService` / `UserService` → 按角色进入 TA / MO / Admin 首页。  
3. **领域数据**：`common.entity.*` + `ta.entity.*` 等 POJO，序列化进 JSON。  
4. **业务服务**：`common.service.*`（跨角色）与 `ta.service.*` / `mo.*`（子域）分工。  
5. **DAO**：`common.dao.*` 与 `ta.dao.*` 负责具体 JSON 文件的读写列表。  
6. **UI 分层**：Controller（若存在）协调 Service；Frame/Panel 只做展示与事件转发。

### 3.3 运行与演示注意

```bash
cd <repo-root>
mvn -q -DskipTests compile
mvn exec:java
```

- 必须在**仓库根**执行，否则相对路径 `./data` 可能指向错误目录。  
- 演示账号以团队 `data/users.json` 为准；**勿在公开文档中粘贴真实密码**。

---

## 4. Iteration 2 Release（本分支里程碑）

- **Release 标签**：`iteration2-release`  
- **含义**：`Zhixuan_GUO` 已与 **`origin/main`（含 Iteration 2 相关合入，如 `feat/iter2-infrastructure` 合并后主线）** 对齐，可作为中期验收演示基线。  
- **获取方式**：

```bash
git fetch origin
git checkout iteration2-release   # 或: git checkout Zhixuan_GUO
```

---

*文档作者：Zhixuan Guo（`Jane-qm`）*  
*最后更新：以本文件提交日期为准*
