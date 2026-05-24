# Shizuku uses reflection for IPC binding
-keep class dev.rikka.shizuku.** { *; }

# Keep Shizuku service connections used via reflection
-keep class * implements dev.rikka.shizuku.IShizukuServiceConnection { *; }

# ProFeature is loaded reflectively for pro module detection
-keep class dev.robin.privacify.core.provider.ProFeature { *; }

# Widget and tile receivers kept for manifest registration
-keep class dev.robin.privacify.core.widget.** { *; }
