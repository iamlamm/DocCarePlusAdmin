package com.healthtech.doccareplusadmin.ui.report.chart


import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart

fun LineChart.applyConfig(config: ChartConfig) {
    description.isEnabled = config.shouldShowDescription
    setTouchEnabled(config.shouldEnableTouch)
    setDragEnabled(config.shouldEnableDrag)
    setScaleEnabled(config.shouldEnableScale)
    setPinchZoom(config.shouldEnablePinchZoom)
    setDrawGridBackground(config.shouldShowGrid)
    axisRight?.isEnabled = config.shouldShowRightAxis
    xAxis?.position = config.xAxisPosition
}

fun BarChart.applyConfig(config: ChartConfig) {
    description.isEnabled = config.shouldShowDescription
    setTouchEnabled(config.shouldEnableTouch)
    setDragEnabled(config.shouldEnableDrag)
    setScaleEnabled(config.shouldEnableScale)
    setPinchZoom(config.shouldEnablePinchZoom)
    setDrawGridBackground(config.shouldShowGrid)
    axisRight?.isEnabled = config.shouldShowRightAxis
    xAxis?.position = config.xAxisPosition
}

fun PieChart.applyConfig(config: ChartConfig) {
    description.isEnabled = config.shouldShowDescription
    setTouchEnabled(config.shouldEnableTouch)
    setDragDecelerationEnabled(config.shouldEnableDrag)
    // Thêm cài đặt cho độ ma sát nếu muốn
    if (config.shouldEnableDrag) {
        setDragDecelerationFrictionCoef(0.9f)
    }
    setDrawHoleEnabled(true)
    setHoleColor(android.graphics.Color.WHITE)
    // Các cài đặt đặc biệt cho PieChart
}