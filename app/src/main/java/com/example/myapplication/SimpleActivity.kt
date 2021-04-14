package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import net.objecthunter.exp4j.ExpressionBuilder
import java.lang.StringBuilder

class SimpleActivity : AppCompatActivity() {

    private lateinit var expressionTextView: TextView
    private lateinit var resultTextView: TextView

    private lateinit var equalButton: Button
    private lateinit var decimalButton: Button
    private lateinit var allClearButton: Button
    private lateinit var backButton: Button
    private lateinit var signChange: Button


    private var lastNumeric: Boolean = false
    private var stateError: Boolean = false
    private var lastDot: Boolean = false
    private var lastOperator: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple)

        expressionTextView = findViewById(R.id.expressionTextView)
        resultTextView = findViewById(R.id.resultTextView)

        equalButton = findViewById(R.id.equalButton)
        decimalButton = findViewById(R.id.decimalButton)
        allClearButton = findViewById(R.id.allClearButton)
        backButton = findViewById(R.id.backButton)
        signChange = findViewById(R.id.signChange)

        equalButton.setOnClickListener {
            if (lastNumeric && !stateError) {
                try {
                    val result = evaluateExpression()
                    resultTextView.text = result
                    expressionTextView.text = result
                    lastDot = true
                } catch (ex: ArithmeticException) {
                    resultTextView.text = "Error"
                    stateError = true
                    lastNumeric = false
                    lastOperator = false
                }
            }
        }

        decimalButton.setOnClickListener {
            if (lastNumeric && !stateError && !lastDot) {
                expressionTextView.append(".")
                lastNumeric = false
                lastOperator = false
                lastDot = true
            }
        }

        allClearButton.setOnClickListener {
            this.expressionTextView.text = ""
            this.resultTextView.text = ""
            lastNumeric = false
            stateError = false
            lastDot = false
            lastOperator = false
        }

        backButton.setOnClickListener {
            if (expressionTextView.text.isNotEmpty()) {
                expressionTextView.text = expressionTextView.text.substring(0, expressionTextView.text.length - 1)
                if (expressionTextView.text.length > 1) {
                    val char = expressionTextView.text.toString().last();
                    if (char == '.') {
                        lastDot = true
                        lastOperator = false
                        lastNumeric = false
                    }
                    if (char.isDigit()) {
                        lastOperator = false
                        lastNumeric = true
                    }
                    if (char == '+' || char == '-' || char == '*' || char == '/') {
                        lastOperator = true
                        lastNumeric = false
                    }

                    evaluateToResultTextView()
                }
                else resultTextView.text = ""
            }
        }

        signChange.setOnClickListener {
            if (lastNumeric) {
                val signs = listOf("+", "-", "*", "/")
                val index = expressionTextView.text.toString().lastIndexOfAny(signs)
                var stringBuilder : StringBuilder? = null

               if (index != -1 && index != 0) {
                       val char = expressionTextView.text.toString().elementAt(index)
                       val prevChar = expressionTextView.text.toString().elementAt(index - 1)

                       if (char == '+') {
                           stringBuilder =
                               StringBuilder(expressionTextView.text.toString()).also { it.setCharAt(index, '-') }
                       }
                       if (char == '-') {
                           if (prevChar == '*' || prevChar == '/') {
                               stringBuilder =
                                   StringBuilder(expressionTextView.text.toString()).also { it.deleteCharAt(index) }
                           } else stringBuilder =
                               StringBuilder(expressionTextView.text.toString()).also { it.setCharAt(index, '+') }
                       }
                       if (char == '*' || char == '/') {
                           stringBuilder = StringBuilder(expressionTextView.text.toString()).insert(index + 1, "-")
                       }
               }
                else {
                   if (expressionTextView.text.toString().elementAt(0) == '-')
                       stringBuilder = StringBuilder(expressionTextView.text.toString()).also { it.deleteCharAt(index) }
                   else stringBuilder = StringBuilder(expressionTextView.text.toString()).insert(0, "-")
               }
                expressionTextView.text = stringBuilder.toString()
            }
            evaluateToResultTextView()
        }
    }

    private fun evaluateToResultTextView() {
        if (lastNumeric && !stateError) {
            try {
                val result = evaluateExpression()
                resultTextView.text = result
            } catch (ex: ArithmeticException) {
                resultTextView.text = "Error"
                stateError = true
                lastNumeric = false
                lastOperator = false
            }
        }
    }

    fun onDigitClick(view: View) {
        lastNumeric = true
        if (stateError) {
            expressionTextView.text = (view as Button).text
            stateError = false
        } else {
            expressionTextView.append((view as Button).text)
            evaluateToResultTextView()
        }
    }

    fun onOperator(view: View) {
        if (lastNumeric && !stateError) {
            expressionTextView.append((view as Button).text)
            lastNumeric = false
            lastDot = false
            lastOperator = true
        }
    }

    private fun evaluateExpression() : String {
        val txt = expressionTextView.text.toString()
        val expression = ExpressionBuilder(txt).build()
        return expression.evaluate().toString()
    }
}