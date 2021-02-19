package com.example.agenda_familial;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class CustomCalendarView extends LinearLayout {
    ImageButton NextButton, PreviousButton;
    TextView CurrentDate;
    GridView GridView;
    Context Context;
    DBOpenHelper DBInstance;

    private static final int MAX_GRID_VIEW = 42;
    Calendar CalendarInstance = Calendar.getInstance(Locale.FRENCH);
    SimpleDateFormat DateFormat = new SimpleDateFormat("MMMM yyyy", Locale.FRENCH);
    SimpleDateFormat MonthFormat = new SimpleDateFormat("MMMM", Locale.FRENCH);
    SimpleDateFormat YearFormat = new SimpleDateFormat("yyyy", Locale.FRENCH);
    SimpleDateFormat DateEventFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.FRENCH);

    MyGridAdapter myGridAdapter;
    AlertDialog AlertDialog;

    List<Date> DateList = new ArrayList<>();
    List<Events> EventsList = new ArrayList<>();

    public CustomCalendarView(Context context){
        super(context);
    }

    public CustomCalendarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.Context = context;
        InitializeLayout();
        SetUpCalendar();

        PreviousButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                CalendarInstance.add(Calendar.MONTH, -1);
                SetUpCalendar();
            }
        });

        NextButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                CalendarInstance.add(Calendar.MONTH, +1);
                SetUpCalendar();
            }
        });

        GridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setCancelable(true);
                View addView = LayoutInflater.from(parent.getContext()).inflate(R.layout.add_new_event_layout, null);
                EditText EventName = addView.findViewById(R.id.EventName);
                TextView EventTime = addView.findViewById(R.id.EventTime);
                ImageButton SelectTime = addView.findViewById(R.id.SetEventTime);
                Calendar dateCalendar = Calendar.getInstance();
                dateCalendar.setTime(DateList.get(position));

                Button AddEvent = addView.findViewById(R.id.AddEvent);
                SelectTime.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Calendar calendar = Calendar.getInstance();
                        int hour = calendar.get(Calendar.HOUR_OF_DAY);
                        int minute = calendar.get(Calendar.MINUTE);
                        TimePickerDialog timePickerDialog = new TimePickerDialog(addView.getContext(), R.style.Theme_AppCompat_Dialog, new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                Calendar c = Calendar.getInstance();
                                c.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                c.set(Calendar.MINUTE, minute);
                                c.setTimeZone(TimeZone.getDefault());
                                SimpleDateFormat HourFormat = new SimpleDateFormat("K:mm a", Locale.FRENCH);
                                String event_time = HourFormat.format(c.getTime());
                                EventTime.setText(event_time);
                            }
                        }, hour, minute, false);
                        timePickerDialog.show();
                    }
                });

                final String date = DateEventFormat.format(DateList.get(position));
                final String month = MonthFormat.format(DateList.get(position));
                final String year = YearFormat.format(DateList.get(position));

                AddEvent.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        SaveEvent(EventName.getText().toString(), EventTime.getText().toString(), date, month, year);
                        SetUpCalendar();
                        AlertDialog.dismiss();
                    }
                });

                builder.setView(addView);
                AlertDialog = builder.create();
                AlertDialog.show();
            }
        });

        GridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                String date = DateEventFormat.format(DateList.get(position));
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setCancelable(true);
                View showView = LayoutInflater.from(parent.getContext()).inflate(R.layout.show_events_layout, null);
                RecyclerView recyclerView = showView.findViewById(R.id.EventsRearView);
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(showView.getContext());
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setHasFixedSize(true);
                EventRecyclerAdapter eventRecyclerAdapter = new EventRecyclerAdapter(showView.getContext(), CollectEventsByDate(date));
                recyclerView.setAdapter(eventRecyclerAdapter);
                eventRecyclerAdapter.notifyDataSetChanged();

                builder.setView(showView);
                AlertDialog = builder.create();
                AlertDialog.show();

                AlertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        SetUpCalendar();
                    }
                });
                return true;
            }
        });
    }

    private ArrayList<Events> CollectEventsByDate(String date){
        ArrayList<Events> arrayList = new ArrayList<>();
        DBInstance = new DBOpenHelper(Context);
        SQLiteDatabase sqLiteDatabase = DBInstance.getReadableDatabase();
        Cursor cursor = DBInstance.ReadEvents(date,sqLiteDatabase);
        while (cursor.moveToNext()){
            String event = cursor.getString(cursor.getColumnIndex(DBStructure.EVENT));
            String Time = cursor.getString(cursor.getColumnIndex(DBStructure.TIME));
            String Date = cursor.getString(cursor.getColumnIndex(DBStructure.DATE));
            String month = cursor.getString(cursor.getColumnIndex(DBStructure.MONTH));
            String year = cursor.getString(cursor.getColumnIndex(DBStructure.YEAR));
            Events events = new Events(event,Time,Date,month,year);
            arrayList.add(events);
        }
        cursor.close();
        DBInstance.close();
        return arrayList;
    }

    private ArrayList<Events> CollectEventsPerMonth(String date){
        ArrayList<Events> arrayList = new ArrayList<>();
        DBInstance = new DBOpenHelper(Context);
        SQLiteDatabase sqLiteDatabase = DBInstance.getReadableDatabase();
        Cursor cursor = DBInstance.ReadEvents(date,sqLiteDatabase);
        while (cursor.moveToNext()){
            String event = cursor.getString(cursor.getColumnIndex(DBStructure.EVENT));
            String Time = cursor.getString(cursor.getColumnIndex(DBStructure.TIME));
            String Date = cursor.getString(cursor.getColumnIndex(DBStructure.DATE));
            String month = cursor.getString(cursor.getColumnIndex(DBStructure.MONTH));
            String year = cursor.getString(cursor.getColumnIndex(DBStructure.YEAR));
            Events events = new Events(event,Time,Date,month,year);
            arrayList.add(events);
        }
        cursor.close();
        DBInstance.close();

        return arrayList;
    }

    public CustomCalendarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void SaveEvent(String event, String time, String date, String month, String year){
        DBInstance = new DBOpenHelper(Context);
        SQLiteDatabase database = DBInstance.getWritableDatabase();
        DBInstance.SaveEvent(event, time, date, month, year, database);
        DBInstance.close();
        Toast.makeText(Context, "Event saved", Toast.LENGTH_SHORT).show();
    }

    private void InitializeLayout(){
        LayoutInflater inflater = (LayoutInflater) Context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.calendar_layout,this);
        NextButton = view.findViewById(R.id.NextButton);
        PreviousButton = view.findViewById(R.id.PreviousButton);
        CurrentDate = view.findViewById(R.id.CurrentDate);
        GridView = view.findViewById(R.id.gridview);
    }

    private void SetUpCalendar(){
        String StartDate = DateFormat.format(CalendarInstance.getTime());
        CurrentDate.setText(StartDate);
        DateList.clear();
        Calendar monthCalendar = (Calendar) CalendarInstance.clone();
        monthCalendar.set(Calendar.DAY_OF_MONTH, 1);
        int FirstDayOfMonth = monthCalendar.get(Calendar.DAY_OF_WEEK)-2;
        monthCalendar.add(Calendar.DAY_OF_MONTH, -FirstDayOfMonth);
        CollectEventsPerMonth(MonthFormat.format(CalendarInstance.getTime()), YearFormat.format(CalendarInstance.getTime()));

        while(DateList.size()< MAX_GRID_VIEW){
            DateList.add(monthCalendar.getTime());
            monthCalendar.add(Calendar.DAY_OF_MONTH,1);
        }

        myGridAdapter = new MyGridAdapter(Context, DateList, CalendarInstance, EventsList);
        GridView.setAdapter(myGridAdapter);
    }

    private void CollectEventsPerMonth(String Month, String year){
        EventsList.clear();
        DBInstance = new DBOpenHelper(Context);
        SQLiteDatabase database = DBInstance.getReadableDatabase();
        Cursor cursor = DBInstance.ReadEventsPerMonth(Month, year, database);
        while (cursor.moveToNext()){
            String event = cursor.getString(cursor.getColumnIndex(DBStructure.EVENT));
            String time = cursor.getString(cursor.getColumnIndex(DBStructure.TIME));
            String date = cursor.getString(cursor.getColumnIndex(DBStructure.DATE));
            String month = cursor.getString(cursor.getColumnIndex(DBStructure.MONTH));
            String Year = cursor.getString(cursor.getColumnIndex(DBStructure.YEAR));
            Events events = new Events(event, time, date, month, Year);
            EventsList.add(events);
        }
        cursor.close();
        DBInstance.close();
    }
}
