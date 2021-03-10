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

import java.time.Duration

sealed class CounterState(val isActive: Boolean) {

    abstract val hours: Int
    abstract val minutes: Int
    abstract val seconds: Int

    val isReady get() = hours + minutes + seconds > 0

    data class Initial(
        override val hours: Int = 0,
        override val minutes: Int = 0,
        override val seconds: Int = 0,
    ) : CounterState(false) {

        val total: Duration get() = Duration.parse("PT${hours}H${minutes}M${seconds}S")
    }

    data class Running(
        val total: Duration,
        val remain: Duration,
    ) : CounterState(true) {
        val past: Duration get() = total - remain

        override val hours: Int get() = remain.toHours().toInt()
        override val minutes: Int get() = remain.toMinutes().rem(60).toInt()
        override val seconds: Int get() = remain.seconds.rem(60).toInt()
    }

    data class Paused(
        val total: Duration,
        val remain: Duration,
    ) : CounterState(false) {

        override val hours: Int get() = remain.toHours().toInt()
        override val minutes: Int get() = remain.toMinutes().rem(60).toInt()
        override val seconds: Int get() = remain.seconds.rem(60).toInt()
    }
}
