package com.example.lzy01.AndroidImageCut;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class CannyActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btn_canny;
    private ImageView showImage;
    private Bitmap srcBitmap;
    private boolean isCanny;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_canny);
        isCanny=false;

        btn_canny=findViewById(R.id.btn_canny);
        showImage=findViewById(R.id.showImage);

        btn_canny.setOnClickListener(this);
        btn_canny.setText(R.string.canny);

        Intent intent=getIntent();
        byte[] srcByte=intent.getByteArrayExtra("srcImage");
        srcBitmap= BitmapFactory.decodeByteArray(srcByte, 0, srcByte.length).copy(Bitmap.Config.ARGB_8888, true);
        showImage.setImageBitmap(srcBitmap);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.btn_canny:
                if(!isCanny){
                    DoCanny();
                    btn_canny.setText(R.string.finish);
                }else{
                    Intent intent =new Intent(CannyActivity.this,MainActivity.class);
                    startActivity(intent);
                }

                break;
        }
    }

    private void DoCanny() {
        isCanny=true;
    }
}
