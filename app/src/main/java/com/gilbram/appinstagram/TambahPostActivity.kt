package com.gilbram.appinstagram

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_tambah_post.*
import java.util.HashMap

class TambahPostActivity : AppCompatActivity() {
    private var myUrl = ""
    private var imageuri: Uri? = null
    private var storagePostPictureRef: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_post)

        storagePostPictureRef = FirebaseStorage.getInstance().reference.child("Post Picture")
        btn_new_post_btn.setOnClickListener{uploadImage()}

        CropImage.activity()
            .setAspectRatio(2,1)
            .start(this)
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode== Activity.RESULT_OK
//            && data !=null){
//            val result= CropImage.getActivityResult(data)
//            imageuri= result.uri
//            image_post.setImageURI(imageuri)

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK
            && data != null) {
            val result = CropImage.getActivityResult(data)
            imageuri = result.uri
            image_post.setImageURI(imageuri)

        }
    }

    private fun uploadImage() {
        when{
            imageuri == null ->Toast.makeText(this,"Please select Image",Toast.LENGTH_SHORT).show()
            TextUtils.isEmpty(deskripsi_post.text.toString()) -> Toast.makeText(this,"Please Write Caption",Toast.LENGTH_SHORT).show()

            else-> {
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Add New Post")
                progressDialog.setMessage("Please Wait, we are adding your picture...")
                progressDialog.show()

                val fileRef = storagePostPictureRef!!.child(System.currentTimeMillis().toString()+"jpg")

                val uploadTask: StorageTask<*>
                uploadTask = fileRef.putFile(imageuri!!)
                uploadTask.continueWithTask(Continuation <UploadTask.TaskSnapshot,Task<Uri>>{ task ->
                    if (!task.isSuccessful) {
                        task.exception.let {
                            throw  it!!
                            progressDialog.dismiss()
                        }
                    }

                    return@Continuation fileRef.downloadUrl
                }).addOnCompleteListener(OnCompleteListener<Uri>{ task ->
                    if (task.isSuccessful){
                        val mydownloadUrl= task .result
                        myUrl = mydownloadUrl.toString()
                        val ref = FirebaseDatabase.getInstance().reference.child("Posts")
                        val postId= ref.push().key

                        val posMap = HashMap<String,Any>()
                        posMap["postid"] = postId!!
                        posMap["description"] = deskripsi_post.text.toString().toLowerCase()
                        posMap["publisher"] = FirebaseAuth.getInstance().currentUser!!.uid
                        posMap["postimage"] = myUrl

                        ref.child(postId).updateChildren(posMap)
                        Toast.makeText(this,"Post sukses..",Toast.LENGTH_SHORT).show()

                        val intent = Intent(this,MainActivity::class.java)
                        startActivity(intent)
                        finish()
                        progressDialog.dismiss()
                    }else{
                        progressDialog.dismiss()
                    }
                })
            }
        }
    }
}