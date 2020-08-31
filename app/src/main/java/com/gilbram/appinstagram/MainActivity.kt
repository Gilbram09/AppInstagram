package com.gilbram.appinstagram

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.gilbram.appinstagram.Fragment.HomeFragment
import com.gilbram.appinstagram.Fragment.NotificationFragment
import com.gilbram.appinstagram.Fragment.ProfileFragment
import com.gilbram.appinstagram.Fragment.SearchFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private val onNavigationItemSelectedListerner = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when(item.itemId){
            R.id.nav_home ->{
                moveToFragment(HomeFragment())
                return@OnNavigationItemSelectedListener true
            }
            R.id.nav_search->{
                moveToFragment(SearchFragment())
                return@OnNavigationItemSelectedListener true
            }
            R.id.nav_add_post ->{
                item.isChecked= false
                startActivity(Intent(this,TambahPostActivity::class.java))
               return@OnNavigationItemSelectedListener true
            }
            R.id.nav_notification ->{
                moveToFragment(NotificationFragment())
                return@OnNavigationItemSelectedListener true
            }
            R.id.nav_profile ->{
                moveToFragment(ProfileFragment())
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navView: BottomNavigationView= findViewById(R.id.nav_view)
        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListerner)

        //supaya home menjadi default ketika aplikasi pertama di jalankan
        moveToFragment(HomeFragment())
    }
//function untuk pindah antar fragment
    private fun moveToFragment(fragment: Fragment) {
        val fragmentTrans = supportFragmentManager.beginTransaction()
        fragmentTrans.replace(R.id.fragment_container, fragment)
        fragmentTrans.commit()

    }

}