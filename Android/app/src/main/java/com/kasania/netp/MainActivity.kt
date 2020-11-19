package com.kasania.netp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager.beginTransaction().replace(R.id.content_root, LoginFragment()).commit()
    }

    override fun onDestroy() {
        super.onDestroy()
        Connection.instance.disconnect()
    }
}