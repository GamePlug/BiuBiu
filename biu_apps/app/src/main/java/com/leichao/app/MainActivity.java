package com.leichao.app;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.alipay.mobilesecuritysdk.face.SecurityClientMobile;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 *
 * Created by leichao on 2018/1/5.
 */

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.main_hello)
    TextView textView;

    private Unbinder unbinder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        unbinder = ButterKnife.bind(this);

        SecurityClientMobile mobile = new SecurityClientMobile();
        textView.setText("aaaaaaa");
        textView.setText("Kotlin:" + TestKotlinKt.getA());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }
}
