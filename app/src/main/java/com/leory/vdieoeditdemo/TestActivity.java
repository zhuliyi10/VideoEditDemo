package com.leory.vdieoeditdemo;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        ImageView del = findViewById(R.id.del);
        TextView delArea = findViewById(R.id.delArea);
        TextView delArea2 = findViewById(R.id.delArea2);
        del.setOnLongClickListener(new View.OnLongClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public boolean onLongClick(View v) {
                View.DragShadowBuilder builder = new View.DragShadowBuilder(del);
                del.startDragAndDrop(null, builder, null, 0);
                dragView(delArea);
                dragView(delArea2);
                return false;
            }
        });

    }


    private void dragView(View v) {
        v.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {

                switch (event.getAction()) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        v.setBackgroundColor(Color.parseColor("#5A909090"));
                        return true;
                    case DragEvent.ACTION_DRAG_ENTERED: // 被拖拽View进入目标区域
                        v.setBackgroundColor(Color.RED);
                        return true;
                    case DragEvent.ACTION_DRAG_LOCATION: // 被拖拽View在目标区域移动
                        return true;
                    case DragEvent.ACTION_DRAG_EXITED: // 被拖拽View离开目标区域
                        v.setBackgroundColor(Color.parseColor("#6A909090"));
                        return true;
                    case DragEvent.ACTION_DROP: // 在目标区域内放开被拖拽View;
                        //这里写相关事件，本文以隐藏view为例

                        return true;
                    case DragEvent.ACTION_DRAG_ENDED: // 拖拽完成
                        return false;
                }
                return false;
            }
        });
    }

}