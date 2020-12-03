package com.developerdepository.noted.adapters;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.developerdepository.noted.R;
import com.developerdepository.noted.entities.Note;
import com.developerdepository.noted.listeners.NotesListener;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {

    private List<Note> notes;
    private NotesListener notesListener;
    private Timer timer;
    private List<Note> notesSource;

    public NotesAdapter(List<Note> notes, NotesListener notesListener) {
        this.notes = notes;
        this.notesListener = notesListener;
        notesSource = notes;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NoteViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.item_container_note,
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        holder.setNote(notes.get(position));
        holder.layoutNote.setOnClickListener(v -> {
            notesListener.onNoteClicked(holder.layoutNote, notes.get(position), position);
        });
        holder.layoutNote.setOnLongClickListener(v -> {
            notesListener.onNoteLongClicked(holder.layoutNote, notes.get(position), position);
            return true;
        });
    }

    @Override
    public int getItemViewType(int position) {
        return position;

    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {

        ConstraintLayout layoutNote;
        TextView itemNoteTitle, itemNoteSubtitle, itemNoteDateTime;
        RoundedImageView itemNoteImage;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);

            layoutNote = itemView.findViewById(R.id.layout_note);
            itemNoteImage = itemView.findViewById(R.id.item_note_image);
            itemNoteTitle = itemView.findViewById(R.id.item_note_title);
            itemNoteSubtitle = itemView.findViewById(R.id.item_note_subtitle);
            itemNoteDateTime = itemView.findViewById(R.id.item_note_date_time);
        }

        void setNote(Note note) {
            itemNoteTitle.setText(note.getTitle());

            if (note.getSubtitle().trim().isEmpty()) {
                itemNoteSubtitle.setVisibility(View.GONE);
            } else {
                itemNoteSubtitle.setText(note.getSubtitle());
            }

            itemNoteDateTime.setText(note.getDateTime());

            GradientDrawable gradientDrawable = (GradientDrawable) layoutNote.getBackground();
            if (note.getColor() != null) {
                gradientDrawable.setColor(Color.parseColor(note.getColor()));
            } else {
                gradientDrawable.setColor(Color.parseColor(String.valueOf(R.color.colorDefaultNoteColor)));
            }

            if (note.getImagePath() != null && !note.getImagePath().isEmpty()) {
                itemNoteImage.setVisibility(View.VISIBLE);
                itemNoteImage.setImageBitmap(BitmapFactory.decodeFile(note.getImagePath()));
            } else {
                itemNoteImage.setVisibility(View.GONE);
            }
        }
    }

    public void searchNotes(final String searchKeyword) {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (searchKeyword.trim().isEmpty()) {
                    notes = notesSource;
                } else {
                    ArrayList<Note> temp = new ArrayList<>();
                    for (Note note : notesSource) {
                        if (note.getTitle().toLowerCase().contains(searchKeyword.toLowerCase())
                                || note.getSubtitle().toLowerCase().contains(searchKeyword.toLowerCase())
                                || note.getNoteText().toLowerCase().contains(searchKeyword.toLowerCase())) {
                            temp.add(note);
                        }
                    }
                    notes = temp;
                }
                new Handler(Looper.getMainLooper()).post(() -> notifyDataSetChanged());
            }
        }, 500);
    }

    public void cancelTimer() {
        if (timer != null) {
            timer.cancel();
        }
    }
}
