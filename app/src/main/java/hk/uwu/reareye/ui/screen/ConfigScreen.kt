@file:Suppress("AssignedValueIsNeverRead")

package hk.uwu.reareye.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import hk.uwu.reareye.R
import hk.uwu.reareye.ui.components.AppSelectorDialog
import hk.uwu.reareye.ui.config.ConfigCategory
import hk.uwu.reareye.ui.config.ConfigItem
import hk.uwu.reareye.ui.config.ConfigType
import hk.uwu.reareye.ui.config.PrefsManager
import hk.uwu.reareye.ui.config.REAREyeConfig
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.extra.SuperArrow
import top.yukonga.miuix.kmp.extra.SuperSwitch

@Composable
fun ConfigScreen() {
    val context = LocalContext.current
    val prefsManager = remember { PrefsManager(context) }

    var showAppSelector by remember { mutableStateOf<String?>(null) }
    var categoryStack by remember { mutableStateOf(emptyList<ConfigCategory>()) }

    val currentCategory = categoryStack.lastOrNull()

    Scaffold(
        topBar = {
            TopAppBar(
                title = currentCategory?.let { stringResource(it.titleRes) }
                    ?: stringResource(R.string.configuration_title)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            BackHandler(enabled = categoryStack.isNotEmpty()) {
                categoryStack = categoryStack.dropLast(1)
            }

            Spacer(modifier = Modifier.height(12.dp))

            AnimatedContent(
                targetState = currentCategory,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith fadeOut(
                        animationSpec = tween(
                            300
                        )
                    )
                },
                label = "CategoryTransition"
            ) { targetCategory ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (targetCategory == null) {
                        item {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    REAREyeConfig.forEachIndexed { index, category ->
                                        CategoryRow(category = category, onClick = {
                                            categoryStack = categoryStack + category
                                        })
                                        if (index < REAREyeConfig.size - 1) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        if (targetCategory.items.isNotEmpty()) {
                            item {
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        targetCategory.items.forEachIndexed { index, item ->
                                            ConfigItemView(
                                                item = item,
                                                prefsManager = prefsManager,
                                                onOpenAppSelector = {
                                                    showAppSelector = it
                                                }
                                            )
                                            if (index < targetCategory.items.size - 1) {
                                                Spacer(modifier = Modifier.height(8.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (targetCategory.subCategories.isNotEmpty()) {
                            item {
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        targetCategory.subCategories.forEachIndexed { index, subCat ->
                                            CategoryRow(category = subCat, onClick = {
                                                categoryStack = categoryStack + subCat
                                            })
                                            if (index < targetCategory.subCategories.size - 1) {
                                                Spacer(modifier = Modifier.height(8.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    showAppSelector?.let {
        AppSelectorDialog(
            configKey = it,
            prefsManager = prefsManager,
            onDismiss = {
                @Suppress("AssignedValueIsNeverRead")
                showAppSelector = null
            }
        )
    }
}

@Composable
fun CategoryRow(category: ConfigCategory, onClick: () -> Unit) {
    SuperArrow(
        title = stringResource(category.titleRes),
        summary = category.descriptionRes?.let {
            stringResource(it)
        },
        onClick = onClick
    )
}

@Composable
fun ConfigItemView(
    item: ConfigItem,
    prefsManager: PrefsManager,
    onOpenAppSelector: (String) -> Unit
) {
    when (val type = item.type) {
        is ConfigType.BooleanVal -> {
            var checked by remember {
                mutableStateOf(prefsManager.getBoolean(item.key, type.defaultValue))
            }
            SuperSwitch(
                title = stringResource(item.titleRes),
                summary = item.descriptionRes?.let {
                    stringResource(it)
                },
                checked = checked,
                onCheckedChange = {
                    checked = it
                    prefsManager.putBoolean(item.key, it)
                }
            )
        }

        is ConfigType.AppList -> {
            SuperArrow(
                title = stringResource(item.titleRes),
                summary = item.descriptionRes?.let {
                    stringResource(it)
                },
                onClick = {
                    onOpenAppSelector(item.key)
                }
            )
        }
    }
}
