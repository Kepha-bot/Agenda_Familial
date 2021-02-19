package com.example.agenda_familial;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EventRecyclerAdapter extends RecyclerView.Adapter<EventRecyclerAdapter.MyViewHolder> {
    Context context;
    ArrayList<Events> EventsList;
    DBOpenHelper DBInstance;

    public EventRecyclerAdapter(Context context, ArrayList<Events> arrayList) {
        this.context = context;
        this.EventsList = arrayList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.show_events_rowlayout,parent,false);
        return new  MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Events events = EventsList.get(position);
        holder.Event.setText(events.getEVENT());
        holder.Date.setText(events.getDATE());
        holder.Time.setText(events.getTIME());
        holder.Delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteCalendarEvent(events.getEVENT(), events.getDATE(),events.getTIME());
                EventsList.remove(position);
                notifyDataSetChanged();
            }
        });

        Calendar DateCalendar = Calendar.getInstance();
        DateCalendar.setTime(convertStringToDate(events.getDATE()));
        Calendar TimeCalendar = Calendar.getInstance();
        TimeCalendar.setTime(convertStringToTime(events.getTIME()));
    }

    @Override
    public int getItemCount() {
        return EventsList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        TextView Date, Event, Time;
        Button Delete;
        ImageButton setAlarm;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            Date = itemView.findViewById(R.id.eventdate);
            Event = itemView.findViewById(R.id.EventName);
            Time = itemView.findViewById(R.id.eventime);
            Delete = itemView.findViewById(R.id.delete);
        }
    }

    private Date convertStringToDate(String dateInString){
        java.text.SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.FRENCH);
        Date date = null;
        try {
            date = format.parse(dateInString);

        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    private Date convertStringToTime(String dateInString){
        java.text.SimpleDateFormat format = new SimpleDateFormat("kk:mm", Locale.FRENCH);
        Date date = null;
        try {
            date = format.parse(dateInString);

        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    private void deleteCalendarEvent(String event,String date,String time){
        DBInstance = new DBOpenHelper(context);
        SQLiteDatabase database = DBInstance.getWritableDatabase();
        DBInstance.DeleteEvent(event, date, time, database);
        DBInstance.close();
    }
}
