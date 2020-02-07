package com.quoinsight.minimal;

public class commonGui {

  //////////////////////////////////////////////////////////////////////

  static final public void writeMessage(android.content.Context parentContext, String tag, String msg, String...args) {  // varargs
    android.widget.Toast.makeText(
      parentContext, tag + ": " +  msg,
        android.widget.Toast.LENGTH_LONG
    ).show();  // .setDuration(int duration)
    //android.util.Log.e(tag, msg);
    return;
  }

  static final public void msgBox(android.content.Context parentContext, String title, String msg) {
    android.app.AlertDialog.Builder alrt = new android.app.AlertDialog.Builder(parentContext);
    alrt.setTitle(title).setMessage(msg).setCancelable(false).setPositiveButton("OK", null).show();
  }

  //////////////////////////////////////////////////////////////////////

  static final public android.widget.EditText makeEditTextSelectableReadOnly(android.widget.EditText edtxt) {
    // https://medium.com/@anna.domashych/selectable-read-only-multiline-text-field-on-android-169c27c55408
    edtxt.setShowSoftInputOnFocus(false);  edtxt.setPadding(10,10,10,10);  edtxt.setBackgroundColor(android.graphics.Color.parseColor("#E8E8E8"));
    edtxt.setHorizontallyScrolling(true);  // android:scrollHorizontally="true" doesn't work

    edtxt.setCustomSelectionActionModeCallback(
      // remove all menu items except "Copy", "Select All", "Share" 
      new android.view.ActionMode.Callback() {
        @Override public boolean onPrepareActionMode(android.view.ActionMode mode, android.view.Menu menu) {
          try {
            android.view.MenuItem copyText = menu.findItem(android.R.id.copy);
            android.view.MenuItem selectAll = menu.findItem(android.R.id.selectAll);
            android.view.MenuItem shareText = menu.findItem(android.R.id.shareText);
            menu.clear();  menu.add(0, android.R.id.copy, 0, copyText.getTitle());
            menu.add(0, android.R.id.selectAll, 0, selectAll.getTitle());
            menu.add(0, android.R.id.shareText, 0, shareText.getTitle());
          } catch (Exception e) {}
          return true;
        }
        @Override public boolean onCreateActionMode(android.view.ActionMode mode, android.view.Menu menu) {return true; }
        @Override public boolean onActionItemClicked(android.view.ActionMode mode, android.view.MenuItem item) { return false; }
        @Override public void onDestroyActionMode(android.view.ActionMode mode) {}
      }
    );

    try {
      edtxt.setCustomInsertionActionModeCallback(
        // completely block a menu which appears when a user taps on cursor
        new android.view.ActionMode.Callback() {
          @Override public boolean onCreateActionMode(android.view.ActionMode mode, android.view.Menu menu) { return false; }
          @Override public boolean onPrepareActionMode(android.view.ActionMode mode, android.view.Menu menu) { return false; }
          @Override public boolean onActionItemClicked(android.view.ActionMode mode, android.view.MenuItem item) { return false; }
          @Override public void onDestroyActionMode(android.view.ActionMode mode) { }
        }
      );
    } catch (Exception e) {}

    return edtxt;
  }

  //////////////////////////////////////////////////////////////////////

  public static float px2dp(android.util.DisplayMetrics displayMetrics, float px) {
    return (float)android.util.TypedValue.applyDimension(
      android.util.TypedValue.COMPLEX_UNIT_PX, px, displayMetrics
    );
  }

  public static float dp2px(android.util.DisplayMetrics displayMetrics, float dp) {
    return (float)android.util.TypedValue.applyDimension(
      android.util.TypedValue.COMPLEX_UNIT_DIP, dp, displayMetrics
    );
  }

  public float[] getVwSzDimensionPx(android.view.View v) {
    android.util.DisplayMetrics m = v.getContext().getResources().getDisplayMetrics();
    android.view.ViewGroup.LayoutParams p = v.getLayoutParams();
    return new float[] {dp2px(m, p.width), dp2px(m, p.height)};
  }

  //////////////////////////////////////////////////////////////////////

}
