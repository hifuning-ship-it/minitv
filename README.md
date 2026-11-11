# Minimal TV Lite — 统帅 YunOS 电视极简桌面

针对已实测设备：`LEADER_RTD2969_A` / `rtd294x` / YunOS 2.1.0 / Android API 19 / ARMv7 / 约448 MB RAM。

这是**测试版源码**，未在实际电视上完成安装/遥控器验证；并不是固件 ROM。无需 Root、不卸载原厂桌面、不联网、不使用第三方库、不自启动后台服务。

## 构建方法 A：Android Studio
1. 使用支持 Android Gradle Plugin 8.5.2 的 Android Studio，安装 JDK 17、Android SDK Platform 35。
2. 打开本文件夹，等待 Gradle 同步。若提示缺少 Gradle wrapper，选择本地 Gradle 8.7，或者先用 Gradle 8.7 执行 `gradle wrapper --gradle-version 8.7`。
3. Build > Build APK(s)，生成 `app/build/outputs/apk/debug/app-debug.apk`。

## 构建方法 B：GitHub Actions
上传整个目录到你拥有的 GitHub 仓库（注意包含 `.github/workflows/build-apk.yml`），运行 `Build Android 4.4 APK` 工作流，从 Artifacts 取 APK。

## Windows 安装与测试
在 `platform-tools` 目录执行（将 APK 路径替换为实际位置）：

```powershell
.\adb.exe connect 192.168.3.122:5555
.\adb.exe install -r "C:\path\to\app-debug.apk"
.\adb.exe shell am start -n net.minimaltvlite.launcher/.MainActivity
```

先测试遥控器上下左右、确认、返回、打开“原厂桌面”、系统设置以及应用列表。软件中“电视/信号源”通过原厂桌面打开；“本地视频”和“投屏”使用包启动意图，如原系统未暴露标准入口会提示失败，需再适配。

**切勿在此阶段禁用 `com.yunos.tv.homeshell`。** 正常情况下可在 HOME 键出现桌面选择器时选择新桌面，但 YunOS 可能强制原厂桌面。仅在测试稳定后考虑设置默认桌面。

## 卸载回退
```powershell
.\adb.exe shell am start -n com.yunos.tv.homeshell/.activity.MainActivity
.\adb.exe uninstall net.minimaltvlite.launcher
```

如卸载后 HOME 默认项异常，可用 ADB 显式启动原厂桌面；不要直接操作系统分区。当前 `AliTvHomeshell.apk` 的导出不等于固件恢复镜像。

## 已知限制
- 未实机验证，尚不能保证 YunOS 的安装策略与默认桌面切换机制。
- 未实现直接调用专用 HDMI / TV 信号源接口；保留原厂桌面做兼容入口。
- 系统硬件很老，替换桌面不保证显著加速所有应用。
- 这里交付**源码工程**，不是已经编译好的 APK。
