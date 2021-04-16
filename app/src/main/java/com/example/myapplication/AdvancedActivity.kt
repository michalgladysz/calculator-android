package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.view.View
import android.widget.Button
import android.widget.TextView
import net.objecthunter.exp4j.ExpressionBuilder
import java.lang.StringBuilder
import java.math.BigDecimal
import java.math.BigDecimal.*

class AdvancedActivity : AppCompatActivity() {

    private lateinit var expressionTextView: TextView
    private lateinit var resultTextView: TextView

    private lateinit var equalButton: Button
    private lateinit var decimalButton: Button
    private lateinit var allClearButton: Button
    private lateinit var backButton: Button
    private lateinit var signChange: Button
    private lateinit var parenthesesOpenButton: Button
    private lateinit var parenthesesCloseButton: Button

    private var lastNumeric: Boolean = false
    private var prevNumeric: Boolean = false
    private var stateError: Boolean = false
    private var lastDot: Boolean = false
    private var lastOperator: Boolean = false
    private var parenthesesClosed: Boolean = true
    private var openedParentheses: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_advanced)

        expressionTextView = findViewById(R.id.expressionTextView)
        resultTextView = findViewById(R.id.resultTextView)
        equalButton = findViewById(R.id.equalButton)
        decimalButton = findViewById(R.id.decimalButton)
        allClearButton = findViewById(R.id.allClearButton)
        backButton = findViewById(R.id.backButton)
        parenthesesOpenButton = findViewById(R.id.parenthesesOpenButton)
        parenthesesCloseButton = findViewById(R.id.parenthesesCloseButton)
        signChange = findViewById(R.id.signChange)

        if (savedInstanceState != null) {
            lastNumeric = savedInstanceState.getBoolean("lastNumeric")
            prevNumeric = savedInstanceState.getBoolean("prevNumeric")
            stateError = savedInstanceState.getBoolean("stateError")
            lastDot = savedInstanceState.getBoolean("lastDot")
            lastOperator = savedInstanceState.getBoolean("lastOperator")
            parenthesesClosed = savedInstanceState.getBoolean("parenthesesClosed")
            openedParentheses = savedInstanceState.getInt("openedParentheses")
            expressionTextView.text = savedInstanceState.getString("expression")
            resultTextView.text = savedInstanceState.getString("result")
        }

        equalButton.setOnClickListener {
            if ((lastNumeric && !stateError || parenthesesClosed && prevNumeric) && openedParentheses == 0) {
                try {
                    val result = evaluateExpression()
                    resultTextView.text = result
                    expressionTextView.text = result
                    lastDot = true
                    prevNumeric = false
                    lastNumeric = true
                } catch (ex: ArithmeticException) {
                    resultTextView.text = "Error"
                    stateError = true
                    lastNumeric = false
                    lastOperator = false
                }
                catch (ex: NumberFormatException) {
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
            expressionTextView.text = ""
            resultTextView.text = ""
            lastNumeric = false
            stateError = false
            lastDot = false
            lastOperator = false
            parenthesesClosed = true
            openedParentheses = 0
        }

        backButton.setOnClickListener {
            if (expressionTextView.text.isNotEmpty() && !stateError) {
                val ifParenthesesChar = expressionTextView.text.toString().last();
                if (ifParenthesesChar == ')') {
                    parenthesesClosed = false
                    openedParentheses++
                }
                if (ifParenthesesChar == '(') {
                    parenthesesClosed = true
                    openedParentheses--
                }
                expressionTextView.text = expressionTextView.text.substring(0, expressionTextView.text.length - 1)
                if (expressionTextView.text.length > 1) {
                    val char = expressionTextView.text.toString().last()
                    if (char == '.') {
                        lastDot = true
                        lastOperator = false
                        lastNumeric = false
                    }
                    if (char.isDigit()) {
                        lastOperator = false
                        lastNumeric = true
                    }
                    if (char == '+' || char == '-' || char == '*' || char == '/' || char == 't' || char == '^' || char == 'n' || char == 's' || char == 'g') {
                        lastOperator = true
                        lastNumeric = false
                    }
                    evaluateToResultTextView()
                }
                else {
                    resultTextView.text = ""
                    if (expressionTextView.text.isNotEmpty())
                        if (expressionTextView.text.last() == '-')
                            expressionTextView.text = ""
                }
            }
        }

        parenthesesOpenButton.setOnClickListener {
            var stringBuilder : StringBuilder? = null
            if (!stateError) {
                stringBuilder = StringBuilder(expressionTextView.text.toString()).also { it.append('(') }
                parenthesesClosed = false
                lastNumeric = false
                lastOperator = false
                lastDot = false
                expressionTextView.text = stringBuilder.toString()
                openedParentheses++
            }
        }

        parenthesesCloseButton.setOnClickListener {
            var stringBuilder : StringBuilder? = null
            if (!lastOperator && !stateError && openedParentheses > 0) {
                stringBuilder = StringBuilder(expressionTextView.text.toString()).also { it.append(')') }
                parenthesesClosed = true
                lastNumeric = false
                prevNumeric = true
                lastOperator = false
                lastDot = false
                expressionTextView.text = stringBuilder.toString()
                openedParentheses--
                evaluateToResultTextView()
            }
        }

        signChange.setOnClickListener {
            if (lastNumeric && !stateError) {
                val signs = listOf("+", "-", "*", "/", "(", ")", "^")
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
                        stringBuilder = if (prevChar == '*' || prevChar == '/' || prevChar == '^' || prevChar == '(') {
                            StringBuilder(expressionTextView.text.toString()).also { it.deleteCharAt(index) }
                        } else StringBuilder(expressionTextView.text.toString()).also { it.setCharAt(index, '+') }
                    }
                    if (char == '*' || char == '/' || char == '(' || char == ')' || char == '^') {
                        stringBuilder = StringBuilder(expressionTextView.text.toString()).insert(index + 1, "-")
                    }
                }
                else {
                    if (expressionTextView.text.isNotEmpty()) {
                        stringBuilder = if (expressionTextView.text.toString().elementAt(0) == '-')
                            StringBuilder(expressionTextView.text.toString()).also { it.deleteCharAt(index) }
                        else StringBuilder(expressionTextView.text.toString()).insert(0, "-")
                    }
                }
                expressionTextView.text = stringBuilder.toString()
            }
            println(expressionTextView.text.length)
            if (expressionTextView.text.toString() != "null") {
                evaluateToResultTextView()
            } else expressionTextView.text = ""

        }
    }

    private fun evaluateToResultTextView() {
        if (lastNumeric || parenthesesClosed && !stateError) {
            try {
                val result = evaluateExpression()
                resultTextView.text = result
            } catch (ex: ArithmeticException ) {
                resultTextView.text = "Error"
                stateError = true
                lastNumeric = false
                lastOperator = false
            }
            catch (ex: NumberFormatException) {
                resultTextView.text = "Error"
                stateError = true
                lastNumeric = false
                lastOperator = false
            }
        }
    }

    fun onDigitClick(view: View) {
        lastNumeric = true
        lastOperator = false
        if (stateError) {
            expressionTextView.text = (view as Button).text
            stateError = false
        } else {
            expressionTextView.append((view as Button).text)
            evaluateToResultTextView()
        }
    }

    fun onOperatorAdvanced(view: View) {
        if (!stateError && (expressionTextView.text.isEmpty() || lastNumeric || expressionTextView.text.last() == ')')) {
            if (((view as Button).text) == "x^2") {
                if (expressionTextView.text.isEmpty()) {
                    expressionTextView.text = ""
                }
                else {
                    expressionTextView.append("^2")
                    lastNumeric = true
                    lastOperator = false
                    evaluateToResultTextView()
                }
            }
            else if ((view.text) == "x^y") {
                if (expressionTextView.text.isEmpty()) {
                    expressionTextView.text = ""
                }
                else {
                    expressionTextView.append("^")
                    lastNumeric = false
                    lastOperator = true
                }
            }
            else if ((view.text) == "sqrt") {
                if (expressionTextView.text.isEmpty()) {
                    expressionTextView.text = "sqrt("
                }
                else expressionTextView.append("sqrt(")
                openedParentheses++
                parenthesesClosed = false
                lastNumeric = false
                lastOperator = true
            }
            else if ((view.text) == "ln") {
                if (expressionTextView.text.isEmpty()) {
                    expressionTextView.text = "log("
                }
                else expressionTextView.append("log(")
                openedParentheses++
                parenthesesClosed = false
                lastNumeric = false
                lastOperator = true
            }
            else if ((view.text) == "log") {
                if (expressionTextView.text.isEmpty()) {
                    expressionTextView.text = "log10("
                }
                else expressionTextView.append("log10(")
                openedParentheses++
                parenthesesClosed = false
                lastNumeric = false
                lastOperator = true
            }
            else if ((view.text) == "sin") {
                if (expressionTextView.text.isEmpty()) {
                    expressionTextView.text = "sin("
                }
                else expressionTextView.append("sin(")
                openedParentheses++
                parenthesesClosed = false
                lastNumeric = false
                lastOperator = true
            }
            else if ((view.text) == "cos") {
                if (expressionTextView.text.isEmpty()) {
                    expressionTextView.text = "cos("
                }
                else expressionTextView.append("cos(")
                openedParentheses++
                parenthesesClosed = false
                lastNumeric = false
                lastOperator = true
            }
            else if ((view.text) == "tan") {
                if (expressionTextView.text.isEmpty()) {
                    expressionTextView.text = "tan("
                }
                else expressionTextView.append("tan(")
                openedParentheses++
                parenthesesClosed = false
                lastNumeric = false
                lastOperator = true
            }
            lastDot = false
        }
    }

    fun onOperator(view: View) {
        if (expressionTextView.text.isNotEmpty() && !stateError && (lastNumeric || expressionTextView.text.last() == ')')) {
            expressionTextView.append((view as Button).text)
            lastNumeric = false
            lastOperator = true
            lastDot = false
        }
    }

    private fun evaluateExpression() : String {
        if ( !lastOperator && ((parenthesesClosed && expressionTextView.text.isNotEmpty()) || (parenthesesClosed && lastNumeric)) && openedParentheses == 0) {
            val txt = expressionTextView.text.toString()
            val expression = ExpressionBuilder(txt).build()
            var ret = BigDecimal(expression.evaluate())
            ret = ret.setScale(4, ROUND_HALF_UP)
            return ret.toString()
        }
        return ""
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean("lastNumeric", lastNumeric)
        outState.putBoolean("prevNumeric", prevNumeric)
        outState.putBoolean("stateError", stateError)
        outState.putBoolean("lastDot", lastDot)
        outState.putBoolean("lastOperator", lastOperator)
        outState.putBoolean("parenthesesClosed", parenthesesClosed)
        outState.putInt("openedParentheses", openedParentheses)
        outState.putString("expression", expressionTextView.text.toString())
        outState.putString("result", resultTextView.text.toString())
        super.onSaveInstanceState(outState)
    }
}
