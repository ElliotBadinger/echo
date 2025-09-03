package eu.mrogalski.saidit.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.Flow

@Composable
fun Waveform(amplitude: Flow<Int>) {
    val amplitudeValue by amplitude.collectAsState(initial = 0)

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
    ) {
        val path = Path()
        val width = size.width
        val height = size.height
        val centerY = height / 2

        path.moveTo(0f, centerY)

        for (i in 0 until width.toInt()) {
            val x = i.toFloat()
            val y = centerY + (amplitudeValue * Math.sin((x * 2 * Math.PI) / width)).toFloat()
            path.lineTo(x, y)
        }

        drawPath(path, color = Color.Red)
    }
}
