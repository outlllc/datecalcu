package com.duckgo.medtools.babyweight

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.duckgo.medtools.R

class BabyWeight : Fragment() {

    private val viewModel: BabyWeightViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    BabyWeightScreen(viewModel)
                }
            }
        }
    }
}

@Composable
fun BabyWeightScreen(viewModel: BabyWeightViewModel) {
    val isEarlyPregnancyMode by viewModel.isEarlyPregnancyMode.observeAsState(false)
    val predictedWeight by viewModel.predictedWeight.observeAsState("")
    val percentileReports by viewModel.percentileReports.observeAsState(emptyList())
    val clearTrigger by viewModel.clearTrigger.observeAsState()
    val focusManager = LocalFocusManager.current

    // Biometric States
    var pregnancyWeek by remember { mutableStateOf("") }
    var bpd by remember { mutableStateOf("") }
    var hc by remember { mutableStateOf("") }
    var ac by remember { mutableStateOf("") }
    var fl by remember { mutableStateOf("") }
    var hl by remember { mutableStateOf("") }
    var ulna by remember { mutableStateOf("") }
    var tibia by remember { mutableStateOf("") }

    // Early Pregnancy States
    var gsAvg by remember { mutableStateOf("") }
    var crl by remember { mutableStateOf("") }
    var gs1 by remember { mutableStateOf("") }
    var gs2 by remember { mutableStateOf("") }
    var gs3 by remember { mutableStateOf("") }

    // Date States
    var lmp by remember { mutableStateOf("") }
    var inspectionDate by remember { mutableStateOf("") }

    // Clear Logic
    LaunchedEffect(clearTrigger) {
        if (clearTrigger != null) {
            pregnancyWeek = ""
            bpd = ""
            hc = ""
            ac = ""
            fl = ""
            hl = ""
            ulna = ""
            tibia = ""
            gsAvg = ""
            crl = ""
            gs1 = ""
            gs2 = ""
            gs3 = ""
            lmp = ""
            inspectionDate = ""
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFFF5F7FA))
    ) {
        Column(modifier = Modifier
            .fillMaxSize()
            .imePadding() // 确保底部按钮被键盘推起
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(colorResource(id = R.color.green_kuan))
                    .padding(start = 20.dp, top = 30.dp)
            ) {
                Column {
                    Text(
                        text = "胎儿体重计算",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Fetal Weight Estimation",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }
            }

            // Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(15.dp))

                // Biometrics Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        if (!isEarlyPregnancyMode) {
                            // Standard Biometrics
                            BiometricRow(
                                label1 = "孕周(周)",
                                value1 = pregnancyWeek,
                                onValueChange1 = { pregnancyWeek = it },
                                hint1 = "手动输入",
                                label2 = "双顶径(mm)",
                                value2 = bpd,
                                onValueChange2 = { bpd = it },
                                hint2 = "31-100",
                                labelColor1 = colorResource(id = R.color.green_kuan),
                                clickableLabel1 = true,
                                onLabelClick1 = { viewModel.toggleMode() }
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = Color(0xFFF5F5F5))
                            BiometricRow(
                                label1 = "头围(mm)",
                                value1 = hc,
                                onValueChange1 = { hc = it },
                                hint1 = "请输入",
                                label2 = "腹围(mm)",
                                value2 = ac,
                                onValueChange2 = { ac = it },
                                hint2 = "200-400"
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = Color(0xFFF5F5F5))
                            BiometricRow(
                                label1 = "股骨(mm)",
                                value1 = fl,
                                onValueChange1 = { fl = it },
                                hint1 = "40-83",
                                label2 = "肱骨(mm)",
                                value2 = hl,
                                onValueChange2 = { hl = it },
                                hint2 = "请输入"
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = Color(0xFFF5F5F5))
                            BiometricRow(
                                label1 = "尺骨(mm)",
                                value1 = ulna,
                                onValueChange1 = { ulna = it },
                                hint1 = "请输入",
                                label2 = "胫骨(mm)",
                                value2 = tibia,
                                onValueChange2 = { tibia = it },
                                hint2 = "请输入"
                            )
                        } else {
                            // Early Pregnancy Biometrics
                            BiometricRow(
                                label1 = "孕囊直径",
                                value1 = gsAvg,
                                onValueChange1 = { gsAvg = it },
                                hint1 = "均值(mm)",
                                label2 = "顶臀长(mm)",
                                value2 = crl,
                                onValueChange2 = { crl = it },
                                hint2 = "请输入",
                                labelColor1 = colorResource(id = R.color.green_kuan),
                                clickableLabel1 = true,
                                onLabelClick1 = { viewModel.toggleMode() }
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = Color(0xFFF5F5F5))
                            BiometricRow(
                                label1 = "孕囊径线1(mm)",
                                value1 = gs1,
                                onValueChange1 = { gs1 = it },
                                hint1 = "请输入",
                                label2 = "孕囊径线2(mm)",
                                value2 = gs2,
                                onValueChange2 = { gs2 = it },
                                hint2 = "请输入"
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = Color(0xFFF5F5F5))
                            Row(modifier = Modifier.fillMaxWidth().height(48.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("孕囊径线3(mm)", modifier = Modifier.width(95.dp), fontSize = 13.sp, color = Color(0xFF444444))
                                CompactTextField(
                                    value = gs3,
                                    onValueChange = { gs3 = it },
                                    modifier = Modifier.width(110.dp),
                                    hint = "请输入"
                                )
                            }
                        }
                    }
                }

                // Dates Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    BiometricRow(
                        modifier = Modifier.padding(12.dp),
                        label1 = "末次月经",
                        value1 = lmp,
                        onValueChange1 = { lmp = it },
                        hint1 = "YYYYMMDD",
                        label2 = "检查日期",
                        value2 = inspectionDate,
                        onValueChange2 = { inspectionDate = it },
                        hint2 = "YYYYMMDD",
                        keyboardType = KeyboardType.Number
                    )
                }

                // Result Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "预测结果", color = Color(0xFF444444), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = predictedWeight,
                            modifier = Modifier.padding(top = 8.dp),
                            color = colorResource(id = R.color.green_kuan),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 22.sp
                        )
                    }
                }

                // Reports Section
                if (!isEarlyPregnancyMode && percentileReports.isNotEmpty()) {
                    Text(
                        text = "计算详情报告",
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        color = Color(0xFF444444),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    percentileReports.forEach { report ->
                        ReportItem(report)
                    }
                }
            }

            // Bottom Actions
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.clearAll() },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF666666))
                    ) {
                        Text("清除数据")
                    }
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.performCalculations(
                                acStr = ac,
                                flStr = fl,
                                bpdStr = bpd,
                                hcStr = hc,
                                hlStr = hl,
                                ulnaStr = ulna,
                                tibiaStr = tibia,
                                weekStr = pregnancyWeek,
                                lmpStr = lmp,
                                inspectStr = inspectionDate,
                                gsAvgStr = gsAvg,
                                gs1Str = gs1,
                                gs2Str = gs2,
                                gs3Str = gs3,
                                crlStr = crl
                            )
                        },
                        modifier = Modifier.weight(1.5f).height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.green_kuan))
                    ) {
                        Text("生成计算报告", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun BiometricRow(
    modifier: Modifier = Modifier,
    label1: String,
    value1: String,
    onValueChange1: (String) -> Unit,
    hint1: String = "请输入",
    label2: String,
    value2: String,
    onValueChange2: (String) -> Unit,
    hint2: String = "请输入",
    labelColor1: Color = Color(0xFF444444),
    clickableLabel1: Boolean = false,
    onLabelClick1: () -> Unit = {},
    keyboardType: KeyboardType = KeyboardType.Decimal
) {
    Row(
        modifier = modifier.fillMaxWidth().height(48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label1,
            modifier = Modifier
                .width(95.dp)
                .then(if (clickableLabel1) Modifier.clickable { onLabelClick1() } else Modifier),
            fontSize = 13.sp,
            color = labelColor1,
            lineHeight = 16.sp
        )
        CompactTextField(
            value = value1,
            onValueChange = onValueChange1,
            modifier = Modifier.weight(1f),
            hint = hint1,
            keyboardType = keyboardType
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label2,
            modifier = Modifier.width(95.dp),
            fontSize = 13.sp,
            color = Color(0xFF444444),
            lineHeight = 16.sp
        )
        CompactTextField(
            value = value2,
            onValueChange = onValueChange2,
            modifier = Modifier.weight(1f),
            hint = hint2,
            keyboardType = keyboardType
        )
    }
}

@Composable
fun CompactTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    hint: String = "",
    keyboardType: KeyboardType = KeyboardType.Decimal
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val borderColor = if (isFocused) colorResource(id = R.color.green_kuan) else Color(0xFFE0E0E0)

    // 自定义文本选择颜色，将 handleColor 设为透明来隐藏水滴形光标
    val customTextSelectionColors = TextSelectionColors(
        handleColor = Color.Transparent,
        backgroundColor = colorResource(id = R.color.green_kuan).copy(alpha = 0.4f)
    )

    CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier
                .height(34.dp)
                .background(Color.White, RoundedCornerShape(6.dp))
                .border(1.dp, borderColor, RoundedCornerShape(6.dp)),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = true,
            interactionSource = interactionSource,
            textStyle = TextStyle(fontSize = 13.sp, color = Color.Black),
            cursorBrush = SolidColor(colorResource(id = R.color.green_kuan)), // 显式设置光标颜色为竖线
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (value.isEmpty()) {
                        Text(text = hint, color = Color.LightGray, fontSize = 12.sp)
                    }
                    innerTextField()
                }
            }
        )
    }
}

@Composable
fun ReportItem(report: UserCompareReport) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header Row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = report.measure, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
                Text(text = "百分位: ${report.interpretation}", fontSize = 13.sp, color = colorResource(id = R.color.green_kuan), fontWeight = FontWeight.Medium)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Reference Row (p5 - p50 - p95)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "p5: ${report.original.p5}", fontSize = 11.sp, color = Color(0xFF999999))
                Text(text = "p50: ${report.original.p50}", fontSize = 11.sp, color = Color(0xFF999999))
                Text(text = "p95: ${report.original.p95}", fontSize = 11.sp, color = Color(0xFF999999))
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Progress/Indicator Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(10.dp))
            ) {
                // Background indicators for p5 and p95 regions (optional visual aid)
                Row(modifier = Modifier.fillMaxSize()) {
                    Spacer(modifier = Modifier.weight(0.05f).fillMaxHeight().background(Color(0xFFEEEEEE)))
                    Spacer(modifier = Modifier.weight(0.90f))
                    Spacer(modifier = Modifier.weight(0.05f).fillMaxHeight().background(Color(0xFFEEEEEE)))
                }

                // Measurement Value Indicator
                report.userValue?.let { value ->
                    val bias = calculateBias(value, report.original)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(bias)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Box(
                            modifier = Modifier
                                .size(height = 20.dp, width = 60.dp)
                                .background(colorResource(id = R.color.green_kuan), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${value}mm",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 计算测量值在显示条中的位置百分比 (0.0 to 1.0)
 * 小于 p5 显示最左侧 (0.0)
 * 大于 p95 显示最右侧 (1.0)
 * 在两者之间，映射到 0.05 到 0.95 之间
 */
private fun calculateBias(value: Double, original: PValues): Float {
    if (value <= original.p5) return 0.08f // Small margin from left
    if (value >= original.p95) return 1.0f
    
    // Linearly interpolate between p5 (0.05) and p95 (0.95)
    val range = original.p95 - original.p5
    if (range <= 0) return 0.5f
    
    val fraction = (value - original.p5) / range
    return (0.08f + fraction * 0.84f).toFloat() // Map to center area [0.08, 0.92]
}
