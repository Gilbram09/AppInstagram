package com.gilbram.appinstagram

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_register.*
import java.util.HashMap

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        btn_signin_link.setOnClickListener {
            startActivity(Intent(this@RegisterActivity,LoginActivity::class.java))
        }
        btn_register.setOnClickListener {
            createAccount()
        }
    }

    private fun createAccount() {
        //untuk memasukan text ke edit text an datanya ke dalam string
        val fullName = fullname_register.text.toString()
        val userName = username_register.text.toString()
        val email = email_register.text.toString()
        val password= password_register.text.toString()

        when{  //sebelum
            TextUtils.isEmpty(fullName) -> Toast.makeText(this,"FullName Is Required"
                ,Toast.LENGTH_SHORT).show()
            TextUtils.isEmpty(userName) -> Toast.makeText(this,"UserName Is Required"
                ,Toast.LENGTH_SHORT).show()
            TextUtils.isEmpty(email) -> Toast.makeText(this,"Email Is Required"
                ,Toast.LENGTH_SHORT).show()
            TextUtils.isEmpty(password) -> Toast.makeText(this,"Password Is Required"
                ,Toast.LENGTH_SHORT).show()

            else ->{  //sesudah
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Register")
                progressDialog.setMessage("Please Wait...")
                progressDialog.setCanceledOnTouchOutside(false) //membuat loading tetap berjalan
                progressDialog.show()

                val mAuth : FirebaseAuth = FirebaseAuth.getInstance()
                mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful){
                            saveUserInfo(fullName, userName, email, progressDialog)
                        }else{
                            val messege= task.exception!!.toString()
                            Toast.makeText(this, "error: $messege", Toast.LENGTH_SHORT).show()
                            mAuth.signOut()
                            progressDialog.dismiss()
                        }
                    }
            }
        }
    }

    private fun saveUserInfo(
        fullName: String,
        userName: String,
        email: String,
        progressDialog: ProgressDialog
    ) {
        val currentUserID = FirebaseAuth.getInstance().currentUser!!.uid
        val usersRef : DatabaseReference = FirebaseDatabase.getInstance().reference
            .child("Users")
        val userMap = HashMap<String, Any>()
        userMap["uid"] = currentUserID
        userMap["fullname"] = fullName.toLowerCase()
        userMap["username"] = userName.toLowerCase()
        userMap["email"] = email
//default bio dan photo profile
        userMap["bio"] = "hey im student at IDN Boarding School"
        userMap["image"] = "https://firebasestorage.googleapis.com/v0/b/sosial-media-5718f.appspot.com/o/Default%20Image%2Fprofile.png?alt=media&token=ede2230d-a454-4f99-b46c-e67c9c4478e4"


        usersRef.child(currentUserID).setValue(userMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    progressDialog.dismiss()
                    Toast.makeText(this@RegisterActivity, "Account Sudah Dibuat", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }else {
                    val message = task.exception!!.toString()
                    Toast.makeText(this, "Error: $message", Toast.LENGTH_SHORT).show()
                    FirebaseAuth.getInstance().signOut()
                    progressDialog.dismiss()
                }
            }
    }
}

