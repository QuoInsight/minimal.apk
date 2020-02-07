package com.quoinsight.minimal;

public class commonUtil {

  //////////////////////////////////////////////////////////////////////

  static final public String getChineseDateStr() {
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
      java.lang.reflect.Method getInstance = Calendar.getDeclaredMethod("getInstance", ULocale);
      Object chineseCalendar = getInstance.invoke(
        null, ULocale.getConstructor(String.class).newInstance("zh_CN@calendar=chinese")
      );
      java.lang.reflect.Method get = chineseCalendar.getClass().getMethod("get", int.class);
      java.lang.reflect.Method getActualMaximum = chineseCalendar.getClass().getMethod("getActualMaximum", int.class);
      java.lang.reflect.Field IS_LEAP_MONTH = chineseCalendar.getClass().getField("IS_LEAP_MONTH");
        // notApplicable for IS_LEAP_MONTH ==> .getDeclaredField(<notForInheritedFields>);  <field>.setAccessible(true);

      String dateStr = ( ((int)IS_LEAP_MONTH.get(chineseCalendar)==1) ? "闰" : "" )
        + monthArr[(int)get.invoke(chineseCalendar, java.util.Calendar.MONTH)] + "月" // MONTH==0..11
        + dayArr[(int)get.invoke(chineseCalendar, java.util.Calendar.DAY_OF_MONTH)-1] // DAY_OF_MONTH==1..31
        + "⁄" + (int)getActualMaximum.invoke(chineseCalendar, java.util.Calendar.DAY_OF_MONTH)
        + hourArr[(int)((int)get.invoke(chineseCalendar, java.util.Calendar.HOUR_OF_DAY)+1)/2] + "时" // HOUR_OF_DAY==0..23
        ;

      //! above is to avoid the java.lang.NoClassDefFoundError at runtime !
      /*
        android.icu.util.Calendar chineseCalendar = android.icu.util.Calendar.getInstance(
          new android.icu.util.ULocale("zh_CN@calendar=chinese")  // android.icu.util.ChineseCalendar.getInstance();
        );
        String dateStr = ( (chineseCalendar.IS_LEAP_MONTH==1) ? "闰" : "" )
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

  static final public String getDateStr(String format) {
    java.text.SimpleDateFormat simpleDateFormat = new java.text.SimpleDateFormat(
      format, java.util.Locale.getDefault()
    );
    return simpleDateFormat.format(new java.util.Date());
  }

  //////////////////////////////////////////////////////////////////////

  static final public String joinStringList(String sep, java.util.List<String> lst) {
    // String.join() is not support by some versions --> use our local function joinStringList() instead
    String str = "";
    if (lst.size() > 0) {
      for (String s : lst) str = str + s + sep;
      str = str.substring(0, str.length()-sep.length()); // remove the last separator
    }
    return str;
  }

  //////////////////////////////////////////////////////////////////////

}
