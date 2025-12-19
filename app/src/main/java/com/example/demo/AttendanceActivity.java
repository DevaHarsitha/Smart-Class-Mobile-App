package com.example.demo;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.*;

public class AttendanceActivity extends AppCompatActivity {

    private TableLayout studentContainer;
    private EditText studentInput, rollNoInput;
    private Button addStudentButton;

    private String year, section;
    private String currentDateKey;

    private SharedPreferences sharedPreferences;
    private final Gson gson = new Gson();

    private final List<Student> students = new ArrayList<>();
    private final Map<String, Boolean> attendanceMap = new HashMap<>();

    private boolean isStaff = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);

        year = getIntent().getStringExtra("year");
        section = getIntent().getStringExtra("section");

        SharedPreferences userPrefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        isStaff = "Staff".equals(userPrefs.getString("userType", "Student"));

        TextView header = findViewById(R.id.date);
        String todayReadable = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(new Date());
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        currentDateKey = "attendance_" + year + "_" + section + "_" + today;

        sharedPreferences = getSharedPreferences("AttendancePrefs", Context.MODE_PRIVATE);
        String lastTime = sharedPreferences.getString(currentDateKey + "_timestamp", "Not updated yet");

        header.setText("Class:\t" + year + "- Section " + section + "\nDate: " + todayReadable + "\nLast Updated: " + lastTime);

        studentInput = findViewById(R.id.studentInput);
        rollNoInput = findViewById(R.id.rollNoInput);
        addStudentButton = findViewById(R.id.addStudentButton);
        studentContainer = findViewById(R.id.studentContainer);

        studentInput.setVisibility(isStaff ? View.VISIBLE : View.GONE);
        rollNoInput.setVisibility(isStaff ? View.VISIBLE : View.GONE);
        addStudentButton.setVisibility(isStaff ? View.VISIBLE : View.GONE);

        loadStudentList();
        loadAttendance();
        populateStudentViews();

        addStudentButton.setOnClickListener(v -> {
            if (!isStaff) return;

            String name = studentInput.getText().toString().trim();
            String roll = rollNoInput.getText().toString().trim();
            String key = roll + " - " + name;

            if (name.isEmpty() || roll.isEmpty()) {
                Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            for (Student s : students) {
                if (s.getDisplayKey().equals(key)) {
                    Toast.makeText(this, "Duplicate entry", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            students.add(new Student(name, roll));
            students.sort(Comparator.comparing(Student::getRollNo));
            saveStudentList();
            saveAttendance(false);
            studentInput.setText("");
            rollNoInput.setText("");
            populateStudentViews();
        });
    }

    private void loadStudentList() {
        String key = "students_" + year + "_" + section;
        String json = sharedPreferences.getString(key, null);
        if (json != null) {
            Type type = new TypeToken<List<Student>>() {}.getType();
            students.clear();
            students.addAll(gson.fromJson(json, type));
            students.sort(Comparator.comparing(Student::getRollNo));
        }
    }

    private void saveStudentList() {
        String key = "students_" + year + "_" + section;
        sharedPreferences.edit().putString(key, gson.toJson(students)).apply();
    }

    private void loadAttendance() {
        String json = sharedPreferences.getString(currentDateKey, null);
        attendanceMap.clear();
        if (json != null) {
            Type type = new TypeToken<Map<String, Boolean>>() {}.getType();
            attendanceMap.putAll(gson.fromJson(json, type));
        }
    }

    private void saveAttendance(boolean updateTimestamp) {
        sharedPreferences.edit().putString(currentDateKey, gson.toJson(attendanceMap)).apply();
        if (updateTimestamp) {
            String timeNow = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());
            sharedPreferences.edit().putString(currentDateKey + "_timestamp", timeNow).apply();
        }
    }

    private void populateStudentViews() {
        studentContainer.removeAllViews();

        TableRow headerRow = new TableRow(this);
        headerRow.addView(makeHeaderCell("Roll No"));
        headerRow.addView(makeHeaderCell("Name"));
        if (isStaff) {
            headerRow.addView(makeHeaderCell("Present"));
            headerRow.addView(makeHeaderCell("Absent"));
        } else {
            headerRow.addView(makeHeaderCell("Status"));
        }
        studentContainer.addView(headerRow);
        for (Student student : students) {
            TableRow row = new TableRow(this);
            TextView rollView = makeCell(student.getRollNo());
            TextView nameView = makeCell(student.getName());

            String key = student.getDisplayKey();
            Boolean status = attendanceMap.get(key);

            if (isStaff) {
                nameView.setOnLongClickListener(v -> {
                    showStudentOptionsDialog(student);
                    return true;
                });

                ToggleButton presentButton = new ToggleButton(this);
                ToggleButton absentButton = new ToggleButton(this);

                presentButton.setTextOn("✔");
                presentButton.setTextOff("✔");
                absentButton.setTextOn("✖");
                absentButton.setTextOff("✖");

                presentButton.setChecked(Boolean.TRUE.equals(status));
                absentButton.setChecked(Boolean.FALSE.equals(status));

                updateToggleButtonColors(presentButton, absentButton);

                presentButton.setOnCheckedChangeListener((btn, isChecked) -> {
                    if (isChecked) {
                        absentButton.setChecked(false);
                        attendanceMap.put(key, true);
                        saveAttendance(true);
                        updateToggleButtonColors(presentButton, absentButton);
                    } else if (!absentButton.isChecked()) {
                        presentButton.setChecked(true);
                    }
                });

                absentButton.setOnCheckedChangeListener((btn, isChecked) -> {
                    if (isChecked) {
                        presentButton.setChecked(false);
                        attendanceMap.put(key, false);
                        saveAttendance(true);
                        updateToggleButtonColors(presentButton, absentButton);
                    } else if (!presentButton.isChecked()) {
                        absentButton.setChecked(true);
                    }
                });

                row.addView(rollView);
                row.addView(nameView);
                row.addView(presentButton);
                row.addView(absentButton);
            } else {
                TextView statusView = makeCell(status == null ? "Not Marked" : status ? "Present" : "Absent");
                row.addView(rollView);
                row.addView(nameView);
                row.addView(statusView);
            }

            studentContainer.addView(row);
        }
    }

    private TextView makeCell(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setPadding(8, 8, 8, 8);
        tv.setBackgroundResource(R.drawable.cell_border);
        return tv;
    }

    private TextView makeHeaderCell(String text) {
        TextView tv = makeCell(text);
        tv.setTypeface(null, android.graphics.Typeface.BOLD);
        return tv;
    }

    private void updateToggleButtonColors(ToggleButton present, ToggleButton absent) {
        if (present.isChecked()) {
            present.setBackgroundColor(0xFF4CAF50);
            absent.setBackgroundResource(R.drawable.cell_border);
        } else if (absent.isChecked()) {
            absent.setBackgroundColor(0xFFF44336);
            present.setBackgroundResource(R.drawable.cell_border);
        } else {
            present.setBackgroundResource(R.drawable.cell_border);
            absent.setBackgroundResource(R.drawable.cell_border);
        }
    }

    private void showStudentOptionsDialog(Student student) {
        String[] options = {"Edit", "Remove"};
        new AlertDialog.Builder(this)
                .setTitle("Choose Option")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showEditStudentDialog(student);
                    } else {
                        students.remove(student);
                        attendanceMap.remove(student.getDisplayKey());
                        saveStudentList();
                        saveAttendance(false);
                        populateStudentViews();
                    }
                }).show();
    }

    private void showEditStudentDialog(Student student) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        EditText rollInput = new EditText(this);
        rollInput.setText(student.getRollNo());
        EditText nameInput = new EditText(this);
        nameInput.setText(student.getName());
        layout.addView(rollInput);
        layout.addView(nameInput);

        new AlertDialog.Builder(this)
                .setTitle("Edit Student")
                .setView(layout)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newName = nameInput.getText().toString().trim();
                    String newRoll = rollInput.getText().toString().trim();
                    if (!newName.isEmpty() && !newRoll.isEmpty()) {
                        String oldKey = student.getDisplayKey();
                        String newKey = newRoll + " - " + newName;
                        int index = students.indexOf(student);
                        students.set(index, new Student(newName, newRoll));

                        Boolean status = attendanceMap.get(oldKey);
                        attendanceMap.remove(oldKey);
                        if (status != null) attendanceMap.put(newKey, status);

                        students.sort(Comparator.comparing(Student::getRollNo));
                        saveStudentList();
                        saveAttendance(false);
                        populateStudentViews();
                    } else {
                        Toast.makeText(this, "Invalid input", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    public static class Student {
        private String name;
        private String rollNo;

        public Student() {}

        public Student(String name, String rollNo) {
            this.name = name;
            this.rollNo = rollNo;
        }

        public String getName() {
            return name;
        }

        public String getRollNo() {
            return rollNo;
        }

        public String getDisplayKey() {
            return rollNo + " - " + name;
        }
    }
}
