package hk.uwu.reareye.hook

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.factory.configs
import com.highcapable.yukihookapi.hook.factory.encase
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit
import de.robv.android.xposed.XposedBridge
import hk.uwu.reareye.ui.config.ConfigKeys

@InjectYukiHookWithXposed(entryClassName = "HookEntrance")
class HookEntry : IYukiHookXposedInit {
    override fun onInit() = configs {
        debugLog {
            tag = "REAREye"
        }
    }

    override fun onHook() = encase {
        loadSystem {
            val asiRef = "com.android.server.wm.ActivityStarterImpl".toClass().resolve()
            asiRef
                .firstMethod {
                    name = "isShouldShowOnRearDisplay"
                    returnType = Boolean::class.java
                }.hook().before {
                    if (prefs.getBoolean(ConfigKeys.HOOK_ACTIVITIES_WHITELIST)) return@before
                    val whitelist = prefs.getStringSet(ConfigKeys.ACTIVITIES_WHITELIST_APPS)
                    val field = asiRef.firstField {
                        name = "REAR_SCREEN_METADATA_WHITE_LIST"
                        type = Set::class.java
                    }
                    val set = field.get<HashSet<String>>() ?: return@before
                    set.clear()
                    set.add("com.retroarch")
                    set.addAll(whitelist)
                    XposedBridge.log("Injected Activities Whitelist")
                }
        }

        loadApp("com.xiaomi.subscreencenter") {
            val clz = "p2.a".toClass().resolve()
            val field = clz.firstField {
                name = "a"
                type = Map::class.java
            }
            val map = buildMap<String, String> {
                @Suppress("UNCHECKED_CAST")
                putAll(field.get() as Map<String, String>)
                prefs.getStringSet(ConfigKeys.MUSIC_CONTROLS_WHITELIST_APPS).forEach {
                    put(it, "music")
                }
            }
            if (prefs.getBoolean(ConfigKeys.HOOK_MUSIC_CONTROLS_WHITELIST)) {
                field.set(map)
                XposedBridge.log("Hooked SubscreenCenter whitelist ${field.get()}")
            }

            /*clz.firstMethod {
                name = "a"
                returnType = String::class.java
            }.hook().after {
                if (prefs.getBoolean(ConfigKeys.HOOK_MUSIC_CONTROLS_WHITELIST)) {
                    val pkg = args[0] as? String
                    if (prefs.getStringSet(ConfigKeys.MUSIC_CONTROLS_WHITELIST_APPS).contains(pkg)) {
                        result = "music"
                        XposedBridge.log("Injected application $pkg with music type")
                    }
                }
            }

            clz.firstMethod {
                name = "b"
                returnType = HashSet::class.java
            }.hook().after {
                if (prefs.getBoolean(ConfigKeys.HOOK_MUSIC_CONTROLS_WHITELIST)) {
                    @Suppress("UNCHECKED_CAST")
                    val hashSet = result as HashSet<String>
                    hashSet.addAll(prefs.getStringSet(ConfigKeys.MUSIC_CONTROLS_WHITELIST_APPS))
                    result = hashSet
                    XposedBridge.log("Injected all music package into hash set")
                }
            }

            clz.firstMethod {
                name = "c"
                returnType = Boolean::class.java
                parameters(String::class.java)
            }.hook().after {
                if (prefs.getBoolean(ConfigKeys.HOOK_MUSIC_CONTROLS_WHITELIST)) {
                    @Suppress("UNCHECKED_CAST")
                    val bl = result as Boolean
                    val pkg = args[0] as String
                    XposedBridge.log("Application $pkg status: $bl")
                    if (!bl && prefs.getStringSet(ConfigKeys.MUSIC_CONTROLS_WHITELIST_APPS)
                            .contains(pkg)
                    ) {
                        XposedBridge.log("Injected application $pkg is whitelist music package")
                        this.resultTrue()
                    }
                }
            }*/
        }
    }
}