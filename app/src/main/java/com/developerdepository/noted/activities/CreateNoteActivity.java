package com.developerdepository.noted.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.developerdepository.noted.R;
import com.developerdepository.noted.database.NotesDatabase;
import com.developerdepository.noted.entities.Note;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.tapadoo.alerter.Alerter;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.util.UIUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CreateNoteActivity extends AppCompatActivity {

    private ImageButton backBtn, saveBtn, optionsMenu;
    private View viewSubtitleIndicator;
    private EditText inputNoteTitle, inputNoteSubtitle, inputNote;
    private TextView textDateTime;

    private String selectedNoteColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);

        initViews();
        setActionOnViews();

        selectedNoteColor = "#" + Integer.toHexString(ContextCompat.getColor(getApplicationContext(), R.color.colorDefaultNoteColor) & 0x00ffffff);

        initMiscellaneous();
        setSubtitleIndicatorColor();
    }

    private void initViews() {
        backBtn = findViewById(R.id.create_note_back_btn);
        saveBtn = findViewById(R.id.create_note_save_btn);
        optionsMenu = findViewById(R.id.create_note_options_menu);
        viewSubtitleIndicator = findViewById(R.id.view_indicator_subtitle);
        inputNoteTitle = findViewById(R.id.input_note_title);
        inputNoteSubtitle = findViewById(R.id.input_note_subtitle);
        inputNote = findViewById(R.id.input_note);
        textDateTime = findViewById(R.id.text_date_time);
    }

    private void setActionOnViews() {
        backBtn.setOnClickListener(v -> onBackPressed());

        inputNoteTitle.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        inputNoteSubtitle.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        inputNoteTitle.setRawInputType(InputType.TYPE_CLASS_TEXT);
        inputNoteSubtitle.setRawInputType(InputType.TYPE_CLASS_TEXT);

        KeyboardVisibilityEvent.setEventListener(CreateNoteActivity.this, isOpen -> {
            if (!isOpen) {
                inputNoteTitle.clearFocus();
                inputNoteSubtitle.clearFocus();
                inputNote.clearFocus();
            }
        });

        textDateTime.setText(
                new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm a", Locale.getDefault())
                        .format(new Date())
        );

        saveBtn.setOnClickListener(v -> saveNote());
    }

    private void saveNote() {
        UIUtil.hideKeyboard(CreateNoteActivity.this);
        if (inputNoteTitle.getText().toString().trim().isEmpty()) {
            Alerter.create(CreateNoteActivity.this)
                    .setText("Note Title can't be kept empty!")
                    .setTextAppearance(R.style.ErrorAlert)
                    .setBackgroundColorRes(R.color.errorColor)
                    .setIcon(R.drawable.ic_error)
                    .setDuration(3000)
                    .enableIconPulse(true)
                    .enableVibration(true)
                    .disableOutsideTouch()
                    .enableProgress(true)
                    .setProgressColorInt(getResources().getColor(android.R.color.white))
                    .show();
            return;
        } else if (inputNoteSubtitle.getText().toString().trim().isEmpty() && inputNote.getText().toString().trim().isEmpty()) {
            Alerter.create(CreateNoteActivity.this)
                    .setText("Note can't be kept empty!")
                    .setTextAppearance(R.style.ErrorAlert)
                    .setBackgroundColorRes(R.color.errorColor)
                    .setIcon(R.drawable.ic_error)
                    .setDuration(3000)
                    .enableIconPulse(true)
                    .enableVibration(true)
                    .disableOutsideTouch()
                    .enableProgress(true)
                    .setProgressColorInt(getResources().getColor(android.R.color.white))
                    .show();
            return;
        }

        final Note note = new Note();
        note.setTitle(inputNoteTitle.getText().toString().trim());
        note.setSubtitle(inputNoteSubtitle.getText().toString().trim());
        note.setNoteText(inputNote.getText().toString().trim());
        note.setDateTime(textDateTime.getText().toString().trim());
        note.setColor(selectedNoteColor);

        @SuppressLint("StaticFieldLeak")
        class SaveNoteTask extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                NotesDatabase.getDatabase(getApplicationContext()).noteDao().insertNote(note);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        }

        new SaveNoteTask().execute();
    }

    private void initMiscellaneous() {
        final ConstraintLayout layoutMiscellaneous = findViewById(R.id.layout_miscellaneous);
        final BottomSheetBehavior<ConstraintLayout> bottomSheetBehavior = BottomSheetBehavior.from(layoutMiscellaneous);
        optionsMenu.setOnClickListener(v -> {
            if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            } else {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

        final ImageView checkColor1 = layoutMiscellaneous.findViewById(R.id.check_color1);
        final ImageView checkColor2 = layoutMiscellaneous.findViewById(R.id.check_color2);
        final ImageView checkColor3 = layoutMiscellaneous.findViewById(R.id.check_color3);
        final ImageView checkColor4 = layoutMiscellaneous.findViewById(R.id.check_color4);
        final ImageView checkColor5 = layoutMiscellaneous.findViewById(R.id.check_color5);
        final ImageView checkColor6 = layoutMiscellaneous.findViewById(R.id.check_color6);
        final ImageView checkColor7 = layoutMiscellaneous.findViewById(R.id.check_color7);
        final ImageView checkColor8 = layoutMiscellaneous.findViewById(R.id.check_color8);

        layoutMiscellaneous.findViewById(R.id.view_color1).setOnClickListener(v -> {
            selectedNoteColor = "#" + Integer.toHexString(ContextCompat.getColor(getApplicationContext(), R.color.colorDefaultNoteColor) & 0x00ffffff);
            checkColor1.setImageResource(R.drawable.ic_check);
            checkColor2.setImageResource(0);
            checkColor3.setImageResource(0);
            checkColor4.setImageResource(0);
            checkColor5.setImageResource(0);
            checkColor6.setImageResource(0);
            checkColor7.setImageResource(0);
            checkColor8.setImageResource(0);
            setSubtitleIndicatorColor();
        });

        layoutMiscellaneous.findViewById(R.id.view_color2).setOnClickListener(v -> {
            selectedNoteColor = "#" + Integer.toHexString(ContextCompat.getColor(getApplicationContext(), R.color.colorNote2) & 0x00ffffff);
            checkColor1.setImageResource(0);
            checkColor2.setImageResource(R.drawable.ic_check);
            checkColor3.setImageResource(0);
            checkColor4.setImageResource(0);
            checkColor5.setImageResource(0);
            checkColor6.setImageResource(0);
            checkColor7.setImageResource(0);
            checkColor8.setImageResource(0);
            setSubtitleIndicatorColor();
        });

        layoutMiscellaneous.findViewById(R.id.view_color3).setOnClickListener(v -> {
            selectedNoteColor = "#" + Integer.toHexString(ContextCompat.getColor(getApplicationContext(), R.color.colorNote3) & 0x00ffffff);
            checkColor1.setImageResource(0);
            checkColor2.setImageResource(0);
            checkColor3.setImageResource(R.drawable.ic_check);
            checkColor4.setImageResource(0);
            checkColor5.setImageResource(0);
            checkColor6.setImageResource(0);
            checkColor7.setImageResource(0);
            checkColor8.setImageResource(0);
            setSubtitleIndicatorColor();
        });

        layoutMiscellaneous.findViewById(R.id.view_color4).setOnClickListener(v -> {
            selectedNoteColor = "#" + Integer.toHexString(ContextCompat.getColor(getApplicationContext(), R.color.colorNote4) & 0x00ffffff);
            checkColor1.setImageResource(0);
            checkColor2.setImageResource(0);
            checkColor3.setImageResource(0);
            checkColor4.setImageResource(R.drawable.ic_check);
            checkColor5.setImageResource(0);
            checkColor6.setImageResource(0);
            checkColor7.setImageResource(0);
            checkColor8.setImageResource(0);
            setSubtitleIndicatorColor();
        });

        layoutMiscellaneous.findViewById(R.id.view_color5).setOnClickListener(v -> {
            selectedNoteColor = "#" + Integer.toHexString(ContextCompat.getColor(getApplicationContext(), R.color.colorNote5) & 0x00ffffff);
            checkColor1.setImageResource(0);
            checkColor2.setImageResource(0);
            checkColor3.setImageResource(0);
            checkColor4.setImageResource(0);
            checkColor5.setImageResource(R.drawable.ic_check);
            checkColor6.setImageResource(0);
            checkColor7.setImageResource(0);
            checkColor8.setImageResource(0);
            setSubtitleIndicatorColor();
        });

        layoutMiscellaneous.findViewById(R.id.view_color6).setOnClickListener(v -> {
            selectedNoteColor = "#" + Integer.toHexString(ContextCompat.getColor(getApplicationContext(), R.color.colorNote6) & 0x00ffffff);
            checkColor1.setImageResource(0);
            checkColor2.setImageResource(0);
            checkColor3.setImageResource(0);
            checkColor4.setImageResource(0);
            checkColor5.setImageResource(0);
            checkColor6.setImageResource(R.drawable.ic_check);
            checkColor7.setImageResource(0);
            checkColor8.setImageResource(0);
            setSubtitleIndicatorColor();
        });

        layoutMiscellaneous.findViewById(R.id.view_color7).setOnClickListener(v -> {
            selectedNoteColor = "#" + Integer.toHexString(ContextCompat.getColor(getApplicationContext(), R.color.colorNote7) & 0x00ffffff);
            checkColor1.setImageResource(0);
            checkColor2.setImageResource(0);
            checkColor3.setImageResource(0);
            checkColor4.setImageResource(0);
            checkColor5.setImageResource(0);
            checkColor6.setImageResource(0);
            checkColor7.setImageResource(R.drawable.ic_check);
            checkColor8.setImageResource(0);
            setSubtitleIndicatorColor();
        });

        layoutMiscellaneous.findViewById(R.id.view_color8).setOnClickListener(v -> {
            selectedNoteColor = "#" + Integer.toHexString(ContextCompat.getColor(getApplicationContext(), R.color.colorNote8) & 0x00ffffff);
            checkColor1.setImageResource(0);
            checkColor2.setImageResource(0);
            checkColor3.setImageResource(0);
            checkColor4.setImageResource(0);
            checkColor5.setImageResource(0);
            checkColor6.setImageResource(0);
            checkColor7.setImageResource(0);
            checkColor8.setImageResource(R.drawable.ic_check);
            setSubtitleIndicatorColor();
        });
    }

    @SuppressLint("ResourceType")
    private void setSubtitleIndicatorColor() {
        GradientDrawable gradientDrawable = (GradientDrawable) viewSubtitleIndicator.getBackground();
        gradientDrawable.setColor(Color.parseColor(selectedNoteColor));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}