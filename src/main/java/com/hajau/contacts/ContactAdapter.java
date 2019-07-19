package com.hajau.contacts;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class ContactAdapter extends BaseAdapter implements ListAdapter {
    private ArrayList<String[]> list;
    private Context context;
    private static final String TAG = "ContactAdapter";
    private AlertDialog editContactDialog;


    public ContactAdapter(ArrayList<String[]> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int pos) {
        return list.get(pos);
    }

    @Override
    public long getItemId(int pos) {
        return 0;
        //just return 0 if your list items do not have an Id variable.
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.listview_item, null);
        }

        //Handle TextView and display string from your list
        TextView nameContact = view.findViewById(R.id.name);
        nameContact.setText(list.get(position)[0]);

        //Handle TextView and display string from your list
        final TextView numContact = view.findViewById(R.id.num);
        numContact.setText(list.get(position)[1]);

        //Handle buttons and add onClickListeners
        ImageButton callBtn = view.findViewById(R.id.call_btn);
        ImageButton messageBtn = view.findViewById(R.id.message_btn);

        callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //do something
                Intent intent = new Intent(Intent.ACTION_CALL);

                intent.setData(Uri.parse("tel:" + numContact.getText()));
                context.startActivity(intent);
            }
        });
        messageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //do something
                Uri sms_uri = Uri.parse("smsto:" + numContact.getText());
                Intent sms_intent = new Intent(Intent.ACTION_SENDTO, sms_uri);
                sms_intent.putExtra("sms_body", "");
                context.startActivity(sms_intent);
            }
        });

        view.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showEditContactDialog(list.get(position)[0], list.get(position)[1]);
            }
        });

        return view;
    }

    @SuppressLint("InflateParams")
    public void showEditContactDialog(final String name, final String phone) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        // Get the layout inflater
        final LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        final View view = inflater.inflate(R.layout.edit_contact, null);
        final EditText editName = view.findViewById(R.id.new_name);
        final EditText editPhone = view.findViewById(R.id.new_num);
        final Button delButton = view.findViewById(R.id.delBtn);
        editName.setText(name);
        editPhone.setText(phone);
        delButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                boolean success = ContactUtil.deleteContact(context, name, phone);
                if (success) {
                    ((MainActivity) context).updateContactList();
                    // Create snackbar
                    final Snackbar snackbar = Snackbar.make(view,
                            Html.fromHtml("Đã xóa số của <b>" + name + "</b>"),
                            Snackbar.LENGTH_LONG);
                    // Set an action on it, and a handler
                    snackbar.setAction("Khôi phục", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ContactUtil.addContact(context, name, phone);
                            ((MainActivity) context).updateContactList();
                            ((MainActivity) context).scrollToContact(name);
                        }
                    });
                    snackbar.setActionTextColor(Color.WHITE);

                    snackbar.show();
                } else {
                    // Create snackbar
                    final Snackbar snackbar = Snackbar.make(view, "Lỗi không thể xóa được",
                            Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        ((MainActivity)context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                editContactDialog.dismiss();
                            }
                        });
                    }
                }, 1000);
            }
        });
        builder.setView(view)
                // Add action buttons
                .setPositiveButton(R.string.edit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // sign in the user ...
                        ContactUtil.deleteContact(context, name, phone);
                        ContactUtil.addContact(context, editName.getText().toString(),
                                editPhone.getText().toString());
                        ((MainActivity) context).updateContactList();
                        ((MainActivity) context).scrollToContact(name);
                        editContactDialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //
                    }
                });
        editContactDialog = builder.create();
        editContactDialog.show();
    }


}