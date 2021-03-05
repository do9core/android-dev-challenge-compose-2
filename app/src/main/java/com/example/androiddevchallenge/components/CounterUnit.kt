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

import androidx.compose.animation.Crossfade
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.androiddevchallenge.CounterState
import com.example.androiddevchallenge.text
import java.time.Duration
import kotlin.math.sqrt

@Composable
fun Counters(
    state: CounterState,
    onBeginCountdown: () -> Unit,
    onTimeSet: (Duration) -> Unit,
    onReset: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
) {
    val remainPart = remember { 0.404f }
    val pastPart = remember(remainPart) { remainPart * sqrt(5f).dec().div(2) }
    val spacerAll = remember(remainPart, pastPart) { 1f - remainPart - pastPart }
    val sideSpacer = remember(spacerAll) { spacerAll * 3f.minus(sqrt(5f)).div(2) }
    val centerSpacer = remember(spacerAll) { spacerAll * sqrt(5f).minus(2) }
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Spacer(modifier = Modifier.weight(sideSpacer))
        CounterRemain(
            state = state,
            modifier = Modifier
                .weight(remainPart)
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = 16.dp),
        )
        Spacer(modifier = Modifier.weight(centerSpacer))
        CounterPast(
            state = state,
            onBeginCountdown = onBeginCountdown,
            onTimeSet = onTimeSet,
            onReset = onReset,
            onPause = onPause,
            onResume = onResume,
            modifier = Modifier
                .weight(pastPart)
                .align(Alignment.End)
                .padding(end = 64.dp),
        )
        Spacer(modifier = Modifier.weight(sideSpacer))
    }
}

@Composable
fun CounterRemain(
    state: CounterState,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = Modifier
            .then(modifier)
            .border(width = 3.dp, color = Color.Black, shape = CircleShape)
            .aspectRatio(1f)
    ) {
        if (state.isActive) {
            BounceRing(
                shrink = false,
                modifier = Modifier.fillMaxSize()
            )
        }
        Crossfade(
            targetState = state,
            modifier = Modifier.align(Alignment.Center),
        ) { counterState ->
            when (counterState) {
                CounterState.Initial -> {
                    Text(
                        text = "Click Start!",
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
                is CounterState.TimeSet -> {
                    Text(
                        text = "Time set!",
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
                is CounterState.Beginning -> {
                    Text(
                        text = counterState.message,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
                is CounterState.Running -> {
                    Text(
                        text = counterState.remain.text(),
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
                is CounterState.Paused -> {
                    val animator = rememberInfiniteTransition()
                    val alpha by animator.animateFloat(
                        initialValue = 1f,
                        targetValue = 0f,
                        animationSpec = infiniteRepeatable(
                            animation = TweenSpec(
                                durationMillis = 1000,
                            ),
                            repeatMode = RepeatMode.Reverse,
                        )
                    )
                    Text(
                        text = counterState.remain.text(),
                        modifier = Modifier
                            .align(Alignment.Center)
                            .alpha(alpha),
                    )
                }
                CounterState.Finish -> {
                    Text(
                        text = "Well done!",
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
            }
        }
    }
}

@Composable
fun CounterPast(
    state: CounterState,
    onTimeSet: (Duration) -> Unit,
    onBeginCountdown: () -> Unit,
    onReset: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = Modifier
            .then(modifier)
            .border(width = 3.dp, color = Color.Black, shape = CircleShape)
            .aspectRatio(1f)
    ) {
        if (state.isActive) {
            BounceRing(
                shrink = true,
                modifier = Modifier.fillMaxSize()
            )
        }
        Crossfade(
            targetState = state,
            modifier = Modifier.align(Alignment.Center)
        ) { counterState ->
            when (counterState) {
                CounterState.Initial -> {
                    Button(
                        onClick = { onTimeSet(Duration.ofSeconds(10)) },
                        modifier = Modifier,
                    ) {
                        Text("Set Time!")
                    }
                }
                is CounterState.TimeSet -> {
                    Button(
                        onClick = { onBeginCountdown() },
                        modifier = Modifier,
                    ) {
                        Text("Start!")
                    }
                }
                is CounterState.Beginning -> {
                    Text(
                        text = counterState.message,
                        modifier = Modifier,
                    )
                }
                is CounterState.Running -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                    ) {
                        Text(
                            text = counterState.past.text(),
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                        )
                        Button(
                            onClick = { onPause() },
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                        ) {
                            Text(
                                text = "Pause",
                                modifier = Modifier,
                            )
                        }
                    }
                }
                is CounterState.Paused -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        Button(
                            onClick = { onReset() },
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                        ) {
                            Text(
                                text = "Stop",
                                modifier = Modifier,
                            )
                        }
                        Button(
                            onClick = { onResume() },
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                        ) {
                            Text(
                                text = "Resume",
                                modifier = Modifier,
                            )
                        }
                    }
                }
                CounterState.Finish -> {
                    Button(
                        onClick = { onReset() },
                        modifier = Modifier,
                    ) {
                        Text("Reset!")
                    }
                }
            }
        }
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
                durationMillis = 900,
                delay = 100,
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
