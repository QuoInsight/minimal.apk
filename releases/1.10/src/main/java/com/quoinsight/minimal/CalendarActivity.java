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
import android.widget.Toast;

public class CalendarActivity extends android.app.Activity {

  //////////////////////////////////////////////////////////////////////

  static final public String getChineseDateStr(java.util.Date date) {
    /*
      [BUG?] https://www.v2ex.com/t/505601
      原生安卓的农历显示居然是错的？
      2018-11-07 == 农历九月【大】三十?? 十月【大】初一?? 
      2018-12-06 == 农历十月【小】廿九?? 十月【大】三十??
      农历有时可能出现两个大月，也可以连续出现两个小月 
    */
    final String[] dayArr = new String[] { "初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十", "十一", "十二", "十三", "十四",
      "十五", "十六", "十七", "十八", "十九", "二十", "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十", "卅一"
     }, monthArr = new String[] { "正", "二", "三", "四", "五", "六", "七", "八", "九", "十", "十一", "十二"
     // }, hourArr = new String[] { "子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥", "子" }
     }, hourArr = new String[] { "子zǐ", "丑chǒu", "寅yín", "卯mǎo", "辰chén", "巳sì", "午wǔ", "未wèi", "申shēn", "酉yǒu", "戌xū", "亥hài", "子zǐ" }
    ;

    String debug = "";
    try {
      Class<?> ULocale = Class.forName("android.icu.util.ULocale");  // some devices or versions may not support this!
      Class<?> Calendar = Class.forName("android.icu.util.Calendar");
      Class<?> ChineseCalendar = Class.forName("android.icu.util.ChineseCalendar");
      Object chineseCalendar = ChineseCalendar.getConstructor(java.util.Date.class).newInstance(date);

      java.lang.reflect.Method set = chineseCalendar.getClass().getMethod("set", int.class, int.class, int.class);
      java.lang.reflect.Method get = chineseCalendar.getClass().getMethod("get", int.class);
      java.lang.reflect.Method getActualMaximum = chineseCalendar.getClass().getMethod("getActualMaximum", int.class);
      //java.lang.reflect.Field IS_LEAP_MONTH = chineseCalendar.getClass().getField("IS_LEAP_MONTH");
        // notApplicable for IS_LEAP_MONTH ==> .getDeclaredField(<notForInheritedFields>);  <field>.setAccessible(true);

      // set.invoke(chineseCalendar, year, month, dayOfMonth); // year, month, dayOfMonth 此皆为农历的年月日
      String dateStr = ( ((int)get.invoke(chineseCalendar, Calendar.getDeclaredField("IS_LEAP_MONTH").get(null))==1) ? "闰" : "" )
        + monthArr[(int)get.invoke(chineseCalendar, java.util.Calendar.MONTH)] + "月" // MONTH==0..11
        + dayArr[(int)get.invoke(chineseCalendar, java.util.Calendar.DAY_OF_MONTH)-1] // DAY_OF_MONTH==1..31
        + "⁄" + (int)getActualMaximum.invoke(chineseCalendar, java.util.Calendar.DAY_OF_MONTH)
        ;

      //! above is to avoid the java.lang.NoClassDefFoundError at runtime !
      /*
        android.icu.util.Calendar chineseCalendar = android.icu.util.Calendar.getInstance(
          new android.icu.util.ULocale("zh_CN@calendar=chinese")  // android.icu.util.ChineseCalendar.getInstance();
        );  // chineseCalendar.set(year, month, dayOfMonth); // year, month, dayOfMonth 此皆为农历的年月日
            // chineseCalendar.setTimeInMillis(date.getTime()); // 此为阳历
        android.icu.util.ChineseCalendar chineseCalendar = android.icu.util.ChineseCalendar(date); // 此为阳历

        String dateStr = ( (chineseCalendar.get(android.icu.util.Calendar.IS_LEAP_MONTH)==1) ? "闰" : "" )
          + monthArr[chineseCalendar.get(java.util.Calendar.MONTH)] + "月" // MONTH==0..11
          + dayArr[chineseCalendar.get(java.util.Calendar.DAY_OF_MONTH)-1] // DAY_OF_MONTH==1..31
          + "⁄" + chineseCalendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
          + hourArr[(int)(chineseCalendar.get(java.util.Calendar.HOUR_OF_DAY)+1)/2] + "时" // HOUR_OF_DAY==0..23
          ; // https://www.ntu.edu.sg/home/ehchua/programming/java/DateTimeCalendar.html
      */
      return dateStr;
    } catch(Exception e) {
      // some devices or versions may not support this
      // return debug + "::" + e.getMessage();
    }
    return "<ChineseDateUnavailable/>";
  }

  //////////////////////////////////////////////////////////////////////

  android.widget.DatePicker simpleCalendar;

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

     /*
      android.widget.CalendarView simpleCalendar = (android.widget.CalendarView) findViewById(R.id.simpleCalendar);
        simpleCalendar.setOnDateChangeListener(new android.widget.CalendarView.OnDateChangeListener() {
          @Override public void onSelectedDayChange(android.widget.CalendarView view, int year, int month, int dayOfMonth) {
          }
        });
     */
      android.widget.DatePicker simpleCalendar = (android.widget.DatePicker) findViewById(R.id.simpleCalendar);
       try {
        simpleCalendar.setOnDateChangedListener(new android.widget.DatePicker.OnDateChangedListener() {
          @Override public void onDateChanged(android.widget.DatePicker view, int year, int month, int dayOfMonth) {
            String dateString = dayOfMonth + "/" + month + "/" + year;
            // java.util.Date date = new java.util.Date(year, month, dayOfMonth); // deprecated: As of JDK version 1.1, replaced by Calendar.set()

            java.util.Calendar cal = java.util.Calendar.getInstance(); cal.set(year, month, dayOfMonth);
            java.util.Date date = cal.getTime();
            dateString = (new java.text.SimpleDateFormat(
              "dd-MMM-yyyy", java.util.Locale.getDefault()
            )).format(date) +  " " + getChineseDateStr(date);

            // display the selected date by using a toast
            Toast.makeText(getApplicationContext(), dateString, Toast.LENGTH_SHORT).show();
          }
        });
       } catch(Exception e) {
        commonGui.writeMessage(this, "CalendarActivity.setOnDateChangedListener", e.getMessage());
       }

      findViewById(R.id.button9).setOnClickListener( // --> .\src\main\res\layout\otheractivity.xml
        new android.view.View.OnClickListener() {
          public void onClick(android.view.View v) {
            //this.finishAffinity();
            finishAndRemoveTask();
          }
        }
      );

      android.widget.TextView txt2 = (android.widget.TextView) findViewById(R.id.txt2);  // --> .\src\main\res\layout\otheractivity.xml
        txt2.setLinksClickable(true);  // do not setAutoLinkMask !! txt2.setAutoLinkMask(android.text.util.Linkify.ALL);
        txt2.setMovementMethod(android.text.method.LinkMovementMethod.getInstance());
        txt2.setText(android.text.Html.fromHtml(txt2.getText().toString()));

      android.widget.TextView txt9 = (android.widget.TextView) findViewById(R.id.txt9);  // --> .\src\main\res\layout\otheractivity.xml
        txt9.setLinksClickable(true);  // do not setAutoLinkMask !! txt9.setAutoLinkMask(android.text.util.Linkify.ALL);
        txt9.setMovementMethod(android.text.method.LinkMovementMethod.getInstance());
        txt9.setText(android.text.Html.fromHtml(txt9.getText().toString()));

    } catch(Exception e) {

      commonGui.writeMessage(this, "CalendarActivity.findViewById", e.getMessage());
      return;

    }

  }

}
