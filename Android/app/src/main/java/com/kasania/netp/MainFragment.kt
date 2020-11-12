package com.kasania.netp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_main.view.*

class MainFragment :Fragment() {

    override fun onCreateView(inflater: LayoutInflater,container: ViewGroup?,savedInstanceState: Bundle?): View? {

        val rootView = inflater.inflate(R.layout.fragment_main, container,false)

        val connection = Connection()
        connection.connect("192.168.219.105",11111)
        rootView.code_confirm_button.setOnClickListener {  }

//        connection.connect("",11111)

        return rootView
    }


}