package com.upt.cti.smartwallet;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    private TextView tStatus;
    private EditText eSearch, eIncome, eExpenses;
    private DatabaseReference databaseReference;
    private String currentMonth="";
    private ValueEventListener databaseListener;

    private final static String PREF_SETTINGS = "pref_settings";
    private SharedPreferences prefUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //currentMonth = new SimpleDateFormat("MMM").format(Calendar.getInstance().getTime());

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference();
        getViewObjects();
        saveToSharedPreferences();
    }

    public void getViewObjects(){
        tStatus = (TextView) findViewById(R.id.tStatus);
        eSearch = (EditText) findViewById(R.id.eSearch);
        eIncome = (EditText) findViewById(R.id.eIncome);
        eExpenses = (EditText) findViewById(R.id.eExpenses);
    }

    public void saveToSharedPreferences(){
        prefUser = getSharedPreferences(PREF_SETTINGS, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = prefUser.edit();
        editor.putString("KEY1",currentMonth);
    }

    public void clicked(View view){
        switch(view.getId()){
            case R.id.bSearch:
                if(!eSearch.getText().toString().isEmpty()){
                    String monthCurrent = prefUser.getString("KEY1",currentMonth);
                    currentMonth = eSearch.getText().toString();
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
                }
                break;
        }
    }

    private void createNewDBListener() {

        databaseListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(currentMonth)){
                    String month, income, expenses;
                    month = dataSnapshot.getKey();
                    income = dataSnapshot.child(currentMonth).child("income").getValue().toString();
                    expenses = dataSnapshot.child(currentMonth).child("expenses").getValue().toString();

                    Float Fincome, Fexpenses;
                    Fincome = Float.parseFloat(income);
                    Fexpenses = Float.parseFloat(expenses);

                    MonthlyExpenses monthlyExpense = new MonthlyExpenses(month, Fincome, Fexpenses);

                    eIncome.setText(String.valueOf(monthlyExpense.getIncome()));
                    eExpenses.setText(String.valueOf(monthlyExpense.getExpenses()));
                    tStatus.setText("Found entry for " + currentMonth);
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