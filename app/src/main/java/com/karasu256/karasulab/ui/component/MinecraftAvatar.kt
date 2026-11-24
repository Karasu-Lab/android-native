package com.karasu256.karasulab.ui.component

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil3.SingletonImageLoader
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.toBitmap

@Composable
fun MinecraftAvatar(
    textureUrl: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
) {
    val context = LocalContext.current
    var skin by remember(textureUrl) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(textureUrl) {
        val request = ImageRequest.Builder(context).data(textureUrl).allowHardware(false).build()
        when (val result = SingletonImageLoader.get(context).execute(request)) {
            is SuccessResult -> skin = result.image.toBitmap()
            is ErrorResult -> Log.e("MinecraftAvatar", "Could not load the skin texture $textureUrl", result.throwable)
        }
    }

    Canvas(modifier = modifier.size(size).clip(MaterialTheme.shapes.small)) {
        val bitmap = skin ?: return@Canvas
        val image = bitmap.asImageBitmap()
        val target = IntSize(this.size.width.toInt(), this.size.height.toInt())
        drawImage(image, srcOffset = IntOffset(8, 8), srcSize = IntSize(8, 8), dstSize = target, filterQuality = FilterQuality.None)
        if (bitmap.height >= 64) {
            drawImage(image, srcOffset = IntOffset(40, 8), srcSize = IntSize(8, 8), dstSize = target, filterQuality = FilterQuality.None)
        }
    }
}
