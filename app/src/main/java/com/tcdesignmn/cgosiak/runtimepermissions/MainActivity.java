package com.tcdesignmn.cgosiak.runtimepermissions;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_CODE = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton create_fab = (FloatingActionButton) findViewById(R.id.floatingActionButton);
        create_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            if (HasPermissions()) {
                // MakeFolder();
                ArrayList<Message> messages = ReadTextMessages();
                MakeChart(messages);
            }
            else {
                RequestPermissions();
            }
            }
        });
    }

    private void MakeToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void MakeChart(ArrayList<Message> messages) {
        Map<String, Integer> uniqueCount = new HashMap<>();

        for (Message message: messages) {
            Integer count = uniqueCount.get(message.contact.name);
            uniqueCount.put(message.contact.name, (count==null) ? 1 : count+1);
        }

        PieChart chart = (PieChart)findViewById(R.id.pieChart);
        List<PieEntry> entries = new ArrayList<>();
        for (String key:uniqueCount.keySet()) {
            entries.add(new PieEntry(uniqueCount.get(key), key));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Received SMS Results");
        PieData data = new PieData(dataSet);
        chart.setData(data);
        chart.invalidate();
    }

    private void DeleteFolder() {
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "RuntimeExample");

        if (file.exists()) {
            Boolean ff = file.delete();
            if (ff) {
                MakeToast("Success");
            }
            else {
                MakeToast("Failed");
            }
        }
        else {
            MakeToast("Folder Does Not Exist");
        }
    }

    private void MakeFolder() {
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "RuntimeExample");

        if (!file.exists()) {
            Boolean ff = file.mkdir();
            if (ff) {
                MakeToast("Success");
            }
            else {
                MakeToast("Failed");
            }
        }
        else {
            MakeToast("Folder Already Exists");
        }
    }

    private ArrayList<Message> ReadTextMessages() {
        Cursor cur = getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);
        ArrayList<Message> messages = new ArrayList<>();

        if (cur.moveToFirst()) { /* false = no sms */
            do {
                String msgInfo = "";

                String address = cur.getString(cur.getColumnIndexOrThrow("address"));
                Contact contact = GetContact(address);
                Date date = new Date(Long.parseLong(cur.getString(cur.getColumnIndexOrThrow("date"))));
                String body = cur.getString(cur.getColumnIndexOrThrow("body"));

                Message newMessage = new Message(contact, date, body);
                messages.add(newMessage);

            } while (cur.moveToNext());
        }
        else {
            MakeToast("No Messages!");
        }

        return messages;
    }

    private Contact GetContact(String address) {
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(address));
        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);

        if (cursor.moveToFirst()) { /* false = no sms */
            Contact newContact = new Contact(address, cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME)));
            return newContact;
        }
        else {
            return new Contact(address, "Undefined");
        }
    }

    private boolean HasPermissions() {
        int res = 0;
        String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_SMS, Manifest.permission.READ_CONTACTS};

        for (String permission: permissions) {
            res = checkCallingOrSelfPermission(permission);

            if (!(res == PackageManager.PERMISSION_GRANTED)) {
                return false;
            }
        }
        return true;
    }

    private void RequestPermissions() {
        String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_SMS, Manifest.permission.READ_CONTACTS};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, PERMISSIONS_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean allowed = true;

        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                for (int res : grantResults) {
                      allowed = allowed && (res == PackageManager.PERMISSION_GRANTED);
                }
                break;
            default:
                allowed = false;
                break;
        }

        if (allowed) {
            MakeToast("Permissions Granted: Please Try Again");
        }
        else {
            if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                MakeToast("Need Permissions to Create Folder");
            }
        }

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
