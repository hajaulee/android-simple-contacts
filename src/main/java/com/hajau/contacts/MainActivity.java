package com.hajau.contacts;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private String[] necessaryPermission = new String[]{Manifest.permission.READ_CONTACTS,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.WRITE_CONTACTS};
    FloatingActionButton fab;
    private ArrayList<String[]> contactList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddNewContactDialog();
            }
        });

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            for (String s : necessaryPermission) {
                if (!checkIfAlreadyHavePermission(s)) {
                    requestForSpecificPermission(necessaryPermission);
                }
            }
        }
        if (checkIfAlreadyHavePermission(Manifest.permission.READ_CONTACTS)) {
            updateContactList();
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

    public void updateContactList() {
        //generate list
        contactList = getContactList();

        //instantiate custom adapter
        ContactAdapter adapter = new ContactAdapter(contactList, this);

        //handle listview and assign adapter
        ListView lView = findViewById(R.id.contact_lv);
        lView.setAdapter(adapter);
    }

    public void scrollToContact(String name) {
        ListView lView = findViewById(R.id.contact_lv);
        for (int i = 0; i < contactList.size(); i++) {
            if (contactList.get(i)[0].equals(name)) {
                lView.smoothScrollToPosition(i);
                lView.setSelection(i);
                return;
            }
        }

    }

    private ArrayList<String[]> getContactList() {
        ArrayList<String[]> list = new ArrayList<>();
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);

        if ((cur != null ? cur.getCount() : 0) > 0) {
            while (cur.moveToNext()) {
                String id = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME));

                if (cur.getInt(cur.getColumnIndex(
                        ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);
                    if (pCur != null) {
                        while (pCur.moveToNext()) {
                            String phoneNo = pCur.getString(pCur.getColumnIndex(
                                    ContactsContract.CommonDataKinds.Phone.NUMBER));
                            list.add(new String[]{name, phoneNo});
                            Log.i(TAG, "Name: " + name);
                            Log.i(TAG, "Phone Number: " + phoneNo);
                        }
                        pCur.close();
                    }
                }
            }
        }
        if (cur != null) {
            cur.close();
        }
        Collections.sort(list, new Comparator<String[]>() {
            public int compare(String[] strings, String[] otherStrings) {
                return strings[0].toLowerCase().compareTo(otherStrings[0].toLowerCase());
            }
        });
        return list;
    }


    private boolean checkIfAlreadyHavePermission(String permission) {
        int result = ContextCompat.checkSelfPermission(this, permission);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestForSpecificPermission(String[] permissions) {
        ActivityCompat.requestPermissions(this,
                permissions,
                101);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //granted
                Snackbar.make(fab, "Cảm ơn bạn đã cấp quyền!", Snackbar.LENGTH_SHORT)
                        .setAction("", null).show();
                if (checkIfAlreadyHavePermission(Manifest.permission.READ_CONTACTS)) {
                    updateContactList();
                }
            } else {
                //not granted
                Snackbar.make(fab, "Xin mở lại ứng dụng", Snackbar.LENGTH_LONG)
                        .setAction("", null).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    public void showAddNewContactDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Get the layout inflater
        final LayoutInflater inflater = this.getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        final View view = inflater.inflate(R.layout.new_contact, null);
        builder.setView(view)
                // Add action buttons
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // sign in the user ...
                        final EditText newName = view.findViewById(R.id.new_name);
                        final EditText newPhone = view.findViewById(R.id.new_num);
                        ContactUtil.addContact(view.getContext(),
                                newName.getText().toString(),
                                newPhone.getText().toString());
                        updateContactList();
                        scrollToContact(newName.getText().toString());
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //
                    }
                });
        AlertDialog addNewContactDialog = builder.create();
        addNewContactDialog.show();
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
