package com.developerdepository.noted.adapters;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.developerdepository.noted.R;
import com.developerdepository.noted.entities.Note;

import java.util.List;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {

    private List<Note> notes;

    public NotesAdapter(List<Note> notes) {
        this.notes = notes;
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

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);

            layoutNote = itemView.findViewById(R.id.layout_note);
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
        }
    }
}
