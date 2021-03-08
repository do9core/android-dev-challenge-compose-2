/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.androiddevchallenge

import android.app.Application
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.content.getSystemService
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.Duration

fun Duration.fullText(): String {
    val hours = "%02d".format(toHours())
    val minutes = "%02d".format(toMinutes() % 60)
    val seconds = "%02d".format(seconds % 60)
    return "$hours:$minutes:$seconds"
}

fun Duration.text(): String {
    val hours = toHours().takeIf { it > 0 }?.let { "%02d:".format(it) }.orEmpty()
    val minutes = "%02d".format(toMinutes() % 60)
    val seconds = "%02d".format(seconds % 60)
    return "${hours}$minutes:$seconds"
}

class CounterViewModel(
    application: Application
) : AndroidViewModel(application) {

    private var counterJob: Job? = null

    private val _counterState = MutableStateFlow<CounterState>(CounterState.Initial(Duration.ZERO))
    val counterState: StateFlow<CounterState> get() = _counterState

    fun reset() {
        _counterState.value = CounterState.Initial(Duration.ZERO)
    }

    fun updateTime(text: String) {
        val (hours, minutes, seconds) = text.split(":")
        var formatText = "PT"
        if (hours.isNotBlank()) {
            formatText += "${hours}H"
        }
        if (minutes.isNotBlank()) {
            formatText += "${minutes}M"
        }
        if (seconds.isNotBlank()) {
            formatText += "${seconds}S"
        }
        _counterState.value = CounterState.Initial(Duration.parse(formatText))
    }

    fun setTime() {
        val state = _counterState.value
        require(state is CounterState.Initial)
        _counterState.value = CounterState.TimeSet(state.total)
    }

    fun start() {
        val state = counterState.value
        require(state is CounterState.TimeSet)
        counterJob = viewModelScope.countdown(state.total)
    }

    fun pause() {
        val state = counterState.value
        require(state is CounterState.Running)
        viewModelScope.launch {
            counterJob?.cancelAndJoin()
            counterJob = null
            _counterState.value = CounterState.Paused(
                total = state.total,
                remain = state.remain,
            )
        }
    }

    fun resume() {
        val state = counterState.value
        require(state is CounterState.Paused)
        counterJob = viewModelScope.countdown(state.total, state.remain, showBeginning = false)
    }

    private fun CoroutineScope.countdown(
        totalTime: Duration,
        remainTime: Duration? = null,
        showBeginning: Boolean = true,
    ): Job {
        return launch(Dispatchers.Default) {
            var remain = remainTime ?: totalTime

            if (showBeginning) {
                _counterState.value = CounterState.Beginning("Ready!")
                delay(1000)
                _counterState.value = CounterState.Beginning("3!")
                delay(1000)
                _counterState.value = CounterState.Beginning("2!")
                delay(1000)
                _counterState.value = CounterState.Beginning("1!")
                delay(1000)
                _counterState.value = CounterState.Beginning("Go!")
                delay(1000)
            }

            _counterState.value = CounterState.Running(
                total = totalTime,
                remain = remain,
            )
            while (isActive) {
                delay(1000)
                remain -= Duration.ofSeconds(1)
                if (remain <= Duration.ZERO) {
                    _counterState.value = CounterState.Finish
                    vibrate()
                    break
                }
                _counterState.value = CounterState.Running(
                    total = totalTime,
                    remain = remain
                )
            }
        }
    }

    private fun vibrate() {
        viewModelScope.launch {
            val context = getApplication<Application>()
            val vibrator = context.getSystemService<Vibrator>() ?: return@launch
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                @Suppress("DEPRECATION") // Low sdk version.
                vibrator.vibrate(500)
            } else {
                val effect = VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
                vibrator.vibrate(effect)
            }
        }
    }
}
