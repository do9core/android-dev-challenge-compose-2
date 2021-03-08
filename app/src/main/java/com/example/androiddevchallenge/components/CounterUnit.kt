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

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RadialGradientShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.androiddevchallenge.CounterState
import com.example.androiddevchallenge.fullText
import com.example.androiddevchallenge.text
import com.example.androiddevchallenge.ui.theme.pastCounterText
import com.example.androiddevchallenge.ui.theme.pastText
import com.example.androiddevchallenge.ui.theme.remainCounterText
import com.example.androiddevchallenge.ui.theme.remainInputText
import com.example.androiddevchallenge.ui.theme.remainText
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.sqrt
import kotlin.random.Random

@Composable
private fun animateDpRandomly(
    size: Dp = 24.dp,
    duration: Int = 2000,
    easing: Easing = LinearEasing,
): State<Dp> {
    val targetValue by produceState(initialValue = 0.dp) {
        while (isActive) {
            value = size * Random.nextFloat()
            delay(duration.toLong())
        }
    }
    return animateDpAsState(
        targetValue = targetValue,
        animationSpec = TweenSpec(
            durationMillis = duration,
            easing = easing,
        )
    )
}

@Composable
fun Counters(
    state: CounterState,
    onBeginCountdown: () -> Unit,
    onTimeUpdate: (String) -> Unit,
    onTimeSet: () -> Unit,
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
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = ShaderBrush(
                    shader = RadialGradientShader(
                        center = Offset(0f, 200f),
                        radius = 3200f,
                        colors = listOf(
                            Color(0f, 0.15f, 0.85f),
                            Color(0.25f, 0.005f, 0.55f),
                            Color.Red,
                        ),
                        tileMode = TileMode.Repeated,
                    )
                )
            )
    ) {
        val remainOffsetX by animateDpRandomly()
        val remainOffsetY by animateDpRandomly()
        Spacer(modifier = Modifier.weight(sideSpacer))
        CounterRemain(
            state = state,
            onTimeUpdate = onTimeUpdate,
            modifier = Modifier
                .weight(remainPart)
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = 16.dp)
                .offset(remainOffsetX, remainOffsetY),
        )
        Spacer(modifier = Modifier.weight(centerSpacer))
        val pastOffsetX by animateDpRandomly()
        val pastOffsetY by animateDpRandomly()
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
                .padding(end = 64.dp)
                .offset(pastOffsetX, pastOffsetY),
        )
        Spacer(modifier = Modifier.weight(sideSpacer))
    }
}

@Composable
fun CounterRemain(
    state: CounterState,
    onTimeUpdate: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = Modifier
            .then(modifier)
            .border(
                width = 2.dp,
                brush = ShaderBrush(
                    shader = RadialGradientShader(
                        colors = listOf(Color.White, Color.Transparent),
                        center = Offset.Zero,
                        radius = 1100f,
                    )
                ),
                shape = CircleShape
            )
            .aspectRatio(1f)
    ) {
        if (state.isActive) {
            BounceRing(
                shrink = false,
                modifier = Modifier.fillMaxSize()
            )
        }
        Box(modifier = Modifier.align(Alignment.Center)) {
            when (state) {
                is CounterState.Initial -> {
                    val (hourText, minuteText, secondText) = remember(state) {
                        state.total.fullText().split(":")
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp)
                    ) {
                        InputField(
                            label = "H",
                            text = hourText,
                            onTextChanged = { onTimeUpdate("$it:$minuteText:$secondText") },
                        )
                        InputField(
                            label = "M",
                            text = minuteText,
                            onTextChanged = { onTimeUpdate("$hourText:$it:$secondText") },
                        )
                        InputField(
                            label = "S",
                            text = secondText,
                            imeAction = ImeAction.Done,
                            onTextChanged = { onTimeUpdate("$hourText:$minuteText:$it") },
                        )
                    }
                }
                is CounterState.TimeSet -> {
                    Text(
                        text = "Time set!",
                        style = remainText,
                        modifier = Modifier,
                    )
                }
                is CounterState.Beginning -> {
                    Text(
                        text = state.message,
                        style = remainText,
                        modifier = Modifier,
                    )
                }
                is CounterState.Running -> {
                    Text(
                        text = state.remain.text(),
                        style = remainCounterText,
                        modifier = Modifier,
                    )
                }
                is CounterState.Paused -> {
                    val animator = rememberInfiniteTransition()
                    val alpha by animator.animateFloat(
                        initialValue = 1f,
                        targetValue = 0.1f,
                        animationSpec = infiniteRepeatable(
                            animation = TweenSpec(
                                durationMillis = 500,
                            ),
                            repeatMode = RepeatMode.Reverse,
                        )
                    )
                    Text(
                        text = state.remain.text(),
                        style = remainCounterText,
                        modifier = Modifier.alpha(alpha),
                    )
                }
                CounterState.Finish -> {
                    Text(
                        text = "Well done!",
                        style = remainText,
                        modifier = Modifier,
                    )
                }
            }
        }
    }
}

@Composable
fun CounterPast(
    state: CounterState,
    onTimeSet: () -> Unit,
    onBeginCountdown: () -> Unit,
    onReset: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = Modifier
            .then(modifier)
            .border(
                width = 2.dp,
                shape = CircleShape,
                brush = ShaderBrush(
                    shader = RadialGradientShader(
                        colors = listOf(Color.White, Color.Transparent),
                        center = Offset(600f, 800f),
                        radius = 950f,
                    )
                ),
            )
            .aspectRatio(1f)
            .clip(CircleShape)
            .clickable {
                when (state) {
                    is CounterState.Initial -> onTimeSet()
                    is CounterState.TimeSet -> onBeginCountdown()
                    is CounterState.Running -> onPause()
                    is CounterState.Paused -> onResume()
                    is CounterState.Finish -> onReset()
                    else -> Unit
                }
            }
    ) {
        if (state.isActive) {
            BounceRing(
                shrink = true,
                modifier = Modifier.fillMaxSize()
            )
        }
        Box(modifier = Modifier.align(Alignment.Center)) {
            when (state) {
                is CounterState.Initial -> {
                    Text(
                        text = "Set time.",
                        style = pastText,
                        modifier = Modifier,
                    )
                }
                is CounterState.TimeSet -> {
                    Text(
                        text = "Click to start.",
                        style = pastText,
                        modifier = Modifier,
                    )
                }
                is CounterState.Beginning -> {
                    Text(
                        text = state.message,
                        style = pastText,
                        modifier = Modifier,
                    )
                }
                is CounterState.Running -> {
                    Text(
                        text = state.past.text(),
                        style = pastCounterText,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
                is CounterState.Paused -> {
                    Text(
                        text = "Paused",
                        style = pastText,
                        modifier = Modifier,
                    )
                }
                CounterState.Finish -> {
                    Text(
                        text = "Restart!",
                        style = pastText,
                        modifier = Modifier,
                    )
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
        color = Color.White,
        strokeWidth = ((1 - scale) * 8f).dp,
        modifier = Modifier
            .then(modifier)
            .scale(scale)
            .alpha(if (shrink) 1 - scale else scale)
    )
}

@Composable
fun InputField(
    label: String,
    text: String,
    imeAction: ImeAction = ImeAction.Next,
    onTextChanged: (String) -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "$label:",
            style = remainInputText.copy(
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.End,
            ),
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .weight(0.55f),
        )
        BasicTextField(
            value = text,
            onValueChange = { onTextChanged(it) },
            textStyle = remainInputText,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = imeAction,
            ),
            modifier = Modifier
                .weight(0.5f),
        )
    }
}
