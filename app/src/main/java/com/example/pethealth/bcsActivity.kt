package com.example.pethealth

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.example.pethealth.databinding.ActivityBcsBinding
import com.example.pethealth.fragments.PetAccount
import com.example.pethealth.ml.MobilenetV110224Quant
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.text.SimpleDateFormat
import java.util.*


class bcsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBcsBinding
    private val CAMERA_REQUEST_CODE = 1
    private val GALLERY_REQUEST_CODE = 2

    lateinit var make_prediction : Button
    lateinit var text_view : TextView
    lateinit var bitmap: Bitmap
    lateinit var img_view: ImageView
    lateinit var btnGallery: Button
    lateinit var petname: EditText
    var uidList = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBcsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        make_prediction = findViewById(R.id.button2)
        text_view = findViewById(R.id.textView)
        img_view = findViewById(R.id.imageView)
        btnGallery = findViewById(R.id.btnGallery)

        val labels = application.assets.open("labels.txt").bufferedReader().use { it.readText() }.split("\n")

        binding.btnGallery.setOnClickListener {
            Log.d("mssg", "button pressed")
            galleryCheckPermission()
        }

        

        make_prediction.setOnClickListener(View.OnClickListener {

            try {
                var resized = Bitmap.createScaledBitmap(bitmap, 224, 224, true)
                val model = MobilenetV110224Quant.newInstance(this)

                var tbuffer = TensorImage.fromBitmap(resized)
                var byteBuffer = tbuffer.buffer

// Creates inputs for reference.
                val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.UINT8)
                inputFeature0.loadBuffer(byteBuffer)



// Runs model inference and gets result.
                val outputs = model.process(inputFeature0)
                val outputFeature0 = outputs.outputFeature0AsTensorBuffer

                var max = getMax(outputFeature0.floatArray)

                text_view.setText(labels[max])
                var result = labels[max]
// Releases model resources if no longer used.
                // BCS 결과값 Firebase에 저장
                binding.btnResult.setOnClickListener {
                    val database : FirebaseDatabase = FirebaseDatabase.getInstance()
                    val user = FirebaseAuth.getInstance().currentUser
                    val the_uid = user!!.uid
                    val pet = database.getReference(the_uid)
                    val the_pid = pet!!.key

                    val intent = intent
                    val position = intent.getIntExtra("position", 0)
                    val now = System.currentTimeMillis()
                    val date = Date(now)
                    val sdf = SimpleDateFormat("yyyy-MM-dd")
                    val getTime = sdf.format(date)

                    val myRef : DatabaseReference = database.getReference(the_pid + "/PetAccount/" + uidList.get(position)).child("bcs")
                    val myRef2 : DatabaseReference = database.getReference(the_uid + "/PetAccount/" + uidList.get(position) + "/BcsReport")

                    val group = bcsgroup()
                    group.bcs = result
                    group.date = getTime

                    //image 라는 테이블에 json 형태로 담긴다.
                    //database.getReference().child("Profile").setValue(imageDTO);
                    //  .push()  :  데이터가 쌓인다.

                    myRef.setValue(result)
                    myRef2.push().setValue(group)
                    if(result != null) {
                        Toast.makeText(this, "결과가 저장되었습니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "측정결과가 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
                model.close()
            } catch (e:UninitializedPropertyAccessException) {
                Toast.makeText(this, "사진을 선택해주세요", Toast.LENGTH_SHORT).show()

            }

        })

        binding.btnCamera.setOnClickListener {
            cameraCheckPermission()
        }

        //BCS 결과값 Firebase 저장
        val user = FirebaseAuth.getInstance().currentUser
        val the_uid = user!!.uid
        val database : FirebaseDatabase = FirebaseDatabase.getInstance()
        database.getReference().child(the_uid).child("PetAccount")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {  //변화된 값이 DataSnapshot 으로 넘어온다.
                    //데이터가 쌓이기 때문에  clear()
                    uidList.clear()
                    for (ds in dataSnapshot.children)  //여러 값을 불러와 하나씩
                    {
                        val petAccount = ds.getValue(PetAccount::class.java)
                        val uidKey = ds.key
                        if (uidKey != null) {
                            uidList.add(uidKey)
                        }
                    }
                    // petdataAdapter.notifyDataSetChanged();
                }

                override fun onCancelled(error: DatabaseError) {}
            })

        //when you click on the image
        binding.imageView.setOnClickListener {
            val pictureDialog = AlertDialog.Builder(this)
            pictureDialog.setTitle("Select Action")
            val pictureDialogItem = arrayOf("Select photo from Gallery",
                "Capture photo from Camera")
            pictureDialog.setItems(pictureDialogItem) { dialog, which ->

                when (which) {
                    0 -> gallery()
                    1 -> camera()
                }
            }

            pictureDialog.show()
        }

    }
    fun getMax(arr:FloatArray) : Int{
        var ind = 0;
        var min = 0.0f;

        for(i in 0..4)
        {
            if(arr[i] > min)
            {
                min = arr[i]
                ind = i;
            }
        }
        return ind
    }


    private fun galleryCheckPermission() {

        Dexter.withContext(this).withPermission(
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        ).withListener(object : PermissionListener {
            override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                gallery()
            }

            override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                Toast.makeText(
                    this@bcsActivity,
                    "You have denied the storage permission to select image",
                    Toast.LENGTH_SHORT
                ).show()
                showRotationalDialogForPermission()
            }

            override fun onPermissionRationaleShouldBeShown(
                p0: PermissionRequest?, p1: PermissionToken?) {
                showRotationalDialogForPermission()
            }
        }).onSameThread().check()
    }

    private fun gallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }


    private fun cameraCheckPermission() {

        Dexter.withContext(this)
            .withPermissions(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.CAMERA).withListener(

                object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        report?.let {

                            if (report.areAllPermissionsGranted()) {
                                camera()
                            }

                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        p0: MutableList<PermissionRequest>?,
                        p1: PermissionToken?) {
                        showRotationalDialogForPermission()
                    }

                }
            ).onSameThread().check()
    }


    private fun camera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        if (resultCode == Activity.RESULT_OK) {

            when (requestCode) {

                CAMERA_REQUEST_CODE -> {

                    bitmap = data?.extras?.get("data") as Bitmap

                    //we are using coroutine image loader (coil)
                    binding.imageView.load(bitmap) {
                        crossfade(true)
                        crossfade(1000)
                        //transformations(CircleCropTransformation())
                    }
                }

                GALLERY_REQUEST_CODE -> {

                    /*binding.imageView.load(data?.data) {
                        crossfade(true)
                        crossfade(1000)
                        // transformations(CircleCropTransformation())
                    }*/
                    img_view.setImageURI(data?.data)

                    var uri : Uri ?= data?.data
                    bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
                }
            }

        }

    }


    private fun showRotationalDialogForPermission() {
        AlertDialog.Builder(this)
            .setMessage("It looks like you have turned off permissions"
                    + "required for this feature. It can be enable under App settings!!!")

            .setPositiveButton("Go TO SETTINGS") { _, _ ->

                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)

                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }

            .setNegativeButton("CANCEL") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }




}