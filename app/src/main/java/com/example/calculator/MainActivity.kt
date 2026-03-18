package com.example.calculator

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var tvResult: TextView
    private val TAG = "Lifecycle_Calc"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d(TAG, "onCreate: Ініціалізує UI")

        tvResult = findViewById(R.id.tvResult)

        if (savedInstanceState != null) {
            val savedText = savedInstanceState.getString("CALC_STATE", "")
            tvResult.text = savedText
        }

        setupButtons()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("CALC_STATE", tvResult.text.toString())
    }

    private fun setupButtons() {
        // Список кнопок, які просто додають текст у поле
        val buttonsToAppend = listOf(
            R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
            R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9,
            R.id.btnPlus, R.id.btnMinus, R.id.btnMultiply, R.id.btnDivide,
            R.id.btnPrecent
        )

        for (id in buttonsToAppend) {
            findViewById<Button>(id).setOnClickListener { button ->
                tvResult.append((button as Button).text)
            }
        }

        // Кнопка очищення (C)
        findViewById<Button>(R.id.btnClear).setOnClickListener {
            tvResult.text = ""
        }

        // Кнопка "Дорівнює" (=) — ТЕПЕР ПРАЦЮЄ
        findViewById<Button>(R.id.btnEquals).setOnClickListener {
            val expression = tvResult.text.toString()
            if (expression.isNotEmpty()) {
                try {
                    val result = evaluate(expression)

                    // Перевіряємо, чи число ціле, щоб не писати ".0"
                    tvResult.text = if (result % 1 == 0.0) {
                        result.toLong().toString()
                    } else {
                        result.toString()
                    }
                } catch (e: Exception) {
                    tvResult.text = "Error"
                    Log.e(TAG, "Помилка обчислення: ${e.message}")
                }
            }
        }
    }

    // Твій алгоритм обчислення (парсер)
    private fun evaluate(str: String): Double {
        return object : Any() {
            var pos = -1
            var ch = 0

            fun nextChar() {
                ch = if (++pos < str.length) str[pos].code else -1
            }

            fun eat(charToEat: Int): Boolean {
                while (ch == ' '.code) nextChar()
                if (ch == charToEat) {
                    nextChar()
                    return true
                }
                return false
            }

            fun parse(): Double {
                nextChar()
                val x = parseExpression()
                if (pos < str.length) throw RuntimeException("Unexpected: " + ch.toChar())
                return x
            }

            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    if (eat('+'.code)) x += parseTerm()
                    else if (eat('-'.code)) x -= parseTerm()
                    else return x
                }
            }

            fun parseTerm(): Double {
                var x = parseFactor()
                while (true) {
                    if (eat('*'.code)) x *= parseFactor()
                    else if (eat('/'.code)) x /= parseFactor()
                    else return x
                }
            }

            fun parseFactor(): Double {
                if (eat('+'.code)) return parseFactor()
                if (eat('-'.code)) return -parseFactor()
                var x: Double
                val startPos = pos
                if (ch in '0'.code..'9'.code || ch == '.'.code) {
                    while (ch in '0'.code..'9'.code || ch == '.'.code) nextChar()
                    x = str.substring(startPos, pos).toDouble()
                    // Обробка відсотка (якщо він йде одразу за числом)
                    while (eat('%'.code)) x /= 100.0
                } else {
                    throw RuntimeException("Unexpected: " + ch.toChar())
                }
                return x
            }
        }.parse()
    }
}