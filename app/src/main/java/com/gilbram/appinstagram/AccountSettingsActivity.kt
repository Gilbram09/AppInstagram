package com.gilbram.appinstagram

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.gilbram.appinstagram.Fragment.ProfileFragment
import com.gilbram.appinstagram.Model.User
import com.google.android.gms.auth.api.signin.internal.Storage
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_account_settings.*

class AccountSettingsActivity : AppCompatActivity() {
    private lateinit var firebaseUser : FirebaseUser
    private var cekInProfile = ""
    private var myUrl = ""
    private var imageUri :Uri? = null
    private var storageProfilePictureRef: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_settings)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        storageProfilePictureRef = FirebaseStorage.getInstance().reference.child("Profile Picture")

        logout_btn_setprofile.setOnClickListener{
            FirebaseAuth.getInstance().signOut()

            val intent = Intent(this@AccountSettingsActivity,LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        chage_settimage_text.setOnClickListener {
            cekInProfile = "clicked"

            CropImage.activity()
                .setAspectRatio(1,1)
                .start(this@AccountSettingsActivity)
        }

        save_info_profile_btn.setOnClickListener{
            if (cekInProfile == "clicked"){

                uploadImageAndUpdateInfo()

            }else{
                updateUserInfoOnly()
            }
        }


        close_profile_button.setOnClickListener {
            val intent = Intent(this@AccountSettingsActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        userInfo()
    }

    private fun uploadImageAndUpdateInfo() {
        when{
            imageUri == null -> Toast.makeText(this,"Please Select Image",Toast.LENGTH_SHORT).show()
            TextUtils.isEmpty(fullname_setprofile_edittext.text.toString()) -> {
                Toast.makeText(this, "Please Don't be empty", Toast.LENGTH_SHORT).show()
            }
            username_setprofile_edittext.text.toString() == "" -> {
                Toast.makeText(this, "Please Don't be empty", Toast.LENGTH_SHORT).show()
            }
            bio_setprofile_edittext.text.toString() == "" -> {
                Toast.makeText(this, "Please Don't be empty", Toast.LENGTH_SHORT).show()
            }
            else->{
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Account Setting")
                progressDialog.setMessage("Upload Profile...")
                progressDialog.show()

                val fileref = storageProfilePictureRef!!.child(firebaseUser!!.uid+"jpg")

                var uploadTask: StorageTask<*>
                uploadTask = fileref.putFile(imageUri!!)
                uploadTask.continueWithTask(Continuation <UploadTask.TaskSnapshot,Task<Uri>>{task ->
                    if (!task.isSuccessful) {
                        task.exception.let {
                            throw it!!
                            progressDialog.dismiss()

                        }
                    }
                    return@Continuation fileref.downloadUrl
                }).addOnCompleteListener(OnCompleteListener<Uri> { task ->
                    if (task.isSuccessful){
                        val downloadUrl = task.result
                        myUrl = downloadUrl.toString()

                        val ref = FirebaseDatabase.getInstance().reference.child("Users")

                        val userMap = HashMap<String ,Any>()
                        userMap["fullname"]= fullname_setprofile_edittext.text.toString().toLowerCase()
                        userMap["username"]= username_setprofile_edittext.text.toString().toLowerCase()
                        userMap["bio"]= bio_setprofile_edittext.text.toString().toLowerCase()
                        userMap["image"] = myUrl


                        ref.child(firebaseUser.uid).updateChildren(userMap)
                        Toast.makeText(this,"Profile Berhasil Di Update",Toast.LENGTH_SHORT).show()

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE&& resultCode == Activity.RESULT_OK
            && data != null){
            val result = CropImage.getActivityResult(data)
            imageUri = result.uri
            setprofile_image_view.setImageURI(imageUri)
        }
    }
    private fun userInfo() {
        val userRef = FirebaseDatabase.getInstance().getReference()
            .child("Users").child(firebaseUser.uid)

        userRef.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val user = p0.getValue<User>(User::class.java)

                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile)
                        .into(setprofile_image_view)
                    username_setprofile_edittext.setText(user.getUsername())
                    fullname_setprofile_edittext.setText(user.getFullname())
                    bio_setprofile_edittext.setText(user.getBio())

                }
            }
        })
    }

    private fun updateUserInfoOnly() {
        when {
            TextUtils.isEmpty(fullname_setprofile_edittext.text.toString()) -> {
                Toast.makeText(this, "Please Don't be empty", Toast.LENGTH_SHORT).show()
            }
            username_setprofile_edittext.text.toString() == "" -> {
                Toast.makeText(this, "Please Don't be empty", Toast.LENGTH_SHORT).show()
            }
            bio_setprofile_edittext.text.toString() == "" -> {
                Toast.makeText(this, "Please Don't be empty", Toast.LENGTH_SHORT).show()
            }
            else ->{
                val usersRef = FirebaseDatabase.getInstance().reference
                    .child("Users")

                val userMap = HashMap<String, Any>()
                userMap["fullname"]= fullname_setprofile_edittext.text.toString().toLowerCase()
                userMap["username"]= username_setprofile_edittext.text.toString().toLowerCase()
                userMap["bio"]= bio_setprofile_edittext.text.toString().toLowerCase()

                usersRef.child(firebaseUser.uid).updateChildren(userMap)

                Toast.makeText(this,"Profile berhasil di update",Toast.LENGTH_SHORT).show()
                val intent = Intent(this@AccountSettingsActivity,MainActivity::class.java)
                startActivity(intent)
                finish()

            }
        }
    }
}