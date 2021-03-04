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

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.androiddevchallenge.components.Counters
import com.example.androiddevchallenge.ui.theme.MyTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.time.Duration

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyTheme {
                var active by remember { mutableStateOf(true) }
                var time by remember { mutableStateOf(Duration.ofMinutes(1)) }
                var past by remember { mutableStateOf(Duration.ZERO) }
                Counters(
                    isActive = active,
                    remainTimeText = time.text(),
                    pastTimeText = past.text(),
                )
                LaunchedEffect(Unit) {
                    while (isActive && active) {
                        delay(1000)
                        time = time.minusSeconds(1)
                        past = past.plusSeconds(1)
                        if (time.isZero) {
                            active = false
                        }
                    }
                }
            }
        }
    }

    private fun Duration.text(): String {
        return "${toMinutes()}:${seconds % 60}"
    }
}
