package com.upt.cti.smartwallet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private TextView tStatus;
    private EditText eSearch, eIncome, eExpenses;
    private DatabaseReference databaseReference;
    private String currentMonth="";
    private String searchMonth="";
    private ValueEventListener databaseListener;
    private ArrayList<String> monthsDB = new ArrayList<>();
    private Spinner monthSpinner;

    private final static String PREF_SETTINGS = "pref_settings";
    private SharedPreferences prefUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Calendar mCalendar = Calendar.getInstance();
        currentMonth = mCalendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()).toLowerCase();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference();
        getViewObjects();
        getMonthsFromDB();

        monthsDB.add("--Please select month");

        SharedPreferences sharedPreferences = getSharedPreferences(PREF_SETTINGS, MODE_PRIVATE);
        eSearch.setText(sharedPreferences.getString("KEY1", ""));
    }

    public void getViewObjects(){
        tStatus = (TextView) findViewById(R.id.tStatus);
        eSearch = (EditText) findViewById(R.id.eSearch);
        eIncome = (EditText) findViewById(R.id.eIncome);
        eExpenses = (EditText) findViewById(R.id.eExpenses);
    }

    public void getMonthsFromDB(){
        DatabaseReference monthsRef = databaseReference.child("calendar");
        monthsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Iterator<DataSnapshot> items = snapshot.getChildren().iterator();

                while(items.hasNext()){
                    DataSnapshot item = items.next();
                    String month;
                    month = item.getKey().toString();
                    monthsDB.add(month);
                }

                setSpinnerItems();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    public void setSpinnerItems(){
        monthSpinner = (Spinner) findViewById(R.id.monthSpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, monthsDB);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner.setAdapter(adapter);
    }

    public void saveToSharedPreferences(String month){
        prefUser = getSharedPreferences(PREF_SETTINGS, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = prefUser.edit();
        editor.putString("KEY1",month);
        editor.apply();
    }

    public void clicked(View view){
        switch(view.getId()){
            case R.id.bSearch:
                if(!eSearch.getText().toString().isEmpty()){
                    // We can search either by typing in the search field or by choosing from the spinner.
                    // The spinner has priority, therefore if something is selected in the spinner,
                    // the search will be done accordingly. If we want to search by text field,
                    // the option "--Please select month" should be chosen in the spinner
                    
                    searchMonth = monthSpinner.getSelectedItem().toString();
                    if(searchMonth.equals("--Please select month"))
                    {
                        searchMonth = eSearch.getText().toString().toLowerCase();
                    }
                    saveToSharedPreferences(searchMonth);
                    tStatus.setText("Searching...");
                    createNewDBListener();
                }else{
                    Toast.makeText(this, "Search field may not be empty", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.bUpdate:
                if(!eIncome.getText().toString().isEmpty() && !eExpenses.getText().toString().isEmpty()){
                    String expenses = eExpenses.getText().toString();
                    String income = eIncome.getText().toString();
                    databaseReference.child("calendar").child(currentMonth).child("expenses").setValue(expenses);
                    databaseReference.child("calendar").child(currentMonth).child("income").setValue(income);

                    Toast.makeText(this, "Income and expenses updated for " + currentMonth, Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    private void createNewDBListener() {
        if (databaseReference != null && currentMonth != null && databaseListener != null)
            databaseReference.child("calendar").child(currentMonth).removeEventListener(databaseListener);

        databaseListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(searchMonth)){
                    String month, income, expenses;
                    month = dataSnapshot.getKey();
                    income = dataSnapshot.child(searchMonth).child("income").getValue().toString();
                    expenses = dataSnapshot.child(searchMonth).child("expenses").getValue().toString();

                    Float Fincome, Fexpenses;
                    Fincome = Float.parseFloat(income);
                    Fexpenses = Float.parseFloat(expenses);

                    MonthlyExpenses monthlyExpense = new MonthlyExpenses(month, Fincome, Fexpenses);

                    eIncome.setText(String.valueOf(monthlyExpense.getIncome()));
                    eExpenses.setText(String.valueOf(monthlyExpense.getExpenses()));
                    tStatus.setText("Found entry for " + searchMonth);
                }else{
                    tStatus.setText("No entries found");
                }

            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        };

        databaseReference.child("calendar").addValueEventListener(databaseListener);
    }

}