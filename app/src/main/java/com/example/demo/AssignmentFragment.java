package com.example.demo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;

import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.HashSet;
import java.util.Set;

public class AssignmentFragment extends Fragment {

    private LinearLayout subjectContainer;
    private FloatingActionButton fabAdd;
    private SharedPreferences prefs;
    private boolean isStaff = false;
    private String userKey;

    private static final String PREFS_NAME = "AssignmentsPrefs";
    private static final String SHARED_SUBJECTS_KEY = "shared_subjects";
    private static final String SUBJECT_CREATOR_MAP_KEY = "subject_creator_map";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_assignment, container, false);

        subjectContainer = view.findViewById(R.id.subjectContainer);
        fabAdd = view.findViewById(R.id.fab_add_subject);
        prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        checkUserTypeAndKey();
        loadSubjects();

        return view;
    }

    private void checkUserTypeAndKey() {
        SharedPreferences loginPrefs = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String userType = loginPrefs.getString("userType", "student");
        userKey = loginPrefs.getString("userKey", null);
        Log.d("AssignmentFragment", "userType: " + userType + ", userKey: " + userKey);

        isStaff = "staff".equalsIgnoreCase(userType);
        if (isStaff) {
            fabAdd.setVisibility(View.VISIBLE);
            fabAdd.setOnClickListener(v -> showAddSubjectDialog());
        } else {
            fabAdd.setVisibility(View.GONE);
        }
    }

    private void showAddSubjectDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Create Subject");

        final EditText input = new EditText(getContext());
        input.setHint("Enter subject name");
        builder.setView(input);

        builder.setPositiveButton("Create", (dialog, which) -> {
            String subjectName = input.getText().toString().trim();
            if (!subjectName.isEmpty()) {
                Set<String> currentSubjects = prefs.getStringSet(SHARED_SUBJECTS_KEY, new HashSet<>());
                if (currentSubjects != null && currentSubjects.contains(subjectName)) {
                    Toast.makeText(getContext(), "Subject already exists", Toast.LENGTH_SHORT).show();
                } else {
                    saveSubject(subjectName);
                    addSubjectView(subjectName);
                }
            } else {
                Toast.makeText(getContext(), "Subject name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void saveSubject(String subjectName) {
        Set<String> subjects = prefs.getStringSet(SHARED_SUBJECTS_KEY, new HashSet<>());
        Set<String> updatedSubjects = new HashSet<>(subjects);
        updatedSubjects.add(subjectName);
        prefs.edit().putStringSet(SHARED_SUBJECTS_KEY, updatedSubjects).apply();

        // Save the creator (userKey)
        prefs.edit().putString(SUBJECT_CREATOR_MAP_KEY + "_" + subjectName, userKey).apply();
    }

    private void loadSubjects() {
        subjectContainer.removeAllViews();
        Set<String> subjects = prefs.getStringSet(SHARED_SUBJECTS_KEY, new HashSet<>());
        if (subjects != null) {
            for (String subject : subjects) {
                String creator = prefs.getString(SUBJECT_CREATOR_MAP_KEY + "_" + subject, null);

                if (!isStaff || (userKey != null && userKey.equals(creator))) {
                    addSubjectView(subject);
                }
            }
        }
    }

    private void addSubjectView(String subjectName) {
        TextView textView = new TextView(getContext());

        String creator = prefs.getString(SUBJECT_CREATOR_MAP_KEY + "_" + subjectName, "unknown");
        String displayText = subjectName + "\n\n" + creator;
        textView.setText(displayText);
        textView.setTextSize(20f);
        textView.setTypeface(null, Typeface.ITALIC);;
        textView.setTextColor(Color.WHITE);
        textView.setShadowLayer(5, 2, 2, Color.BLACK);
        textView.setPadding(32, 32, 32, 32);

        int[] backgroundImages = {
                R.drawable.dbms,
                R.drawable.dsa,
                R.drawable.bg
        };
        int randomIndex = (int) (Math.random() * backgroundImages.length);
        textView.setBackgroundResource(backgroundImages[randomIndex]);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(120)
        );
        params.setMargins(0, 0, 0, 24);
        textView.setLayoutParams(params);

        textView.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SubjectActivity.class);
            intent.putExtra("subject_name", subjectName);
            startActivity(intent);
        });

        if (isStaff) {
             creator = prefs.getString(SUBJECT_CREATOR_MAP_KEY + "_" + subjectName, "");
            if (userKey != null && userKey.equals(creator)) {
                textView.setOnLongClickListener(v -> {
                    new AlertDialog.Builder(getContext())
                            .setTitle("Delete Subject")
                            .setMessage("Do you want to delete \"" + subjectName + "\"?")
                            .setPositiveButton("Delete", (dialog, which) -> deleteSubject(subjectName))
                            .setNegativeButton("Cancel", null)
                            .show();
                    return true;
                });
            }
        }

        subjectContainer.addView(textView);
    }

    private void deleteSubject(String subjectName) {
        Set<String> subjects = prefs.getStringSet(SHARED_SUBJECTS_KEY, new HashSet<>());
        Set<String> updatedSubjects = new HashSet<>(subjects);
        updatedSubjects.remove(subjectName);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet(SHARED_SUBJECTS_KEY, updatedSubjects);
        editor.remove(SUBJECT_CREATOR_MAP_KEY + "_" + subjectName);
        editor.apply();

        loadSubjects();
        Toast.makeText(getContext(), "Deleted \"" + subjectName + "\"", Toast.LENGTH_SHORT).show();
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
