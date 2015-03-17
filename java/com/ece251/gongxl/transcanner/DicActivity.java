package com.ece251.gongxl.transcanner;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;


public class DicActivity extends ActionBarActivity {
    private Handler trans_handler;
    private Translator translator;
    EditText editText;
    TextView textView;
    Boolean autoC = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dic);

        editText = (EditText)findViewById(R.id.input_word);
        ImageButton btn = (ImageButton)findViewById(R.id.btn_dic);
        final CheckBox auto_correct = (CheckBox)findViewById(R.id.auto_correct);
        textView = (TextView)findViewById(R.id.dic_result);
        textView.setMovementMethod(ScrollingMovementMethod.getInstance());

        auto_correct.setOnClickListener(new CheckBox.OnClickListener(){
            @Override
            public void onClick(View v){
                if(auto_correct.isChecked()){
                    autoC = true;


                }
                else autoC = false;
            }
        });

        btn.setOnClickListener(new ImageButton.OnClickListener(){

            @Override
            public void onClick(View v) {
                translator.lookupDictionary(editText.getText().toString(),autoC);
            }
        });

        trans_handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if(msg.arg1 == Translator.MESSAGE_AUTOCORRECT) {
                    //editText.setText((String) msg.obj);
                    System.out.println("received autocorrect msg" + (String) msg.obj);
                    editText.setText((String)msg.obj);
                }
                else if(msg.arg1 == Translator.MESSAGE_TRANSLATE){

                    System.out.println("received translate msg");

                } else {

                    System.out.println("received lookup msg");
                    textView.setText((String) msg.obj);

                }
            }
        };
        translator = new Translator(getApplicationContext(), trans_handler);
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
