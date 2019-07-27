package com.hajau.contacts;

import android.Manifest;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import java.util.ArrayList;

public class ContactUtil {
    public static final String TAG = "ContactUtil";

    public static void addContact(Context context, String name, String phone) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_CONTACTS)
                == PackageManager.PERMISSION_DENIED) {
            return;
        }
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        int rawContactInsertIndex = ops.size();

        ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null).build());
        ops.add(ContentProviderOperation
                .newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                .withValue(ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                        name) // Name of the person
                .build());
        ops.add(ContentProviderOperation
                .newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(
                        ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                .withValue(ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER,
                        phone) // Number of the person
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE).build()); // Type of
        // mobile number
        try {
            context.getContentResolver().applyBatch(
                    ContactsContract.AUTHORITY, ops);
            Toast.makeText(context, "Đã lưu lại số điện thoại của " + name,
                    Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Lỗi: Không thể lưu lại số này.", Toast.LENGTH_SHORT).show();
        }
    }

    static boolean deleteContact(Context ctx, String name, String phone) {
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(name));
        Cursor c = ctx.getContentResolver().query(uri,
                new String[]{ContactsContract.PhoneLookup._ID}, null, null, null);
        if (c != null) {
            if (c.moveToNext()) {
                Long contactId = c.getLong(c.getColumnIndex(ContactsContract.PhoneLookup._ID));
                ArrayList<ContentProviderOperation> ops = new ArrayList<>();
                ops.add(ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI)
                        .withSelection(ContactsContract.Data._ID + "=? and " +
                                        ContactsContract.Data.MIMETYPE + "=?",
                                new String[]{String.valueOf(contactId),
                                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE})
                        .build());
                try {
                    ctx.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
                    return true;
                } catch (OperationApplicationException e) {
                    e.printStackTrace();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            c.close();
        }
        return false;
    }
}
