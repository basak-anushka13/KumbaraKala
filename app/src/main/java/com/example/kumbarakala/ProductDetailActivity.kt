package com.example.kumbarakala

import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream

class ProductDetailActivity : AppCompatActivity() {

    private var generatedBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        // 🔹 Views
        val back = findViewById<ImageView>(R.id.btnBack)
        val img = findViewById<ImageView>(R.id.detailImage)
        val name = findViewById<TextView>(R.id.detailName)
        val btnGenerate = findViewById<Button>(R.id.btnGenerate)
        val btnShare = findViewById<Button>(R.id.btnShare)
        val etName = findViewById<EditText>(R.id.etName)
        val etPhone = findViewById<EditText>(R.id.etPhone)

        // 🔹 Back button
        back.setOnClickListener { finish() }

        // 🔹 Data
        val productName = intent.getStringExtra("name")
        val imageRes = intent.getIntExtra("image", 0)
        val benefit = intent.getStringExtra("benefit") ?: ""

        name.text = productName
        img.setImageResource(imageRes)

        // 🔥 GENERATE CARD
        btnGenerate.setOnClickListener {

            val userName = etName.text.toString()
            val userPhone = etPhone.text.toString()

            if (userName.isEmpty() || userPhone.isEmpty()) {
                Toast.makeText(this, "Enter details first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()

            val data = hashMapOf(
                "name" to userName,
                "phone" to userPhone,
                "product" to productName,
                "timestamp" to System.currentTimeMillis()
            )

            db.collection("generated_cards")
                .add(data)
                .addOnSuccessListener {
                    Toast.makeText(this, "Saved to Firebase", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to save", Toast.LENGTH_SHORT).show()
                }
            try {
                // Convert drawable → bitmap
                val drawable = resources.getDrawable(imageRes, null)

                val bitmap = Bitmap.createBitmap(
                    drawable.intrinsicWidth,
                    drawable.intrinsicHeight,
                    Bitmap.Config.ARGB_8888
                )

                val canvas = Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)

                val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
                val drawCanvas = Canvas(mutableBitmap)
                val rectPaint = Paint().apply {
                    color = Color.WHITE
                    alpha = 220
                }
                val rectTop = bitmap.height * 0.6f

                drawCanvas.drawRect(
                    0f,
                    rectTop,
                    bitmap.width.toFloat(),
                    bitmap.height.toFloat(),
                    rectPaint
                )
                val paint = Paint().apply {
                    color = Color.BLACK
                    textSize = bitmap.width * 0.045f
                    isAntiAlias = true
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    setShadowLayer(4f, 2f, 2f, Color.GRAY)
                }

                val padding = 40f
                val maxWidth = bitmap.width - 2 * padding
                val lineGap = paint.textSize + 20f
                val totalTextHeight = lineGap * 3

                val startY = bitmap.height - totalTextHeight - 40f

                drawMultilineText(drawCanvas, benefit, padding, startY, paint, maxWidth)
                drawMultilineText(drawCanvas, "Non-toxic & eco-friendly", padding, startY + lineGap, paint, maxWidth)
                drawMultilineText(drawCanvas, "By: $userName | Ph: $userPhone", padding, startY + 2 * lineGap, paint, maxWidth)

                img.setImageBitmap(mutableBitmap)

                // Save for sharing
                generatedBitmap = mutableBitmap

            } catch (e: Exception) {
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        // 🔥 SHARE TO WHATSAPP
        btnShare.setOnClickListener {

            if (generatedBitmap == null) {
                Toast.makeText(this, "Generate card first!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                val file = File(cacheDir, "card.png")
                val fos = FileOutputStream(file)
                generatedBitmap!!.compress(Bitmap.CompressFormat.PNG, 100, fos)

                fos.close()

                val uri = androidx.core.content.FileProvider.getUriForFile(
                    this,
                    "${packageName}.provider",
                    file
                )

                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "image/*"
                intent.putExtra(Intent.EXTRA_STREAM, uri)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                startActivity(Intent.createChooser(intent, "Share via"))

            } catch (e: Exception) {
                Toast.makeText(this, "Error sharing image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 🔹 Multiline text helper
    private fun drawMultilineText(
        canvas: Canvas,
        text: String,
        x: Float,
        y: Float,
        paint: Paint,
        maxWidth: Float
    ) {
        val words = text.split(" ")
        var line = ""
        var yPos = y

        for (word in words) {
            val testLine = if (line.isEmpty()) word else "$line $word"
            if (paint.measureText(testLine) > maxWidth) {
                canvas.drawText(line, x, yPos, paint)
                line = word
                yPos += paint.textSize + 20
            } else {
                line = testLine
            }
        }
        canvas.drawText(line, x, yPos, paint)
    }
}