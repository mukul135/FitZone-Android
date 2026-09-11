package com.fitzone.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fitzone.app.R;
import com.fitzone.app.models.Program;

import java.util.List;

// ===============================
// ProgramFullAdapter.java
// ===============================
// WHAT THIS FILE DOES:
// Same job as ProgramAdapter (Phase 8), but for the full-screen vertical
// Programs list (ProgramsActivity) instead of Home's compact horizontal
// preview row. A separate class exists only because ProgramAdapter
// hardcodes R.layout.item_program in onCreateViewHolder — the layout
// resource isn't swappable, so this reuses the same Program model and
// the same click-listener pattern against a different, full-width card
// (item_program_full.xml) that also shows an explicit Register button.

public class ProgramFullAdapter extends RecyclerView.Adapter<ProgramFullAdapter.ProgramFullViewHolder> {

    public interface OnRegisterClickListener {
        void onRegisterClick(Program program);
    }

    private final List<Program> programs;
    private final OnRegisterClickListener listener;

    public ProgramFullAdapter(List<Program> programs, OnRegisterClickListener listener) {
        this.programs = programs;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProgramFullViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_program_full, parent, false);
        return new ProgramFullViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProgramFullViewHolder holder, int position) {
        Program program = programs.get(position);
        holder.tvName.setText(program.getName());
        holder.tvDescription.setText(program.getDescription());
        holder.btnRegister.setOnClickListener(v -> listener.onRegisterClick(program));
    }

    @Override
    public int getItemCount() {
        return programs.size();
    }

    static class ProgramFullViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDescription;
        Button btnRegister;

        ProgramFullViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvProgramName);
            tvDescription = itemView.findViewById(R.id.tvProgramDescription);
            btnRegister = itemView.findViewById(R.id.btnRegisterProgram);
        }
    }
}