package com.healthtech.doccareplusadmin.ui.report.chart

import android.annotation.SuppressLint
import android.content.Context
import androidx.core.content.ContextCompat
import com.healthtech.doccareplusadmin.R
import timber.log.Timber

object ChartUtils {
    fun getChartColors(context: Context): List<Int> = listOf(
        ContextCompat.getColor(context, R.color.chart_color_1),
        ContextCompat.getColor(context, R.color.chart_color_2),
        ContextCompat.getColor(context, R.color.chart_color_3),
        ContextCompat.getColor(context, R.color.chart_color_4),
        ContextCompat.getColor(context, R.color.chart_color_5)
    )

    @SuppressLint("DefaultLocale")
    fun formatValue(value: Float): String {
        return when {
            value >= 1_000_000 -> String.format("%.1fM", value / 1_000_000)
            value >= 1_000 -> String.format("%.1fK", value / 1_000)
            else -> value.toInt().toString()
        }
    }

    @SuppressLint("DefaultLocale")
    fun formatCurrency(value: Float): String {
        return try {
            String.format("$%.2f", value)
        } catch (e: Exception) {
            Timber.e(e, "Error formatting currency")
            "$0.00"
        }
    }

    fun formatRevenueForChart(value: Double): Float {
        return try {
            String.format("%.2f", value).toFloat()
        } catch (e: Exception) {
            Timber.e(e, "Error formatting revenue for chart")
            0f
        }
    }
}