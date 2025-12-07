package com.example.doan_ai

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var imageView: ImageView
    private lateinit var imageCapture: ImageCapture
    private lateinit var progressBar: ProgressBar

    private lateinit var tflite: Interpreter
    private lateinit var classNames: List<String>
    private val imgSize = 224

    private var detectedPlantId: String? = null
    private var isImageClassified = false

    private val IMAGE_MEAN = 0.0f
    private val IMAGE_STD = 255.0f
    //
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    companion object {
        const val RESULT_ID_KEY = "result_id"
        private val REQUIRED_PERMISSIONS = arrayOf(android.Manifest.permission.CAMERA)
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) startCamera()
            else Toast.makeText(this, "Quyền camera bị từ chối", Toast.LENGTH_SHORT).show()
        }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri = result.data?.data
            imageUri?.let { uri ->
                try {
                    val inputStream = contentResolver.openInputStream(uri)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream?.close()

                    if (bitmap != null) {
                        isImageClassified = true
                        imageView.setImageBitmap(bitmap)
                        imageView.visibility = View.VISIBLE
                        previewView.visibility = View.GONE
                        runClassification(bitmap)
                    } else {
                        Toast.makeText(this, "Không thể tải ảnh.", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e("Gallery", "Lỗi khi tải ảnh: ${e.message}", e)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        previewView = findViewById(R.id.previewView)
        imageView = findViewById(R.id.imageView)
        progressBar = findViewById(R.id.progressBar)
        //
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        val btnCapture = findViewById<ImageButton>(R.id.btnCapture)
        val btnGallery = findViewById<ImageButton>(R.id.btnGallery)

        val btnUser = findViewById<ImageButton>(R.id.btnUser)

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }

        try {
            classNames = loadLabels("label.txt")
            tflite = Interpreter(loadModelFile("model_final.tflite"))
            Log.d("TFLite", "Model đã sẵn sàng. Số nhãn: ${classNames.size}")
        } catch (e: Exception) {
            Log.e("TFLite", "Lỗi khởi tạo model: ${e.message}", e)
            Toast.makeText(this, "Không thể tải model.", Toast.LENGTH_LONG).show()
        }

        btnCapture.setOnClickListener { takePhoto() }
        btnGallery.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            galleryLauncher.launch(intent)
        }
        btnUser.setOnClickListener {
            navigateToUserActivity()
        }
    }
    private fun navigateToUserActivity() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            imageCapture = ImageCapture.Builder().build()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (exc: Exception) {
                Log.e("Camera", "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        imageCapture.takePicture(
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(imageProxy: androidx.camera.core.ImageProxy) {
                    val bitmap = imageProxy.toBitmap()
                    isImageClassified = true
                    imageView.setImageBitmap(bitmap)
                    imageView.visibility = View.VISIBLE
                    previewView.visibility = View.GONE
                    runClassification(bitmap)
                    imageProxy.close()
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("Camera", "Lỗi chụp ảnh: ${exception.message}", exception)
                }
            }
        )
    }
    private fun runClassification(bitmap: Bitmap) {
        progressBar.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.IO).launch {
            val result = classifyImage(bitmap)
            withContext(Dispatchers.Main) {
                progressBar.visibility = View.GONE
                showResultDialog(result)
            }
        }
    }
    private fun classifyImage(bitmap: Bitmap): String {
        val tensorImage = TensorImage.fromBitmap(bitmap)
        val processor = org.tensorflow.lite.support.image.ImageProcessor.Builder()
            .add(ResizeOp(imgSize, imgSize, ResizeOp.ResizeMethod.BILINEAR))
            .add(NormalizeOp(IMAGE_MEAN, IMAGE_STD))
            .build()

        val processedImage = processor.process(tensorImage)
        val inputBuffer = processedImage.buffer

        val outputBuffer = TensorBuffer.createFixedSize(
            intArrayOf(1, classNames.size),
            org.tensorflow.lite.DataType.FLOAT32
        )

        tflite.run(inputBuffer, outputBuffer.buffer.rewind())

        val predictions = outputBuffer.floatArray
        val maxIndex = predictions.indices.maxByOrNull { predictions[it] } ?: -1
        return if (maxIndex >= 0) classNames[maxIndex] else "Không xác định"
    }

    private fun showResultDialog(resultLabel: String) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_result)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val tvResult = dialog.findViewById<TextView>(R.id.tvPopupResult)
        val btnDetails = dialog.findViewById<Button>(R.id.btnPopupDetails)
        val btnClose = dialog.findViewById<Button>(R.id.btnPopupClose)

        //Gán giá trị
        detectedPlantId = resultLabel
        tvResult.text = resultLabel
        // xử lý
        if (resultLabel == "không xác định") {
            btnDetails.visibility = View.GONE
        } else {
            saveDiagosisHistory(resultLabel)
        }

        btnDetails.setOnClickListener {
            isImageClassified = false
            navigateToResultActivity()
            dialog.dismiss()
        }

        btnClose.setOnClickListener {
            isImageClassified = false
            imageView.visibility = View.GONE
            previewView.visibility = View.VISIBLE
            if (allPermissionsGranted()) {
                startCamera()
            }
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onResume() {
        super.onResume()
        if (isImageClassified) {
            return
        }
        if (allPermissionsGranted()) {
            startCamera()
        }

        imageView.visibility = View.GONE
        previewView.visibility = View.VISIBLE
    }

    private fun navigateToResultActivity() {
        detectedPlantId?.let {
            val intent = Intent(this, ResultActivity::class.java).apply {
                // Chuyển ID bệnh đi kèm với Intent
                putExtra(RESULT_ID_KEY, it)
            }
            startActivity(intent)
        }
    }
    //
    private fun saveDiagosisHistory(diseaseId: String) {
        val user = auth.currentUser
        if (user == null || diseaseId == "Không xác định") {
            Log.e("FirebaseDB", "Không thể lưu lịch sử: Người dùng chưa đăng nhập hoặc ID không hợp lệ.")
            return
        }

        val uid = user.uid
        val historyRef = database.getReference("history").child(uid)
        val timestamp = System.currentTimeMillis()

        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val dateString = dateFormat.format(Date(timestamp))
        val historyEntry = HashMap<String, Any>()
        historyEntry["diseaseId"] = diseaseId
        historyEntry["date"] = dateString

        historyRef.child(timestamp.toString()).setValue(historyEntry)
            .addOnSuccessListener {
                Log.d("FirebaseDB", "Lưu lịch sử thành công cho UID: $uid")
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseDB", "Lỗi lưu lịch sử: ${e.message}", e)
                Toast.makeText(this, "Lỗi lưu lịch sử chẩn đoán.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadModelFile(modelFileName: String): MappedByteBuffer {
        val fileDescriptor = assets.openFd(modelFileName)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun loadLabels(fileName: String): List<String> {
        val labels = mutableListOf<String>()
        assets.open(fileName).bufferedReader().useLines { lines ->
            lines.forEach { line ->
                if (line.isNotBlank()) labels.add(line.trim())
            }
        }
        return labels
    }
}
