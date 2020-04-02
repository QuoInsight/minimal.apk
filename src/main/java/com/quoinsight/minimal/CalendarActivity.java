package com.quoinsight.minimal;
/*
  https://abhiandroid.com/ui/calendarview
  https://developer.android.com/reference/android/widget/CalendarView
*/

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CalendarView;
import android.widget.Toast;

public class CalendarActivity extends android.app.Activity {

  CalendarView simpleCalendarView;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.calendaractivity);

    try {

      android.widget.TextView txt1 = (android.widget.TextView) findViewById(R.id.txt1);;
        txt1.setGravity(android.view.Gravity.CENTER_HORIZONTAL);  // txt1.setText("Hello world!\n[" + commonUtil.getDateStr("yyyy-MM-dd HH:mm:ss") + "]");
        txt1.setText(android.text.Html.fromHtml(
          "Current Date: <br><small><small>[" + commonUtil.getDateStr("yyyy-MM-dd HH:mm:ss") + "]</small></small>"
            + "<br><font size='1.75em'>" + commonUtil.getChineseDateStr() + "</font>"
        )); // Current Date:\n[2020-01-09 十二月十五⁄30巳时 09:06:21] --> Hello world\n[2020-01-09 09:06:21]\n十二月十五⁄30巳时

      simpleCalendarView = (CalendarView) findViewById(R.id.simpleCalendarView); // get the reference of CalendarView
        simpleCalendarView.setFocusedMonthDateColor(Color.RED); // set the red color for the dates of  focused month
        simpleCalendarView.setUnfocusedMonthDateColor(Color.BLUE); // set the yellow color for the dates of an unfocused month
        simpleCalendarView.setSelectedWeekBackgroundColor(Color.RED); // red color for the selected week's background
        simpleCalendarView.setWeekSeparatorLineColor(Color.GREEN); // green color for the week separator line
        // perform setOnDateChangeListener event on CalendarView
        simpleCalendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
          @Override public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
            // display the selected date by using a toast
            Toast.makeText(getApplicationContext(), dayOfMonth + "/" + month + "/" + year, Toast.LENGTH_LONG).show();
          }
        });

      findViewById(R.id.button9).setOnClickListener( // --> .\src\main\res\layout\otheractivity.xml
        new android.view.View.OnClickListener() {
          public void onClick(android.view.View v) {
            //this.finishAffinity();
            finishAndRemoveTask();
          }
        }
      );

     /*
      android.widget.TextView txt9 = (android.widget.TextView) findViewById(R.id.txt9);  // --> .\src\main\res\layout\otheractivity.xml
        txt9.setLinksClickable(true);  // do not setAutoLinkMask !! txt9.setAutoLinkMask(android.text.util.Linkify.ALL);
        txt9.setMovementMethod(android.text.method.LinkMovementMethod.getInstance());
        txt9.setText(android.text.Html.fromHtml(txt9.getText().toString()));
     */

    } catch(Exception e) {

      commonGui.writeMessage(this, "CalendarActivity.findViewById", e.getMessage());
      return;

    }

  }

}
