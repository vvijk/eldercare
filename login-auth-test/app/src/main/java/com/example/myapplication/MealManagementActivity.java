package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.util.FocusOnNewLine;
import com.example.myapplication.util.LogStorage;
import com.example.myapplication.util.MealStorage;
import com.example.myapplication.util.TimeFixer;

public class MealManagementActivity extends AppCompatActivity implements View.OnClickListener {

    LinearLayout scrolledLayout=null;
    Button btn_mealPlan = null;
    Button btn_recipients = null;
    Button btn_back = null;

    View.OnClickListener btn_listener = null;

    MealStorage getMealStorage() {
        return ((MainApp) getApplicationContext()).mealStorage;
    }
    LogStorage getLogStorage() {
        return ((MainApp) getApplicationContext()).logStorage;
    }

    FocusOnNewLine.ViewCallback saveCallback = new FocusOnNewLine.ViewCallback() {
        @Override
        public void run(EditText view) {
            saveAllMeals();
        }
    };

    boolean showingPatients = true;

    String caregiverUID = "";
    int currentCaregiverId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_manage);
        scrolledLayout = findViewById(R.id.meal_scroll);
        btn_mealPlan = findViewById(R.id.btn_meal_plan);
        btn_recipients = findViewById(R.id.btn_recipients);
        btn_back = findViewById(R.id.btn_back);

        btn_back.setOnClickListener(this);

        getMealStorage().initDBConnection();
        getLogStorage().initDBConnection();

        dbLibrary lib = new dbLibrary(this);
        caregiverUID = lib.getUserID();

        if(caregiverUID == null) {
            Intent intent = getIntent();
            // The activity that created meal management activity should pass caregiverUID
            caregiverUID = intent.getStringExtra("caregiverUID");
            if (caregiverUID == null) {
                Toast.makeText(this, getResources().getString(R.string.str_caregiverUID_was_null), Toast.LENGTH_LONG).show();
                caregiverUID = "0GOIORHtHQRvqWAhib6svaTGBHp1"; // TODO: Don't hardcode
            }
        }
        currentCaregiverId = getMealStorage().idFromCaregiverUID(caregiverUID);

        btn_listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view == btn_mealPlan) {
                    refreshMealPlan();
                } else if (view == btn_recipients){
                    if(!showingPatients)
                        saveAllMeals();
                    refreshPatients();
                }
            }
        };
        btn_mealPlan.setOnClickListener(btn_listener);
        btn_recipients.setOnClickListener(btn_listener);
        getMealStorage().pushRefresher_caregiver(currentCaregiverId, new Runnable() {
            @Override
            public void run() {
                if(showingPatients)
                    refreshPatients();
                else
                    refreshMealPlan(); // TODO: All text view and layouts are deleted and added each time childEventListener runs. It would be better to wait with refreshing until childEventListener is done.
            }
        });
    }
    @Override
    protected void onDestroy() {
        getMealStorage().popRefresher();
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        Integer deleteMealIndex = (Integer)view.getTag(R.id.clicked_deleteMeal);
        Integer recipientId = (Integer)view.getTag(R.id.tag_recipientId);
        Boolean addMeal = (Boolean)view.getTag(R.id.tag_template_add_meal);

        if(deleteMealIndex != null) {
            saveAllMeals();
            getMealStorage().caregiver_template_deleteMeal(currentCaregiverId,deleteMealIndex);
    //            refreshMeals();
        } else if(recipientId != null) {
            // Button button = (Button) view;
//            System.out.println("Press " + getMealStorage().nameOfPatient(recipientId));

            Intent intent = new Intent(getApplicationContext(), MealActivity.class);
            intent.putExtra("recipientUID", getMealStorage().UIDOfCaretaker(recipientId));
            intent.putExtra("caregiverUID", getMealStorage().UIDOfCaregiver(currentCaregiverId));
            startActivity(intent);
            refreshPatients();
        } else if(addMeal != null) {
            saveAllMeals();
            getMealStorage().caregiver_template_addMeal(currentCaregiverId, getResources().getString(R.string.default_meal_name));
        } else if(btn_back != null) {
            saveAllMeals();

            if(isTaskRoot()) {
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
            }

            finish();
        }
    }
    void saveAllMeals() {
        if(showingPatients)
            return;
        for(int i=0;i<scrolledLayout.getChildCount();i++) {
            if(!(scrolledLayout.getChildAt(i) instanceof LinearLayout))
                continue;
            LinearLayout itemLayout = (LinearLayout)scrolledLayout.getChildAt(i);
            Integer mealIndex = (Integer)itemLayout.getTag(R.id.template_mealIndex);
            if(mealIndex == null)
                continue;
            if(!getMealStorage().caregiver_template_isMealIndexValid(currentCaregiverId, mealIndex))
                continue;

            if(itemLayout.getChildCount()>1) {
                // only save if itemLayout is being edited. if it's not being edited then the meal plan name should be up to date.
                LinearLayout headLayout = (LinearLayout) itemLayout.getChildAt(0);

                TextView view_time = (TextView)headLayout.getChildAt(0);
                TextView view_name = (TextView)headLayout.getChildAt(1);

                String[] split = view_time.getText().toString().split(":");
                if(split.length>1) {
                    try {
                        int hour = Integer.parseInt(split[0]);
                        int minute = Integer.parseInt(split[1]);
                        getMealStorage().caregiver_template_setHourOfMeal(currentCaregiverId, mealIndex, hour);
                        getMealStorage().caregiver_template_setMinuteOfMeal(currentCaregiverId, mealIndex, minute);
                    } catch (Exception e) {
                        // TODO(Emarioo): Handle parse exception. Toast the user?
                        //   Tell the user which meal was bad. We shouldn't tell the user that here because this function
                        //   will save the content right before exiting this activity and then it will be to late for
                        //   the user to do anything about the bad format.
                        Toast.makeText(this, R.string.meal_time_bad_format, Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(this, R.string.meal_time_missing_colon, Toast.LENGTH_LONG).show();
                }
                getMealStorage().caregiver_template_setNameOfMeal(currentCaregiverId, mealIndex, view_name.getText().toString());

                if(itemLayout.getChildCount()>1) {
                    LinearLayout subLayout = (LinearLayout) itemLayout.getChildAt(1);
                    TextView view_desc = (TextView) subLayout.getChildAt(0);
                    getMealStorage().caregiver_template_setDescriptionOfMeal(currentCaregiverId,mealIndex, view_desc.getText().toString());
                    // Toast.makeText(this,"saved "+view_desc.getText(),Toast.LENGTH_LONG).show();
                }
            }
        }
    }
    void refreshPatients() {
        showingPatients = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            btn_mealPlan.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.almost_black)));
            btn_recipients.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.purple_dark)));
        } else {
            // Colors won't work
        }
        // btn_add.setText(R.string.str_add_recipient);
        // TODO(Emarioo): Optimize by reusing view instead of removing them?
        //   Another optimization would be to hide the list instead of removing and recreating them.
        scrolledLayout.removeAllViews();
        int recipientCount = getMealStorage().caretakerCountOfCaregiver(currentCaregiverId);

        if(recipientCount == 0){
            TextView textView = new TextView(this);
            textView.setText(getResources().getString(R.string.str_no_recipients));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25); // TODO(Emarioo): Don't hardcode text size
            textView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            textView.setGravity(Gravity.CENTER);
            scrolledLayout.addView(textView);
        } else {
            for (int i = 0; i < recipientCount; i++) {
                int caretakerId = getMealStorage().caretakerIdFromIndex(currentCaregiverId, i);
                String name = getMealStorage().nameOfCaretaker(caretakerId);

                LinearLayout itemLayout = new LinearLayout(this);
                itemLayout.setOrientation(LinearLayout.VERTICAL);
                itemLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                scrolledLayout.addView(itemLayout);

                LinearLayout headLayout = new LinearLayout(this);
                headLayout.setOrientation(LinearLayout.HORIZONTAL);
                headLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                itemLayout.addView(headLayout);

                TextView textview = new TextView(this);
                textview.setText(name);
                textview.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 30); // TODO(Emarioo): Don't hardcode text size
                textview.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));

                headLayout.addView(textview);

                LinearLayout buttonLayout = new LinearLayout(this);
                buttonLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                buttonLayout.setGravity(Gravity.RIGHT);

                headLayout.addView(buttonLayout);

                Button button = new Button(this);
                button.setText(">");
                // button.setText(R.string.recipient_meals_edit);
                button.setAllCaps(false);
                button.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20); // TODO(Emarioo): Don't hardcode text size
                button.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                button.setTag(R.id.tag_recipientId, caretakerId);
                button.setOnClickListener(this);

                buttonLayout.addView(button);
            }
        }

        EditText addCaretakerInputText = new EditText(this);
        addCaretakerInputText.setHint(getString(R.string.write_recipient_name));
        addCaretakerInputText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        addCaretakerInputText.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        scrolledLayout.addView(addCaretakerInputText);

        // Skapa knappen för att lägga till recipient
        Button addButton = new Button(this);
        addButton.setText(getString(R.string.str_add_recipient));
        addButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        addButton.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String caretakerFromInput = String.valueOf(addCaretakerInputText.getText());
                if (TextUtils.isEmpty(caretakerFromInput) || !android.util.Patterns.EMAIL_ADDRESS.matcher(caretakerFromInput).matches()) {
                    Toast.makeText(MealManagementActivity.this, getResources().getString(R.string.str_invalid_email), Toast.LENGTH_SHORT).show();
                    return;
                }
                dbLibrary db = new dbLibrary(MealManagementActivity.this);
                db.getCaretakerUidByEmail(caretakerFromInput, new dbLibrary.UserUidCallback() {
                    @Override
                    public void onUserUidFound(String uid) {
                        db.addCaretakerToGiver(db.getUserID(), uid, new dbLibrary.CaretakerAddCallback() {
                            @Override
                            public void onCaretakerAdded(String message) {
                                getLogStorage().submitLog(LogStorage.Category.PATIENT_ADD, uid, caregiverUID, null);
                                Toast.makeText(MealManagementActivity.this, getResources().getString(R.string.fmt_added_recipient, caretakerFromInput), Toast.LENGTH_SHORT).show();
                            }
                            @Override
                            public void onCaretakerAddError(String errorMessage) {
                                Toast.makeText(MealManagementActivity.this, getResources().getString(R.string.fmt_exists_recipient, caretakerFromInput), Toast.LENGTH_SHORT).show();
                            }
                        });
                        addCaretakerInputText.setText("");
                    }
                    @Override
                    public void onUserUidNotFound() {
                        // Handle the case where no user with the specified email was found
                        Toast.makeText(MealManagementActivity.this, getResources().getString(R.string.fmt_unknown_recipient, caretakerFromInput), Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onUserUidError(String errorMessage) {
                        Toast.makeText(MealManagementActivity.this, "ERROR, onUserUidError()", Toast.LENGTH_SHORT).show();
                        // Handle the error
                    }
                });
            }
        });
        scrolledLayout.addView(addButton);

        Button delButton = new Button(this);
        delButton.setText(getString(R.string.str_remove_recipient));
        delButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        delButton.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        delButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String caretakerFromInput = String.valueOf(addCaretakerInputText.getText());
                if (TextUtils.isEmpty(caretakerFromInput) || !android.util.Patterns.EMAIL_ADDRESS.matcher(caretakerFromInput).matches()) {
                    Toast.makeText(MealManagementActivity.this, getResources().getString(R.string.str_invalid_email), Toast.LENGTH_SHORT).show();
                    return;
                }
                dbLibrary db = new dbLibrary(MealManagementActivity.this);
                db.getCaretakerUidByEmail(caretakerFromInput, new dbLibrary.UserUidCallback() {
                    @Override
                    public void onUserUidFound(String uid) {
                        db.removeCaretakerFromGiver(db.getUserID(), uid, new dbLibrary.GeneralCallback() {
                            @Override
                            public void onSuccess(String message) {
                                Toast.makeText(MealManagementActivity.this, getResources().getString(R.string.fmt_removed_recipient, caretakerFromInput), Toast.LENGTH_SHORT).show();
                            }
                            @Override
                            public void onFailure(String errorMessage) {
                                Toast.makeText(MealManagementActivity.this, getResources().getString(R.string.fmt_not_exist_recipient, caretakerFromInput), Toast.LENGTH_SHORT).show();
                            }
                        });
                        addCaretakerInputText.setText("");
                    }
                    @Override
                    public void onUserUidNotFound() {
                        // Handle the case where no user with the specified email was found
                        Toast.makeText(MealManagementActivity.this, getResources().getString(R.string.fmt_unknown_recipient, caretakerFromInput), Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onUserUidError(String errorMessage) {
                        Toast.makeText(MealManagementActivity.this, "ERROR, onUserUidError()", Toast.LENGTH_SHORT).show();
                        // Handle the error
                    }
                });
            }
        });
        scrolledLayout.addView(delButton);
    }
    void refreshMealPlan(){
        // btn_add.setText(R.string.str_add_meal_plan);
        showingPatients = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            btn_mealPlan.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.purple_dark)));
            btn_recipients.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.almost_black)));
        } else {
            // Colors won't work
        }
        scrolledLayout.removeAllViews();

        int[] sortedMeals_index = getMealStorage().caregiver_template_sortedMealIndices(currentCaregiverId);

        if(sortedMeals_index.length == 0){
            TextView textView = new TextView(this);
            textView.setText(getResources().getString(R.string.str_no_meals));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25); // TODO(Emarioo): Don't hardcode text size
            textView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            textView.setGravity(Gravity.CENTER);
            scrolledLayout.addView(textView);
        } else {
            for (int i = 0; i < sortedMeals_index.length; i++) {
                int mealIndex = sortedMeals_index[i];
                if (!getMealStorage().caregiver_template_isMealIndexValid(currentCaregiverId, mealIndex))
                    continue;

                String name = getMealStorage().caregiver_template_nameOfMeal(currentCaregiverId, mealIndex);
                int hour = getMealStorage().caregiver_template_hourOfMeal(currentCaregiverId, mealIndex);
                int minute = getMealStorage().caregiver_template_minuteOfMeal(currentCaregiverId, mealIndex);
                String description = getMealStorage().caregiver_template_descriptionOfMeal(currentCaregiverId, mealIndex);

                LinearLayout itemLayout = new LinearLayout(this);
                itemLayout.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                itemLayout.setOrientation(LinearLayout.VERTICAL);
                itemLayout.setGravity(Gravity.LEFT);
                itemLayout.setTag(R.id.template_mealIndex, mealIndex);
                scrolledLayout.addView(itemLayout);

                // layout for the meal's name and time
                LinearLayout headLayout = new LinearLayout(this);
                headLayout.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                headLayout.setGravity(Gravity.LEFT);
                itemLayout.addView(headLayout);

                refreshMealHeader(headLayout, true, name, Helpers.FormatTime(hour, minute));

                itemLayout.setBackgroundColor(getResources().getColor(R.color.dry_green_brigher));

                LinearLayout subLayout = new LinearLayout(itemLayout.getContext());
                subLayout.setOrientation(LinearLayout.VERTICAL);
                subLayout.setBackgroundColor(getResources().getColor(R.color.dry_green));
                subLayout.setPadding(25, 10, 25, 12); // TODO(Emarioo): don't hardcode padding
                subLayout.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                itemLayout.addView(subLayout);

                // NOTE(Emarioo): Disabling editing of description when you click in on a recipient.
                //  This is because you would modify the meal plan and thus changing the meals
                //  for other recipients too. We could allow you to edit description if each
                //  recipient has some kind of individual plan which wouldn't affect other recipients.
                TextView editText = null;
                // if(curPatientId!=0){
                //     editText = new TextView(itemLayout.getContext());
                // } else {
                editText = new EditText(itemLayout.getContext());
                ((EditText) editText).addTextChangedListener(new FocusOnNewLine((EditText) editText, saveCallback));
                // }

                editText.setText(description);
                editText.setHint(R.string.str_no_description);
                editText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 24); // TODO(Emarioo): Don't hardcode text size
                editText.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                subLayout.addView(editText);

                LinearLayout buttonLayout = new LinearLayout(this);
                buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
                buttonLayout.setGravity(Gravity.RIGHT);
                buttonLayout.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                subLayout.addView(buttonLayout);

                Button delButton = new Button(itemLayout.getContext());
                delButton.setAllCaps(false);
                delButton.setText(getResources().getString(R.string.str_delete));
                delButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18); // TODO(Emarioo): Don't hardcode text size
                delButton.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                GradientDrawable shape = new GradientDrawable();
                shape.setCornerRadius(16);
                shape.setColor(getResources().getColor(R.color.delete_button));
                delButton.setBackground(shape);
                delButton.setTag(R.id.clicked_deleteMeal, mealIndex);
                delButton.setOnClickListener(this);
                buttonLayout.addView(delButton);
            }
        }
        {
            LinearLayout footLayout = new LinearLayout(this);
            footLayout.setOrientation(LinearLayout.HORIZONTAL);
            footLayout.setGravity(Gravity.CENTER);
            footLayout.setPadding(0,20,0,0);
            footLayout.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            scrolledLayout.addView(footLayout);

            Button addButton = new Button(footLayout.getContext());
            addButton.setAllCaps(false);
            addButton.setText(getResources().getString(R.string.str_add_meal));
            addButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18); // TODO(Emarioo): Don't hardcode text size
            addButton.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            GradientDrawable shape = new GradientDrawable();
            // shape.setPadding(10,0,10,0);
            shape.setCornerRadius(16);
            shape.setColor(getResources().getColor(R.color.purple));
            addButton.setBackground(shape);
            addButton.setPadding(30,0,30,0);
            addButton.setTextColor(getResources().getColor(R.color.black));
            addButton.setTag(R.id.tag_template_add_meal, true);
            addButton.setOnClickListener(this);
            footLayout.addView(addButton);
        }
    }
    void refreshMealHeader(LinearLayout headLayout, boolean editable, String mealName, String mealTime) {
        // NOTE(Emarioo): view_mealName may be EditText or TextView. We can use TextView since EditText
        //   from it inherits.
        if (mealTime == null){
            TextView view_mealTime = (TextView)headLayout.getChildAt(0);
            mealTime = view_mealTime.getText().toString();
        }
        if(mealName == null) {
            TextView view_mealName = (TextView) headLayout.getChildAt(1);
            mealName = view_mealName.getText().toString();
        }

        if(headLayout.getChildCount()>0)
            headLayout.removeViews(0,headLayout.getChildCount());

        TextView view_mealName = null;
        TextView view_mealTime = null;

        if (editable) {
            view_mealName = new EditText(this);
            ((EditText)view_mealName).addTextChangedListener(new FocusOnNewLine((EditText)view_mealName,saveCallback));
            view_mealTime = new EditText(this);
            ((EditText)view_mealTime).addTextChangedListener(new TimeFixer((EditText)view_mealTime));
            ((EditText)view_mealTime).addTextChangedListener(new FocusOnNewLine((EditText)view_mealTime,saveCallback));
        } else {
            view_mealName = new TextView(this);
            view_mealTime = new TextView(this);
        }
        view_mealTime.setText(mealTime);
        view_mealTime.setPadding(25, 8, 25, 8); // TODO(Emarioo): don't hardcode padding
        view_mealTime.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 30); // TODO(Emarioo): Don't hardcode text size
        view_mealTime.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        headLayout.addView(view_mealTime);

        view_mealName.setText(mealName);
        view_mealName.setPadding(25, 8, 25, 8); // TODO(Emarioo): don't hardcode padding
        view_mealName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 30); // TODO(Emarioo): Don't hardcode text size
        view_mealName.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        headLayout.addView(view_mealName);

    }
}