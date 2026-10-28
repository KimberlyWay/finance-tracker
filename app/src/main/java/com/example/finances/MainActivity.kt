package com.example.finances

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import java.util.Locale
import java.util.UUID

class MainActivity : Activity() {
    private lateinit var storage: FinanceStorage
    private var entries = emptyList<Entry>()
    private var selectedDate = FinanceStorage.today()
    private val incomeColor = Color.rgb(46, 107, 70)
    private val expenseColor = Color.rgb(163, 59, 57)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        storage = FinanceStorage(this)
        entries = storage.load()
        selectedDate = savedInstanceState?.getString("date") ?: FinanceStorage.today()
        updateDateButton()
        findViewById<Button>(R.id.dateButton).setOnClickListener { chooseDate() }
        findViewById<Button>(R.id.addButton).setOnClickListener { addEntry() }
        showEntries()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("date", selectedDate)
        super.onSaveInstanceState(outState)
    }

    private fun addEntry() {
        val amountInput = findViewById<EditText>(R.id.amountInput)
        val amount = Money.parse(amountInput.text.toString())
        if (amount == null) {
            amountInput.error = getString(R.string.invalid_amount)
            amountInput.requestFocus()
            return
        }
        val income = findViewById<RadioButton>(R.id.incomeType).isChecked
        val noteInput = findViewById<EditText>(R.id.noteInput)
        val note = noteInput.text.toString().trim().ifEmpty {
            getString(if (income) R.string.income else R.string.expense)
        }
        val entry = Entry(UUID.randomUUID().toString(), selectedDate, note, income, amount)
        entries = listOf(entry) + entries
        storage.save(entries)
        amountInput.text.clear()
        noteInput.text.clear()
        (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(amountInput.windowToken, 0)
        showEntries()
        Toast.makeText(this, R.string.added, Toast.LENGTH_SHORT).show()
    }

    private fun chooseDate() {
        val parts = selectedDate.split('-').map { it.toInt() }
        DatePickerDialog(this, { _, year, month, day ->
            selectedDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day)
            updateDateButton()
        }, parts[0], parts[1] - 1, parts[2]).show()
    }

    private fun updateDateButton() {
        findViewById<Button>(R.id.dateButton).text = getString(R.string.date_button, displayDate(selectedDate))
    }

    private fun showEntries() {
        val income = entries.filter { it.income }.sumOf { it.amount }
        val expense = entries.filter { !it.income }.sumOf { it.amount }
        findViewById<TextView>(R.id.incomeTotal).text = getString(R.string.total_income, Money.format(income))
        findViewById<TextView>(R.id.expenseTotal).text = getString(R.string.total_expense, Money.format(expense))
        findViewById<TextView>(R.id.balanceTotal).apply {
            text = getString(R.string.total_balance, Money.format(income - expense))
            setTextColor(if (income >= expense) incomeColor else expenseColor)
        }
        findViewById<TextView>(R.id.entryCount).text = getString(R.string.entry_count, entries.size)
        findViewById<TextView>(R.id.emptyText).visibility = if (entries.isEmpty()) View.VISIBLE else View.GONE

        val table = findViewById<TableLayout>(R.id.entriesTable)
        table.removeAllViews()
        val header = TableRow(this).apply { setBackgroundColor(Color.rgb(230, 235, 231)) }
        header.addView(cell(getString(R.string.date_header), 96, bold = true))
        header.addView(cell(getString(R.string.note_header), 152, bold = true))
        header.addView(cell(getString(R.string.income_header), 120, true, true))
        header.addView(cell(getString(R.string.expense_header), 120, true, true))
        table.addView(header)

        // Формат YYYY-MM-DD позволяет правильно сортировать календарные даты как строки.
        for ((index, entry) in entries.sortedByDescending { it.date }.withIndex()) {
            val row = TableRow(this).apply {
                setBackgroundColor(if (index % 2 == 0) Color.WHITE else Color.rgb(246, 247, 246))
                minimumHeight = dp(48)
                isFocusable = true
                contentDescription = getString(R.string.row_details, displayDate(entry.date), entry.note,
                    getString(if (entry.income) R.string.income else R.string.expense), Money.format(entry.amount))
                setOnClickListener { confirmDelete(entry) }
            }
            row.addView(cell(displayDate(entry.date), 96))
            row.addView(cell(entry.note, 152))
            row.addView(cell(if (entry.income) Money.number(entry.amount) else getString(R.string.dash),
                120, true).apply { setTextColor(incomeColor) })
            row.addView(cell(if (!entry.income) Money.number(entry.amount) else getString(R.string.dash),
                120, true).apply { setTextColor(expenseColor) })
            table.addView(row)
        }
    }

    private fun confirmDelete(entry: Entry) {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_title)
            .setMessage(getString(R.string.delete_details, displayDate(entry.date), entry.note,
                getString(if (entry.income) R.string.income else R.string.expense), Money.format(entry.amount)))
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.delete) { _, _ ->
                entries = entries.filter { it.id != entry.id }
                storage.save(entries)
                showEntries()
            }.show()
    }

    private fun cell(value: String, width: Int, right: Boolean = false, bold: Boolean = false) = TextView(this).apply {
        text = value
        textSize = 14f
        setTextColor(Color.rgb(34, 34, 34))
        setPadding(dp(8), dp(12), dp(8), dp(12))
        gravity = if (right) Gravity.END else Gravity.START
        if (bold) setTypeface(null, Typeface.BOLD)
        layoutParams = TableRow.LayoutParams(dp(width), TableRow.LayoutParams.WRAP_CONTENT)
    }

    private fun displayDate(value: String): String {
        val parts = value.split('-')
        return "${parts[2]}.${parts[1]}.${parts[0]}"
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}
