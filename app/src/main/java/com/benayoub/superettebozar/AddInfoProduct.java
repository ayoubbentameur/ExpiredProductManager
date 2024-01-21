package com.benayoub.superettebozar;

import static java.security.AccessController.getContext;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.datepicker.MaterialTextInputPicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;

import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.RemoteMessage;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.Locale;

public class AddInfoProduct extends AppCompatActivity {
    RemoteMessage remoteMessage;
    TextInputEditText nameproduct, codebar;
    private EditText preexpiredday;
    private DatabaseReference mDatabase;
    long timestampexpired;
    long hoursmill;
    long minmill;
    String firebaseKey;
    String path;
    FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    String uid = currentFirebaseUser.getUid();
    // button to open the material date picker dialog
    private Button mPickDateButton, save, test;
    Calendar selectedCalendar;
    MaterialDatePicker materialDatePicker;
    String formattedDate;
    ActivityResultLauncher<String> mGetContent;
    // textview to preview the selected date
    // private TextView mShowSelectedDateText;
    ImageView productView;
    private boolean isPictureSelected = false;

    private static final int REQUEST_GALLERY = 1;
    private static final int REQUEST_CAMERA = 2;
    String aa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_info_product);


        Intent intent = getIntent();

        // Retrieve the string using the key
        String receivedData = intent.getStringExtra("keyCode");
        Log.d("data", receivedData);
        // Display the received data in a TextView or handle it as needed
        codebar = findViewById(R.id.editTextcodebar);
        productView = findViewById(R.id.imageView);
        nameproduct = findViewById(R.id.editTextname);
        save = findViewById(R.id.save_id);
        test = findViewById(R.id.test_id);
        preexpiredday = findViewById(R.id.editTextText3);
        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimePicker();

            }
        });
        productView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialAlertDialogBuilder(AddInfoProduct.this)

                        .setPositiveButton("galerry", (dialog, which) -> {
                            // Handle positive button click
                            openGallery();


                        })
                        .setNegativeButton("camera", (dialog, which) -> {
                            // Handle negative button click
                            requestCameraPermission();

                        })
                        .show();

            }

        });
        codebar.setText(receivedData);

// register all the UI widgets with their
        // appropriate IDs
        mPickDateButton = findViewById(R.id.pick_date_button);

        // create the calendar constraint builder
        CalendarConstraints.Builder calendarConstraintBuilder = new CalendarConstraints.Builder();

        // set the validator point forward from june
        // this mean the all the dates before the June month
        // are blocked
        calendarConstraintBuilder.setValidator(DateValidatorPointForward.now());

        // instantiate the Material date picker dialog
        // builder
        final MaterialDatePicker.Builder materialDatePickerBuilder = MaterialDatePicker.Builder.datePicker();
        materialDatePickerBuilder.setTitleText("SELECT A DATE");

        // now pass the constrained calendar builder to
        // material date picker Calendar constraints
        materialDatePickerBuilder.setCalendarConstraints(calendarConstraintBuilder.build());

        // now build the material date picker dialog
        materialDatePicker = materialDatePickerBuilder.build();

        // handle the Select date button to open the
        // material date picker
        mPickDateButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // show the material date picker with
                        // supportable fragment manager to
                        // interact with dialog material date
                        // picker dialog fragments
                        materialDatePicker.show(getSupportFragmentManager(), "MATERIAL_DATE_PICKER");
                    }
                });

        materialDatePicker.addOnPositiveButtonClickListener(
                new MaterialPickerOnPositiveButtonClickListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onPositiveButtonClick(Object selection) {
                        // now update the selected date preview

                        // Create a SimpleDateFormat object with the desired format and locale
                        selectedCalendar = Calendar.getInstance();
                        selectedCalendar.setTimeInMillis((Long) selection);

                        // Convert Calendar to Timestamp
                        Timestamp timestamp = new Timestamp(selectedCalendar.getTimeInMillis());

                        // Format the timestamp as a string (you can adjust the format)
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        String formattedDate = dateFormat.format(timestamp);
                        mPickDateButton.setText(formattedDate);

                        // Update the selected date preview
                        // mShowSelectedDateText.setText("Selected Date is : " + materialDatePicker.getHeaderText());
                        // mShowSelectedDateText.setText("Selected Date is: " + selectedCalendar.getTimeInMillis());
                        aa = materialDatePicker.getHeaderText();
                        Log.d("toime", String.valueOf(selectedCalendar.getTimeInMillis()));
                    }
                });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postData();

            }
        });
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, REQUEST_GALLERY);
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, REQUEST_CAMERA);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_GALLERY) {
                // Handle gallery result
                handleGalleryResult(data);
            } else if (requestCode == REQUEST_CAMERA) {
                // Handle camera result
                handleCameraResult(data);
            }
        }
    }

    private void handleGalleryResult(Intent data) {
        if (data != null && data.getData() != null) {
            // Get the selected image URI and set it to the ImageView
            productView.setImageURI(data.getData());
            isPictureSelected = true;

        }
    }

    private void handleCameraResult(Intent data) {
        if (data != null && data.getExtras() != null) {
            // Get the captured image bitmap and set it to the ImageView
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            productView.setImageBitmap(photo);
            isPictureSelected = true;

        }
    }

    // Add this code in your activity or fragment
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;

    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            // Permission is already granted, proceed with the camera operation
            openCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Camera permission granted, proceed with the camera operation
                openCamera();
            } else {
                // Camera permission denied, handle accordingly (e.g., show a message)
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String predate() {
        int preday = Integer.valueOf(String.valueOf(preexpiredday.getText()));
        timestampexpired = (long) (selectedCalendar.getTimeInMillis() - (preday * 86400000) + (hoursmill + minmill) - 3600000);

        // Create a Date object using the timestamp
        Date date = new Date(timestampexpired);

        // Format the date using SimpleDateFormat
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        formattedDate = dateFormat.format(date);

        // Print the formatted date
        System.out.println("Formatted Date: " + formattedDate);
        Log.e("date", formattedDate);

        return formattedDate;
    }

    private void postData() {


        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("Products");
// Generate a random key for Posts
        String randomKey = databaseReference.push().getKey();
        upload(randomKey);
        DataProduct dataProduct;
        if (nameproduct.getText().toString().isEmpty() || selectedCalendar==null){
            Toast.makeText(this,"Please complete your data",Toast.LENGTH_LONG).show();
            return;
        }else {
            predate();
            if (!isPictureSelected) {


                dataProduct = new DataProduct(nameproduct.getText().toString(), codebar.getText().toString(), aa, formattedDate, selectedCalendar.getTimeInMillis(), timestampexpired,null);


                // Display a message to the user that they need to select a picture

                // Exit the method without proceeding further
            }else {
                dataProduct = new DataProduct(nameproduct.getText().toString(), codebar.getText().toString(), aa, formattedDate, selectedCalendar.getTimeInMillis(), timestampexpired,path);

            }
            databaseReference.push().setValue(dataProduct).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Intent homeIntent = new Intent(AddInfoProduct.this, MainActivity.class);
                    startActivity(homeIntent);
                    homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("TAG", "Failed to add data", e);
                }
            });



        }



    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // Handle the notification when the activity is already running
        handleNotification(intent);
    }

    private void handleNotification(Intent intent) {
        if (intent != null && intent.getExtras() != null) {
            // Check if the intent has the data from the notification
            String title = intent.getStringExtra("title");
            String body = intent.getStringExtra("body");

            // Perform actions based on notification content
            if (title != null && body != null) {
                // Display the notification content or perform other actions
                // For example, update UI elements with the notification content
            }
        }
    }


    private void showTimePicker() {
        MaterialTimePicker materialTimePicker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .build();

        materialTimePicker.addOnPositiveButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hour = materialTimePicker.getHour();
                int minute = materialTimePicker.getMinute();
                hoursmill = hour * 3600000;
                minmill = minute * 60000;
                // Convert hour and minute to timestamp using Calendar
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
                //  long timestamp = calendar.getTimeInMillis();

                // You now have the timestamp
                //Toast.makeText(AddInfoProduct.this, "Timestamp: " + timestamp, Toast.LENGTH_SHORT).show();
                //Log.e("aaa", String.valueOf(timestamp));
            }
        });

        materialTimePicker.show(getSupportFragmentManager(), "tag");
    }

    private void upload(String Key) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("Products");

        if (!isPictureSelected) {
            // Display a message to the user that they need to select a picture
            StorageReference storageRef = storage.getReference();
            path = null;
//            StorageReference mountainsRef = storageRef.child(path);

        }else {

            StorageReference storageRef = storage.getReference();
            path = "images/"+Key+".jpeg";
// Create a reference to "mountains.jpg"
            StorageReference mountainsRef = storageRef.child(path);


            // Get the data from an ImageView as bytes
            productView.setDrawingCacheEnabled(true);
            productView.buildDrawingCache();
            Bitmap bitmap = ((BitmapDrawable) productView.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            UploadTask uploadTask = mountainsRef.putBytes(data);
            final ProgressDialog progressDialog = new ProgressDialog(this);
            uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    // mack progress bar dialog

                    //   progressDialog.setTitle("Uploading....");
                    //   progressDialog.setCancelable(false);
                    //   progressDialog.show();
                    //  double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    //  progressDialog.setMessage("upload " + (int) progress + "%");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(AddInfoProduct.this, "Failed to upload picture", Toast.LENGTH_SHORT).show();
                }
            });
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                    Toast.makeText(AddInfoProduct.this, "Failed", Toast.LENGTH_LONG).show();
                    //Uri photoUrl = currentUser.getPhotoUrl();

                    //  productView.setImageResource(R.drawable.user_pic);
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                    // ...
                    //  progressDialog.dismiss();

                    // Toast.makeText(getContext(), "Uploaded", Toast.LENGTH_LONG).show();
                }
            });
        }





    }
}