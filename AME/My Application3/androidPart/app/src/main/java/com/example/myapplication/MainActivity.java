package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private EditText city;
    private TextView textResult, textResult2;
    private ImageView imageView1, imageView2, imageView3, imageView4, imageView5;
    private DecimalFormat df = new DecimalFormat("#.#");

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private InputStream inputStream;

    private static final int MESSAGE_READ = 1;
    private static final int MY_PERMISSIONS_REQUEST_BLUETOOTH_CONNECT = 1;

    private final Handler handler = new Handler(msg -> {
        if (msg.what == MESSAGE_READ) {
            String data = (String) msg.obj;
            //writeInfoEmbedded(data);
            String[] p = data.split(",");
            if (p.length >= 2) {

                try {
                    textResult2.setText("CurrentLux: " + Float.parseFloat(p[0]) + "\nCurrentTemp: " + Float.parseFloat(p[1]));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeViews();
        setupBluetooth();
    }

    private void initializeViews() {
        city = findViewById(R.id.city);
        textResult = findViewById(R.id.textView);
        textResult2 = findViewById(R.id.textView2);
        imageView1 = findViewById(R.id.clear);
        imageView2 = findViewById(R.id.clouds);
        imageView3 = findViewById(R.id.drizzle);
        imageView4 = findViewById(R.id.mist);
        imageView5 = findViewById(R.id.snow);
    }

    private void setupBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // Handle the case where the device doesn't support Bluetooth
            return;
        }

        BluetoothDevice selectedDevice = findBlDisp("HMSoft"); // Change to your device name
        if (selectedDevice != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, MY_PERMISSIONS_REQUEST_BLUETOOTH_CONNECT);
                return;
            }

            try {
                UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
                bluetoothSocket = selectedDevice.createRfcommSocketToServiceRecord(uuid);
                bluetoothSocket.connect();
                inputStream = bluetoothSocket.getInputStream();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        startBluetoothReadingThread();
    }

    private void startBluetoothReadingThread() {
        new Thread(() -> {
            byte[] input = new byte[1000];
            int bytes;

            while (true) {
                try {
                    bytes = inputStream.read(input);
                    String data = new String(input, 0, bytes);
                    Message message = handler.obtainMessage(MESSAGE_READ, data);
                    handler.sendMessage(message);
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }).start();
    }



    private BluetoothDevice findBlDisp(String deviceName) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, MY_PERMISSIONS_REQUEST_BLUETOOTH_CONNECT);
            return null;
        }

        for (BluetoothDevice pairedDevice : bluetoothAdapter.getBondedDevices()) {
            if (pairedDevice.getName().equals(deviceName)) {
                return pairedDevice;
            }
        }
        return null;
    }


    public void getCurrentWeather(View view) {
        String baseUrl = "https://api.openweathermap.org/data/2.5/weather";
        String apiKey = "d2ce52f6fe7d5903ef26865fbd1df0ce";

        String c = city.getText().toString().trim();

        Uri.Builder builder = Uri.parse(baseUrl)
                .buildUpon()
                .appendQueryParameter("q", c)
                .appendQueryParameter("appid", apiKey);

        String urlF = builder.build().toString();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, urlF, response -> {
            clear();
            try {
                JSONObject json = new JSONObject(response);

                String main = json.getJSONArray("weather").getJSONObject(0).getString("main");
                JSONObject main2 = json.getJSONObject("main");

                double temp = main2.getDouble("temp");

                String humidity = main2.getString("humidity");

                String speed = json.getJSONObject("wind").getString("speed");

                String place = json.getString("name");

                textResult.setText("Current weather of " + place + "\nTemperature: " + df.format(temp) + " K" + "\nState of sky: " + main + "\nHumidity: " + humidity + " g/m³" + "\nWind speed: " + speed + " km/h");
                represent(main);

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }, error -> {
            Toast.makeText(getApplicationContext(), error.toString().trim(), Toast.LENGTH_SHORT).show();
            textResult.setText("the city has not been found");
        });

        RequestQueue r = Volley.newRequestQueue(getApplicationContext());
        r.add(stringRequest);
    }

    public void represent(String main) {
        imageView1.setVisibility(View.INVISIBLE);
        imageView2.setVisibility(View.INVISIBLE);
        imageView3.setVisibility(View.INVISIBLE);
        imageView4.setVisibility(View.INVISIBLE);
        imageView5.setVisibility(View.INVISIBLE);

        switch (main.toLowerCase()) {
            case "clear":
                imageView1.setVisibility(View.VISIBLE);
                break;
            case "clouds":
                imageView2.setVisibility(View.VISIBLE);
                break;
            case "drizzle":
            case "rain":
                imageView3.setVisibility(View.VISIBLE);
                break;
            case "mist":
                imageView4.setVisibility(View.VISIBLE);
                break;
            case "snow":
                imageView5.setVisibility(View.VISIBLE);
                break;
        }
    }

    public void clear() {
        imageView1.setVisibility(View.INVISIBLE);
        imageView2.setVisibility(View.INVISIBLE);
        imageView3.setVisibility(View.INVISIBLE);
        imageView4.setVisibility(View.INVISIBLE);
        imageView5.setVisibility(View.INVISIBLE);
    }
}