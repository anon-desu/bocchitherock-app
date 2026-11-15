# 孤独摇滚 (Bocchi the Rock!) Android

基于原 `anon-desu/Bocchi-the-Rock` Web 项目重写为原生 **Kotlin + Jetpack Compose** Android 应用。

## 核心功能

- **主页 (Hero)**: 动态进场剧场版 Logo 与全屏氛围封面。
- **角色档案 (Characters)**: 结束乐队四位成员（后藤一里、伊地知虹夏、山田凉、喜多郁代）完整档案，支持主题色切换、像素字体名牌、循环罗马音跑马灯、台词框、CV 简介及底部海报选择器。
- **沉浸式音乐播放器 (Music)**:
  - 基于 **AndroidX Media3 (ExoPlayer + MediaSessionService)** 实现本地音频播放、流媒体兜底播放与系统媒体通知栏控制。
  - 支持「結束バンド」与「バンド」双歌单独立浏览与切歌队列、黑胶唱片旋转动画、飘动音符特效、音量与进度条调节。
  - 接入 后端api实现歌曲收藏同步、基于收藏歌单的 5 倍权重随机播放，以及内置 Bilibili MV 播放器弹窗。
- **动态相册 (Photos)**: 23 张图集（含 GIF 动图）三行交错循环滚动展示，保持原版 `±15°` 透视倾斜与点击原地翻转展平并暂停当前行滚动的交互效果。
- **关于制作组 (About)**: 动画五线谱背景、制作组人员卡片、GitHub 源码链接与萌 ICP 备链接。
- **登录与注册 (Login / Register)**: 对接 Cloudflare 后端与在线验证码，支持双端令牌自动刷新与 DataStore 会话持久化。
