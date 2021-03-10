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

fun Duration.text(): String {
    val hours = "%02d:".format(toHours())
    val minutes = "%02d".format(toMinutes() % 60)
    val seconds = "%02d".format(seconds % 60)
    return "${hours}$minutes:$seconds"
}

class CounterViewModel(
    application: Application
) : AndroidViewModel(application) {

    private var counterJob: Job? = null

    private val _counterState = MutableStateFlow<CounterState>(CounterState.Initial())
    val counterState: StateFlow<CounterState> get() = _counterState

    fun updateTime(field: TimerField, value: Int) {
        val state = counterState.value
        check(state is CounterState.Initial)
        val internalValue = value.coerceIn(0..99)
        _counterState.value = when (field) {
            TimerField.HOUR -> state.copy(hours = internalValue)
            TimerField.MINUTE -> state.copy(minutes = internalValue)
            TimerField.SECOND -> state.copy(seconds = internalValue)
        }
    }

    fun start() {
        val state = counterState.value
        require(state is CounterState.Initial)
        counterJob = viewModelScope.countdown(state.total)
    }

    fun pause() {
        val state = counterState.value
        if (state !is CounterState.Running) {
            return
        }
        viewModelScope.launch {
            counterJob?.cancelAndJoin()
            counterJob = null
            _counterState.value = CounterState.Paused(state.total, state.remain)
        }
    }

    fun resume() {
        val state = counterState.value
        require(state is CounterState.Paused)
        counterJob = viewModelScope.countdown(state.total, state.remain)
    }

    private fun CoroutineScope.countdown(
        totalTime: Duration,
        remainTime: Duration? = null,
    ): Job {
        return launch(Dispatchers.Default) {
            var remain = remainTime ?: totalTime
            _counterState.value = CounterState.Running(totalTime, remain)
            while (isActive) {
                delay(1000)
                remain -= Duration.ofSeconds(1)
                if (remain <= Duration.ZERO) {
                    _counterState.value = CounterState.Initial()
                    vibrate()
                    break
                }
                _counterState.value = CounterState.Running(totalTime, remain)
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
