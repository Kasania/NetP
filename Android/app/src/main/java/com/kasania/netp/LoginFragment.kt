package com.kasania.netp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.login_fragment.*

class LoginFragment : Fragment(){

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val rootView = inflater.inflate(R.layout.login_fragment, container,false)

        Connection.instance.connect("192.168.219.101",11111)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        code_confirm_button.setOnClickListener {

            if(Connection.instance.isConnected()){
                Connection.instance.sendVerificationCode(code_input.text.toString().toInt())
                Connection.instance.onSyncSucceed { requireActivity().supportFragmentManager.beginTransaction().replace(R.id.content_root, CameraFragment()).commit() }

            }else{
                Toast.makeText(context, "서버에 연결되지 않았습니다.", Toast.LENGTH_SHORT).show()
            }

        }
    }
}