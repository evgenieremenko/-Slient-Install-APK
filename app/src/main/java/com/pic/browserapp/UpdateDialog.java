package com.pic.browserapp;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

public class UpdateDialog extends Dialog {

    private Button m_btnOk;
    private TextView m_txtMessage;
    private View.OnClickListener 	m_listenerPositive;

    public UpdateDialog(Context context) {
        super(context);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_update);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        m_btnOk			= findViewById(R.id.ID_BTN_OK);
        m_txtMessage	= findViewById(R.id.ID_TXTVIEW_MESSAGE);
    }

    public void setOnClickListenerPositive(View.OnClickListener a_listenerPositive) {
        m_listenerPositive = a_listenerPositive;
        m_btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_listenerPositive.onClick(v);
                dismiss();
            }
        });
    }

    public void setMessage(String a_sMessage) {
        m_txtMessage.setText(a_sMessage);
    }

}
