package tz.geologist.ai

import android.graphics.Bitmap

/**
 * M4 · On-device rock & mineral identification (offline).
 *
 * Inatumia TensorFlow Lite CNN (MobileNet/EfficientNet-lite) iliyofunzwa kwa
 * dataset ya miamba + madini, ime-quantize (<50MB), inference <1s.
 * Kwa madini ya kawaida katika mwanga mzuri, accuracy > 90%.
 *
 * Muunganiko na determinative key (hardness, streak, luster, cleavage)
 * huongeza uhakika pale picha peke yake haitoshi.
 */
interface RockClassifier {
    data class Prediction(
        val label: String,          // e.g. "Quartz", "Biotite", "Gold-bearing quartz vein"
        val confidence: Float,      // 0..1
        val group: String           // igneous / sedimentary / metamorphic / ore mineral
    )

    /** Rudisha top-k predictions kwa picha ya specimen. */
    fun classify(bitmap: Bitmap, topK: Int = 3): List<Prediction>
}

/**
 * Stub — badilisha na TFLite Interpreter halisi.
 * TODO: pakia model.tflite kutoka assets, tumia tensorflow-lite-support
 *       ImageProcessor (resize 224x224, normalize), toa probabilities.
 */
class TFLiteRockClassifier /* @Inject constructor(...) */ : RockClassifier {
    override fun classify(bitmap: Bitmap, topK: Int): List<RockClassifier.Prediction> {
        // TODO: interpreter.run(input, output); map kwa labels.txt
        return emptyList()
    }
}
