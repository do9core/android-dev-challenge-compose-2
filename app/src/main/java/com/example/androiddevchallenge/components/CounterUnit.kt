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
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
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
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.SweepGradientShader
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.transform
import com.example.androiddevchallenge.CounterState
import com.example.androiddevchallenge.TimerField
import com.example.androiddevchallenge.text
import com.example.androiddevchallenge.ui.theme.pastCounterText
import com.example.androiddevchallenge.ui.theme.remainCounterText
import com.example.androiddevchallenge.ui.theme.veilWhite
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
    val targetValue by produceState(0.dp) {
        while (isActive) {
            value = size * Random.nextFloat()
            delay(duration.toLong())
        }
    }
    return animateDpAsState(
        targetValue = targetValue,
        animationSpec = tween(durationMillis = duration, easing = easing)
    )
}

@Composable
private fun animateColorRandomly(
    red: Float = 0f,
    green: Float = 0f,
    blue: Float = 0f,
    duration: Int = 2000,
    easing: Easing = LinearEasing,
): State<Color> {
    val targetValue by produceState(Color.Transparent) {
        while (isActive) {
            val (r, g, b) = listOf(red, green, blue).map { field ->
                val offset = 0.5f * Random.nextFloat()
                val direction = if (Random.nextBoolean()) -1 else 1
                field.plus(direction * offset).coerceIn(0f..1f)
            }
            value = Color(r, g, b)
            delay(duration.toLong())
        }
    }
    return animateColorAsState(
        targetValue = targetValue,
        animationSpec = tween(durationMillis = duration, easing = easing)
    )
}

@Composable
fun Counters(
    state: CounterState,
    onBeginCountdown: () -> Unit,
    onTimeUpdate: (TimerField, Int) -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
) {
    val remainPart = remember { 0.476f }
    val pastPart = remember(remainPart) { remainPart * sqrt(5f).dec().div(2) }
    val spacerAll = remember(remainPart, pastPart) { 1f - remainPart - pastPart }
    val sideSpacer = remember(spacerAll) { spacerAll * 3f.minus(sqrt(5f)).div(2) }
    val centerSpacer = remember(spacerAll) { spacerAll * sqrt(5f).minus(2) }

    val a by animateColorRandomly(1.00f, 0.57f, 0.72f, 2847, FastOutLinearInEasing)
    val b by animateColorRandomly(0.42f, 0.21f, 0.33f, 3571, LinearOutSlowInEasing)
    val c by animateColorRandomly(0.23f, 0.76f, 0.48f, 2543, FastOutSlowInEasing)
    val d by animateColorRandomly(1.00f, 0.43f, 1.00f, 3344, LinearEasing)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = ShaderBrush(
                    shader = RadialGradientShader(
                        center = Offset(-100f, -200f),
                        radius = 3600f,
                        colors = listOf(a, b, c, d),
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

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CounterRemain(
    state: CounterState,
    onTimeUpdate: (TimerField, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = Modifier
            .then(modifier)
            .background(
                shape = CircleShape,
                brush = ShaderBrush(
                    shader = RadialGradientShader(
                        colors = listOf(veilWhite, Color.Transparent),
                        center = Offset(-200f, -100f),
                        radius = 1450f,
                    )
                )
            )
            .aspectRatio(1f)
    ) {
        AnimatedVisibility(
            visible = state.isActive,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            BounceRing(shrink = false, modifier = Modifier.fillMaxSize())
        }
        Box(modifier = Modifier.align(Alignment.Center)) {
            val showArrow = remember(state) { state is CounterState.Initial }
            val alpha by if (state !is CounterState.Paused) {
                animateFloatAsState(targetValue = 1f)
            } else {
                val animator = rememberInfiniteTransition()
                animator.animateFloat(
                    initialValue = 1f,
                    targetValue = 0.1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 500),
                        repeatMode = RepeatMode.Reverse,
                    )
                )
            }
            val paddingSize by animateDpAsState(
                targetValue = when (state) {
                    is CounterState.Initial -> 10.dp
                    is CounterState.Running -> 4.dp
                    is CounterState.Paused -> 6.dp
                }
            )
            Row(
                modifier = Modifier
                    .align(Alignment.Center)
                    .alpha(alpha)
            ) {
                PickerField(
                    value = state.hours,
                    showArrows = showArrow,
                    onValueChanged = { onTimeUpdate(TimerField.HOUR, it) },
                )
                Text(
                    text = ":",
                    style = remainCounterText,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(horizontal = paddingSize)
                )
                PickerField(
                    value = state.minutes,
                    showArrows = showArrow,
                    onValueChanged = { onTimeUpdate(TimerField.MINUTE, it) },
                )
                Text(
                    text = ":",
                    style = remainCounterText,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(horizontal = paddingSize)
                )
                PickerField(
                    value = state.seconds,
                    showArrows = showArrow,
                    onValueChanged = { onTimeUpdate(TimerField.SECOND, it) },
                )
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CounterPast(
    state: CounterState,
    onBeginCountdown: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rotateBorderProducer by produceState(0f) {
        while (isActive) {
            delay(1400)
            value += 365f
        }
    }
    val rotateBorder by animateFloatAsState(
        targetValue = rotateBorderProducer,
        animationSpec = tween(
            durationMillis = 1400
        )
    )
    Box(
        modifier = Modifier
            .then(modifier)
            .border(
                width = 1.dp,
                shape = CircleShape,
                brush = ShaderBrush(
                    shader = SweepGradientShader(
                        colors = listOf(veilWhite, Color.Transparent),
                        center = Offset(1000f, 1000f),
                    ).apply {
                        transform {
                            postRotate(rotateBorder)
                        }
                    }
                ),
            )
            .aspectRatio(1f)
            .clip(CircleShape)
            .clickable {
                when (state) {
                    is CounterState.Initial -> {
                        if (state.isReady) {
                            onBeginCountdown()
                        }
                    }
                    is CounterState.Running -> onPause()
                    is CounterState.Paused -> onResume()
                }
            }
    ) {
        AnimatedVisibility(
            visible = state.isActive,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            BounceRing(shrink = true, modifier = Modifier.fillMaxSize())
        }
        Box(modifier = Modifier.align(Alignment.Center)) {
            when (state) {
                is CounterState.Initial -> {
                    val rotate by animateFloatAsState(targetValue = if (state.isReady) 90f else 180f)
                    val scale by animateFloatAsState(targetValue = if (state.isReady) 1f else 0.6f)
                    Triangle(
                        color = veilWhite,
                        modifier = Modifier
                            .size(20.dp, 18.dp)
                            .rotate(rotate)
                            .scale(scale)
                    )
                }
                is CounterState.Running -> {
                    Text(
                        text = state.past.text(),
                        style = pastCounterText,
                        modifier = Modifier,
                    )
                }
                is CounterState.Paused -> {
                    Triangle(
                        color = veilWhite,
                        modifier = Modifier
                            .size(20.dp, 18.dp)
                            .rotate(90f)
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
            animation = tween(
                durationMillis = 900,
                delayMillis = 100,
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

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PickerField(
    value: Int,
    showArrows: Boolean = true,
    onValueChanged: (newValue: Int) -> Unit
) {
    Column {
        AnimatedVisibility(
            visible = showArrows,
            enter = fadeIn(),
            exit = fadeOut() + slideOutVertically(),
        ) {
            IconButton(
                onClick = { onValueChanged(value.inc()) },
                modifier = Modifier.align(Alignment.CenterHorizontally),
            ) {
                Triangle(
                    color = veilWhite,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
        Text(
            text = "%02d".format(value),
            style = remainCounterText,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        AnimatedVisibility(
            visible = showArrows,
            enter = fadeIn(),
            exit = fadeOut() + slideOutVertically(
                targetOffsetY = { it / 2 }
            ),
        ) {
            IconButton(
                onClick = { onValueChanged(value.dec()) },
                modifier = Modifier.align(Alignment.CenterHorizontally),
            ) {
                Triangle(
                    color = veilWhite,
                    modifier = Modifier
                        .size(14.dp)
                        .rotate(180f),
                )
            }
        }
    }
}

@Composable
fun Triangle(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        drawPath(
            color = color,
            style = Stroke(width = density * 3f, join = StrokeJoin.Round),
            path = Path().apply {
                val centerX = size.width / 2
                moveTo(centerX, 0f)
                relativeLineTo(-centerX, size.height)
                relativeLineTo(size.width, 0f)
                close()
            },
        )
    }
}
