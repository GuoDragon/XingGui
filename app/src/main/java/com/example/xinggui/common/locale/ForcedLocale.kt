package com.example.xinggui.common.locale

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object ForcedLocale {
    private val zhCn = Locale("zh", "CN")

    fun wrap(context: Context): Context {
        ensureProcessLocale()
        val config = Configuration(context.resources.configuration)
        config.setLocale(zhCn)
        return context.createConfigurationContext(config)
    }

    fun ensureProcessLocale() {
        Locale.setDefault(zhCn)
    }
}
