package com.example.batrakov.alarmmanagertask;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity {

    ImageButton mAddAlarmManagerElement;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_activity);


        mAddAlarmManagerElement = (ImageButton) findViewById(R.id.add_alarm_manager);


        final Intent startEditActivity = new Intent(this, EditNoteActivity.class);
        mAddAlarmManagerElement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(startEditActivity));
            }
        });
    }
}
