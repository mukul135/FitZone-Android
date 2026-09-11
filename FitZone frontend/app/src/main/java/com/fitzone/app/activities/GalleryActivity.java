package com.fitzone.app.activities;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.fitzone.app.R;

public class GalleryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Gym Areas — matches original gallery.html exactly (6 images)
        bindCell(R.id.cellGymArea, R.drawable.gym_area, R.string.gallery_caption_gym_area);
        bindCell(R.id.cellDumbbell, R.drawable.dumbbell_section, R.string.gallery_caption_dumbbell);
        bindCell(R.id.cellCardio, R.drawable.cardio_section, R.string.gallery_caption_cardio);
        bindCell(R.id.cellCrossfit, R.drawable.crossfit_section, R.string.gallery_caption_crossfit);
        bindCell(R.id.cellPool, R.drawable.swimming_pool, R.string.gallery_caption_pool);
        bindCell(R.id.cellReception, R.drawable.reception, R.string.gallery_caption_reception);

        // Gym Machines — matches original gallery.html exactly (5 images)
        bindCell(R.id.cellBench, R.drawable.bench_press, R.string.gallery_caption_bench);
        bindCell(R.id.cellSquat, R.drawable.squat_rack, R.string.gallery_caption_squat);
        bindCell(R.id.cellLegPress, R.drawable.leg_press, R.string.gallery_caption_leg_press);
        bindCell(R.id.cellCable, R.drawable.cable_station, R.string.gallery_caption_cable);
        bindCell(R.id.cellLatPulldown, R.drawable.lat_pulldown, R.string.gallery_caption_lat_pulldown);
    }

    private void bindCell(int includedRootId, int drawableRes, int captionRes) {
        android.view.View cell = findViewById(includedRootId);
        ImageView iv = cell.findViewById(R.id.ivGalleryImage);
        TextView tv = cell.findViewById(R.id.tvGalleryCaption);
        iv.setImageResource(drawableRes);
        tv.setText(captionRes);
    }
}