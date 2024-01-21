package com.benayoub.superettebozar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;
import android.widget.Toast;

import java.util.Calendar;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.mlkit.common.MlKitException;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner;
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    Query query;
    ProductAdapter adapter;
    DatabaseReference databaseReference;
    List<DataProduct> products;
    String productname, barcode,preexpireddate,expireddate;
    Long expiredtime,preexpiredtime;
    FloatingActionButton floatingActionButton;
    String firebaseKey2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this); // Initialize Firebase
    //    FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        recyclerView = findViewById(R.id.recycler1);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Channel Name";
            String description = "Channel Description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("channel_id", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }


        // Set the alarm based on the timestamp string

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        floatingActionButton = findViewById(R.id.floatingActionButton);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onScanButtonClicked(floatingActionButton);
            }
        });
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                setUpRecyclerView();

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.overvlow, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_red) {
            Intent intent = new Intent(MainActivity.this, MainActivity2.class);
            startActivity(intent);
        }
        if (item.getItemId() == R.id.menu_green) {
            Intent intent2 = new Intent(MainActivity.this, MainActivity3.class);
            startActivity(intent2);

        }
        return super.onOptionsItemSelected(item);
    }


    String code;


    public void onScanButtonClicked(View view) {
        GmsBarcodeScannerOptions.Builder optionsBuilder = new GmsBarcodeScannerOptions.Builder();
        optionsBuilder.allowManualInput();

        optionsBuilder.enableAutoZoom();

        GmsBarcodeScanner gmsBarcodeScanner =
                GmsBarcodeScanning.getClient(this, optionsBuilder.build());
        gmsBarcodeScanner
                .startScan()
                .addOnSuccessListener(barcode -> {

                            String scannedCode = barcode.getDisplayValue();
                            passingdata(scannedCode);
                        }
                )
                .addOnFailureListener(
                        e -> Toast.makeText(this, getErrorMessage(e), Toast.LENGTH_SHORT).show())
                .addOnCanceledListener(
                        () -> Toast.makeText(this,getString(R.string.error_scanner_cancelled), Toast.LENGTH_SHORT).show());
    }

    private String getSuccessfulMessage(Barcode barcode) {
        String barcodeValue =
                String.format(
                        Locale.US,
                        "Display Value: %s\nRaw Value: %s\nFormat: %s\nValue Type: %s",
                        barcode.getDisplayValue(),
                        barcode.getRawValue(),
                        barcode.getFormat(),
                        barcode.getValueType());
        return getString(R.string.barcode_result, barcodeValue);
    }


    @SuppressLint("SwitchIntDef")
    private String getErrorMessage(Exception e) {
        if (e instanceof MlKitException) {
            switch (((MlKitException) e).getErrorCode()) {
                case MlKitException.CODE_SCANNER_CAMERA_PERMISSION_NOT_GRANTED:
                    return getString(R.string.error_camera_permission_not_granted);
                case MlKitException.CODE_SCANNER_APP_NAME_UNAVAILABLE:
                    return getString(R.string.error_app_name_unavailable);
                default:
                    Log.d("aaa", e.getLocalizedMessage());
                    return getString(R.string.error_default_message, e);

            }
        } else {
            return e.getMessage();
        }
    }

    private String passingdata(String code) {

        Intent intent = new Intent(MainActivity.this, AddInfoProduct.class);

        // Attach the string to the intent with a key
        intent.putExtra("keyCode", code);

        // Start the new activity
        startActivity(intent);
        return code;
    }


    private void setUpRecyclerView() {
         query = databaseReference.child("Products").orderByChild("expiredTime");

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                products = new ArrayList<>();

                for (DataSnapshot keySnapshot : dataSnapshot.getChildren()) {
                    // Assuming "productName" is a direct child of each key
                    DataSnapshot productNameSnapshot = keySnapshot.child("productName");
                    DataSnapshot productCodeSnapshot = keySnapshot.child("productCode");
                    DataSnapshot preexpiredDateSnapshot = keySnapshot.child("preexpireddate");
                    DataSnapshot expiredDateSnapshot = keySnapshot.child("expereddate");
                    DataSnapshot expiredTimeSnapshot = keySnapshot.child("expiredTime");
                    DataSnapshot preexpiredTimeSnapshot = keySnapshot.child("preexpiredtime");
                    DataSnapshot uriPric=keySnapshot.child("uriPic");

                    // Check if "productName" exists
                    productname = productNameSnapshot.getValue(String.class);
                    barcode = productCodeSnapshot.getValue(String.class);
                    preexpireddate = preexpiredDateSnapshot.getValue(String.class);
                    expireddate = expiredDateSnapshot.getValue(String.class);
                    expiredtime = expiredTimeSnapshot.getValue(Long.class);
                    preexpiredtime = preexpiredTimeSnapshot.getValue(Long.class);
                    String uri=uriPric.getValue(String.class);

                    DataProduct product = new DataProduct(productname, barcode, expireddate, preexpireddate, expiredtime, preexpiredtime,uri);
                    products.add(product);
                    // Use the Firebase key as a unique request code
                     firebaseKey2 = keySnapshot.getKey();
                    Intent intentkey = new Intent(MainActivity.this, AddInfoProduct.class);

                    intentkey.putExtra("postid", firebaseKey2);

                    Log.d("KEY",firebaseKey2);
                    int requestCode = firebaseKey2.hashCode();
                    // Use the hash code of the key as the request code
// Reference to an image file in Cloud Storage
// Inside your onDataChange method after obtaining the firebaseKey value

// Create an Intent to start AnotherActivity


                    retrieveDataAndSetAlarms();

                    Log.d("FirebaseData", "Product Name does not exist for this key: " + keySnapshot.getKey());
                }

             //   Collections.reverse(products); // Reverse the list to display the newest first

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if (adapter == null) {

                            adapter = new ProductAdapter(products, firebaseKey2, MainActivity.this);
                            recyclerView.setAdapter(adapter);
                        } else {
                            adapter.updateData(products);
                            adapter.notifyDataSetChanged();
                        }
                    }
                });


                recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("firebase", "Error getting data", error.toException());
            }
        });
    }




    private void setAlarm(String preexpireddate, int requestCode, String productName, String expiryDate) {
        // Parse the timestamp string into a Date object

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        try {
            Date date = dateFormat.parse(preexpireddate);

            // Create a Calendar instance and set it to the specified date and time
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            // Create an intent to broadcast to the AlarmReceiver
            Intent intent = new Intent(this, AlarmReceiver.class);
            intent.putExtra("REQUEST_CODE", requestCode);
            intent.putExtra("productName", productName);
            intent.putExtra("expiryDate", expiryDate);
            intent.putExtra("requestCode", requestCode);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    MainActivity.this,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
            );
            // Get the AlarmManager service
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

            // Cancel any existing alarms with the same request code
            alarmManager.cancel(pendingIntent);

            // Get the AlarmManager service

            // Set the alarm based on the SDK version
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
            } else {
                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

     void retrieveDataAndSetAlarms() {
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                products = new ArrayList<>();

                for (DataSnapshot keySnapshot : dataSnapshot.getChildren()) {
                    // Retrieve data for each product
                    DataSnapshot productNameSnapshot = keySnapshot.child("productName");
                    DataSnapshot productCodeSnapshot = keySnapshot.child("productCode");
                    DataSnapshot preexpiredDateSnapshot = keySnapshot.child("preexpireddate");
                    DataSnapshot expiredDateSnapshot = keySnapshot.child("expereddate");
                    DataSnapshot uriPric=keySnapshot.child("uriPic");
                    // Check if all required fields exist
                    if (productNameSnapshot.exists() && productCodeSnapshot.exists()
                            && preexpiredDateSnapshot.exists() && expiredDateSnapshot.exists()) {

                        // Retrieve data for each product
                        String productname = productNameSnapshot.getValue(String.class);
                        String barcode = productCodeSnapshot.getValue(String.class);
                        String preexpireddate = preexpiredDateSnapshot.getValue(String.class);
                        String expireddate = expiredDateSnapshot.getValue(String.class);
                        String uri=uriPric.getValue(String.class);
                        // Use the Firebase key as a unique request code
                        String firebaseKey = keySnapshot.getKey();
                        int requestCode = firebaseKey.hashCode();

                        // Assuming you have the product name and expiry date available here
                        Log.d("ProductName", productname);
                        Log.d("ExpiryDate", expireddate);

                        DataProduct product = new DataProduct(productname, barcode, expireddate, preexpireddate, 0, 0,uri);
                        products.add(product);

                        // Set the alarm
                        setAlarm(preexpireddate, requestCode, productname, expireddate);
                    } else {
                        Log.d("FirebaseData", "One or more required fields do not exist for this key: " + keySnapshot.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("firebase", "Error getting data", error.toException());
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();

    }
}