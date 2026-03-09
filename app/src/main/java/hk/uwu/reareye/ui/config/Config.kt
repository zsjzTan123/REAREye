package hk.uwu.reareye.ui.config

import androidx.annotation.StringRes

sealed class ConfigType {
    data class BooleanVal(val defaultValue: Boolean = false) : ConfigType()
    data class AppList(val defaultValues: Set<String> = emptySet()) : ConfigType()
    // Additional types like StringVal, IntVal can be added here
}

data class ConfigItem(
    val key: String,
    @param:StringRes val titleRes: Int,
    @param:StringRes val descriptionRes: Int? = null,
    val type: ConfigType
)

data class ConfigCategory(
    val key: String,
    @param:StringRes val titleRes: Int,
    @param:StringRes val descriptionRes: Int? = null,
    val items: List<ConfigItem> = emptyList(),
    val subCategories: List<ConfigCategory> = emptyList()
)
