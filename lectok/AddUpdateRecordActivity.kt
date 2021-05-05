package com.lectok

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_add_update_record.*
import kotlinx.android.synthetic.main.activity_record_detail.*

class AddUpdateRecordActivity : AppCompatActivity() {

    //permission constants
    private val CAMERA_REQUEST_CODE = 100;
    private val STORAGE_REQUEST_CODE = 101;
    //image pick constants
    private val IMAGE_PICK_CAMERA_CODE = 102;
    private val IMAGE_PICK_GALLERY_CODE = 103;
    //arrays of permissions
    private lateinit var cameraPermissions:Array<String> //camera and storage
    private lateinit var storagePermissions:Array<String> //only storage

    //variables that will contain data to save in database
    private var id:String? = ""
    private var title:String? = ""
    private var author:String? = ""
    private var imageUri: Uri? = null
    private var totalPages: String? = ""
    private var readPages:String? = ""
    private var addedTime:String?=""
    private var updatedTime:String?=""


    private var isEditMode = false



    //action bar
    private var actionBar: ActionBar? = null;

    lateinit var dbHelper:DbHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_update_record)

        actionBar = supportActionBar
        //title of actionBar
        actionBar!!.title = "Add Record"
        //back button in actionbar
        actionBar!!.setDisplayHomeAsUpEnabled(true)
        actionBar!!.setDisplayShowHomeEnabled(true)

        //get data from intent
        val intent = intent
        isEditMode = intent.getBooleanExtra("isEditMode", false)

        if(isEditMode){
            //editing data, came from adapter
            actionBar!!.title = "Update Record"

            id = intent.getStringExtra("ID")
            title = intent.getStringExtra("TITLE")
            author = intent.getStringExtra("AUTHOR")
            imageUri = Uri.parse(intent.getStringExtra("IMAGE"))
            totalPages = intent.getStringExtra("TOTALPAGES")
            readPages = intent.getStringExtra("READPAGES")
            addedTime = intent.getStringExtra("ADDED")
            updatedTime = intent.getStringExtra("UPDATED")

            // set data to views --> activity_add_update_record.xml
            /*if use didn't attached image while saving record then
            * imageUri value will be null, so set default image*/
            if (imageUri.toString() == "null"){
                //no image
                iv_cover.setImageResource(R.drawable.ic_book)
            }
            else{
                //have image
                iv_cover.setImageURI(imageUri)
            }

            //data goes to activity_add_update_record.xml
            et_title.setText(title)
            et_author.setText(author)
            et_pages_total.setText(totalPages)
            et_pages_read.setText(readPages)
        }
        else{
            //adding new data, came here from MainActivity
            actionBar!!.title = "Add Record"
        }


        //init dbhelper class
        dbHelper = DbHelper(this)

        //init permissions arrays
        cameraPermissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        storagePermissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

        //click imageview to pick picture
        iv_cover.setOnClickListener{
            //show image pick dialog
            imagePickDialog();
        }

        //click savebtn to save record
        saveBtn.setOnClickListener {
            inputData()
        }
    }

    private fun inputData() {
        //get data from XML file activity_add_update_record.xml
        title = ""+et_title.text.toString().trim()
        author = ""+et_author.text.toString().trim()
        totalPages = ""+et_pages_total.text.toString().trim()
        readPages = ""+et_pages_read.text.toString().trim()

        if (isEditMode){
            //editing
            val timeStamp = "${System.currentTimeMillis()}"
            dbHelper?.updateRecord(
                "$id",
                "$title",
                "$author",
                "$imageUri",
                "$totalPages",
                "$readPages",
                "$addedTime",
                "$timeStamp"
            )

            Toast.makeText(this,"Updated...",Toast.LENGTH_SHORT).show()
        }
        else{
            //adding new

            //save data to db
            val timestamp = System.currentTimeMillis()
            val id = dbHelper.insertRecord(
                ""+title,
                ""+author,
                ""+imageUri,
                ""+totalPages,
                ""+readPages,
                "$timestamp",
                "$timestamp"
            )
            Toast.makeText(this,"Record Added against ID $id", Toast.LENGTH_SHORT).show()
        }



    }

    private fun imagePickDialog() {
        //options to display in dialog
        val options = arrayOf("Camera","Gallery")
        //dialog
        val builder:AlertDialog.Builder = AlertDialog.Builder(this)
        //title
        builder.setTitle("Pick Image From")
        //set items/options
        builder.setItems(options){dialog, which ->
            //handle item clicks
            if (which==0){
                //camera clicked
                if (!checkCameraPermissions()){
                    //permission not granted
                    requestCameraPermission()
                }
                else{
                    //permission already granted
                    pickFromCamera()
                }
            }
            else{
                //gallery clicked
                if (!checkStoragePermission()){
                    //permission not granted
                    requestStoragePermission()
                }
                else{
                    //permission already granted
                    pickFromGallery()
                }
            }
        }
        //show dialog
        builder.show()
    }

    private fun pickFromGallery(){
        //pick image from gallery using Intent
        val galleryIntent = Intent(Intent.ACTION_PICK)
        galleryIntent.type = "image/+" //only image to be picked
        startActivityForResult(
            galleryIntent, IMAGE_PICK_GALLERY_CODE
        )
    }

    private fun requestStoragePermission(){
        //request the storage permission
        ActivityCompat.requestPermissions(
            this,
            storagePermissions,
            STORAGE_REQUEST_CODE
        )
    }

    private fun checkStoragePermission(): Boolean {
        //check if storage permission is enabled or not
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun pickFromCamera() {
        //pick image from camera using Intent
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE,"Image Title")
        values.put(MediaStore.Images.Media.DESCRIPTION,"Image Description")
        //put image url
        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        //intent to open camera
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        startActivityForResult(
            cameraIntent,
            IMAGE_PICK_CAMERA_CODE
        )
    }

    private fun requestCameraPermission(){
        //request the camera permission
        ActivityCompat.requestPermissions(
            this,
            cameraPermissions,
            CAMERA_REQUEST_CODE
        )
    }

    private fun checkCameraPermissions(): Boolean {
        //check if camera permissions (camera and storage) are enabled or not
        val results = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

        val results1 = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

        return results && results1

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed() //go back to previous activity
        return super.onSupportNavigateUp()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            CAMERA_REQUEST_CODE->{
                if (grantResults.isNotEmpty()){
                    //if allowed returns true, otherwise false
                    val cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    val storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED

                    if (cameraAccepted && storageAccepted){
                        pickFromCamera()
                    }
                    else{
                        Toast.makeText(this,"Camera and Storage permissions are required", Toast.LENGTH_SHORT).show()
                    }

                }
            }
            STORAGE_REQUEST_CODE->{
                if(grantResults.isNotEmpty()){
                    //if allowed returns true, otherwise false
                    val storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED

                    if(storageAccepted){
                        pickFromGallery()
                    }
                    else{
                        Toast.makeText(this, "Storage permission is required", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //image picked from camera or gallery will be received here
        if(resultCode == Activity.RESULT_OK){
            //image is picked
            if (requestCode == IMAGE_PICK_GALLERY_CODE){
                //picked from gallery
                //crop image
                CropImage.activity(data!!.data)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this)
            }
            else if (requestCode == IMAGE_PICK_CAMERA_CODE){
                //picked from camera
                //crop image
                CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this)
            }
            else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
                //cropped image received
                val result = CropImage.getActivityResult(data)
                if (resultCode == Activity.RESULT_OK){
                    val resultUri = result.uri
                    imageUri = resultUri
                    //set image
                    iv_cover.setImageURI(resultUri)
                }
                else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                    //error
                    val error = result.error
                    Toast.makeText(this,""+error, Toast.LENGTH_SHORT).show()
                }
            }

        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}