package com.developerdepository.noted.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bumptech.glide.Glide;
import com.developerdepository.noted.R;
import com.developerdepository.noted.adapters.NotesAdapter;
import com.developerdepository.noted.database.NotesDatabase;
import com.developerdepository.noted.entities.Note;
import com.developerdepository.noted.listeners.NotesListener;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.tapadoo.alerter.Alerter;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.util.UIUtil;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import maes.tech.intentanim.CustomIntent;

public class MainActivity extends AppCompatActivity implements NotesListener, NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    private ConstraintLayout contentView;
    private ImageView imageEmpty;
    private ImageButton navigationMenu;
    private TextView textEmpty;
    private EditText inputSearch;
    private RecyclerView notesRecyclerView;
    private BottomAppBar bottomAppBar;
    private FloatingActionButton addNoteFloatingBtn;

    private List<Note> noteList;
    private NotesAdapter notesAdapter;

    public static final int REQUEST_CODE_ADD_NOTE = 1;
    public static final int REQUEST_CODE_UPDATE_NOTE = 2;
    public static final int REQUEST_CODE_SHOW_NOTES = 3;
    public static final int REQUEST_CODE_TAKE_PHOTO = 4;
    public static final int REQUEST_CODE_SELECT_IMAGE = 5;
    public static final int REQUEST_CODE_VOICE_NOTE = 6;

    private int noteClickedPosition = -1;

    private AlertDialog dialogAddURL;
    private AlertDialog dialogAddImage;

    private static final float END_SCALE = 0.8f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setNavigationBarColor(ContextCompat.getColor(MainActivity.this, R.color.colorQuickActionsBackground));

        initViews();
        setNavigationMenu();
        setActionOnViews();

        getNotes(REQUEST_CODE_SHOW_NOTES, false);
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.main_drawer_layout);
        navigationView = findViewById(R.id.main_navigation_menu);
        contentView = findViewById(R.id.content_view);
        navigationMenu = findViewById(R.id.main_navigation);
        imageEmpty = findViewById(R.id.image_empty);
        textEmpty = findViewById(R.id.text_empty);
        inputSearch = findViewById(R.id.input_search);
        notesRecyclerView = findViewById(R.id.notes_recycler_view);
        bottomAppBar = findViewById(R.id.main_bottom_app_bar);
        addNoteFloatingBtn = findViewById(R.id.floating_action_add_notes_btn);
    }

    private void setActionOnViews() {
        KeyboardVisibilityEvent.setEventListener(MainActivity.this, isOpen -> {
            if (!isOpen) {
                inputSearch.clearFocus();
            }
        });

        notesRecyclerView.setLayoutManager(
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        );
        noteList = new ArrayList<>();
        notesAdapter = new NotesAdapter(noteList, this);
        notesRecyclerView.setAdapter(notesAdapter);

        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                notesAdapter.cancelTimer();
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(noteList.size() != 0) {
                    notesAdapter.searchNotes(s.toString());
                }
            }
        });

        bottomAppBar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.menu_add :
                    startActivityForResult(
                            new Intent(getApplicationContext(), CreateNoteActivity.class),
                            REQUEST_CODE_ADD_NOTE
                    );
                    CustomIntent.customType(MainActivity.this, "bottom-to-up");
                    inputSearch.setText(null);
                    break;
                case R.id.menu_image :
                    showAddImageDialog();
                    break;
                case R.id.menu_voice :
                    voiceNote();
                    break;
                case R.id.menu_web_link :
                    showAddURLDialog();
                    break;
            }
            return false;
        });

        addNoteFloatingBtn.setOnClickListener(v -> {
            startActivityForResult(
                    new Intent(getApplicationContext(), CreateNoteActivity.class),
                    REQUEST_CODE_ADD_NOTE
            );
            CustomIntent.customType(MainActivity.this, "bottom-to-up");
            inputSearch.setText(null);
        });
    }

    private void setNavigationMenu() {
        navigationView.bringToFront();
        navigationView.setNavigationItemSelectedListener(MainActivity.this);

        navigationMenu.setOnClickListener(v -> {
            if(drawerLayout.isDrawerVisible(GravityCompat.END))
                drawerLayout.closeDrawer(GravityCompat.END);
            else drawerLayout.openDrawer(GravityCompat.END);
        });

        animateNavigationDrawer();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_app_info :
                break;
            case R.id.nav_rate_noted :
                break;
            case R.id.nav_share_noted :
                break;
            case R.id.nav_more_apps :
                break;
            case R.id.nav_privacy_policy :
                break;
        }
        return false;
    }

    private void animateNavigationDrawer() {
        drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                final float diffScaledOffset = slideOffset * (1 - END_SCALE);
                final float offsetScale = 1 - diffScaledOffset;
                contentView.setScaleX(offsetScale);
                contentView.setScaleY(offsetScale);

                final float xOffset = drawerView.getWidth() * slideOffset;
                final float xOffsetDiff = contentView.getWidth() * diffScaledOffset / 2;
                final float xTranslation = xOffsetDiff - xOffset;
                contentView.setTranslationX(xTranslation);
            }
        });
    }

    @Override
    public void onNoteClicked(Note note, int position) {
        noteClickedPosition = position;
        Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
        intent.putExtra("isViewOrUpdate", true);
        intent.putExtra("note", note);
        startActivityForResult(intent, REQUEST_CODE_UPDATE_NOTE);
        CustomIntent.customType(MainActivity.this, "bottom-to-up");
        inputSearch.setText(null);
    }

    private void getNotes(final int requestCode, final boolean isNoteDeleted) {

        @SuppressLint("StaticFieldLeak")
        class GetNotesTask extends AsyncTask<Void, Void, List<Note>> {

            @Override
            protected List<Note> doInBackground(Void... voids) {
                return NotesDatabase
                        .getDatabase(getApplicationContext())
                        .noteDao().getAllNotes();
            }

            @Override
            protected void onPostExecute(List<Note> notes) {
                super.onPostExecute(notes);
                if(requestCode == REQUEST_CODE_SHOW_NOTES) {
                    noteList.addAll(notes);
                    notesAdapter.notifyDataSetChanged();
                } else if(requestCode == REQUEST_CODE_ADD_NOTE) {
                    noteList.add(0, notes.get(0));
                    notesAdapter.notifyItemInserted(0);
                    notesRecyclerView.smoothScrollToPosition(0);
                } else if(requestCode == REQUEST_CODE_UPDATE_NOTE) {
                    noteList.remove(noteClickedPosition);
                    if(isNoteDeleted) {
                        notesAdapter.notifyItemRemoved(noteClickedPosition);
                    } else {
                        noteList.add(noteClickedPosition, notes.get(noteClickedPosition));
                        notesAdapter.notifyItemChanged(noteClickedPosition);
                    }
                }

                if(noteList.size() != 0) {
                    imageEmpty.setVisibility(View.GONE);
                    textEmpty.setVisibility(View.GONE);
                } else {
                    imageEmpty.setVisibility(View.VISIBLE);
                    textEmpty.setVisibility(View.VISIBLE);
                }
            }
        }

        new GetNotesTask().execute();
    }

    private void showAddImageDialog() {
        if(dialogAddImage == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.layout_add_image,
                    (ViewGroup) findViewById(R.id.layout_add_image_container)
            );
            builder.setView(view);

            dialogAddImage = builder.create();
            if(dialogAddImage.getWindow() != null) {
                dialogAddImage.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }

            view.findViewById(R.id.layout_take_photo).setOnClickListener(v -> {
                takePhoto();
                dialogAddImage.dismiss();
            });

            view.findViewById(R.id.layout_add_image).setOnClickListener(v -> {
                selectImage();
                dialogAddImage.dismiss();
            });
        }
        dialogAddImage.show();
    }

    private void takePhoto() {
        ImagePicker.Companion.with(MainActivity.this)
                .cameraOnly()
                .crop()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .start(REQUEST_CODE_TAKE_PHOTO);
    }

    private void selectImage() {
        ImagePicker.Companion.with(MainActivity.this)
                .galleryOnly()
                .crop()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .start(REQUEST_CODE_SELECT_IMAGE);
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
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
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
                    Toast.makeText(MainActivity.this, "Enter URL", Toast.LENGTH_SHORT).show();
                } else if(!Patterns.WEB_URL.matcher(inputURL.getText().toString().trim()).matches()) {
                    Toast.makeText(MainActivity.this, "Enter Valid URL", Toast.LENGTH_SHORT).show();
                } else {
                    dialogAddURL.dismiss();
                    UIUtil.hideKeyboard(view.getContext(), inputURL);
                    Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
                    intent.putExtra("isFromQuickActions", true);
                    intent.putExtra("quickActionType", "URL");
                    intent.putExtra("URL", inputURL.getText().toString().trim());
                    startActivityForResult(intent, REQUEST_CODE_ADD_NOTE);
                    CustomIntent.customType(MainActivity.this, "bottom-to-up");
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

    private void voiceNote() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak something to add note!");
        startActivityForResult(intent, REQUEST_CODE_VOICE_NOTE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_NOTE && resultCode == RESULT_OK) {
            getNotes(REQUEST_CODE_ADD_NOTE, false);
        } else if(requestCode == REQUEST_CODE_UPDATE_NOTE && resultCode == RESULT_OK) {
            if(data != null) {
                getNotes(REQUEST_CODE_UPDATE_NOTE, data.getBooleanExtra("isNoteDeleted", false));
            }
        } else if(requestCode == REQUEST_CODE_TAKE_PHOTO && resultCode == RESULT_OK) {
            if(data != null) {
                Uri takePhotoUri = data.getData();
                if(takePhotoUri != null) {
                    try {
                        String selectedImagePath = getPathFromUri(takePhotoUri);
                        Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
                        intent.putExtra("isFromQuickActions", true);
                        intent.putExtra("quickActionType", "image");
                        intent.putExtra("imagePath", selectedImagePath);
                        startActivityForResult(intent, REQUEST_CODE_ADD_NOTE);
                        CustomIntent.customType(MainActivity.this, "bottom-to-up");
                    } catch (Exception exception) {
                        Alerter.create(MainActivity.this)
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
        } else if(requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK) {
            if(data != null) {
                Uri selectedImageUri = data.getData();
                if(selectedImageUri != null) {
                    try {
                        String selectedImagePath = getPathFromUri(selectedImageUri);
                        Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
                        intent.putExtra("isFromQuickActions", true);
                        intent.putExtra("quickActionType", "image");
                        intent.putExtra("imagePath", selectedImagePath);
                        startActivityForResult(intent, REQUEST_CODE_ADD_NOTE);
                        CustomIntent.customType(MainActivity.this, "bottom-to-up");
                    } catch (Exception exception) {
                        Alerter.create(MainActivity.this)
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
        } else if(requestCode == REQUEST_CODE_VOICE_NOTE && resultCode == RESULT_OK) {
            if(data != null) {
                ArrayList<String> voiceResult = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
                intent.putExtra("isFromQuickActions", true);
                intent.putExtra("quickActionType", "voiceNote");
                intent.putExtra("inputText", voiceResult.get(0));
                startActivityForResult(intent, REQUEST_CODE_ADD_NOTE);
                CustomIntent.customType(MainActivity.this, "bottom-to-up");
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(drawerLayout.isDrawerVisible(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END);
        } else {
            finishAffinity();
        }
    }
}