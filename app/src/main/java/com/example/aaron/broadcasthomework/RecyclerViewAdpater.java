package com.example.aaron.broadcasthomework;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.aaron.greendao.db.PhoneStatus;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by User on 10/15/2016.
 */

public class RecyclerViewAdpater extends RecyclerView.Adapter<RecyclerViewAdpater.ViewHolder>{

    List<PhoneStatus> phoneStatusList;

    public RecyclerViewAdpater(List<PhoneStatus> statusList){
        phoneStatusList = statusList;
    }

    @Override
    public RecyclerViewAdpater.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.phone_status, parent, false);

        // Return a new holder instance
        RecyclerViewAdpater.ViewHolder viewHolder = new RecyclerViewAdpater.ViewHolder(contactView);
        return viewHolder;
    }

    public void updateList(List<PhoneStatus> newList){
        this.phoneStatusList = newList;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(RecyclerViewAdpater.ViewHolder holder, int position) {
        SimpleDateFormat dt = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
        PhoneStatus phoneStat = phoneStatusList.get(position);

        holder.idId.setText("DB_ID: " + String.valueOf(phoneStat.getId()));
        if(phoneStat.getIsPowerOn()){
            holder.isPowerOn.setText("Power Connected");
        }
        else{
            holder.isPowerOn.setText("Power Disconnected");
        }

        if(phoneStat.getDate() != null){
            holder.date.setText(dt.format(phoneStat.getDate()));
        }
        else{
            holder.date.setText("null");
        }
        if(phoneStat.getUsbCharge()){
            holder.usbCharge.setText("Usb charge: OK");
        }
        else{
            holder.usbCharge.setText("Usb charge:  -- ");
        }
        if(phoneStat.getAcCharge()){
            holder.acCharge.setText("AC charge: OK");
        }
        else{
            holder.acCharge.setText("AC charge: --  ");
        }
        holder.batteryLevel.setText("Battery level " + phoneStat.getBatteryLevel().toString() + "%");
        holder.address.setText("Address: " + phoneStat.getAddress());

    }

    @Override
    public int getItemCount() {
        return phoneStatusList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView idId;
        public TextView isPowerOn;
        public TextView date;
        public TextView usbCharge;
        public TextView acCharge;
        public TextView batteryLevel;
        public TextView address;

        public ViewHolder(View itemView) {
            super(itemView);

            idId = ((TextView) itemView.findViewById(R.id.dbId));
            isPowerOn = ((TextView) itemView.findViewById(R.id.isPowerOn));
            date = ((TextView) itemView.findViewById(R.id.date));
            usbCharge = ((TextView) itemView.findViewById(R.id.usbCharge));
            acCharge = ((TextView) itemView.findViewById(R.id.acCharge));
            batteryLevel = ((TextView) itemView.findViewById(R.id.batteryLevel));
            address = ((TextView) itemView.findViewById(R.id.address));

        }
    }

}
