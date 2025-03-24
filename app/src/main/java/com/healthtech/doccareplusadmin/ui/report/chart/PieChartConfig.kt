package com.healthtech.doccareplusadmin.ui.report.chart

import android.graphics.Color
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend

data class PieChartConfig(
    val holeEnabled: Boolean = true,
    val holeColor: Int = Color.WHITE,
    val transparentCircleColor: Int = Color.WHITE,
    val transparentCircleAlpha: Int = 110,
    val holeRadius: Float = 58f,
    val transparentCircleRadius: Float = 61f,
    val centerText: Boolean = true,
    val rotationEnabled: Boolean = true,
    val highlightPerTapEnabled: Boolean = true,
    val entryLabels: Boolean = false,
    val legend: Boolean = true
) {
    fun applyTo(chart: PieChart) {
        chart.apply {
            isDrawHoleEnabled = holeEnabled
            setHoleColor(holeColor)
            setTransparentCircleColor(transparentCircleColor)
            setTransparentCircleAlpha(transparentCircleAlpha)
            this.holeRadius = this@PieChartConfig.holeRadius
            this.transparentCircleRadius = this@PieChartConfig.transparentCircleRadius
            setDrawCenterText(true)
            isRotationEnabled = rotationEnabled
            isHighlightPerTapEnabled = highlightPerTapEnabled
            setDrawEntryLabels(entryLabels)
            legend.isEnabled = this@PieChartConfig.legend
            legend.orientation = Legend.LegendOrientation.VERTICAL
            legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
            legend.verticalAlignment = Legend.LegendVerticalAlignment.CENTER
        }
    }
}