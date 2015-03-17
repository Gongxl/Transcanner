package com.ece251.gongxl.transcanner;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


public class DicActivity extends ActionBarActivity {
    private Handler trans_handler;
    private Translator translator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dic);


//        trans_handler = new Handler() {
//            @Override
//            public void handleMessage(Message msg) {
//                super.handleMessage(msg);
//                if(msg.arg1 == Translator.MESSAGE_AUTOCORRECT) {
//                    //editText.setText((String) msg.obj);
//                    System.out.println("received autocorrect msg" + (String) msg.obj);
//                    editText.setText((String)msg.obj);
//                }
//                else if(msg.arg1 == Translator.MESSAGE_TRANSLATE){
//
//                    System.out.println("received translate msg");
//                    editText.setText((String) msg.obj);
//                } else {
//
//                    System.out.println("received lookup msg");
//
//                }
//            }
//        };
//        translator = new Translator(getApplicationContext(), trans_handler);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_dic, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.jump_sentence) {
            Intent intent = new Intent(DicActivity.this,RealtimeActivity.class);
            startActivity(intent);
            finish();
        }

        if (id == R.id.back_home) {
            Intent intent = new Intent(DicActivity.this,MainMenu.class);
            startActivity(intent);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
