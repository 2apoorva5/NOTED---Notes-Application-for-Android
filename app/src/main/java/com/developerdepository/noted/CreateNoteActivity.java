package com.developerdepository.noted;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.text.InputType;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.developerdepository.noted.database.NotesDatabase;
import com.developerdepository.noted.entities.Note;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.shreyaspatil.MaterialDialog.MaterialDialog;
import com.tapadoo.alerter.Alerter;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.util.UIUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import maes.tech.intentanim.CustomIntent;

public class CreateNoteActivity extends AppCompatActivity {

    private ImageButton backBtn, saveBtn, removeUrlBtn, removeImgBtn, addActionsBtn, optionsMenu;
    private View viewSubtitleIndicator;
    private EditText inputNoteTitle, inputNoteSubtitle, inputNote;
    private ImageView imageNote;
    private TextView textDateTime, textUrl, textReadingNote;

    private BottomSheetBehavior<ConstraintLayout> bottomSheetAddActions;
    private BottomSheetBehavior<ConstraintLayout> bottomSheetMiscellaneous;

    private String selectedNoteColor;
    private String selectedImagePath;

    private AlertDialog dialogAddURL;
    private AlertDialog dialogReadingNote;

    private Note alreadyAvailableNote;

    public static final int REQUEST_CODE_TAKE_PHOTO = 4;
    public static final int REQUEST_CODE_SELECT_IMAGE = 5;
    public static final int REQUEST_CODE_VOICE_NOTE = 6;

    private TextToSpeech textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);

        initViews();
        setActionOnViews();

        selectedNoteColor = "#" + Integer.toHexString(ContextCompat.getColor(getApplicationContext(), R.color.colorDefaultNoteColor) & 0x00ffffff);
        selectedImagePath = "";

        if(getIntent().getBooleanExtra("isViewOrUpdate", false)) {
            alreadyAvailableNote = (Note) getIntent().getSerializableExtra("note");
            setViewOrUpdateNote();
        }

        removeUrlBtn.setOnClickListener(v -> {
            textUrl.setText(null);
            textUrl.setVisibility(View.GONE);
            removeUrlBtn.setVisibility(View.GONE);
        });

        removeImgBtn.setOnClickListener(v -> {
            imageNote.setImageBitmap(null);
            imageNote.setVisibility(View.GONE);
            removeImgBtn.setVisibility(View.GONE);
            selectedImagePath = "";
        });

        if(getIntent().getBooleanExtra("isFromQuickActions", false)) {
            String type = getIntent().getStringExtra("quickActionType");
            if(type != null) {
                if(type.equals("image")) {
                    selectedImagePath = getIntent().getStringExtra("imagePath");
                    Glide.with(imageNote.getContext()).load(selectedImagePath).centerCrop().into(imageNote);
                    imageNote.setVisibility(View.VISIBLE);
                    removeImgBtn.setVisibility(View.VISIBLE);
                } else if(type.equals("URL")) {
                    textUrl.setText(getIntent().getStringExtra("URL"));
                    textUrl.setVisibility(View.VISIBLE);
                    removeUrlBtn.setVisibility(View.VISIBLE);
                } else if(type.equals("voiceNote")) {
                    inputNote.setText(getIntent().getStringExtra("inputText"));
                }
            }
        }

        initAddActions();
        initMiscellaneous();
        setSubtitleIndicatorColor();
    }

    private void initViews() {
        backBtn = findViewById(R.id.create_note_back_btn);
        saveBtn = findViewById(R.id.create_note_save_btn);
        removeUrlBtn = findViewById(R.id.remove_url_btn);
        removeImgBtn = findViewById(R.id.remove_img_btn);
        addActionsBtn = findViewById(R.id.create_note_add_actions);
        optionsMenu = findViewById(R.id.create_note_options_menu);
        viewSubtitleIndicator = findViewById(R.id.view_indicator_subtitle);
        inputNoteTitle = findViewById(R.id.input_note_title);
        inputNoteSubtitle = findViewById(R.id.input_note_subtitle);
        inputNote = findViewById(R.id.input_note);
        imageNote = findViewById(R.id.image_note);
        textDateTime = findViewById(R.id.text_date_time);
        textUrl = findViewById(R.id.text_url);
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
                String.format("Edited %s", new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm a", Locale.getDefault())
                        .format(new Date()))
        );

        saveBtn.setOnClickListener(v -> saveNote());
    }

    private void setViewOrUpdateNote() {
        inputNoteTitle.setText(alreadyAvailableNote.getTitle());
        inputNoteSubtitle.setText(alreadyAvailableNote.getSubtitle());
        inputNote.setText(alreadyAvailableNote.getNoteText());
        textDateTime.setText(alreadyAvailableNote.getDateTime());
        if(alreadyAvailableNote.getImagePath() != null && !alreadyAvailableNote.getImagePath().trim().isEmpty()) {
            Glide.with(imageNote.getContext()).load(alreadyAvailableNote.getImagePath()).centerCrop().into(imageNote);
            imageNote.setVisibility(View.VISIBLE);
            removeImgBtn.setVisibility(View.VISIBLE);
            selectedImagePath = alreadyAvailableNote.getImagePath();
        }

        if(alreadyAvailableNote.getWebLink() != null && !alreadyAvailableNote.getWebLink().trim().isEmpty()) {
            textUrl.setText(alreadyAvailableNote.getWebLink());
            textUrl.setVisibility(View.VISIBLE);
            removeUrlBtn.setVisibility(View.VISIBLE);
        }
    }

    private void saveNote() {
        UIUtil.hideKeyboard(CreateNoteActivity.this);
        if (inputNoteTitle.getText().toString().trim().isEmpty()) {
            Alerter.create(CreateNoteActivity.this)
                    .setText("Note Title can't be kept empty!")
                    .setTextAppearance(R.style.InfoAlert)
                    .setBackgroundColorRes(R.color.infoColor)
                    .setIcon(R.drawable.ic_info)
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
                    .setTextAppearance(R.style.InfoAlert)
                    .setBackgroundColorRes(R.color.infoColor)
                    .setIcon(R.drawable.ic_info)
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

        if(imageNote.getVisibility() == View.VISIBLE) {
            note.setImagePath(selectedImagePath);
        }

        if(textUrl.getVisibility() == View.VISIBLE && removeUrlBtn.getVisibility() == View.VISIBLE) {
            note.setWebLink(textUrl.getText().toString().trim());
        }

        if(alreadyAvailableNote != null) {
            note.setId(alreadyAvailableNote.getId());
        }

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

    private void initAddActions() {
        final ConstraintLayout layoutAddActions = findViewById(R.id.layout_add_actions);
        bottomSheetAddActions = BottomSheetBehavior.from(layoutAddActions);

        addActionsBtn.setOnClickListener(v -> {
            if (bottomSheetAddActions.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetAddActions.setState(BottomSheetBehavior.STATE_EXPANDED);
            } else {
                bottomSheetAddActions.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }

            if (bottomSheetMiscellaneous.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetMiscellaneous.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

        layoutAddActions.findViewById(R.id.layout_take_photo).setOnClickListener(v -> {
            bottomSheetAddActions.setState(BottomSheetBehavior.STATE_COLLAPSED);
            takePhoto();
        });

        layoutAddActions.findViewById(R.id.layout_add_image).setOnClickListener(v -> {
            bottomSheetAddActions.setState(BottomSheetBehavior.STATE_COLLAPSED);
            selectImage();
        });

        layoutAddActions.findViewById(R.id.layout_voice_note).setOnClickListener(v -> {
            bottomSheetAddActions.setState(BottomSheetBehavior.STATE_COLLAPSED);
            voiceNote();
        });

        layoutAddActions.findViewById(R.id.layout_add_url).setOnClickListener(v -> {
            bottomSheetAddActions.setState(BottomSheetBehavior.STATE_COLLAPSED);
            showAddURLDialog();
        });
    }

    private void takePhoto() {
        ImagePicker.Companion.with(CreateNoteActivity.this)
                .cameraOnly()
                .crop()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .start(REQUEST_CODE_TAKE_PHOTO);
    }

    private void selectImage() {
        ImagePicker.Companion.with(CreateNoteActivity.this)
                .galleryOnly()
                .crop()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .start(REQUEST_CODE_SELECT_IMAGE);
    }

    private void voiceNote() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak something to add note!");
        startActivityForResult(intent, REQUEST_CODE_VOICE_NOTE);
    }

    private String getPathFromUri(Uri contentUri) {
        String filePath;
        Cursor cursor = getContentResolver()
                .query(contentUri, null, null, null, null);
        if(cursor == null) {
            filePath = contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex("_data");
            filePath = cursor.getString(index);
            cursor.close();
        }
        return filePath;
    }

    private void showAddURLDialog() {
        if(dialogAddURL == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateNoteActivity.this);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.layout_add_url,
                    (ViewGroup) findViewById(R.id.layout_add_url_container)
            );
            builder.setView(view);

            dialogAddURL = builder.create();
            if(dialogAddURL.getWindow() != null) {
                dialogAddURL.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }

            final EditText inputURL = view.findViewById(R.id.input_url);
            inputURL.requestFocus();

            view.findViewById(R.id.dialog_add_btn).setOnClickListener(v -> {
                if(inputURL.getText().toString().trim().isEmpty()) {
                    Toast.makeText(CreateNoteActivity.this, "Enter URL", Toast.LENGTH_SHORT).show();
                } else if(!Patterns.WEB_URL.matcher(inputURL.getText().toString().trim()).matches()) {
                    Toast.makeText(CreateNoteActivity.this, "Enter Valid URL", Toast.LENGTH_SHORT).show();
                } else {
                    UIUtil.hideKeyboard(view.getContext(), inputURL);
                    textUrl.setText(inputURL.getText().toString().trim());
                    textUrl.setVisibility(View.VISIBLE);
                    removeUrlBtn.setVisibility(View.VISIBLE);
                    dialogAddURL.dismiss();
                }
            });

            view.findViewById(R.id.dialog_cancel_btn).setOnClickListener(v -> {
                UIUtil.hideKeyboard(view.getContext(), inputURL);
                dialogAddURL.dismiss();
            });
        }
        dialogAddURL.setCancelable(false);
        dialogAddURL.show();
    }

    private void initMiscellaneous() {
        final ConstraintLayout layoutMiscellaneous = findViewById(R.id.layout_miscellaneous);
        bottomSheetMiscellaneous = BottomSheetBehavior.from(layoutMiscellaneous);

        optionsMenu.setOnClickListener(v -> {
            if (bottomSheetMiscellaneous.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetMiscellaneous.setState(BottomSheetBehavior.STATE_EXPANDED);
            } else {
                bottomSheetMiscellaneous.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }

            if (bottomSheetAddActions.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetAddActions.setState(BottomSheetBehavior.STATE_COLLAPSED);
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
            selectedNoteColor = "#FFB400";
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
            selectedNoteColor = "#3B81FF";
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
            selectedNoteColor = "#FF4E4E";
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
            selectedNoteColor = "#13A662";
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
            selectedNoteColor = "#FF388E";
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
            selectedNoteColor = "#118E9C";
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
            selectedNoteColor = "#FF822E";
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

        if(alreadyAvailableNote != null && alreadyAvailableNote.getColor() != null && !alreadyAvailableNote.getColor().trim().isEmpty()) {
            if(alreadyAvailableNote.getColor().equals("#FFB400")) {
                layoutMiscellaneous.findViewById(R.id.view_color2).performClick();
            } else if(alreadyAvailableNote.getColor().equals("#3B81FF")) {
                layoutMiscellaneous.findViewById(R.id.view_color3).performClick();
            } else if(alreadyAvailableNote.getColor().equals("#FF4E4E")) {
                layoutMiscellaneous.findViewById(R.id.view_color4).performClick();
            } else if(alreadyAvailableNote.getColor().equals("#13A662")) {
                layoutMiscellaneous.findViewById(R.id.view_color5).performClick();
            } else if(alreadyAvailableNote.getColor().equals("#FF388E")) {
                layoutMiscellaneous.findViewById(R.id.view_color6).performClick();
            } else if(alreadyAvailableNote.getColor().equals("#118E9C")) {
                layoutMiscellaneous.findViewById(R.id.view_color7).performClick();
            } else if(alreadyAvailableNote.getColor().equals("#FF822E")) {
                layoutMiscellaneous.findViewById(R.id.view_color8).performClick();
            }
        }

        if(alreadyAvailableNote != null) {
            layoutMiscellaneous.findViewById(R.id.layout_delete_note).setVisibility(View.VISIBLE);
            layoutMiscellaneous.findViewById(R.id.layout_delete_note).setOnClickListener(v -> {
                bottomSheetMiscellaneous.setState(BottomSheetBehavior.STATE_COLLAPSED);
                showDeleteNoteDialog();
            });
        }

        if(!inputNoteTitle.getText().toString().trim().isEmpty() && !inputNoteSubtitle.getText().toString().trim().isEmpty()) {
            layoutMiscellaneous.findViewById(R.id.layout_read_note).setVisibility(View.VISIBLE);
            layoutMiscellaneous.findViewById(R.id.layout_read_note).setOnClickListener(v -> textToSpeech = new TextToSpeech(CreateNoteActivity.this, status -> {
                if(status == TextToSpeech.SUCCESS) {
                    int result = textToSpeech.setLanguage(Locale.ENGLISH);

                    if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Toast.makeText(CreateNoteActivity.this, "Sorry, language not supported!", Toast.LENGTH_SHORT).show();
                    } else {
                        bottomSheetMiscellaneous.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        showDialogReadingNote();
                    }
                } else {
                    Toast.makeText(CreateNoteActivity.this, "Initialization Failed!", Toast.LENGTH_SHORT).show();
                }
            }));
        } else {
            layoutMiscellaneous.findViewById(R.id.layout_read_note).setVisibility(View.GONE);
        }
    }

    private void showDeleteNoteDialog() {
        MaterialDialog materialDialog = new MaterialDialog.Builder(CreateNoteActivity.this)
                .setTitle("Are you sure?")
                .setMessage("Are you sure you want to delete this note?")
                .setAnimation(R.raw.lottie_delete)
                .setCancelable(false)
                .setPositiveButton("Delete", R.drawable.material_dialog_delete, (dialogInterface, which) -> {
                    @SuppressLint("StaticFieldLeak")
                    class DeleteNoteTask extends AsyncTask<Void, Void, Void> {

                        @Override
                        protected Void doInBackground(Void... voids) {
                            NotesDatabase.getDatabase(getApplicationContext()).noteDao()
                                    .deleteNote(alreadyAvailableNote);
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            super.onPostExecute(aVoid);
                            Intent intent = new Intent();
                            intent.putExtra("isNoteDeleted", true);
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    }

                    new DeleteNoteTask().execute();
                    dialogInterface.dismiss();
                })
                .setNegativeButton("Cancel", R.drawable.material_dialog_cancel, (dialogInterface, which) -> dialogInterface.dismiss())
                .build();
        materialDialog.show();
    }

    @SuppressLint("ResourceType")
    private void setSubtitleIndicatorColor() {
        GradientDrawable gradientDrawable = (GradientDrawable) viewSubtitleIndicator.getBackground();
        gradientDrawable.setColor(Color.parseColor(selectedNoteColor));
    }

    private void showDialogReadingNote() {
        if(dialogReadingNote == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateNoteActivity.this);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.layout_reading_note,
                    (ViewGroup) findViewById(R.id.layout_reading_note_container)
            );
            builder.setView(view);

            dialogReadingNote = builder.create();
            if(dialogReadingNote.getWindow() != null) {
                dialogReadingNote.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }

            textReadingNote = view.findViewById(R.id.text_reading_text);

            String textToRead1 = inputNoteTitle.getText().toString().trim();
            String textToRead2 = inputNoteSubtitle.getText().toString().trim();
            String textToRead3 = inputNote.getText().toString().trim();

            view.findViewById(R.id.start_reading).setOnClickListener(v -> {
                textReadingNote.setText("Reading Note...");
                textToSpeech.speak(textToRead1, TextToSpeech.QUEUE_ADD, null);
                textToSpeech.speak(textToRead2, TextToSpeech.QUEUE_ADD, null);
                textToSpeech.speak(textToRead3, TextToSpeech.QUEUE_ADD, null);
            });

            view.findViewById(R.id.stop_reading).setOnClickListener(v -> {
                textReadingNote.setText("Do you want us to read the note for you?");
                if(textToSpeech != null) {
                    textToSpeech.stop();
                }
                dialogReadingNote.dismiss();
            });
        }
        dialogReadingNote.setCancelable(false);
        dialogReadingNote.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    try {
                        Glide.with(imageNote.getContext()).load(selectedImageUri).centerCrop().into(imageNote);
                        imageNote.setVisibility(View.VISIBLE);
                        removeImgBtn.setVisibility(View.VISIBLE);

                        selectedImagePath = getPathFromUri(selectedImageUri);
                    } catch (Exception exception) {
                        Alerter.create(CreateNoteActivity.this)
                                .setText("Some ERROR occurred!")
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
                    }
                }
            }
        } else if (requestCode == REQUEST_CODE_TAKE_PHOTO && resultCode == RESULT_OK) {
            if (data != null) {
                Uri takePhotoUri = data.getData();
                if (takePhotoUri != null) {
                    try {
                        Glide.with(imageNote.getContext()).load(takePhotoUri).centerCrop().into(imageNote);
                        imageNote.setVisibility(View.VISIBLE);
                        removeImgBtn.setVisibility(View.VISIBLE);

                        selectedImagePath = getPathFromUri(takePhotoUri);
                    } catch (Exception exception) {
                        Alerter.create(CreateNoteActivity.this)
                                .setText("Some ERROR occurred!")
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
                    }
                }
            }
        } else if (requestCode == REQUEST_CODE_VOICE_NOTE && resultCode == RESULT_OK) {
            if(data != null) {
                ArrayList<String> voiceResult = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                inputNote.setText(voiceResult.get(0));
            }
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Alerter.create(CreateNoteActivity.this)
                    .setText("Some ERROR occurred!")
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
        } else {
            return;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(textToSpeech != null) {
            textToSpeech.stop();
            dialogReadingNote.dismiss();
            textReadingNote.setText("Do you want us to read the note for you?");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            dialogReadingNote.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            dialogReadingNote.dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void finish() {
        super.finish();
        CustomIntent.customType(CreateNoteActivity.this, "right-to-left");
    }
}