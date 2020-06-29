package com.developerdepository.noted.listeners;

import com.developerdepository.noted.entities.Note;

public interface NotesListener {
    void onNoteClicked(Note note, int position);
}
