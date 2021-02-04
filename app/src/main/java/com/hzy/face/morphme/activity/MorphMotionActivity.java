package com.hzy.face.morphme.activity;

import android.os.Bundle;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.hzy.face.morphme.R;
import com.hzy.face.morphme.consts.RouterHub;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

@Route(path = RouterHub.MORPH_MOTION_ACTIVITY)
public class MorphMotionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_morph_motion);
    }
}
