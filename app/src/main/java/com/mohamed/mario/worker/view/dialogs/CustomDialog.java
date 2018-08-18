package com.mohamed.mario.worker.view.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.mohamed.mario.worker.R;

import org.w3c.dom.Text;


public class CustomDialog extends Dialog {

    @LayoutRes
    private int layoutRes;
    private String title;
    private String message;
    private String positiveName;
    private String cancelName;
    private String type="none";
    private View.OnClickListener okButtonClick;
    private View.OnClickListener cancelButtonClick;
    View rootView;
    //Views
    TextView title_textView,message_textView;
    Button dialog_postive_button,dialog_negative_button;


    public CustomDialog(@NonNull Context context, int layoutRes, String title,
                        String message, String positiveName, String cancelName,String type) {
        super(context);
        this.layoutRes = layoutRes;
        this.title = title;
        this.message = message;
        this.positiveName = positiveName;
        this.cancelName = cancelName;
        this.type= type;
    }
    public CustomDialog(@NonNull Context context, int layoutRes, String title,
                        String message, String positiveName, String cancelName) {
        super(context);
        this.layoutRes = layoutRes;
        this.title = title;
        this.message = message;
        this.positiveName = positiveName;
        this.cancelName = cancelName;

    }





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //requestWindowFeature(Window.FEATURE_NO_TITLE);



        setContentView(R.layout.dialog_custom);
        rootView = findViewById(R.id.rootView);
        if(type.equals("none")) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.user_register,
                    (ViewGroup) rootView.findViewById(R.id.frameLayout), true);
        }
        title_textView=findViewById(R.id.dialog_title_txt);
        message_textView=findViewById(R.id.dialog_message_txt);
        dialog_postive_button=findViewById(R.id.dialog_postive_button);
        dialog_negative_button=findViewById(R.id.dialog_negative_button);


        title_textView.setText(title);
        message_textView.setText(message);
        dialog_postive_button.setText(positiveName);
        dialog_negative_button.setText(cancelName);


    }

    public View getRootView(){
        return rootView;
    }

    public void setOnPositiveClickLisner(  View.OnClickListener okButtonClick){
        dialog_postive_button.setOnClickListener(okButtonClick);
    }
    public void setOnCancelClickLisner(  View.OnClickListener cancelButtonClick){
        dialog_negative_button.setOnClickListener(cancelButtonClick);


    }

}

