package com.fitzone.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fitzone.app.R;
import com.fitzone.app.models.Program;

import java.util.List;

public class ProgramAdapter extends RecyclerView.Adapter<ProgramAdapter.ProgramViewHolder> {

    public interface OnProgramClickListener {
        void onProgramClick(Program program);
    }

    private final List<Program> programs;
    private final OnProgramClickListener listener;

    public ProgramAdapter(List<Program> programs, OnProgramClickListener listener) {
        this.programs = programs;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProgramViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_program, parent, false);
        return new ProgramViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProgramViewHolder holder, int position) {
        Program program = programs.get(position);
        holder.tvName.setText(program.getName());
        holder.tvDescription.setText(program.getDescription());
        holder.itemView.setOnClickListener(v -> listener.onProgramClick(program));
    }

    @Override
    public int getItemCount() {
        return programs.size();
    }

    static class ProgramViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDescription;

        ProgramViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvProgramName);
            tvDescription = itemView.findViewById(R.id.tvProgramDescription);
        }
    }
}