package com.ece251.gongxl.transcanner;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;


public class MainMenu extends Activity {

    ListView list;
    String[] name = {
            "SCAN",
            "TRANSLATE",
            "IMPORT",
            "ABOUT US",

    } ;
    Integer[] imageId = {
            R.drawable.scan2,
            R.drawable.trans2,
            R.drawable.inbox,
            R.drawable.info2,

    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
////
        CustomList adapter = new
                CustomList(MainMenu.this, name, imageId);
        list = (ListView) findViewById(R.id.list);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Log.i("position", String.valueOf(position));
                Intent intent;
                switch (position) {
                    case 0: {
                        intent = new Intent(MainMenu.this, CameraActivity.class);       ////// huangchong
                        startActivity(intent);
                        break;
                    }
                    case 1: {
                        intent = new Intent(MainMenu.this, RealtimeActivity.class);
                        startActivity(intent);
                        break;
                    }
                    case 2: {
                        intent = new Intent(MainMenu.this, ImportActivity.class);
                        startActivity(intent);
                        break;
                    }
                    case 3: {
                        LayoutInflater layoutInflater
                                = (LayoutInflater) getBaseContext()
                                .getSystemService(LAYOUT_INFLATER_SERVICE);
                        View popupView = layoutInflater.inflate(R.layout.info_popup, null);
                        final PopupWindow popupWindow = new PopupWindow(
                                popupView,
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT);

                        Button btnDismiss = (Button) popupView.findViewById(R.id.dismiss);
                        btnDismiss.setOnClickListener(new Button.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                // TODO Auto-generated method stub
                                popupWindow.dismiss();
                            }
                        });
                        popupWindow.showAtLocation(findViewById(R.id.menu), Gravity.CENTER, 0, 0);

                    }
                }
            }
        });
    }
}
