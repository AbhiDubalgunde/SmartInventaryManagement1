package com.example.smartinventarymanagement.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.smartinventarymanagement.Item
import com.example.smartinventarymanagement.R
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

object ExportHelper {

    fun exportToPdf(context: Context, items: List<Item>, onComplete: (Uri?) -> Unit) {
        val fileName = "Inventory_Report_${System.currentTimeMillis()}.pdf"
        val outputStream: OutputStream?
        var uri: Uri? = null

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val contentResolver = context.contentResolver
                uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                outputStream = uri?.let { contentResolver.openOutputStream(it) }
            } else {
                val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
                outputStream = FileOutputStream(file)
                uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            }

            outputStream?.use { os ->
                val writer = PdfWriter(os)
                val pdf = PdfDocument(writer)
                val document = Document(pdf)

                // Add Logo
                val drawable = ContextCompat.getDrawable(context, R.mipmap.ic_launcher)
                if (drawable != null) {
                    val bitmap = drawableToBitmap(drawable)
                    val stream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    val imageData = ImageDataFactory.create(stream.toByteArray())
                    val image = Image(imageData).setWidth(50f).setHeight(50f)
                    document.add(image)
                }

                document.add(Paragraph("Smart Inventory Report").apply {
                    setTextAlignment(TextAlignment.CENTER)
                    setFontSize(20f)
                    setBold()
                })

                val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
                document.add(Paragraph("Exported on: ${sdf.format(Date())}").apply {
                    setTextAlignment(TextAlignment.RIGHT)
                    setFontSize(10f)
                })

                document.add(Paragraph("\n"))

                val table = Table(UnitValue.createPercentArray(floatArrayOf(30f, 20f, 15f, 15f, 20f))).apply {
                    width = UnitValue.createPercentValue(100f)
                }

                // Headers
                val headers = listOf("Item Name", "Category", "Qty", "Price", "Total")
                headers.forEach { header ->
                    table.addHeaderCell(Cell().add(Paragraph(header).setBold().setBackgroundColor(ColorConstants.LIGHT_GRAY)))
                }

                var totalInventoryValue = 0.0
                items.forEach { item ->
                    val total = item.quantity * item.price
                    totalInventoryValue += total
                    table.addCell(Cell().add(Paragraph(item.name)))
                    table.addCell(Cell().add(Paragraph(item.category)))
                    table.addCell(Cell().add(Paragraph(item.quantity.toString())))
                    table.addCell(Cell().add(Paragraph("₹${item.price}")))
                    table.addCell(Cell().add(Paragraph("₹$total")))
                }

                document.add(table)
                document.add(Paragraph("\n"))
                document.add(Paragraph("Total Inventory Value: ₹$totalInventoryValue").apply {
                    setBold()
                    setFontSize(14f)
                    setTextAlignment(TextAlignment.RIGHT)
                })

                document.close()
                onComplete(uri)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onComplete(null)
        }
    }

    fun exportToExcel(context: Context, items: List<Item>, onComplete: (Uri?) -> Unit) {
        val fileName = "Inventory_Report_${System.currentTimeMillis()}.xlsx"
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Inventory")

        val headerRow = sheet.createRow(0)
        val headers = listOf("Item Name", "Category", "Quantity", "Price", "Total Value", "Low Stock Status")
        headers.forEachIndexed { index, header ->
            headerRow.createCell(index).setCellValue(header)
        }

        items.forEachIndexed { index, item ->
            val row = sheet.createRow(index + 1)
            row.createCell(0).setCellValue(item.name)
            row.createCell(1).setCellValue(item.category)
            row.createCell(2).setCellValue(item.quantity.toDouble())
            row.createCell(3).setCellValue(item.price)
            row.createCell(4).setCellValue(item.quantity * item.price)
            row.createCell(5).setCellValue(if (item.quantity == 0) "Out of Stock" else if (item.quantity < item.lowStockThreshold) "Low Stock" else "Normal")
        }

        for (i in headers.indices) {
            sheet.autoSizeColumn(i)
        }

        var uri: Uri? = null
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val contentResolver = context.contentResolver
                uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                uri?.let {
                    contentResolver.openOutputStream(it)?.use { os ->
                        workbook.write(os)
                    }
                }
            } else {
                val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
                FileOutputStream(file).use { os ->
                    workbook.write(os)
                }
                uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            }
            workbook.close()
            onComplete(uri)
        } catch (e: Exception) {
            e.printStackTrace()
            onComplete(null)
        }
    }

    fun shareFile(context: Context, uri: Uri, mimeType: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Report"))
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable) return drawable.bitmap
        val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
}
