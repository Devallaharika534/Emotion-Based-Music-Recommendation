package com.example.emotionmusicapp

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.view.PreviewView
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import java.nio.ByteBuffer
import java.nio.ByteOrder

/*
 * Emotion-Based Music Recommendation System
 *
 * Main Activity - Core Application Logic
 *
 * This file contains the source-code snippets documented
 * in the project report.
 *
 * Note: The project report provides this implementation
 * as partial source code rather than a complete Android
 * Studio project.
 */

class MainActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var emotionText: TextView
    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        previewView = PreviewView(this)

        emotionText = TextView(this).apply {
            text = "Emotion: Detecting..."
        }

        statusText = TextView(this)

        layout.addView(previewView)
        layout.addView(emotionText)
        layout.addView(statusText)

        setContentView(layout)
    }

    /*
     * Emotion Detection Module
     */
    private fun runModel(bitmap: Bitmap): Result {

        val input = ByteBuffer.allocateDirect(
            4 * INPUT_SIZE * INPUT_SIZE
        )

        input.order(ByteOrder.nativeOrder())

        val pixels = IntArray(
            INPUT_SIZE * INPUT_SIZE
        )

        bitmap.getPixels(
            pixels,
            0,
            INPUT_SIZE,
            0,
            0,
            INPUT_SIZE,
            INPUT_SIZE
        )

        for (p in pixels) {

            val r = (p shr 16) and 0xFF
            val g = (p shr 8) and 0xFF
            val b = p and 0xFF

            val gray =
                0.299f * r +
                0.587f * g +
                0.114f * b

            input.putFloat(
                (gray - 128f) / 128f
            )
        }

        val output =
            Array(1) {
                FloatArray(EMOTIONS.size)
            }

        interpreter?.run(input, output)

        val index =
            output[0].indices.maxByOrNull {
                output[0][it]
            } ?: 0

        return Result(
            index,
            EMOTIONS[index],
            output[0][index]
        )
    }

    /*
     * Music Recommendation Module
     */
    private fun openSpotify(emotion: String) {

        val query = when (emotion) {

            "Happy" -> "happy songs"

            "Sad" -> "sad songs"

            "Angry" -> "calm music"

            "Neutral" -> "chill music"

            else -> "chill music"
        }

        val url =
            "https://open.spotify.com/search/" +
            query.replace(" ", "%20")

        val intent =
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(url)
            )

        intent.setPackage("com.spotify.music")

        startActivity(intent)
    }

    companion object {

        const val INPUT_SIZE = 48

        val EMOTIONS = arrayOf(
            "Angry",
            "Disgust",
            "Fear",
            "Happy",
            "Sad",
            "Surprise",
            "Neutral"
        )
    }

    data class Result(
        val index: Int,
        val emotion: String,
        val confidence: Float
    )

    /*
     * The complete project report contains the model
     * initialization and face-detection implementation.
     */
    private var interpreter: Any? = null
}
