package com.event.leo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.event.leo.event.notify.ActivityEventManager;
import com.event.leo.event.notify.Event;
import com.event.leo.event.notify.EventKey;
import com.event.leo.event.notify.EventManager;
import com.event.leo.event.notify.EventType;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EventManager.getDefult().register(this);
        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < 100; i++) {
                    ActivityEventManager.newInstance().post("leo","i="+i);
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventManager.getDefult().unregister(this);
    }

//    @Event(key = EventKey.EVENT_LEO_TEXT)
//    public void a(Object o) {
//        if (o instanceof String) {
//            ((Button) findViewById(R.id.btn)).setText(String.valueOf(o));
//        }
//    }
//
//    @Event(key = EventKey.EVENT_LEO_TEXT2)
//    public void b(Object o) {
//        if (o instanceof String) {
//            Toast.makeText(MainActivity.this, String.valueOf(o), Toast.LENGTH_LONG).show();
//        }
//    }

    @Event(key = "leo",type = EventType.Event_ACTIVITY)
    public void leo(Object o){
        if (o instanceof String){
            Log.e(MainActivity.class.getCanonicalName(),o.toString());
        }
    }

    @Event(key = "hero",type = EventType.Event_ACTIVITY)
    public void hero(){

    }
}
