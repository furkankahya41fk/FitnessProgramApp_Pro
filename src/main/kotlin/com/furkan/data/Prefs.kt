
package com.furkan.fitnessapp.data

import android.content.Context

class Prefs(ctx: Context) {
    private val sp = ctx.getSharedPreferences("prefs", Context.MODE_PRIVATE)

    fun putInt(k: String, v: Int) { sp.edit().putInt(k, v).apply() }
    fun getInt(k: String, def: Int) = sp.getInt(k, def)
}
