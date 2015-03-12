package com.ece251.gongxl.transcanner;

/**
 * Created by david on 3/11/15.
 */
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {
    private Button localization;
    private Button connect;
    private Button disconnect;
    private TextView location;
    private TextView address;
    private LocationService locationService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.address = (TextView) findViewById(R.id.address);


        this.location = (TextView) findViewById(R.id.location);
        this.locationService = new LocationService(getApplicationContext());

        this.localization = (Button) findViewById(R.id.localization);
        this.localization.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                location.setText(locationService.getLocation());
                address.setText(locationService.getAddress());
            }
        });

        this.disconnect = (Button) findViewById(R.id.disconnect);
        this.disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationService.stopService();
                Toast.makeText(getApplicationContext(),
                        R.string.prompt_disconnecting,
                        Toast.LENGTH_LONG).show();
            }
        });

        this.connect = (Button) findViewById(R.id.connect);
        this.connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationService.startService();
                Toast.makeText(getApplicationContext(),
                        R.string.prompt_connecting,
                        Toast.LENGTH_LONG).show();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}