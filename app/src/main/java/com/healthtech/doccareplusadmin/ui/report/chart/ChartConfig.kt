package com.healthtech.doccareplusadmin.ui.report.chart

import com.github.mikephil.charting.components.XAxis

data class ChartConfig(
    val shouldShowDescription: Boolean = false,
    val shouldEnableTouch: Boolean = true,
    val shouldEnableDrag: Boolean = true,
    val shouldEnableScale: Boolean = true,
    val shouldEnablePinchZoom: Boolean = true,
    val shouldShowGrid: Boolean = false,
    val shouldShowRightAxis: Boolean = false,
    val xAxisPosition: XAxis.XAxisPosition = XAxis.XAxisPosition.BOTTOM
)