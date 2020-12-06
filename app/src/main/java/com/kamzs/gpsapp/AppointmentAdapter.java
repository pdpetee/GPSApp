package com.kamzs.gpsapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.Layout;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder> {
    private DatabaseHelper dbHelper;
    private List<Appointment> appointmentList;
    private Appointment currentItem;
    private View view;

    @NonNull
    @Override
    public AppointmentAdapter.AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.element_recyclerview, parent, false);
        dbHelper = new DatabaseHelper(parent.getContext());
        return new AppointmentViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull AppointmentAdapter.AppointmentViewHolder holder, final int position) {
        final Context context = view.getContext();
        currentItem = appointmentList.get(position);
        holder.titleTV.setText(currentItem.getClientName());
        holder.descriptionTV.setText(currentItem.getDescription());
        holder.dateTV.setText(currentItem.getAppointmentDate() + " " + currentItem.getAppointmentTime());

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((Activity)context).finish();
                Intent intent = new Intent(context, ViewAppointmentActivity.class);
                intent.putExtra("appointment_class", appointmentList.get(position));
                context.startActivity(intent);
            }
        });

        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
                popupMenu.inflate(R.menu.appointment_menu);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.option_1:
                                Intent intentEdit = new Intent(context, CreateAppointmentActivity.class);
                                intentEdit.putExtra("createNew", false);
                                intentEdit.putExtra("appointment", appointmentList.get(position));
                                context.startActivity(intentEdit);
                                return true;

                            case R.id.option_2:
                                new AlertDialog
                                        .Builder(context)
                                        .setTitle("Delete appointment")
                                        .setMessage("Are you sure you want to delete this entry?")
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dbHelper.deleteData(appointmentList.get(position).getAppointmentID());
                                                Intent intentDel = new Intent(context, MainActivity.class);
                                                ((Activity)context).finish();
                                                context.startActivity(intentDel);
                                            }
                                        })
                                        .setNegativeButton("No", null)
                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                        .show();
                                return true;

                            default:
                                return false;
                        }
                    }
                });
                popupMenu.show();
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        if (appointmentList != null){
            return appointmentList.size();
        }
        return 0;
    }

    public static class AppointmentViewHolder extends RecyclerView.ViewHolder {

        public TextView titleTV, dateTV, descriptionTV;

        public AppointmentViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTV = itemView.findViewById(R.id.title_textview);
            dateTV = itemView.findViewById(R.id.date_textview);
            descriptionTV = itemView.findViewById(R.id.description_textview);
        }
    }

    public AppointmentAdapter(List<Appointment> mAppointmentList){
        appointmentList = mAppointmentList;
    }
}
