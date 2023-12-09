package de.libf.kordbook.ui.components


import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/*********************************************************
  from https://github.com/a914-gowtham/compose-ratingbar/
    Copyright (c) 2021 Gowtham Balamurugan, MIT License
 *********************************************************/

sealed interface StepSize {
    object ONE : StepSize
    object HALF : StepSize
}

sealed class RatingBarStyle(open val activeColor: Color) {
    companion object {
        val Default = Stroke()
    }

    open class Fill(
        override val activeColor: Color = Color(0xFFFFCA00),
        val inActiveColor: Color = Color(0x66FFCA00),
    ) : RatingBarStyle(activeColor)

    /**
     * @param width width for each star
     * @param color A border [Color] shown on inactive star.
     */
    class Stroke(
        val width: Float = 1f,
        override val activeColor: Color = Color(0xFFFFCA00),
        val strokeColor: Color = Color(0xFF888888)
    ) : RatingBarStyle(activeColor)
}

//For ui testing
val StarRatingKey = SemanticsPropertyKey<Float>("StarRating")
var SemanticsPropertyReceiver.starRating by StarRatingKey


/**
 * @param value is current selected rating count
 * @param numOfStars count of stars to be shown.
 * @param size size for each star
 * @param spaceBetween padding between each star.
 * @param isIndicator isIndicator Whether this rating bar is only an indicator or the value is changeable on user interaction.
 * @param stepSize Can be [StepSize.ONE] or [StepSize.HALF]
 * @param hideInactiveStars Whether the inactive stars should be hidden.
 * @param style the different style applied to the Rating Bar.
 * @param onRatingChanged A function to be called when the click or drag is released and rating value is passed
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun RatingBar(
    value: Float,
    modifier: Modifier = Modifier,
    numOfStars: Int = 5,
    size: Dp = 32.dp,
    spaceBetween: Dp = 6.dp,
    isIndicator: Boolean = false,
    stepSize: StepSize = StepSize.ONE,
    hideInactiveStars: Boolean = false,
    style: RatingBarStyle = RatingBarStyle.Default,
    painterEmpty: Painter? = null,
    painterFilled: Painter? = null,
    onValueChange: (Float) -> Unit,
    onRatingChanged: (Float) -> Unit
) {
    var rowSize by remember { mutableStateOf(Size.Zero) }
    var lastDraggedValue by remember { mutableStateOf(0f) }
    val direction = LocalLayoutDirection.current
    val density = LocalDensity.current


    val paddingInPx = remember {
        with(density) { spaceBetween.toPx() }
    }
    val starSizeInPx = remember() {
        with(density) { size.toPx() }
    }

    Row(modifier = modifier
        .onSizeChanged { rowSize = it.toSize() }
        .pointerInput(
            onValueChange
        ) {
            //handling dragging events
            detectHorizontalDragGestures(
                onDragEnd = {
                    if (isIndicator || hideInactiveStars)
                        return@detectHorizontalDragGestures
                    onRatingChanged(lastDraggedValue)
                },
                onDragCancel = {

                },
                onDragStart = {

                },
                onHorizontalDrag = { change, _ ->
                    if (isIndicator || hideInactiveStars)
                        return@detectHorizontalDragGestures
                    change.consume()
                    val dragX = change.position.x.coerceIn(-1f, rowSize.width)
                    var calculatedStars =
                        RatingBarUtils.calculateStars(
                            dragX,
                            paddingInPx,
                            numOfStars, stepSize, starSizeInPx
                        )
                    if (direction == LayoutDirection.Rtl) {
                        // calculatedStars -> reversed
                        // 1 -> 5, 2 -> 4, 3 -> 3, 4 -> 2,5 -> 1
                        calculatedStars = (numOfStars - calculatedStars)
                    }
                    onValueChange(calculatedStars)
                    lastDraggedValue = calculatedStars
                }
            )
        }
        .pointerInput(onValueChange) {
            //handling when click events
            detectTapGestures(onTap = {
                if (isIndicator || hideInactiveStars)
                    return@detectTapGestures
                val dragX = it.x.coerceIn(-1f, rowSize.width)
                var calculatedStars =
                    RatingBarUtils.calculateStars(
                        dragX,
                        paddingInPx,
                        numOfStars, stepSize, starSizeInPx
                    )
                if (direction == LayoutDirection.Rtl) {
                    // calculatedStars -> reversed
                    // 1 -> 5, 2 -> 4, 3 -> 3, 4 -> 2,5 -> 1
                    calculatedStars = (numOfStars - calculatedStars) + 1
                }
                onValueChange(calculatedStars)
                onRatingChanged(calculatedStars)
            })
        }) {
        ComposeStars(
            value,
            numOfStars,
            size,
            spaceBetween,
            hideInactiveStars,
            style = style,
            painterEmpty,
            painterFilled
        )
    }
}

@Composable
fun RatingBar(
    value: Float,
    modifier: Modifier = Modifier,
    numOfStars: Int = 5,
    size: Dp = 32.dp,
    spaceBetween: Dp = 6.dp,
    isIndicator: Boolean = false,
    stepSize: StepSize = StepSize.ONE,
    hideInactiveStars: Boolean = false,
    style: RatingBarStyle,
    onValueChange: (Float) -> Unit,
    onRatingChanged: (Float) -> Unit
) {
    RatingBar(
        value = value,
        modifier = modifier,
        numOfStars = numOfStars,
        size = size,
        spaceBetween = spaceBetween,
        isIndicator = isIndicator,
        stepSize = stepSize,
        hideInactiveStars = hideInactiveStars,
        style = style,
        painterEmpty = null,
        painterFilled = null,
        onValueChange = onValueChange,
        onRatingChanged = onRatingChanged
    )
}

@Composable
fun RatingBar(
    value: Float,
    modifier: Modifier = Modifier,
    numOfStars: Int = 5,
    size: Dp = 32.dp,
    spaceBetween: Dp = 6.dp,
    isIndicator: Boolean = false,
    stepSize: StepSize = StepSize.ONE,
    hideInactiveStars: Boolean = false,
    painterEmpty: Painter,
    painterFilled: Painter,
    onValueChange: (Float) -> Unit,
    onRatingChanged: (Float) -> Unit
) {
    RatingBar(
        value = value,
        modifier = modifier,
        numOfStars = numOfStars,
        size = size,
        spaceBetween = spaceBetween,
        isIndicator = isIndicator,
        stepSize = stepSize,
        hideInactiveStars = hideInactiveStars,
        style = RatingBarStyle.Default,
        painterEmpty = painterEmpty,
        painterFilled = painterFilled,
        onValueChange = onValueChange,
        onRatingChanged = onRatingChanged
    )
}

@Composable
fun ComposeStars(
    value: Float,
    numOfStars: Int,
    size: Dp,
    spaceBetween: Dp,
    hideInactiveStars: Boolean,
    style: RatingBarStyle,
    painterEmpty: Painter?,
    painterFilled: Painter?
) {

    val ratingPerStar = 1f
    var remainingRating = value

    Row(modifier = Modifier
        .semantics { starRating = value }) {
        for (i in 1..numOfStars) {
            val starRating = when {
                remainingRating == 0f -> {
                    0f
                }

                remainingRating >= ratingPerStar -> {
                    remainingRating -= ratingPerStar
                    1f
                }

                else -> {
                    val fraction = remainingRating / ratingPerStar
                    remainingRating = 0f
                    fraction
                }
            }
            if (hideInactiveStars && starRating == 0.0f)
                break

            RatingStar(
                fraction = starRating,
                style = style,
                modifier = Modifier
                    .padding(
                        start = if (i > 1) spaceBetween else 0.dp,
                        end = if (i < numOfStars) spaceBetween else 0.dp
                    )
                    .size(size = size)
                    .testTag("RatingStar"),
                painterEmpty = painterEmpty, painterFilled = painterFilled
            )
        }
    }
}

object RatingBarUtils {

    fun calculateStars(
        draggedX: Float,
        horizontalPaddingInPx: Float,
        numOfStars: Int,
        stepSize: StepSize,
        starSizeInPx: Float,
    ): Float {

        if(draggedX<=0){
            return 0f
        }

        val starWidthWithRightPadding = starSizeInPx + (2 * horizontalPaddingInPx)
        val halfStarWidth = starSizeInPx / 2
        for (i in 1..numOfStars) {
            if (draggedX < (i * starWidthWithRightPadding)) {
                return if (stepSize is StepSize.ONE) {
                    i.toFloat()
                } else {
                    val crossedStarsWidth = (i - 1) * starWidthWithRightPadding
                    val remainingWidth = draggedX - crossedStarsWidth
                    if (remainingWidth <= halfStarWidth) {
                        i.toFloat().minus(0.5f)
                    } else {
                        i.toFloat()
                    }
                }
            }
        }
        return 0f
    }
}

@Composable
fun RatingStar(
    fraction: Float,
    modifier: Modifier = Modifier,
    style: RatingBarStyle,
    painterEmpty: Painter?,
    painterFilled: Painter?
) {
    if(fraction !in 0.0f..1.0f) throw IndexOutOfBoundsException("fraction must be between 0.0f and 1.0f")

    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    Box(modifier = modifier) {
        FilledStar(
            fraction,
            style,
            isRtl,
            painterFilled
        )
        EmptyStar(fraction, style, isRtl, painterEmpty)
    }
}

@Composable
private fun FilledStar(
    fraction: Float, style: RatingBarStyle, isRtl: Boolean, painterFilled: Painter?
) = Canvas(
    modifier = Modifier
        .fillMaxSize()
        .clip(
            if (isRtl) rtlFilledStarFractionalShape(fraction = fraction)
            else FractionalRectangleShape(0f, fraction)
        )
) {

    if (painterFilled != null) {
        with(painterFilled) {
            draw(
                size = Size(size.height, size.height),
            )
        }
    } else {
        val path = Path().addStar(size)

        drawPath(path, color = style.activeColor, style = Fill) // Filled Star
        drawPath(
            path,
            color = style.activeColor,
            style = Stroke(width = if (style is RatingBarStyle.Stroke) style.width else 1f)
        ) // Border
    }


}

@Composable
private fun EmptyStar(
    fraction: Float,
    style: RatingBarStyle,
    isRtl: Boolean,
    painterEmpty: Painter?
) =
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .clip(
                if (isRtl) rtlEmptyStarFractionalShape(fraction = fraction)
                else FractionalRectangleShape(fraction, 1f)
            )
    ) {

        if (painterEmpty != null) {
            with(painterEmpty) {
                draw(
                    size = Size(size.height, size.height),
                )
            }
        } else {
            val path = Path().addStar(size)
            if (style is RatingBarStyle.Fill) drawPath(
                path,
                color = style.inActiveColor,
                style = Fill
            ) // Border
            else if (style is RatingBarStyle.Stroke) drawPath(
                path, color = style.strokeColor, style = Stroke(width = style.width)
            ) // Border
        }

    }

fun rtlEmptyStarFractionalShape(fraction: Float): FractionalRectangleShape {
    return if (fraction == 1f || fraction == 0f)
        FractionalRectangleShape(fraction, 1f)
    else FractionalRectangleShape(0f, 1f - fraction)
}

fun rtlFilledStarFractionalShape(fraction: Float): FractionalRectangleShape {
    return if (fraction == 0f || fraction == 1f)
        FractionalRectangleShape(0f, fraction)
    else FractionalRectangleShape(1f - fraction, 1f)
}

fun Path.addStar(
    size: Size,
    spikes: Int = 5,
    outerRadiusFraction: Float = 0.5f,
    innerRadiusFraction: Float = 0.2f
): Path {
    if(outerRadiusFraction !in 0.0f..1.0f) throw IndexOutOfBoundsException("outerRadiusFraction must be between 0.0f and 1.0f")
    if(innerRadiusFraction !in 0.0f..1.0f) throw IndexOutOfBoundsException("innerRadiusFraction must be between 0.0f and 1.0f")

    val outerRadius = size.minDimension * outerRadiusFraction
    val innerRadius = size.minDimension * innerRadiusFraction

    val centerX = size.width / 2
    val centerY = size.height / 2

    var totalAngle = PI / 2 // Since we start at the top center, the initial angle will be 90°
    val degreesPerSection = (2 * PI) / spikes

    moveTo(centerX, 0f) // Starts at the top center of the bounds

    var x: Double
    var y: Double

    for (i in 1..spikes) {
        // Line going inwards from outerCircle to innerCircle
        totalAngle += degreesPerSection / 2
        x = centerX + cos(totalAngle) * innerRadius
        y = centerY - sin(totalAngle) * innerRadius
        lineTo(x.toFloat(), y.toFloat())


        // Line going outwards from innerCircle to outerCircle
        totalAngle += degreesPerSection / 2
        x = centerX + cos(totalAngle) * outerRadius
        y = centerY - sin(totalAngle) * outerRadius
        lineTo(x.toFloat(), y.toFloat())
    }

    // Path should be closed to ensure it's not an open shape
    close()

    return this
}

@Stable
class FractionalRectangleShape(
    private val startFraction: Float,
    private val endFraction: Float
) : Shape {

    init {
        if(startFraction !in 0.0f..1.0f) throw IndexOutOfBoundsException("startFraction must be between 0.0f and 1.0f")
        if(endFraction !in 0.0f..1.0f) throw IndexOutOfBoundsException("endFraction must be between 0.0f and 1.0f")
    }
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        return Outline.Rectangle(
            Rect(
                left = (startFraction * size.width).coerceAtMost(size.width - 1f),
                top = 0f,
                right = (endFraction * size.width).coerceAtLeast(1f),
                bottom = size.height
            )
        )
    }

}