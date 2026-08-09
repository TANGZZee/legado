package io.legado.app.ui.about

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import androidx.annotation.DrawableRes
import io.legado.app.R
import io.legado.app.lib.theme.UiCorner
import io.legado.app.lib.theme.primaryTextColor
import io.legado.app.lib.theme.rememberThemeUiPalette
import io.legado.app.lib.theme.accentColor
import io.legado.app.lib.theme.secondaryTextColor
import io.legado.app.lib.theme.titleTypeface
import io.legado.app.lib.theme.uiTypeface
import io.legado.app.ui.widget.compose.appSettingPanelBackground
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import java.time.format.DateTimeFormatter

@Immutable
data class ReadRecordTrendItem(
    val metric: ReadRecordTrendMetric,
    val chartType: ReadRecordTrendChartType
)

@Composable
fun ReadRecordTrendCard(
    ui: ReadRecordTrendUi,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val palette = rememberThemeUiPalette()
    val primaryText = Color(context.primaryTextColor)
    val secondaryText = Color(context.secondaryTextColor)
    val chartColor = Color(context.accentColor)
    val titleFont = FontFamily(context.titleTypeface())
    val bodyFont = FontFamily(context.uiTypeface())
    val panelRadius = UiCorner.panelRadius(context)
    val panelImage = remember(context, panelRadius, palette.signature) {
        UiCorner.panelImageDrawable(context, panelRadius)
    }
    val surface = UiCorner.surfaceColor(palette.cardColor)
    val border = UiCorner.panelBorderColor(context)

    var selectedMetric by remember { mutableStateOf(ReadRecordTrendMetric.TIME) }
    var selectedChartType by remember { mutableStateOf(ReadRecordTrendChartType.BAR) }

    val title = when (selectedMetric) {
        ReadRecordTrendMetric.TIME -> context.getString(R.string.read_record_trend_time_title) + "趋势"
        ReadRecordTrendMetric.WORDS -> context.getString(R.string.read_record_trend_words_title) + "趋势"
        ReadRecordTrendMetric.SPEED -> context.getString(R.string.read_record_trend_speed_title) + "趋势"
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .appSettingPanelBackground(
                normalColor = surface,
                panelImage = panelImage,
                borderColor = border,
                radiusPx = panelRadius
            )
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = primaryText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = titleFont,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricSelectorButton(
                    metric = selectedMetric,
                    onMetricSelected = { selectedMetric = it },
                    primaryText = primaryText,
                    secondaryText = secondaryText,
                    bodyFont = bodyFont
                )
                ChartTypeSelectorButton(
                    chartType = selectedChartType,
                    onChartTypeSelected = { selectedChartType = it },
                    primaryText = primaryText,
                    secondaryText = secondaryText,
                    bodyFont = bodyFont
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .padding(top = 16.dp)
        ) {
            ReadRecordTrendChart(
                period = ui.period,
                data = ui.data,
                metric = selectedMetric,
                chartType = selectedChartType,
                primaryText = primaryText,
                secondaryText = secondaryText,
                titleFont = titleFont,
                bodyFont = bodyFont,
                chartColor = chartColor
            )
        }
    }
}

@Composable
private fun MetricSelectorButton(
    metric: ReadRecordTrendMetric,
    onMetricSelected: (ReadRecordTrendMetric) -> Unit,
    primaryText: Color,
    secondaryText: Color,
    bodyFont: FontFamily,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    val iconRes = metricIconRes(metric)
    Box(modifier = modifier) {
        TrendIconButton(
            iconRes = iconRes,
            contentDescription = metricLabel(context, metric),
            onClick = { expanded = true },
            primaryText = primaryText,
            secondaryText = secondaryText
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            ReadRecordTrendMetric.entries.forEach { item ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(metricIconRes(item)),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(primaryText),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = metricLabel(context, item),
                                color = primaryText,
                                fontSize = 14.sp,
                                fontFamily = bodyFont
                            )
                        }
                    },
                    trailingIcon = {
                        if (item == metric) {
                            Image(
                                painter = painterResource(R.drawable.ic_check),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(primaryText),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    onClick = {
                        onMetricSelected(item)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ChartTypeSelectorButton(
    chartType: ReadRecordTrendChartType,
    onChartTypeSelected: (ReadRecordTrendChartType) -> Unit,
    primaryText: Color,
    secondaryText: Color,
    bodyFont: FontFamily,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    val iconRes = chartTypeIconRes(chartType)
    Box(modifier = modifier) {
        TrendIconButton(
            iconRes = iconRes,
            contentDescription = chartTypeLabel(context, chartType),
            onClick = { expanded = true },
            primaryText = primaryText,
            secondaryText = secondaryText
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            ReadRecordTrendChartType.entries.forEach { item ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(chartTypeIconRes(item)),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(primaryText),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = chartTypeLabel(context, item),
                                color = primaryText,
                                fontSize = 14.sp,
                                fontFamily = bodyFont
                            )
                        }
                    },
                    trailingIcon = {
                        if (item == chartType) {
                            Image(
                                painter = painterResource(R.drawable.ic_check),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(primaryText),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    onClick = {
                        onChartTypeSelected(item)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun TrendIconButton(
    @DrawableRes iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit,
    primaryText: Color,
    secondaryText: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(36.dp)
            .background(
                color = secondaryText.copy(alpha = 0.10f),
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            colorFilter = ColorFilter.tint(primaryText),
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun ReadRecordTrendChart(
    period: ReadRecordStatsPeriod,
    data: ReadRecordTrendData,
    metric: ReadRecordTrendMetric,
    chartType: ReadRecordTrendChartType,
    primaryText: Color,
    secondaryText: Color,
    titleFont: FontFamily,
    bodyFont: FontFamily,
    chartColor: Color,
    modifier: Modifier = Modifier
) {
    val values = data.points.map { it.value(metric) }
    val rawMax = values.maxOrNull() ?: 0L
    val ticks = trendAxisTicks(metric, rawMax)
    val maxValue = ticks.maxValue.coerceAtLeast(1L)
    val textMeasurer = rememberTextMeasurer()
    var selectedIndex by remember { mutableStateOf(-1) }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(data.points, metric) {
                detectTapGestures { tap ->
                    val tapWidth = this.size.width
                    val tapPaddingLeft = 52.dp.toPx()
                    val tapPaddingRight = 12.dp.toPx()
                    val tapChartLeft = tapPaddingLeft
                    val tapChartRight = tapWidth - tapPaddingRight
                    if (data.points.isEmpty() || tap.x < tapChartLeft || tap.x > tapChartRight) {
                        selectedIndex = -1
                        return@detectTapGestures
                    }
                    val tapChartWidth = tapChartRight - tapChartLeft
                    val count = data.points.size
                    val nearest = data.points.indices.minByOrNull { index ->
                        val x = tapChartLeft + if (count == 1) {
                            tapChartWidth / 2f
                        } else {
                            tapChartWidth * index / (count - 1).toFloat()
                        }
                        abs(x - tap.x)
                    } ?: -1
                    if (nearest >= 0) {
                        selectedIndex = if (selectedIndex == nearest) -1 else nearest
                    }
                }
            }
    ) {
        val paddingLeft = 52.dp.toPx()
        val paddingBottom = 28.dp.toPx()
        val paddingTop = 16.dp.toPx()
        val paddingRight = 12.dp.toPx()
        val chartLeft = paddingLeft
        val chartTop = paddingTop
        val chartRight = size.width - paddingRight
        val chartBottom = size.height - paddingBottom
        val chartWidth = chartRight - chartLeft
        val chartHeight = chartBottom - chartTop

        drawGridLines(
            ticks = ticks,
            maxValue = maxValue,
            chartLeft = chartLeft,
            chartTop = chartTop,
            chartRight = chartRight,
            chartBottom = chartBottom,
            textMeasurer = textMeasurer,
            secondaryText = secondaryText,
            bodyFont = bodyFont
        )

        if (data.points.isEmpty()) return@Canvas

        val points = data.points.mapIndexed { index, point ->
            val x = chartLeft + if (data.points.size == 1) {
                chartWidth / 2f
            } else {
                chartWidth * index / (data.points.size - 1).toFloat()
            }
            val y = chartBottom - (point.value(metric).toFloat() / maxValue) * chartHeight
            Offset(x, y)
        }

        when (chartType) {
            ReadRecordTrendChartType.BAR -> {
                val barWidth = if (data.points.size == 1) {
                    chartWidth * 0.2f
                } else {
                    (chartWidth / (data.points.size - 1)) * 0.5f
                }
                points.forEach { point ->
                    drawRoundRect(
                        color = chartColor,
                        topLeft = Offset(point.x - barWidth / 2, point.y),
                        size = androidx.compose.ui.geometry.Size(barWidth, chartBottom - point.y),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth / 4, barWidth / 4)
                    )
                }
                if (selectedIndex in points.indices) {
                    val sp = points[selectedIndex]
                    drawRoundRect(
                        color = chartColor,
                        topLeft = Offset(sp.x - barWidth / 2, sp.y),
                        size = androidx.compose.ui.geometry.Size(barWidth, chartBottom - sp.y),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth / 4, barWidth / 4),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }
            ReadRecordTrendChartType.LINE -> {
                if (points.size == 1) {
                    val p = points.first()
                    drawCircle(
                        color = chartColor,
                        radius = 3.dp.toPx(),
                        center = p
                    )
                } else {
                    val curvePath = Path()
                    curvePath.moveTo(points.first().x, points.first().y)
                    for (i in 1 until points.size) {
                        val prev = points[i - 1]
                        val curr = points[i]
                        val midX = (prev.x + curr.x) / 2f
                        curvePath.cubicTo(
                            midX, prev.y,
                            midX, curr.y,
                            curr.x, curr.y
                        )
                    }
                    val areaPath = Path().apply {
                        addPath(curvePath)
                        lineTo(points.last().x, chartBottom)
                        lineTo(points.first().x, chartBottom)
                        close()
                    }
                    drawPath(areaPath, chartColor.copy(alpha = 0.14f))
                    drawPath(curvePath, chartColor, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
                    points.forEach { p ->
                        drawCircle(
                            color = chartColor,
                            radius = 2.5.dp.toPx(),
                            center = p
                        )
                    }
                }
            }
        }

        if (selectedIndex in data.points.indices) {
            drawTrendPopup(
                period = period,
                index = selectedIndex,
                point = data.points[selectedIndex],
                metric = metric,
                anchor = points[selectedIndex],
                chartTop = chartTop,
                chartLeft = chartLeft,
                canvasWidth = size.width,
                canvasHeight = size.height,
                textMeasurer = textMeasurer,
                bodyFont = bodyFont
            )
        }

        drawXAxisLabels(
            period = period,
            data = data,
            chartLeft = chartLeft,
            chartRight = chartRight,
            chartBottom = chartBottom,
            textMeasurer = textMeasurer,
            secondaryText = secondaryText,
            bodyFont = bodyFont
        )
    }
}

private fun DrawScope.drawTrendPopup(
    period: ReadRecordStatsPeriod,
    index: Int,
    point: ReadRecordTrendPoint,
    metric: ReadRecordTrendMetric,
    anchor: Offset,
    chartTop: Float,
    chartLeft: Float,
    canvasWidth: Float,
    canvasHeight: Float,
    textMeasurer: TextMeasurer,
    bodyFont: FontFamily
) {
    val timeLabel = "时间：" + trendTimeLabel(period, point, index)
    val valueLabel = trendMetricLabel(metric, point)
    val textColor = Color.White
    val popupBg = Color(0xE6262626)
    val lineStyle = TextStyle(color = textColor, fontSize = 12.sp, fontFamily = bodyFont)
    val timeLayout = textMeasurer.measure(timeLabel, lineStyle)
    val valueLayout = textMeasurer.measure(valueLabel, lineStyle)
    val padding = 10.dp.toPx()
    val lineGap = 4.dp.toPx()
    val width = maxOf(timeLayout.size.width, valueLayout.size.width) + padding * 2f
    val height = timeLayout.size.height + valueLayout.size.height + lineGap + padding * 2f
    val gap = 10.dp.toPx()
    var left = anchor.x + gap
    if (left + width > canvasWidth) {
        left = anchor.x - gap - width
    }
    left = left.coerceIn(chartLeft, (canvasWidth - width).coerceAtLeast(chartLeft))
    var top = anchor.y - height - gap
    if (top < chartTop) {
        top = anchor.y + gap
    }
    top = top.coerceIn(0f, (canvasHeight - height).coerceAtLeast(0f))
    drawRoundRect(
        color = popupBg,
        topLeft = Offset(left, top),
        size = androidx.compose.ui.geometry.Size(width, height),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx(), 8.dp.toPx())
    )
    drawText(
        timeLayout,
        color = textColor,
        topLeft = Offset(left + padding, top + padding)
    )
    drawText(
        valueLayout,
        color = textColor,
        topLeft = Offset(left + padding, top + padding + timeLayout.size.height + lineGap)
    )
}

private fun trendTimeLabel(period: ReadRecordStatsPeriod, point: ReadRecordTrendPoint, index: Int): String {
    return when (period) {
        ReadRecordStatsPeriod.DAY -> {
            val hour = index.coerceIn(0, 24)
            "${hour.toString().padStart(2, '0')}:00"
        }
        ReadRecordStatsPeriod.WEEK -> {
            val weekdays = listOf("一", "二", "三", "四", "五", "六", "日")
            val weekday = weekdays.getOrElse(index.coerceIn(0, 6)) { "" }
            "${point.date.monthValue}月${point.date.dayOfMonth}日 周$weekday"
        }
        ReadRecordStatsPeriod.MONTH -> "${point.date.monthValue}月${point.date.dayOfMonth}日"
        ReadRecordStatsPeriod.YEAR -> "${point.date.monthValue}月"
        ReadRecordStatsPeriod.TOTAL -> "${point.date.year}年"
    }
}

private fun trendMetricLabel(metric: ReadRecordTrendMetric, point: ReadRecordTrendPoint): String {
    return when (metric) {
        ReadRecordTrendMetric.TIME -> "阅读时间：" + formatTrendTime(point.readTime)
        ReadRecordTrendMetric.WORDS -> "阅读字数：" + formatTrendWords(point.readWords)
        ReadRecordTrendMetric.SPEED -> "阅读速度：" + formatTrendSpeed(point.speed) + "字/分钟"
    }
}
private fun DrawScope.drawGridLines(
    ticks: TrendAxisTicks,
    maxValue: Long,
    chartLeft: Float,
    chartTop: Float,
    chartRight: Float,
    chartBottom: Float,
    textMeasurer: TextMeasurer,
    secondaryText: Color,
    bodyFont: FontFamily
) {
    val step = if (ticks.ticks.size >= 2) {
        maxValue / (ticks.ticks.size - 1).toFloat()
    } else {
        maxValue.toFloat()
    }
    ticks.ticks.forEachIndexed { index, tickValue ->
        val y = chartBottom - (tickValue.toFloat() / maxValue) * (chartBottom - chartTop)
        drawLine(
            color = secondaryText.copy(alpha = 0.15f),
            start = Offset(chartLeft, y),
            end = Offset(chartRight, y),
            strokeWidth = 1.dp.toPx(),
            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                floatArrayOf(8.dp.toPx(), 4.dp.toPx()),
                0f
            )
        )
        val text = ticks.format(tickValue)
        val textLayout = textMeasurer.measure(
            text,
            TextStyle(
                color = secondaryText,
                fontSize = 11.sp,
                fontFamily = bodyFont
            )
        )
        drawText(
            textLayout,
            color = secondaryText,
            topLeft = Offset(
                chartLeft - textLayout.size.width - 6.dp.toPx(),
                y - textLayout.size.height / 2f
            )
        )
    }
}

private fun DrawScope.drawXAxisLabels(
    period: ReadRecordStatsPeriod,
    data: ReadRecordTrendData,
    chartLeft: Float,
    chartRight: Float,
    chartBottom: Float,
    textMeasurer: TextMeasurer,
    secondaryText: Color,
    bodyFont: FontFamily
) {
    val count = data.points.size
    if (count == 0) return
    val indices = when (period) {
        ReadRecordStatsPeriod.DAY -> (0..24 step 6).filter { it < count }
        ReadRecordStatsPeriod.WEEK -> (0 until count).toList()
        ReadRecordStatsPeriod.MONTH -> {
            val monthLength = data.points.lastOrNull()?.date?.lengthOfMonth() ?: 31
            val labelDays = setOf(1, 6, 11, 16, 21, 26, monthLength)
            data.points.indices.filter { data.points[it].date.dayOfMonth in labelDays }
        }
        else -> {
            val step = if (count <= 8) 1 else max(1, count / 7)
            (0 until count).filter { it % step == 0 || it == count - 1 }.distinct()
        }
    }
    indices.forEach { index ->
        val point = data.points[index]
        val x = chartLeft + if (count == 1) {
            (chartRight - chartLeft) / 2f
        } else {
            (chartRight - chartLeft) * index / (count - 1).toFloat()
        }
        val label = xAxisLabel(period, point.date, index, count)
        val textLayout = textMeasurer.measure(
            label,
            TextStyle(
                color = secondaryText,
                fontSize = 11.sp,
                fontFamily = bodyFont
            )
        )
        drawText(
            textLayout,
            color = secondaryText,
            topLeft = Offset(x - textLayout.size.width / 2f, chartBottom + 4.dp.toPx())
        )
    }
}

private fun xAxisLabel(period: ReadRecordStatsPeriod, date: java.time.LocalDate, index: Int, count: Int): String {
    return when (period) {
        ReadRecordStatsPeriod.DAY -> {
            val hours = listOf("0", "6", "12", "18", "24")
            hours.getOrElse(index / 6) { "" }
        }
        ReadRecordStatsPeriod.WEEK -> {
            val weekdays = listOf("一", "二", "三", "四", "五", "六", "日")
            weekdays.getOrElse(index) { "" }
        }
        ReadRecordStatsPeriod.MONTH -> "${date.dayOfMonth}"
        ReadRecordStatsPeriod.YEAR -> "${date.monthValue}"
        ReadRecordStatsPeriod.TOTAL -> "${date.year}"
    }
}

private fun metricIconRes(metric: ReadRecordTrendMetric): Int {
    return when (metric) {
        ReadRecordTrendMetric.TIME -> R.drawable.ic_timer_black_24dp
        ReadRecordTrendMetric.WORDS -> R.drawable.ic_read_record_trend_words
        ReadRecordTrendMetric.SPEED -> R.drawable.ic_speed_control
    }
}

private fun metricLabel(context: Context, metric: ReadRecordTrendMetric): String {
    return when (metric) {
        ReadRecordTrendMetric.TIME -> context.getString(R.string.read_record_trend_time_title) + "趋势"
        ReadRecordTrendMetric.WORDS -> context.getString(R.string.read_record_trend_words_title) + "趋势"
        ReadRecordTrendMetric.SPEED -> context.getString(R.string.read_record_trend_speed_title) + "趋势"
    }
}

private fun chartTypeIconRes(chartType: ReadRecordTrendChartType): Int {
    return when (chartType) {
        ReadRecordTrendChartType.BAR -> R.drawable.ic_read_record_trend_bar
        ReadRecordTrendChartType.LINE -> R.drawable.ic_read_record_trend_line
    }
}

private fun chartTypeLabel(context: Context, chartType: ReadRecordTrendChartType): String {
    return when (chartType) {
        ReadRecordTrendChartType.BAR -> context.getString(R.string.read_record_trend_bar_label)
        ReadRecordTrendChartType.LINE -> context.getString(R.string.read_record_trend_line_label)
    }
}
