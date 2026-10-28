package com.example.finances

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar
import java.util.Locale

data class Entry(val id: String, val date: String, val note: String, val income: Boolean, val amount: Long)

class FinanceStorage(context: Context) {
    private val preferences = context.applicationContext
        .getSharedPreferences("finances", Context.MODE_PRIVATE)

    fun load(): List<Entry> {
        if (!preferences.contains("entries")) {
            val date = today()
            val examples = listOf(
                Entry("demo-income", date, "Зарплата", true, 4500000),
                Entry("demo-work", date, "Подработка", true, 300000),
                Entry("demo-food", date, "Продукты", false, 180000)
            )
            save(examples)
            return examples
        }
        val array = JSONArray(preferences.getString("entries", "[]"))
        return (0 until array.length()).map { index ->
            val item = array.getJSONObject(index)
            Entry(item.getString("id"), item.getString("date"), item.getString("note"),
                item.getBoolean("income"), item.getLong("amount"))
        }
    }

    fun save(entries: List<Entry>) {
        val array = JSONArray()
        for (entry in entries) {
            array.put(JSONObject().apply {
                put("id", entry.id)
                put("date", entry.date)
                put("note", entry.note)
                put("income", entry.income)
                put("amount", entry.amount)
            })
        }
        preferences.edit().putString("entries", array.toString()).apply()
    }

    companion object {
        fun today(): String {
            val date = Calendar.getInstance()
            return String.format(Locale.US, "%04d-%02d-%02d",
                date.get(Calendar.YEAR), date.get(Calendar.MONTH) + 1, date.get(Calendar.DAY_OF_MONTH))
        }
    }
}
