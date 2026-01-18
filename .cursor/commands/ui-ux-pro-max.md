# UI-UX Pro Max (Vue 3 + Element Plus 深度定制版)

本指南是针对 Web 和移动端应用的全面设计手册，包含 50+ 样式、97 种配色方案及 99 条 UX 准则。已针对 Vue 3 生态系统进行专项优化，旨在通过 AI 驱动生成具备大厂质感的工业级前端代码。

---

## 🛠 技术栈锁定 (Tech Stack)

在执行任何代码生成任务时，必须严格遵守以下技术规范，严禁生成原生 HTML 或其他框架代码：

| 维度 | 技术选型 | 版本要求 |
| :--- | :--- | :--- |
| **核心框架** | Vue 3 (Composition API / `<script setup>`) | 3.3.4+ |
| **UI 组件库** | Element Plus (优先使用 el- 组件) | 2.3.14+ |
| **CSS 框架** | Tailwind CSS (必须使用 Utility-first 模式) | 4.1.18+ |
| **图表库** | ECharts | 5.4.3+ |
| **图标库** | Lucide Vue Next / Element Plus Icons | 严禁使用 Emoji |

---

## 🚀 核心工作流 (Workflow)

当用户请求 UI/UX 相关工作（设计、构建、审查、修复、改进）时，必须执行以下流程：

### 第一步：需求分析 (Analyze User Requirements)

从用户请求中提取：
- **Product type**: 产品类型（SaaS、仪表盘、门户等）
- **Style keywords**: 视觉风格（极简、专业、科技感等）
- **Industry**: 行业领域（金融、医疗等）

### 第二步：生成设计系统 (Generate Design System - REQUIRED)

在编写任何 Vue 代码前，**必须**运行搜索脚本以获取基于数据驱动的设计建议。请注意，脚本路径和参数必须保持英文原样：

```bash
# 示例：python3 .shared/ui-ux-pro-max/scripts/search.py "iot dashboard industrial professional" --design-system --stack vue
python3 .shared/ui-ux-pro-max/scripts/search.py "<product_type> <industry> <keywords>" --design-system --stack vue
```

### 第三步：详细域搜索 (Supplement with Detailed Searches - as needed)

获取设计系统后，可使用域搜索获取更多详细信息。`--domain` 后的参数必须保持英文原词：

  - **图表建议 (Chart)**: `python3 .shared/ui-ux-pro-max/scripts/search.py "real-time dashboard" --domain chart`
  - **交互规范 (UX)**: `python3 .shared/ui-ux-pro-max/scripts/search.py "complex form validation" --domain ux`
  - **样式细节 (Style)**: `python3 .shared/ui-ux-pro-max/scripts/search.py "glassmorphism dark" --domain style`

---

## 🎨 专业 UI 准则 (Common Rules for Professional UI)

以下是一些经常被忽视但会让 UI 看起来不专业的问题，必须严格遵守：

### 1. 视觉质量与图标 (Icons & Visual Elements)

  - **禁用 Emoji**: UI 图标必须使用 SVG 组件（如 Heroicons、Lucide），严禁使用 🚀 ⚙️ 等表情符号作为 UI 图标。
  - **Element Plus 增强**: 不要直接使用 `el-button` 的默认样式，必须通过 Tailwind 增加阴影、缩放等细节（如 `hover:shadow-md transition-transform`）。
  - **中文排版**: 必须优化中文字体声明，包含 `PingFang SC, Microsoft YaHei`，并保持行高在 1.6 左右。
  - **稳定悬停状态**: 在悬停时使用颜色/不透明度过渡，避免使用导致布局移动的缩放变换。
  - **一致的图标尺寸**: 使用固定的 viewBox (24x24) 配合 `w-6 h-6`，避免随机混合不同图标尺寸。

### 2. 交互与反馈 (Interaction & Cursor)

  - **手形光标**: 为所有可点击/可悬停的卡片、元素显式添加 `cursor-pointer`。
  - **悬停反馈**: 必须提供清晰的视觉反馈（颜色、阴影、边框），明确元素可交互。
  - **平滑过渡**: 所有颜色、投影、位移变化必须添加 `transition-all duration-300`，避免即时状态更改或过慢（>500ms）。

### 3. 布局与间距 (Layout & Spacing)

  - **悬浮导航**: 推荐使用 `top-4 left-4 right-4` 的悬浮式导航栏，并配合 `backdrop-blur-md` 效果，同时考虑内容填充避免遮挡。
  - **容器一致性**: 严格使用相同的 `max-w-6xl` 或 `max-w-7xl` 等自定义全局宽度限制，确保视觉重心集中。
  - **表单标签与输入框必须同一行**: 所有表单的标签（label）和输入框（input）必须在同一行水平排列，标签在左，输入框在右。禁止标签在上方、输入框在下方的垂直布局。提示文字可放在输入框下方或右侧，但不影响标签与输入框的水平对齐。

---

## ✅ 交付前自检清单 (Pre-Delivery Checklist)

在输出最终 Vue 代码前，AI 必须完成以下所有项目的自检：

### Visual Quality (视觉质量)
- [ ] 未使用表情符号作为图标（已替换为 SVG）。
- [ ] 所有图标来自一致的图标集 (Heroicons/Lucide)。
- [ ] 品牌 Logo 正确（已从 Simple Icons 验证）。
- [ ] 悬停状态不会导致布局移动。
- [ ] 直接使用主题颜色 (bg-primary)，不使用 `var()` 包装。

### Interaction (交互)
- [ ] 所有可点击元素都有 `cursor-pointer`。
- [ ] 悬停状态提供清晰的视觉反馈。
- [ ] 过渡平滑 (150-300ms)。
- [ ] 键盘导航的焦点状态可见。

### Light/Dark Mode (亮色/暗色模式对比度)
- [ ] 亮色模式下文本对比度足够 (至少 4.5:1)。
- [ ] 亮色模式下玻璃/透明元素可见。
- [ ] 两种模式下边框都可见。
- [ ] 交付前测试两种模式。

### Layout (布局)
- [ ] 悬浮元素与边缘有适当的间距。
- [ ] 没有内容隐藏在固定导航栏后面。
- [ ] 在 375px、768px、1024px、1440px 均响应式。
- [ ] 移动端没有水平滚动。

### Accessibility (无障碍)
- [ ] 所有图片都有 `alt` 文本。
- [ ] 表单输入有标签。
- [ ] 颜色不是唯一的指示器。
- [ ] 尊重 `prefers-reduced-motion`。

---

## 📝 示例 Prompt (开发者参考)

```
@ui-ux-pro-max.md 请帮我做一个项目：

1. 运行设计搜索脚本，匹配一套适合'智慧能源管理系统'的深色模式调色盘
2. 使用 Vue 3 + Element Plus + ECharts 编写一个数据大屏卡片
3. 确保图表配色符合设计系统，且组件具备毛玻璃质感
```

---

## Prerequisites (前置条件)

检查是否已安装 Python：

```bash
python3 --version || python --version
```

如果未安装 Python，请根据操作系统进行安装：

**macOS:**
```bash
brew install python3
```

**Ubuntu/Debian:**
```bash
sudo apt update && sudo apt install python3
```

**Windows:**
```powershell
winget install Python.Python.3.12
```

---

## Search Reference (搜索参考)

### Available Domains (可用域)

| Domain | Use For (用途) | Example Keywords (示例关键词) |
|--------|------------------|--------------------------------|
| `product` | 产品类型建议 | SaaS, e-commerce, portfolio, healthcare, beauty, service |
| `style` | UI 样式、颜色、效果 | glassmorphism, minimalism, dark mode, brutalism |
| `typography` | 字体配对、Google Fonts | elegant, playful, professional, modern |
| `color` | 按产品类型的配色方案 | saas, ecommerce, healthcare, beauty, fintech, service |
| `landing` | 页面结构、CTA 策略 | hero, hero-centric, testimonial, pricing, social-proof |
| `chart` | 图表类型、库建议 | trend, comparison, timeline, funnel, pie |
| `ux` | 最佳实践、反模式 | animation, accessibility, z-index, loading |
| `react` | React/Next.js 性能 | waterfall, bundle, suspense, memo, rerender, cache |
| `web` | Web 界面指南 | aria, focus, keyboard, semantic, virtualize |
| `prompt` | AI 提示词、CSS 关键词 | (样式名称) |

### Available Stacks (可用技术栈)

| Stack | Focus (侧重) |
|-------|--------------|
| `html-tailwind` | Tailwind 工具类、响应式、a11y（默认） |
| `react` | 状态、Hooks、性能、模式 |
| `nextjs` | SSR、路由、图片、API 路由 |
| `vue` | Composition API、Pinia、Vue Router |
| `svelte` | Runes、stores、SvelteKit |
| `swiftui` | Views、State、Navigation、Animation |
| `react-native` | Components、Navigation、Lists |
| `flutter` | Widgets、State、Layout、Theming |
| `shadcn` | shadcn/ui 组件、主题、表单、模式 |

---