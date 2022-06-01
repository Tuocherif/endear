package com.imot.endear

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import com.imot.endear.databinding.ActivityHomeBinding
import com.imot.endear.databinding.NavHeaderHomeBinding
import com.imot.endear.utils.Common
import pl.droidsonroids.gif.GifImageView

class HomeActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityHomeBinding
    //private lateinit var navHeaderHomeBinding: NavHeaderHomeBinding
    private lateinit var user_email: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //navHeaderHomeBinding = NavHeaderHomeBinding.inflate(layoutInflater)




        setSupportActionBar(binding.appBarHome.toolbar)

        val alarm_fix = findViewById<ImageView>(R.id.alarm_fix)
        val alarm = findViewById<GifImageView>(R.id.alarm)
        //user_email= findViewById(R.id.user_email)




        //binding.appBarHome.
        alarm_fix.setOnClickListener { view ->
            alarm_fix.visibility = View.GONE
            alarm.visibility = View.VISIBLE
            Snackbar.make(view, "!!! Alerte d'urgence envoyée !!!", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
            Intent(this, AllPeopleActivity::class.java).also {
               startActivity(it)
           }
        }


        alarm.setOnClickListener { view ->
            alarm_fix.visibility = View.VISIBLE
            alarm.visibility = View.GONE
            Snackbar.make(view, "!!! Fin de l'alerte !!!", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
            Intent(this, AllPeopleActivity::class.java).also {
                startActivity(it)
            }
        }

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_home)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(setOf(
            R.id.nav_home, R.id.nav_find_people, R.id.nav_friend_request, R.id.nav_friend_list,R.id.nav_sign_out), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        //val headerView = navView.getHeaderView(0)
        //user_email.text = Common.loggedUser!!.email!!

    }// end onCreate


//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        menuInflater.inflate(R.menu.home, menu)
//
//        return true
//    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_home)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}