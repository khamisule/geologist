package tz.geologist.core

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream

/**
 * Picha za shambani: ubora WA JUU lakini uzito MDOGO.
 *
 * Simu hupiga picha 3–12 MP (2–8 MB). Hapa tunapunguza hadi ~150–400 KB bila kupoteza
 * ubora unaoonekana (viewing + mineral ID zote zinatosha), ili sync iwe haraka na
 * storage/data ipungue.
 *
 * Mbinu (sawa na `ml/image_utils.py`):
 *  - inSampleSize: decode kwa ukubwa mdogo (kuepuka OutOfMemory).
 *  - Punguza upande mrefu hadi `maxSide` (1600px default).
 *  - Rekebisha orientation kutoka EXIF (picha isiwe imelala).
 *  - JPEG quality 82 (karibu-lossless kwa macho).
 *
 * (Imethibitishwa kwa upande wa Python: 4.2 MB -> ~98 KB, PSNR ~42 dB.)
 */
object ImageCompressor {

    fun compress(
        context: Context,
        source: Uri,
        maxSide: Int = 1600,
        quality: Int = 82,
    ): File {
        val cr = context.contentResolver

        // 1. Soma vipimo bila kupakia bitmap nzima
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        cr.openInputStream(source).use { BitmapFactory.decodeStream(it, null, bounds) }

        // 2. inSampleSize (power of two) ili decode iwe ndogo
        var sample = 1
        val longest = maxOf(bounds.outWidth, bounds.outHeight)
        while (longest / sample > maxSide * 2) sample *= 2

        val opts = BitmapFactory.Options().apply { inSampleSize = sample }
        var bmp = cr.openInputStream(source).use { BitmapFactory.decodeStream(it, null, opts) }
            ?: error("Imeshindwa kusoma picha")

        // 3. Punguza hasa hadi maxSide (bila ku-upscale)
        val scale = minOf(1f, maxSide.toFloat() / maxOf(bmp.width, bmp.height))
        if (scale < 1f) {
            bmp = Bitmap.createScaledBitmap(bmp, (bmp.width * scale).toInt(), (bmp.height * scale).toInt(), true)
        }

        // 4. Rekebisha orientation kutoka EXIF
        bmp = applyExifRotation(context, source, bmp)

        // 5. Compress JPEG -> cache file
        val out = File(context.cacheDir, "geo_${System.currentTimeMillis()}.jpg")
        FileOutputStream(out).use { bmp.compress(Bitmap.CompressFormat.JPEG, quality, it) }
        return out
    }

    private fun applyExifRotation(context: Context, uri: Uri, bmp: Bitmap): Bitmap {
        val orientation = runCatching {
            context.contentResolver.openInputStream(uri)?.use {
                ExifInterface(it).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            }
        }.getOrNull() ?: ExifInterface.ORIENTATION_NORMAL

        val deg = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
        if (deg == 0f) return bmp
        val m = Matrix().apply { postRotate(deg) }
        return Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, m, true)
    }
}
