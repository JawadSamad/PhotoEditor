package com.jawad.photoeditor;

import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

public class Month {
    MainActivity main;

    public Month(MainActivity main){
        super();
        this.main = main;
    }

    public String date() {

        //Storing the months in a String array
        String[] monthName = {"januari", "februari",
                "maart", "april", "mei", "juni", "juli",
                "augustus", "september", "oktober", "november",
                "december"};

        //Creating a new calendar
        Calendar cal = Calendar.getInstance();
        String month = monthName[cal.get(Calendar.MONTH)];

        //Assigning the date to a string which shows the month
        String date = "Medewerker van de maand " + month;
        //Toast.makeText(main, date, Toast.LENGTH_LONG).show();
        return date;
    }
}
