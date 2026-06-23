智能旅游助手 - 项目技术文档
基于 AI 大语言模型的全栈 Web 应用 | Vue 3 + Spring Boot | SSE 流式对话 | 智能行程规划

📖 目录
项目简介

核心功能

技术架构

技术栈总览

项目结构

核心实现要点

API 接口

快速启动

技术亮点

项目成果

项目简介
智能旅游助手 是一个基于 AI 大语言模型的智能旅游规划平台。用户输入目的地、天数、预算，系统自动生成包含每日行程、景点详情、交通建议、预算拆解和实用贴士的完整旅游规划。

同时支持与 AI 助手进行 SSE 流式实时对话，实现打字机效果的智能问答体验。

项目定位：前后端完全分离，移动端优先，AI 深度集成，生产级代码质量。

核心功能
功能	说明
🗺️ 智能行程规划	根据目的地、天数、预算，AI 自动生成完整行程（含每日安排、景点、交通、门票、餐饮）
💬 AI 实时对话	基于 SSE 流式响应，打字机效果，支持多轮对话和追问
📊 预算明细拆解	自动拆分住宿、餐饮、交通、门票、其他等费用
📱 移动端适配	Vant 4 组件库，完美适配手机屏幕
⚠️ 贴心提示	提供旅游小贴士和注意事项
👤 个人中心	用户信息展示、功能菜单入口
技术架构
text
┌─────────────────────────────────────────────────┐
│                   浏览器（移动端）               │
│    ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐      │
│    │ 首页 │ │ 对话 │ │ 详情 │ │ 个人 │      │
│    └──┬───┘ └──┬───┘ └──┬───┘ └──┬───┘      │
│       └────────┴────────┴────────┘            │
│              Vue 3 + Vant 4                    │
│           Axios + SSE 流式处理                 │
└───────────────────┬─────────────────────────────┘
                    │ HTTP / SSE
                    ▼
┌─────────────────────────────────────────────────┐
│              Spring Boot 4.1 后端               │
│   Controller → Service → Utils(LLM封装)       │
└───────────────────┬─────────────────────────────┘
                    │ HTTPS
                    ▼
┌─────────────────────────────────────────────────┐
│              大语言模型 API                     │
│           (OpenAI 兼容格式)                    │
└─────────────────────────────────────────────────┘
技术栈总览
前端
技术	版本	用途
Vue 3	^3.5.32	核心框架，Composition API
Vite	^8.0.10	构建工具，极速热更新
Vant 4	^4.9.24	移动端 UI 组件库
Vue Router	^4.3.0	路由管理（懒加载）
Axios	^1.15.2	HTTP 请求 + SSE 扩展
后端
技术	版本	用途
Java	17	开发语言
Spring Boot	4.1.0	核心框架
Spring Web MVC	-	RESTful API
OkHttp3	4.12.0	调用 LLM API
Jackson	-	JSON 序列化
Jakarta Validation	-	参数校验
Lombok	-	代码简化
项目结构
前端
text
frontend/
├── views/
│   ├── HomeView.vue       # 首页 - 行程规划表单
│   ├── ChatView.vue       # 对话页 - SSE 流式对话
│   ├── DetailView.vue     # 详情页 - 行程展示
│   └── ProfileView.vue    # 个人页 - 用户中心
├── components/
│   ├── ChatBubble.vue     # 对话气泡
│   ├── SpotItem.vue       # 景点条目
│   └── BudgetTable.vue    # 预算明细表
├── utils/
│   └── request.js         # Axios + SSE 封装
├── router/
│   └── index.js           # 路由配置
└── App.vue                # 根组件（含底部导航）
后端
text
backend/
├── controller/
│   └── TravelController.java     # 请求入口
├── service/
│   └── TravelService.java        # 业务逻辑
├── utils/
│   └── LLMUtils.java             # LLM API 封装
├── dto/
│   ├── TravelRequestDTO.java
│   └── ChatRequestDTO.java
├── vo/
│   ├── Result.java               # 统一响应
│   ├── TravelRecommendVO.java
│   └── StreamChunkVO.java
└── config/
    └── CorsConfig.java           # 跨域配置
核心实现要点
前端
要点	实现方式
响应式数据	ref() / reactive()
路由懒加载	动态 import()
统一请求封装	Axios 实例 + 拦截器
SSE 流式处理	fetch + ReadableStream + TextDecoder
打字机效果	逐字回调 + onChunk 实时更新
移动端 UI	Vant 4 组件库
状态管理	组件内状态（轻量级，未引入 Pinia）
后端
要点	实现方式
分层架构	Controller → Service → Utils
SSE 流式	SseEmitter + CompletableFuture 异步
LLM 调用	OkHttp3 同步 + 流式（SSE）
JSON 智能提取	支持 Markdown 代码块和纯 JSON
参数校验	@Valid + Jakarta Validation
统一响应	Result<T> 泛型封装
全局异常	@RestControllerAdvice
跨域配置	CORS 全局配置
API 接口
方法	路径	说明	响应格式
GET	/api/travel/hello	健康检查	JSON
POST	/api/travel/recommend	行程推荐	JSON
POST	/api/travel/chat	AI 流式对话	SSE (text/event-stream)
请求/响应示例
POST /api/travel/recommend

json
// 请求
{ "city": "北京", "days": 2, "budget": 5000 }

// 响应
{
  "code": 200,
  "data": {
    "city": "北京",
    "days": 2,
    "totalBudget": 5000,
    "dailyItinerary": [...],
    "budgetBreakdown": {...},
    "tips": [...],
    "warnings": [...]
  },
  "success": true
}
POST /api/travel/chat（SSE 流式）

text
data: {"type":"chunk","content":"北京"}
data: {"type":"chunk","content":"是"}
data: {"type":"chunk","content":"一座"}
...
data: [DONE]
快速启动
前置条件
Node.js 18+

Java 17+

Maven 3.8+

启动前端
bash
cd frontend
npm install
npm run dev
# 访问 http://localhost:5173
启动后端
bash
cd backend
mvn spring-boot:run
# 服务运行在 http://127.0.0.1:3200
环境变量配置
前端（.env）
text
VITE_API_BASE_URL=http://127.0.0.1:3200/api
后端（application.yml）
yaml
llm:
  api:
    key: ${LLM_API_KEY:}        # 从环境变量读取
    base-url: ${LLM_API_BASE_URL:}
技术亮点
类别	亮点	说明
前端	SSE 流式对话	基于 fetch + ReadableStream 实现打字机效果
前端	组件复用	ChatBubble、SpotItem、BudgetTable 多页面共享
前端	移动端适配	Vant 4 + 750px 最大宽度限制
后端	异步流式处理	SseEmitter + CompletableFuture 非阻塞
后端	智能 JSON 提取	自动从 Markdown 代码块或纯文本中提取 JSON
后端	统一异常处理	@RestControllerAdvice 全局捕获
架构	前后端分离	独立部署 + CORS 配置
架构	DTO/VO 分离	请求/响应对象解耦
项目成果
✅ 4 个核心页面：首页、对话页、详情页、个人页

✅ 5+ 可复用组件：气泡、景点卡片、预算表格等

✅ 3 个核心接口：推荐生成、流式对话、健康检查

✅ 完整前后端联调：跨域配置 + 统一响应格式

✅ SSE 流式对话：实时打字机效果

✅ AI 深度集成：OkHttp3 + 大语言模型

许可证
MIT License

📌 项目特点：技术栈主流、分层清晰、AI 集成落地、移动端体验优化、前后端完全分离、生产级代码质量。

