package com.worldprices.converter.ui.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import androidx.core.content.FileProvider
import com.worldprices.converter.data.model.Currency
import java.io.File
import java.io.FileOutputStream
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object ShareHelper {

    fun shareConversionSummary(
        context: Context,
        sourceCurrency: Currency,
        amount: Double,
        targets: List<Currency>,
        effectiveRates: Map<String, Double>,
        isRussian: Boolean
    ) {
        try {
            val bitmap = createSummaryBitmap(context, sourceCurrency, amount, targets, effectiveRates, isRussian)
            val cachePath = File(context.cacheDir, "shared")
            if (!cachePath.exists()) cachePath.mkdirs()

            val imageFile = File(cachePath, "conversion_summary.png")
            FileOutputStream(imageFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                imageFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_TEXT, "${amount.toInt()} ${sourceCurrency.code.uppercase()} — World Prices")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(
                shareIntent,
                if (isRussian) "Поделиться расчетом" else "Share conversion"
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createSummaryBitmap(
        context: Context,
        sourceCurrency: Currency,
        amount: Double,
        targets: List<Currency>,
        effectiveRates: Map<String, Double>,
        isRussian: Boolean
    ): Bitmap {
        val width = 1080
        val headerHeight = 280
        val rowHeight = 130
        val footerHeight = 140
        val height = headerHeight + (targets.size * rowHeight) + footerHeight

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background
        val bgPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#0F172A") // Deep modern dark blue
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        val symbols = DecimalFormatSymbols(Locale.US).apply { groupingSeparator = ' '; decimalSeparator = '.' }
        val amountFormatter = DecimalFormat("#,##0.00", symbols)

        // Header Card
        val cardPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#1E293B")
            style = Paint.Style.FILL
        }
        val headerRect = RectF(40f, 40f, (width - 40).toFloat(), (headerHeight - 20).toFloat())
        canvas.drawRoundRect(headerRect, 32f, 32f, cardPaint)

        val titlePaint = Paint().apply {
            color = android.graphics.Color.parseColor("#38BDF8")
            textSize = 34f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("💱 World Prices Converter", 80f, 110f, titlePaint)

        val amountPaint = Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 58f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("${amountFormatter.format(amount)} ${sourceCurrency.code.uppercase()}", 80f, 195f, amountPaint)

        // Currency Rows
        var currentY = headerHeight.toFloat() + 20f

        val textCodePaint = Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 42f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val textNamePaint = Paint().apply {
            color = android.graphics.Color.parseColor("#94A3B8")
            textSize = 28f
            isAntiAlias = true
        }

        val textValuePaint = Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 46f
            textAlign = Paint.Align.RIGHT
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val rowCardPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#1E293B")
            style = Paint.Style.FILL
        }

        for (target in targets) {
            val rate = effectiveRates[target.code.lowercase()] ?: 0.0
            val converted = amount * rate

            val rowRect = RectF(40f, currentY, (width - 40).toFloat(), currentY + rowHeight - 20f)
            canvas.drawRoundRect(rowRect, 24f, 24f, rowCardPaint)

            canvas.drawText(target.code.uppercase(), 80f, currentY + 54f, textCodePaint)
            canvas.drawText(target.localizedName(isRussian), 80f, currentY + 92f, textNamePaint)

            val valStr = "${amountFormatter.format(converted)} ${target.symbol}"
            canvas.drawText(valStr, (width - 80).toFloat(), currentY + 70f, textValuePaint)

            currentY += rowHeight
        }

        // Footer
        val footerPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#64748B")
            textSize = 26f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(
            if (isRussian) "Создано в приложении World Prices" else "Generated via World Prices App",
            (width / 2).toFloat(),
            (height - 50).toFloat(),
            footerPaint
        )

        return bitmap
    }
}
