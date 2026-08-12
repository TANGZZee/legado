package io.legado.app.ui.about

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.legado.app.R
import io.legado.app.lib.theme.UiCorner
import io.legado.app.lib.theme.primaryTextColor
import io.legado.app.lib.theme.rememberThemeUiPalette
import io.legado.app.lib.theme.secondaryTextColor
import io.legado.app.lib.theme.titleTypeface
import io.legado.app.lib.theme.uiTypeface
import io.legado.app.ui.widget.compose.appSettingPanelBackground
import kotlin.math.roundToInt

@Immutable
data class ReadRecordTimeOfDayUi(
    val title: String,
    val hourly: List<ReadRecordHourlyValue>,
    val peakHour: String,
    val peakBucket: String,
    val averageStart: String,
    val nightRatio: String,
    val bucketValues: List<ReadRecordTimeBucketUi>
)

@Immutable
data class ReadRecordTimeBucketUi(
    val label: String,
    val duration: String,
    val millis: Long,
    val color: Long
)

@Composable
fun ReadRecordTimeOfDayCard(
    ui: ReadRecordTimeOfDayUi,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val palette = rememberThemeUiPalette()
    val primary = Color(context.primaryTextColor)
    val secondary = Color(context.secondaryTextColor)
    val titleFont = FontFamily(context.titleTypeface())
    val bodyFont = FontFamily(context.uiTypeface())
    val radius = UiCorner.panelRadius(context)
    val panelImage = remember(context, radius, palette.signature) {
        UiCorner.panelImageDrawable(context, radius)
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .appSettingPanelBackground(
                normalColor = UiCorner.surfaceColor(palette.cardColor),
                panelImage = panelImage,
                borderColor = UiCorner.panelBorderColor(context),
                radiusPx = radius
            )
            .padding(horizontal = 18.dp, vertical = 17.dp)
    ) {
        Text(ui.title, color = primary, fontSize = 20.sp, fontWeight = FontWeight.Bold, fontFamily = titleFont)
        Spacer(Modifier.height(14.dp))
        ReadRecordHourlyChart(ui.hourly, primary, secondary, bodyFont)
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            ReadRecordTimeMetric("最常阅读", ui.peakHour, primary, secondary, titleFont, bodyFont, Modifier.weight(1f))
            ReadRecordTimeMetric("最常时段", ui.peakBucket, primary, secondary, titleFont, bodyFont, Modifier.weight(1f))
            ReadRecordTimeMetric("平均开始", ui.averageStart, primary, secondary, titleFont, bodyFont, Modifier.weight(1f))
            ReadRecordTimeMetric("夜间占比", ui.nightRatio, primary, secondary, titleFont, bodyFont, Modifier.weight(1f))
        }
    }
}

@Composable
fun ReadRecordTimeBucketCard(
    title: String,
    buckets: List<ReadRecordTimeBucketUi>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val palette = rememberThemeUiPalette()
    val primary = Color(context.primaryTextColor)
    val secondary = Color(context.secondaryTextColor)
    val titleFont = FontFamily(context.titleTypeface())
    val bodyFont = FontFamily(context.uiTypeface())
    val radius = UiCorner.panelRadius(context)
    val panelImage = remember(context, radius, palette.signature) {
        UiCorner.panelImageDrawable(context, radius)
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .appSettingPanelBackground(
                normalColor = UiCorner.surfaceColor(palette.cardColor),
                panelImage = panelImage,
                borderColor = UiCorner.panelBorderColor(context),
                radiusPx = radius
            )
            .padding(horizontal = 18.dp, vertical = 17.dp)
    ) {
        Text(title, color = primary, fontSize = 20.sp, fontWeight = FontWeight.Bold, fontFamily = titleFont)
        Spacer(Modifier.height(15.dp))
        Canvas(Modifier.fillMaxWidth().height(20.dp)) {
            val totalMillis = buckets.sumOf { it.millis }
            val radius = 10.dp.toPx()
            if (totalMillis <= 0L) {
                drawRoundRect(
                    color = Color(buckets.firstOrNull()?.color ?: 0xFFE3E5E8L),
                    topLeft = Offset.Zero,
                    size = size,
                    cornerRadius = CornerRadius(radius, radius)
                )
            } else {
                val total = totalMillis.toFloat()
                var left = 0f
                val roundedClip = Path().apply {
                    addRoundRect(
                        androidx.compose.ui.geometry.RoundRect(
                            left = 0f,
                            top = 0f,
                            right = size.width,
                            bottom = size.height,
                            radiusX = radius,
                            radiusY = radius
                        )
                    )
                }
                clipPath(roundedClip) {
                    buckets.forEach { bucket ->
                        val width = size.width * bucket.millis / total
                        if (width > 0f) {
                            drawRect(
                                color = Color(bucket.color),
                                topLeft = Offset(left, 0f),
                                size = androidx.compose.ui.geometry.Size(width, size.height)
                            )
                        }
                        left += width
                    }
                }
            }
        }
        Spacer(Modifier.height(13.dp))
        Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                buckets.take(3).forEach { bucket ->
                    ReadRecordBucketLegend(
                        bucket = bucket,
                        secondary = secondary,
                        bodyFont = bodyFont,
                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                    )
                }
            }
            buckets.getOrNull(3)?.let { bucket ->
                ReadRecordBucketLegend(bucket, secondary, bodyFont, Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun ReadRecordBucketLegend(
    bucket: ReadRecordTimeBucketUi,
    secondary: Color,
    bodyFont: FontFamily,
    modifier: Modifier
) {
    Row(modifier, verticalAlignment = Alignment.Top) {
        Box(Modifier.size(11.dp).background(Color(bucket.color), RoundedCornerShape(2.dp)))
        Spacer(Modifier.size(7.dp))
        Text(
            text = "${bucket.label} ${bucket.duration}",
            color = secondary,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            fontFamily = bodyFont
        )
    }
}

@Composable
private fun ReadRecordHourlyChart(
    values: List<ReadRecordHourlyValue>,
    primary: Color,
    secondary: Color,
    bodyFont: FontFamily
) {
    Column {
        Canvas(Modifier.fillMaxWidth().height(128.dp)) {
            val max = values.maxOfOrNull { it.readTime }?.coerceAtLeast(1L) ?: 1L
            val slot = size.width / 24f
            val barWidth = (slot * 0.42f).coerceAtLeast(2f)
            values.forEach { value ->
                val height = size.height * value.readTime / max.toFloat()
                // Each bar represents one hourly interval: [hour, hour + 1).
                // The axis labels are interval boundaries, so 0 and 24 are
                // the left and right edges of the same 24-hour day.
                val centerX = (value.hour + 0.5f) * slot
                val x = centerX - barWidth / 2f
                drawRoundRect(
                    color = if (value.readTime == max && value.readTime > 0L) primary else secondary.copy(alpha = 0.30f),
                    topLeft = Offset(x, size.height - height),
                    size = androidx.compose.ui.geometry.Size(barWidth, height.coerceAtLeast(2f)),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth / 2f, barWidth / 2f)
                )
            }
            drawLine(
                color = secondary.copy(alpha = 0.18f),
                start = Offset(0f, size.height - 1.dp.toPx()),
                end = Offset(size.width, size.height - 1.dp.toPx()),
                strokeWidth = 1.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
        ReadRecordHourAxis(secondary = secondary, bodyFont = bodyFont)
    }
}

@Composable
private fun ReadRecordHourAxis(
    secondary: Color,
    bodyFont: FontFamily
) {
    val labels = listOf("0", "4", "8", "12", "16", "20", "24")
    Layout(
        content = {
            labels.forEach { label ->
                Text(
                    text = label,
                    color = secondary,
                    fontSize = 11.sp,
                    fontFamily = bodyFont,
                    maxLines = 1
                )
            }
        },
        modifier = Modifier.fillMaxWidth()
    ) { measurables, constraints ->
        val placeables = measurables.map { it.measure(Constraints()) }
        val width = constraints.maxWidth
        val height = placeables.maxOfOrNull { it.height } ?: 0
        layout(width, height) {
            placeables.forEachIndexed { index, placeable ->
                val hour = index * 4
                val coordinate = width * hour / 24f
                val x = when (hour) {
                    0 -> 0
                    24 -> width - placeable.width
                    else -> (coordinate - placeable.width / 2f)
                        .roundToInt()
                        .coerceIn(0, width - placeable.width)
                }
                placeable.placeRelative(x, 0)
            }
        }
    }
}

@Composable
private fun ReadRecordTimeMetric(
    label: String,
    value: String,
    primary: Color,
    secondary: Color,
    titleFont: FontFamily,
    bodyFont: FontFamily,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxWidth()) {
        Text(label, color = secondary, fontSize = 11.sp, fontFamily = bodyFont)
        Spacer(Modifier.height(4.dp))
        Text(value, color = primary, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = titleFont)
    }
}
