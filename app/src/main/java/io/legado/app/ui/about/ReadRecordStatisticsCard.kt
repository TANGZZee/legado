package io.legado.app.ui.about

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import androidx.annotation.DrawableRes
import io.legado.app.R
import io.legado.app.lib.theme.UiCorner
import io.legado.app.lib.theme.primaryTextColor
import io.legado.app.lib.theme.rememberThemeUiPalette
import io.legado.app.lib.theme.secondaryTextColor
import io.legado.app.lib.theme.titleTypeface
import io.legado.app.lib.theme.uiTypeface
import io.legado.app.ui.widget.compose.appSettingPanelBackground

@Immutable
data class ReadRecordStatisticsMetricUi(
    @DrawableRes val iconRes: Int,
    val value: String,
    val label: String,
    val trend: String,
    val trendColor: Int,
    val onClick: (() -> Unit)? = null
)

@Immutable
data class ReadRecordStatisticsUi(
    val period: ReadRecordStatsPeriod,
    val tabs: List<String>,
    val title: String,
    val canGoNext: Boolean,
    val metrics: List<ReadRecordStatisticsMetricUi>
)

@Composable
fun ReadRecordStatisticsCard(
    ui: ReadRecordStatisticsUi,
    onPeriodSelected: (ReadRecordStatsPeriod) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val palette = rememberThemeUiPalette()
    val primaryText = Color(context.primaryTextColor)
    val secondaryText = Color(context.secondaryTextColor)
    val titleFont = FontFamily(context.titleTypeface())
    val bodyFont = FontFamily(context.uiTypeface())
    val panelRadius = UiCorner.panelRadius(context)
    val panelImage = remember(context, panelRadius, palette.signature) {
        UiCorner.panelImageDrawable(context, panelRadius)
    }
    val surface = UiCorner.surfaceColor(palette.cardColor)
    val selectedSurface = Color(UiCorner.surfaceColor(palette.shelfColor))
    val border = UiCorner.panelBorderColor(context)

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
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = secondaryText.copy(alpha = 0.10f),
                    shape = RoundedCornerShape(22.dp)
                )
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            val periodValues = ReadRecordStatsPeriod.entries
            periodValues.forEachIndexed { index, period ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .background(
                            color = if (ui.period == period) selectedSurface else Color.Transparent,
                            shape = RoundedCornerShape(18.dp)
                        )
                        .clickable { onPeriodSelected(period) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = ui.tabs.getOrElse(index) { period.name },
                        color = if (ui.period == period) primaryText else secondaryText,
                        fontSize = 16.sp,
                        fontWeight = if (ui.period == period) FontWeight.Bold else FontWeight.Normal,
                        fontFamily = titleFont,
                        maxLines = 1
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ReadRecordStatisticsArrow(
                iconRes = R.drawable.ic_arrow_back,
                enabled = true,
                onClick = onPrevious,
                primaryText = primaryText,
                secondaryText = secondaryText,
                modifier = Modifier.size(58.dp)
            )
            Text(
                text = ui.title,
                color = primaryText,
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = titleFont,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            ReadRecordStatisticsArrow(
                iconRes = R.drawable.ic_arrow_right,
                enabled = ui.canGoNext,
                onClick = onNext,
                primaryText = primaryText,
                secondaryText = secondaryText,
                modifier = Modifier.size(58.dp)
            )
        }

        Column(
            modifier = Modifier.padding(top = 18.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            ui.metrics.chunked(2).forEach { rowMetrics ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    rowMetrics.forEach { metric ->
                        ReadRecordStatisticsMetric(
                            metric = metric,
                            primaryText = primaryText,
                            secondaryText = secondaryText,
                            titleFont = titleFont,
                            bodyFont = bodyFont,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowMetrics.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ReadRecordStatisticsArrow(
    @DrawableRes iconRes: Int,
    enabled: Boolean,
    onClick: () -> Unit,
    primaryText: Color,
    secondaryText: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 1.dp,
                color = if (enabled) secondaryText.copy(alpha = 0.35f) else secondaryText.copy(alpha = 0.12f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = null,
            colorFilter = ColorFilter.tint(if (enabled) primaryText else secondaryText.copy(alpha = 0.35f)),
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
private fun ReadRecordStatisticsMetric(
    metric: ReadRecordStatisticsMetricUi,
    primaryText: Color,
    secondaryText: Color,
    titleFont: FontFamily,
    bodyFont: FontFamily,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .then(
                if (metric.onClick != null) {
                    Modifier.clickable { metric.onClick?.invoke() }
                } else {
                    Modifier
                }
            ),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = metric.value,
            color = primaryText,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = titleFont,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(metric.iconRes),
                contentDescription = null,
                colorFilter = ColorFilter.tint(secondaryText),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = metric.label,
                color = secondaryText,
                fontSize = 14.sp,
                fontFamily = bodyFont,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (metric.onClick != null) {
                Spacer(modifier = Modifier.width(4.dp))
                Image(
                    painter = painterResource(R.drawable.ic_swap_horiz),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(secondaryText.copy(alpha = 0.7f)),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
        Text(
            text = metric.trend,
            color = Color(metric.trendColor),
            fontSize = 13.sp,
            fontFamily = bodyFont,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
