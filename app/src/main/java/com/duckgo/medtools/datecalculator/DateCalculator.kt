package com.duckgo.medtools.datecalculator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import com.duckgo.medtools.ui.theme.RecycleTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

class DateCalculator : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    DateCalculatorScreen()
                }
            }
        }
    }
}

@Composable
fun DateCalculatorScreen() {
    var firstNumber by remember { mutableStateOf("") }
    var secondNumber by remember { mutableStateOf("") }
    var operator by remember { mutableStateOf("") }
    var displayExpression by remember { mutableStateOf("") }
    var isResultShown by remember { mutableStateOf(false) }
    var modelSelect by remember { mutableStateOf("WEEK") }
    var isErrorShowing by remember { mutableStateOf(false) }
    var resultText by remember { mutableStateOf("") }
    var errorChar by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    val currentLocalDate = remember {
        val c = Calendar.getInstance()
        String.format(Locale.US, "%d/%d/%d", c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH))
    }

    fun isInputIsDate(inp: String): Boolean = inp.contains("/")
    fun isCompleteDatePattern(inp: String): Boolean = Regex("\\d{1,4}/\\d{1,2}/\\d{1,2}").matches(inp)
    fun parseDateString(s: String): MyDateData {
        val parts = s.split("/")
        return MyDateData(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
    }

    fun formatDifference(diff: Int): String {
        val sign = if (diff < 0) "-" else ""
        val abs = kotlin.math.abs(diff)
        return when (modelSelect) {
            "WEEK" -> if (abs % 7 == 0) "$sign${abs / 7}周" else "$sign${abs / 7}周${abs % 7}天"
            "DAY" -> "$sign${abs}天"
            "MONTH" -> String.format(Locale.US, "%s%.3f月", sign, abs / 30.0)
            "YEAR" -> String.format(Locale.US, "%s%.3f年", sign, abs / 365.0)
            else -> diff.toString()
        }
    }

    fun parseBracketToDays(input: String): Int? {
        val content = input.replace("(", "").replace(")", "")
        return if (content.contains("+")) {
            val parts = content.split("+")
            val w = parts[0].toIntOrNull() ?: 0
            val d = parts.getOrNull(1)?.toIntOrNull() ?: 0
            w * 7 + d
        } else {
            (content.toIntOrNull() ?: 0) * 7
        }
    }

    fun parseToNumericValue(input: String): Double? {
        if (input.isEmpty() || input.contains("/")) return null
        if (input.contains("(")) {
            val content = input.replace("(", "").replace(")", "")
            return if (content.contains("+")) {
                val parts = content.split("+")
                val w = parts[0].toDoubleOrNull() ?: 0.0
                val d = parts.getOrNull(1)?.toDoubleOrNull() ?: 0.0
                w * 7.0 + d
            } else {
                (content.toDoubleOrNull() ?: 0.0) * 7.0
            }
        }
        return input.toDoubleOrNull()
    }

    fun calculate(): String {
        val isFDate = isInputIsDate(firstNumber)
        val isSDate = isInputIsDate(secondNumber)

        if (isFDate || isSDate) {
            if (isFDate && isSDate) {
                if (operator == "-") {
                    val d1 = parseDateString(firstNumber)
                    val d2 = parseDateString(secondNumber)
                    val cal1 = Calendar.getInstance().apply { set(d1.year, d1.month - 1, d1.day, 0, 0, 0); set(Calendar.MILLISECOND, 0) }
                    val cal2 = Calendar.getInstance().apply { set(d2.year, d2.month - 1, d2.day, 0, 0, 0); set(Calendar.MILLISECOND, 0) }
                    val diff = (cal1.timeInMillis - cal2.timeInMillis) / (24L * 60L * 60L * 1000L)
                    return formatDifference(diff.toInt())
                } else return "Error"
            }

            val datePart = if (isFDate) firstNumber else secondNumber
            val otherPart = if (isFDate) secondNumber else firstNumber
            val numericVal = parseToNumericValue(otherPart) ?: return "Error"

            val daysOffset = if (otherPart.contains("(")) {
                numericVal.toInt()
            } else {
                val factor = when (modelSelect) {
                    "YEAR" -> 365.0
                    "MONTH" -> 30.0
                    "WEEK" -> 7.0
                    else -> 1.0
                }
                (numericVal * factor).toInt()
            }

            val d = parseDateString(datePart)
            val cal = Calendar.getInstance().apply { set(d.year, d.month - 1, d.day, 0, 0, 0); set(Calendar.MILLISECOND, 0) }
            val amount = if (operator == "-") { if (isFDate) -daysOffset else return "Error" } else daysOffset
            cal.add(Calendar.DAY_OF_MONTH, amount)
            return String.format(Locale.US, "%d/%d/%d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
        }

        val v1 = if (firstNumber.contains("(")) parseBracketToDays(firstNumber)?.toDouble() else firstNumber.toDoubleOrNull()
        val v2 = if (secondNumber.contains("(")) parseBracketToDays(secondNumber)?.toDouble() else secondNumber.toDoubleOrNull()

        if (v1 == null || v2 == null) return "Error"

        val mathRes = when (operator) {
            "+" -> v1 + v2
            "-" -> v1 - v2
            "*" -> v1 * v2
            "÷" -> if (v2 != 0.0) v1 / v2 else return "Error"
            else -> return "Error"
        }
        return if (mathRes % 1.0 == 0.0) mathRes.toLong().toString() else String.format(Locale.US, "%.3f", mathRes).trimEnd('0').trimEnd('.')
    }

    fun showResultInDisplay(res: String) {
        resultText = res
    }

    fun updateInputDisplay() {
        // In compose, UI updates automatically based on state.
    }

    fun tryAutoCalculate() {
        if (firstNumber.isEmpty() || operator.isEmpty() || secondNumber.isEmpty()) {
            showResultInDisplay("")
            return
        }

        if (firstNumber.contains("/") && !isCompleteDatePattern(firstNumber)) {
            showResultInDisplay("")
            return
        }
        if (secondNumber.contains("/") && !isCompleteDatePattern(secondNumber)) {
            showResultInDisplay("")
            return
        }

        val res = calculate().replace(Regex("\\.0$"), "")
        if (res != "Error") {
            showResultInDisplay(res)
        } else {
            showResultInDisplay("")
        }
    }

    fun clearAll() {
        firstNumber = ""
        secondNumber = ""
        operator = ""
        displayExpression = ""
        isResultShown = false
        showResultInDisplay("")
    }

    fun showErrorFeedback(char: String) {
        if (isErrorShowing) return
        isErrorShowing = true
        errorChar = char
        scope.launch {
            delay(1000)
            isErrorShowing = false
            errorChar = ""
        }
    }

    fun validateChar(current: String, next: String): Boolean {
        val combined = current + next
        if (next == ".") {
            if (current.contains(".") || current.contains("/") || current.contains("(")) return false
            return true
        }
        if (next == "/") {
            if (current.contains("(")) return false
            if (current.count { it == '/' } >= 2) return false
            if (current.isEmpty() || !current.last().isDigit()) return false
            if (current.contains(".")) return false
            if (operator == "*" || operator == "÷") return false
            if (operator == "+" && isInputIsDate(firstNumber)) return false
            return true
        }

        if (current.contains("(")) {
            if (next == "+" && current.contains("+")) return false
            if (current.endsWith("(") && !next.all { it.isDigit() }) return false
            if (next.all { it.isDigit() } && current.contains("+")) {
                val daysStr = combined.substringAfter("+").replace(")", "")
                if (daysStr.isNotEmpty() && (daysStr.toIntOrNull() ?: 0) >= 7) return false
            }
            if (!next.all { it.isDigit() } && next != "+") return false
        }

        if (next.all { it.isDigit() } && current.contains("/")) {
            val parts = combined.split("/")
            if (parts.size == 2 && parts[1].isNotEmpty() && (parts[1].toIntOrNull() ?: 0) > 12) return false
            if (parts.size == 3 && parts[2].isNotEmpty() && (parts[2].toIntOrNull() ?: 0) > 31) return false
        }
        return true
    }

    fun handleInputChar(char: String) {
        val currentPart = if (operator.isEmpty()) firstNumber else secondNumber
        if (validateChar(currentPart, char)) {
            if (operator.isEmpty()) {
                firstNumber += char
            } else {
                secondNumber += char
            }
            displayExpression += char
            isResultShown = false
            tryAutoCalculate()
        } else {
            showErrorFeedback(char)
        }
    }

    fun handleNumericInput(input: String) {
        if (isResultShown && operator.isEmpty()) {
            clearAll()
        }
        handleInputChar(input)
    }

    fun insertBracket(b: String) {
        if (operator.isEmpty()) firstNumber += b else secondNumber += b
        displayExpression += b
        isResultShown = false
        tryAutoCalculate()
    }

    fun handleBracketClick() {
        if (modelSelect != "WEEK") {
            showErrorFeedback("( )")
            return
        }
        if (isResultShown && operator.isEmpty()) clearAll()

        val hasLeft = displayExpression.contains("(")
        val hasRight = displayExpression.contains(")")

        if (!hasLeft || (hasLeft && hasRight)) {
            val isAtStart = displayExpression.isEmpty()
            val isAfterOp = operator.isNotEmpty() && secondNumber.isEmpty()
            if (isAtStart || isAfterOp) {
                insertBracket("(")
            } else {
                showErrorFeedback("(")
            }
        } else if (!hasRight) {
            if (displayExpression.isNotEmpty() && (displayExpression.last().isDigit() || displayExpression.last() == ')')) {
                insertBracket(")")
            } else {
                showErrorFeedback(")")
            }
        } else {
            showErrorFeedback("( )")
        }
    }

    fun handleOperatorClick(opText: String) {
        val currentPart = if (operator.isEmpty()) firstNumber else secondNumber

        if (currentPart.contains("(") && !currentPart.contains(")") && opText == "+") {
            if (currentPart.last().isDigit() && !currentPart.contains("+")) {
                handleInputChar("+")
                return
            }
        }

        if (currentPart.contains("(") && !currentPart.contains(")")) {
            showErrorFeedback(opText)
            return
        }

        if (isResultShown) {
            isResultShown = false
            displayExpression = firstNumber
        }

        if (firstNumber == "Error") { clearAll(); return }

        val isDateContext = currentPart.contains("/") || (operator.isNotEmpty() && isInputIsDate(firstNumber))
        if (opText == "÷" && isDateContext) {
            handleInputChar("/")
            return
        }

        if (firstNumber.isEmpty()) {
            if (opText == "-") {
                firstNumber = "-"; displayExpression = "-"
            }
            return
        }

        if (currentPart.contains("/") && !isCompleteDatePattern(currentPart)) {
            showErrorFeedback(opText); return
        }

        if ((isInputIsDate(firstNumber) || currentPart.contains("/")) && (opText == "*" || opText == "÷")) {
            showErrorFeedback(opText); return
        }

        if (operator.isNotEmpty() && secondNumber.isNotEmpty()) {
            val res = calculate().replace(Regex("\\.0$"), "")
            if (res == "Error") { showResultInDisplay(res); return }

            firstNumber = res; operator = opText; secondNumber = ""
            displayExpression = res + opText
            showResultInDisplay(res)
        } else if (firstNumber != "-") {
            if (operator.isNotEmpty() && secondNumber.isEmpty()) {
                if (displayExpression.isNotEmpty()) displayExpression = displayExpression.dropLast(1)
            }
            operator = opText; displayExpression += opText
        }
    }

    fun handleEqual() {
        if (firstNumber.isEmpty() || operator.isEmpty() || secondNumber.isEmpty()) return
        val res = calculate().replace(Regex("\\.0$"), "")
        showResultInDisplay(res)
        if (res != "Error") {
            firstNumber = res; operator = ""; secondNumber = ""; isResultShown = true
        }
    }

    fun handlePlusMinus() {
        val currentPart = if (operator.isEmpty()) firstNumber else secondNumber
        if (currentPart.isNotEmpty() && !currentPart.contains("/") && !currentPart.contains("(")) {
            val oldLen = currentPart.length
            val newPart = if (currentPart.startsWith("-")) currentPart.substring(1) else "-$currentPart"
            if (operator.isEmpty()) firstNumber = newPart else secondNumber = newPart
            displayExpression = displayExpression.dropLast(oldLen) + newPart
        }
        tryAutoCalculate()
    }

    fun deleteOneChar() {
        if (displayExpression.isEmpty()) return
        displayExpression = displayExpression.dropLast(1)
        if (secondNumber.isNotEmpty()) secondNumber = secondNumber.dropLast(1)
        else if (operator.isNotEmpty()) operator = ""
        else if (firstNumber.isNotEmpty()) firstNumber = firstNumber.dropLast(1)
        tryAutoCalculate()
    }

    fun handleLocalDateClick() {
        if (isResultShown && operator.isEmpty()) clearAll()
        val ds = currentLocalDate
        if (operator.isEmpty()) { firstNumber = ds; displayExpression = ds }
        else if (operator == "-" || (operator == "+" && !isInputIsDate(firstNumber))) {
            secondNumber = ds; displayExpression += ds
        } else { showErrorFeedback("Date"); return }
        tryAutoCalculate()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F3F4))
    ) {
        // Display Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color.White)
        ) {
            Text(
                text = when (modelSelect) {
                    "YEAR" -> "年"
                    "MONTH" -> "月"
                    "DAY" -> "日"
                    else -> "周"
                },
                modifier = Modifier.padding(start = 10.dp, top = 10.dp),
                color = Color(0xFF109D58),
                fontSize = 25.sp
            )
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(130f),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Row(modifier = Modifier.padding(end = 20.dp)) {
                        val baseText = if (displayExpression.isEmpty()) "0" else displayExpression
                        Text(
                            text = baseText,
                            color = Color(0xFF3C4043),
                            fontSize = 30.sp,
                            textAlign = TextAlign.End
                        )
                        if (isErrorShowing) {
                            Text(
                                text = errorChar,
                                color = Color.Red,
                                fontSize = 30.sp,
                                textAlign = TextAlign.End
                            )
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(Color(0xFFF1F3F4))
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(70f),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        text = resultText,
                        modifier = Modifier.padding(end = 10.dp),
                        color = Color(0xFF109D58),
                        fontSize = 30.sp,
                        textAlign = TextAlign.End
                    )
                }
            }
        }

        // Mode Selector and Brackets
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .height(60.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .weight(4f)
                    .fillMaxHeight()
                    .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))
            ) {
                listOf("年" to "YEAR", "月" to "MONTH", "日" to "DAY", "周" to "WEEK").forEach { (label, model) ->
                    val isSelected = modelSelect == model
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(if (isSelected) Color(0xFF109D58) else Color.White)
                            .clickable {
                                modelSelect = model
                                tryAutoCalculate()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Color.White else Color(0xFF3C4043),
                            fontSize = 14.sp
                        )
                    }
                }
            }
            Button(
                onClick = { handleBracketClick() },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                contentPadding = PaddingValues(0.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Text("( )", color = Color(0xFF3C4043), fontSize = 20.sp)
            }
        }

        // Keyboard Rows
        val buttonModifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .padding(horizontal = 5.dp, vertical = 5.dp)

        Column(modifier = Modifier.weight(1f).padding(horizontal = 0.dp)) {
            // Row 1
            Row(modifier = Modifier.weight(1f)) {
                CalcButton("AC", Color(0xFFD93025), Color(0xFFFEEBEB), buttonModifier) { clearAll() }
                CalcButton("-+", Color(0xFF3C4043), Color(0xFFF8F9FA), buttonModifier) { handlePlusMinus() }
                CalcButton("÷", Color(0xFF3C4043), Color(0xFFF8F9FA), buttonModifier) { handleOperatorClick("÷") }
                CalcButton("*", Color(0xFF3C4043), Color(0xFFF8F9FA), buttonModifier) { handleOperatorClick("*") }
            }
            // Row 2
            Row(modifier = Modifier.weight(1f)) {
                CalcButton("7", Color(0xFF3C4043), Color.White, buttonModifier) { handleNumericInput("7") }
                CalcButton("8", Color(0xFF3C4043), Color.White, buttonModifier) { handleNumericInput("8") }
                CalcButton("9", Color(0xFF3C4043), Color.White, buttonModifier) { handleNumericInput("9") }
                CalcButton("←", Color(0xFF3C4043), Color(0xFFF8F9FA), buttonModifier) { deleteOneChar() }
            }
            // Row 3
            Row(modifier = Modifier.weight(1f)) {
                CalcButton("4", Color(0xFF3C4043), Color.White, buttonModifier) { handleNumericInput("4") }
                CalcButton("5", Color(0xFF3C4043), Color.White, buttonModifier) { handleNumericInput("5") }
                CalcButton("6", Color(0xFF3C4043), Color.White, buttonModifier) { handleNumericInput("6") }
                CalcButton("-", Color(0xFF3C4043), Color(0xFFF8F9FA), buttonModifier) { handleOperatorClick("-") }
            }
            // Row 4
            Row(modifier = Modifier.weight(1f)) {
                CalcButton("1", Color(0xFF3C4043), Color.White, buttonModifier) { handleNumericInput("1") }
                CalcButton("2", Color(0xFF3C4043), Color.White, buttonModifier) { handleNumericInput("2") }
                CalcButton("3", Color(0xFF3C4043), Color.White, buttonModifier) { handleNumericInput("3") }
                CalcButton("+", Color(0xFF3C4043), Color(0xFFF8F9FA), buttonModifier) { handleOperatorClick("+") }
            }
            // Row 5 & 6 combined layout to match spans
            Row(modifier = Modifier.weight(2f)) {
                Column(modifier = Modifier.weight(2f).fillMaxHeight()) {
                    Row(modifier = Modifier.weight(1f)) {
                        CalcButton("0", Color(0xFF3C4043), Color.White, Modifier.weight(1f).fillMaxHeight().padding(5.dp)) { handleNumericInput("0") }
                        CalcButton(".", Color(0xFF3C4043), Color.White, Modifier.weight(1f).fillMaxHeight().padding(5.dp)) { handleNumericInput(".") }
                    }
                    CalcButton(currentLocalDate, Color(0xFF109D58), Color.White, Modifier.weight(1f).fillMaxSize().padding(5.dp), fontSize = 18.sp) { handleLocalDateClick() }
                }
                CalcButton("/", Color(0xFF3C4043), Color(0xFFF8F9FA), Modifier.weight(1f).fillMaxHeight().padding(5.dp)) { handleInputChar("/") }
                CalcButton("=", Color.White, Color(0xFF109D58), Modifier.weight(1f).fillMaxHeight().padding(5.dp)) { handleEqual() }
            }
        }
    }
}

@Composable
fun CalcButton(
    text: String,
    textColor: Color,
    backgroundColor: Color,
    modifier: Modifier,
    fontSize: androidx.compose.ui.unit.TextUnit = 30.sp,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(4.dp),
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
        contentPadding = PaddingValues(0.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = fontSize,
            textAlign = TextAlign.Center,
            fontWeight = if (text == "←") FontWeight.Bold else FontWeight.Normal
        )
    }
}

data class MyDateData(var year: Int, var month: Int, var day: Int)

@Preview(showBackground = true)
@Composable
fun DateCalculatorScreenPreview() {
    RecycleTheme {
        DateCalculatorScreen()
    }
}
