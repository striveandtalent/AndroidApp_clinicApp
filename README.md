使用了BlurView，添加类IOS高斯模糊效果，网址：https://gitcode.com/gh_mirrors/bl/BlurView


分支推送测试标记：用于验证 codex 分支推送流程。

## BlurView 实现学习笔记（本项目）

### 1) 核心概念
- `BlurTarget`：**被采样**的内容容器（要被模糊的来源）。
- `BlurView`：**显示模糊结果**的控件（自己不被模糊，模糊的是它下面 target 的内容）。

### 2) 典型初始化链路
```java
blurView.setupWith(target)
        .setFrameClearDrawable(windowBackground)
        .setBlurRadius(25f);
```

### 3) 本项目里的两套模糊
- 底部导航 blur：`MainActivity` 中 `bottomBlurView -> activity_main.xml` 的 `target`。
- 病例页搜索 blur：`CaseFragment` 中 `topBlurView -> fragment_list.xml` 的 `caseBlurTarget`。

### 4) 为什么要分开 target
如果病例页搜索框拿 Activity 的 target 采样，容易出现跨层级采样问题（甚至崩溃）。
所以搜索框必须采样病例页内部的 `caseBlurTarget`。
