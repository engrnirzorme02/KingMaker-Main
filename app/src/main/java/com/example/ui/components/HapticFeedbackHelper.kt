package com.example.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

object HapticFeedbackHelper {

    /**
     * Heavy Impact: Used for Screen 5 CEO Approval Slider
     */
    fun triggerHeavyImpact(context: Context) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(100)
        }
    }

    /**
     * Light Impact: Used for FAB, Branch Forking, and primary taps
     */
    fun triggerLightImpact(context: Context) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(25, 120))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(25)
        }
    }

    fun triggerSelectionClick(context: Context) {
        triggerLightImpact(context)
    }

    fun triggerMediumImpact(context: Context) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(50, 180))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(50)
        }
    }

    /**
     * Warning Notification Pattern: Double-pulse vibration for Deadlock / Smart Query
     */
    fun triggerWarningPattern(context: Context) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Pattern: 0ms wait, 40ms buzz, 60ms pause, 90ms warning buzz
            val timings = longArrayOf(0, 40, 60, 90)
            val amplitudes = intArrayOf(0, 180, 0, 255)
            vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(longArrayOf(0, 40, 60, 90), -1)
        }
    }
}
