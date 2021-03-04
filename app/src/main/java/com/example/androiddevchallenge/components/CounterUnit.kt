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
package com.example.androiddevchallenge.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun Counters(
    isActive: Boolean,
    remainTimeText: String,
    pastTimeText: String,
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        CounterRemain(isActive, remainTimeText)
        Spacer(Modifier.height(20.dp))
        CounterPast(isActive, pastTimeText)
    }
}

@Composable
fun CounterRemain(
    isActive: Boolean,
    timeText: String,
) {
    Box(
        modifier = Modifier
            .border(width = 3.dp, color = Color.Black, shape = CircleShape)
            .width(200.dp)
            .aspectRatio(1f)
    ) {
        if (isActive) {
            BounceRing(
                shrink = false,
                modifier = Modifier.fillMaxSize()
            )
        }
        Text(
            text = timeText,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

@Composable
fun CounterPast(
    isActive: Boolean,
    timeText: String,
) {
    Box(
        modifier = Modifier
            .border(width = 3.dp, color = Color.Black, shape = CircleShape)
            .width(200.dp)
            .aspectRatio(1f)
    ) {
        if (isActive) {
            BounceRing(
                shrink = true,
                modifier = Modifier.fillMaxSize()
            )
        }
        Text(
            text = timeText,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

@Composable
fun BounceRing(shrink: Boolean, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = if (shrink) 1f else 0f,
        targetValue = if (shrink) 0f else 1f,
        animationSpec = infiniteRepeatable(
            animation = TweenSpec(
                durationMillis = 800,
                delay = 200,
            ),
            repeatMode = RepeatMode.Restart,
        ),
    )
    CircularProgressIndicator(
        progress = 1f,
        color = Color.Blue,
        strokeWidth = ((1 - scale) * 36f).dp,
        modifier = Modifier
            .then(modifier)
            .scale(scale)
            .alpha(if (shrink) 1 - scale else scale)
    )
}
