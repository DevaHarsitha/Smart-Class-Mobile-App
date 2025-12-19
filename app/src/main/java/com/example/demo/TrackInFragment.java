package com.example.demo;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class TrackInFragment extends Fragment {

    private LinearLayout[] sectionLayouts = new LinearLayout[4];
    private TextView[] yearTitles = new TextView[4];

    private final String[] years = {"1st Year", "2nd Year", "3rd Year", "4th Year"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_track_in, container, false);
        sectionLayouts[0] = view.findViewById(R.id.year1Sections);
        sectionLayouts[1] = view.findViewById(R.id.year2Sections);
        sectionLayouts[2] = view.findViewById(R.id.year3Sections);
        sectionLayouts[3] = view.findViewById(R.id.year4Sections);
        yearTitles[0] = view.findViewById(R.id.year1Title);
        yearTitles[1] = view.findViewById(R.id.year2Title);
        yearTitles[2] = view.findViewById(R.id.year3Title);
        yearTitles[3] = view.findViewById(R.id.year4Title);
        for (int i = 0; i < yearTitles.length; i++) {
            final int index = i;
            yearTitles[i].setOnClickListener(v -> toggleVisibility(sectionLayouts[index]));
        }
        setupSectionButtons(view);
        return view;
    }

    private void toggleVisibility(LinearLayout sectionLayout) {
        if (sectionLayout.getVisibility() == View.VISIBLE) {
            sectionLayout.setVisibility(View.GONE);
        } else {
            sectionLayout.setVisibility(View.VISIBLE);
        }
    }

    private void setupSectionButtons(View view) {
        String[] sections = {"A", "B", "C"};
        for (int year = 1; year <= 4; year++) {
            for (String sec : sections) {
                String buttonID = "btn" + year + sec;
                int resID = getResources().getIdentifier(buttonID, "id", requireContext().getPackageName());
                Button button = view.findViewById(resID);

                final String yearText = years[year - 1];
                final String sectionText = sec;

                button.setOnClickListener(v -> openAttendanceActivity(yearText, sectionText));
            }
        }
    }

    private void openAttendanceActivity(String year, String section) {
        Intent intent = new Intent(requireContext(), AttendanceActivity.class);
        intent.putExtra("year", year);
        intent.putExtra("section", section);
        startActivity(intent);
    }
}
