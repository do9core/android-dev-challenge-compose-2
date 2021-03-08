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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RadialGradientShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androiddevchallenge.CounterState
import com.example.androiddevchallenge.TimerField
import com.example.androiddevchallenge.text
import com.example.androiddevchallenge.ui.theme.pastCounterText
import com.example.androiddevchallenge.ui.theme.pastText
import com.example.androiddevchallenge.ui.theme.remainCounterText
import com.example.androiddevchallenge.ui.theme.remainText
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
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
    onTimeUpdate: (TimerField, Int) -> Unit,
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
    onTimeUpdate: (TimerField, Int) -> Unit,
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
                    Row(
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        PickerField(
                            value = state.hours,
                            onValueChanged = { onTimeUpdate(TimerField.HOUR, it) },
                        )
                        PickerField(
                            value = state.minutes,
                            onValueChanged = { onTimeUpdate(TimerField.MINUTE, it) },
                        )
                        PickerField(
                            value = state.seconds,
                            onValueChanged = { onTimeUpdate(TimerField.SECOND, it) },
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

@OptIn(ExperimentalAnimationApi::class)
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
    val scope = rememberCoroutineScope()
    var isHolding by remember { mutableStateOf(false) }
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
            .pointerInput(state) {
                detectTapGestures(
                    onPress = {
                        val timeStart = System.currentTimeMillis()
                        isHolding = true
                        scope.launch {
                            try {
                                awaitRelease()
                            } finally {
                                isHolding = false
                                val duration = System.currentTimeMillis() - timeStart
                                if (duration < 200) {
                                    when (state) {
                                        is CounterState.Initial -> onTimeSet()
                                        is CounterState.TimeSet -> onBeginCountdown()
                                        is CounterState.Running -> onPause()
                                        is CounterState.Paused -> onResume()
                                        is CounterState.Finish -> onReset()
                                        else -> Unit
                                    }
                                }
                            }
                        }
                    },
                    
                )
            }
    ) {
        if (state.isActive) {
            BounceRing(
                shrink = true,
                modifier = Modifier.fillMaxSize()
            )
        }
        AnimatedVisibility(
            visible = state is CounterState.Paused && isHolding,
            modifier = Modifier.align(Alignment.Center)
        ) {
            val pressScale by produceState(0f) {
                while (isActive && value < 1f) {
                    value += .25f
                    delay(400)
                }
            }
            val scale by animateFloatAsState(
                targetValue = pressScale,
                animationSpec = TweenSpec(
                    durationMillis = 400,
                    easing = LinearEasing,
                ),
                finishedListener = {
                    if (it >= 1f) {
                        onReset()
                    }
                }
            )
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Red.copy(alpha = 0.25f * scale))
                    .scale(scale)
                    .align(Alignment.Center)
            ) {
                drawCircle(
                    color = Color.Red.copy(alpha = .6f),
                    center = center,
                    radius = minOf(size.width, size.height) / 2,
                )
            }
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
                        text = if (isHolding) "Hold to Stop" else "Paused",
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
        strokeWidth = 8f.dp * (1 - scale),
        modifier = Modifier
            .then(modifier)
            .scale(scale)
            .alpha(if (shrink) 1 - scale else scale)
    )
}

@Composable
fun PickerField(value: Int, onValueChanged: (newValue: Int) -> Unit) {
    Column {
        IconButton(
            onClick = { onValueChanged(value.inc()) },
            modifier = Modifier.align(Alignment.CenterHorizontally),
        ) {
            Triangle(
                color = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp),
            )
        }
        Text(
            text = "%02d".format(value),
            style = remainCounterText.copy(
                fontSize = 36.sp
            ),
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        IconButton(
            onClick = { onValueChanged(value.dec()) },
            modifier = Modifier.align(Alignment.CenterHorizontally),
        ) {
            Triangle(
                color = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp).rotate(180f),
            )
        }
    }
}

@Composable
fun Triangle(
    color: Color,
    modifier: Modifier,
) {
    Canvas(modifier = modifier) {
        val path = Path().apply {
            val centerX = size.width / 2
            moveTo(centerX, 0f)
            relativeLineTo(-centerX, size.height)
            relativeLineTo(size.width, 0f)
            close()
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = density * 3f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
            )
        )
    }
}
