package com.apicta.wraphumic.util

import android.content.Context
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

class TFLiteHelper(private val context: Context) {

    private var interpreter: Interpreter

    init {
        val model = loadModelFile("model_2.tflite")
        interpreter = Interpreter(model)
    }

    fun runInference(inputValues: FloatArray): FloatArray{
        if (inputValues.size != numInputElements) {
            throw IllegalArgumentException("Input array size does not match the specified shape.")
        }

        val inputBuffer = TensorBuffer.createFixedSize(intArrayOf(1, numInputElements), DataType.FLOAT32)
        inputBuffer.loadArray(inputValues)

        val outputBuffer = TensorBuffer.createFixedSize(intArrayOf(1, numOutputElements), DataType.FLOAT32)

        interpreter.run(inputBuffer.buffer, outputBuffer.buffer)

        return outputBuffer.floatArray
    }

    private fun loadModelFile(modelPath: String): ByteBuffer {
        val assetManager = context.assets
        val inputStream = assetManager.open(modelPath)
        val modelBuffer = inputStream.readBytes()

        val byteBuffer = ByteBuffer.allocateDirect(modelBuffer.size)
        byteBuffer.order(ByteOrder.nativeOrder())
        byteBuffer.put(modelBuffer)
        byteBuffer.flip()

        return byteBuffer
    }

    companion object {
        private const val numInputElements = 3
        private const val numOutputElements = 2
    }
}