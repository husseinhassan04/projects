package com.example.noteapp3

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MlFragment : Fragment() {

    private lateinit var processBtn: Button
    private lateinit var addImageBtn: ImageButton
    private lateinit var image: ImageView
    private lateinit var imageBitmap: Bitmap
    private lateinit var textResult: TextView

    private val REQUEST_IMAGE_CAPTURE = 1
    private val FILE_SELECT_CODE = 2
    private val REQUEST_CAMERA_PERMISSION = 3

    private var currentPhotoPath: String? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ml, container, false)

        processBtn = view.findViewById(R.id.process_btn)
        addImageBtn = view.findViewById(R.id.add_image)
        image = view.findViewById(R.id.image)
        textResult = view.findViewById(R.id.text)

        addImageBtn.setOnClickListener {
            val bottomSheet = UserImageBottomSheet()
            bottomSheet.setOnImageOptionClickListener(object : UserImageBottomSheet.OnImageOptionClickListener {
                override fun onViewImage() {
                    if(image.getDrawable() != null || (image.getDrawable() != null)) {
                        PhotoPreviewActivity.imageBitmap = imageBitmap


                        val intent = Intent(requireContext(), PhotoPreviewActivity::class.java)
                        startActivity(intent)
                    }
                    else{
                        Toast.makeText(requireContext(),"Select image first",Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onChangeImageFromCamera() {
                    dispatchTakePictureIntent()
                }

                override fun onChangeImageFromStorage() {
                    selectFile()
                }
            })
            bottomSheet.show(requireActivity().supportFragmentManager, bottomSheet.tag)
        }

        processBtn.setOnClickListener {
            if(image.getDrawable() == null || (image.getDrawable() == null)){
                Toast.makeText(requireContext(),"Select an Image",Toast.LENGTH_SHORT).show()
        }
            else{
            recognizeText(imageBitmap)
        }
        }

        return view
    }

    private fun recognizeText(bitmap: Bitmap) {
        val inputImage = InputImage.fromBitmap(bitmap, 0)

        // Create an instance of TextRecognizer with options
        val recognizer: TextRecognizer = TextRecognition.getClient(TextRecognizerOptions.Builder().build())

        recognizer.process(inputImage)
            .addOnSuccessListener { visionText ->
                val resultText = visionText.text
                textResult.text = resultText
            }
            .addOnFailureListener { e ->
                textResult.text = "Text recognition failed: ${e.message}"
            }
    }

    private fun selectFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(intent, FILE_SELECT_CODE)
    }

    private fun dispatchTakePictureIntent() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // If the camera permission is not granted, request the permission
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CAMERA_PERMISSION
            )
        } else {
            // If permission is already granted, proceed with taking the picture
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                takePictureIntent.resolveActivity(requireActivity().packageManager)?.also {
                    // Create the File where the photo should go
                    val photoFile: File? = try {
                        createImageFile()
                    } catch (ex: IOException) {
                        // Error occurred while creating the File
                        ex.printStackTrace()
                        null
                    }
                    // Continue only if the File was successfully created
                    photoFile?.also {
                        val photoURI: Uri = FileProvider.getUriForFile(
                            requireContext(),
                            "${requireActivity().packageName}.fileprovider",
                            it
                        )
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                    }
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_SELECT_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                try {
                    val inputStream = context?.contentResolver?.openInputStream(uri)
                    val selectedBitmap = BitmapFactory.decodeStream(inputStream)
                    image.setImageBitmap(selectedBitmap)
                    imageBitmap = selectedBitmap
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
            else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {

            currentPhotoPath?.let {
                val imageFile = File(it)
                val selectedBitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
                image.setImageBitmap(selectedBitmap)
                imageBitmap = selectedBitmap
            }
        }

    }
}
