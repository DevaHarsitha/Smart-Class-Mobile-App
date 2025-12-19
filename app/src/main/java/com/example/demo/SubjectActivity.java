package com.example.demo;

import android.annotation.SuppressLint;
import android.content.*;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.*;

public class SubjectActivity extends AppCompatActivity {

    private static final int PICK_PDF_ASSIGNMENT = 1001;
    private static final int PICK_PDF_MATERIAL = 2001;

    private TextView subjectTitle, selectedPdfText;
    private RecyclerView assignmentRecyclerView, materialRecyclerView;
    private Button btnUploadAssignment, btnSubmitAssignment, btnUploadMaterial;

    private ArrayList<Assignment> assignments = new ArrayList<>();
    private ArrayList<Material> materials = new ArrayList<>();

    private AssignmentAdapter assignmentAdapter;
    private MaterialAdapter materialAdapter;

    private int selectedAssignmentPosition = -1;
    private Uri selectedPdfUri = null;
    private String subjectName;
    private String userType;
    private String userKey;

    private SharedPreferences prefs;
    private Gson gson = new Gson();

    static class Assignment {
        String title, dueDate;
        Assignment(String t, String d) { title = t; dueDate = d; }
    }

    static class Material {
        String title, fileUri;
        Material(String t, String u) { title = t; fileUri = u; }
    }

    static class AssignmentUpload {
        String fileName, fileUri, uploadedBy;
        AssignmentUpload(String name, String uri, String by) {
            fileName = name;
            fileUri = uri;
            uploadedBy = by;
        }
    }

    interface AssignmentClickListener {
        void onAssignmentClick(int position);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject);

        subjectTitle = findViewById(R.id.subjectTitle);
        selectedPdfText = findViewById(R.id.selectedPdfText);
        assignmentRecyclerView = findViewById(R.id.assignmentRecyclerView);
        materialRecyclerView = findViewById(R.id.materialRecyclerView);
        btnUploadAssignment = findViewById(R.id.btnUploadAssignment);
        btnSubmitAssignment = findViewById(R.id.btnSubmitAssignment);
        btnUploadMaterial = findViewById(R.id.btnUploadMaterial);

        prefs = getSharedPreferences("AssignmentPrefs", MODE_PRIVATE);
        SharedPreferences loginPrefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        userType = loginPrefs.getString("userType", "student");
        userKey = loginPrefs.getString("userKey", "");

        subjectName = getIntent().getStringExtra("subject_name");
        subjectTitle.setText(subjectName != null ? subjectName : "Unknown Subject");

        assignmentRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        materialRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        assignmentAdapter = new AssignmentAdapter(assignments, userType, userKey, subjectName, position -> selectedAssignmentPosition = position);
        materialAdapter = new MaterialAdapter(materials, userType, subjectName);

        assignmentRecyclerView.setAdapter(assignmentAdapter);
        materialRecyclerView.setAdapter(materialAdapter);

        loadAssignments();
        loadMaterials();

        if (!userType.equalsIgnoreCase("Staff")) {
            btnUploadMaterial.setVisibility(View.GONE);
        }

        btnUploadAssignment.setOnClickListener(v -> openPdfPicker(PICK_PDF_ASSIGNMENT));
        btnSubmitAssignment.setOnClickListener(v -> submitAssignment());
        btnUploadMaterial.setOnClickListener(v -> openPdfPicker(PICK_PDF_MATERIAL));
    }

    private void openPdfPicker(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("application/pdf");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Select PDF"), requestCode);
    }

    private void submitAssignment() {
        if (selectedAssignmentPosition == -1 || selectedPdfUri == null) {
            Toast.makeText(this, "Select assignment and PDF", Toast.LENGTH_SHORT).show();
            return;
        }

        String assignmentTitle = assignments.get(selectedAssignmentPosition).title;
        AssignmentUpload upload = new AssignmentUpload(
                selectedPdfUri.getLastPathSegment(),
                selectedPdfUri.toString(),
                userKey
        );

        String key = "uploads_" + subjectName;
        Map<String, Map<String, AssignmentUpload>> uploadMap = new HashMap<>();
        String json = prefs.getString(key, null);
        if (json != null) {
            Type type = new TypeToken<Map<String, Map<String, AssignmentUpload>>>(){}.getType();
            uploadMap = gson.fromJson(json, type);
        }

        Map<String, AssignmentUpload> userUploads = uploadMap.getOrDefault(assignmentTitle, new HashMap<>());
        userUploads.put(userKey, upload);
        uploadMap.put(assignmentTitle, userUploads);
        prefs.edit().putString(key, gson.toJson(uploadMap)).apply();

        selectedPdfText.setText("Uploaded: " + upload.fileName + " by " + userKey);
        assignmentAdapter.notifyItemChanged(selectedAssignmentPosition);
    }

    @SuppressLint("WrongConstant")
    @Override
    protected void onActivityResult(int reqCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedPdfUri = data.getData();
            final int takeFlags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            try {
                getContentResolver().takePersistableUriPermission(selectedPdfUri, takeFlags);
            } catch (SecurityException e) {
                e.printStackTrace();
            }

            if (reqCode == PICK_PDF_ASSIGNMENT) {
                selectedPdfText.setText("PDF Selected: " + selectedPdfUri.getLastPathSegment());
            } else if (reqCode == PICK_PDF_MATERIAL) {
                Material m = new Material(selectedPdfUri.getLastPathSegment(), selectedPdfUri.toString());
                materials.add(m);
                saveMaterials();
                materialAdapter.notifyItemInserted(materials.size() - 1);
                Toast.makeText(this, "Material uploaded", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadAssignments() {
        assignments.add(new Assignment("Assignment 1", "Due: 2025-06-20"));
        assignments.add(new Assignment("Assignment 2", "Due: 2025-06-25"));
        assignments.add(new Assignment("Assignment 3", "Due: 2025-07-01"));
        assignmentAdapter.notifyDataSetChanged();
    }

    private void loadMaterials() {
        String json = prefs.getString("materials_" + subjectName, null);
        if (json != null) {
            Type type = new TypeToken<ArrayList<Material>>() {}.getType();
            materials.addAll(gson.fromJson(json, type));
        }
        materialAdapter.notifyDataSetChanged();
    }

    private void saveMaterials() {
        prefs.edit().putString("materials_" + subjectName, gson.toJson(materials)).apply();
    }

    static class AssignmentAdapter extends RecyclerView.Adapter<AssignmentAdapter.ViewHolder> {
        ArrayList<Assignment> list;
        String userType, userKey, subjectName;
        AssignmentClickListener listener;

        AssignmentAdapter(ArrayList<Assignment> l, String userType, String userKey, String subjectName, AssignmentClickListener listener) {
            list = l;
            this.userType = userType;
            this.userKey = userKey;
            this.subjectName = subjectName;
            this.listener = listener;
        }

        public ViewHolder onCreateViewHolder(ViewGroup p, int v) {
            TextView tv = new TextView(p.getContext());
            tv.setPadding(32, 24, 32, 24);
            tv.setTextSize(18f);
            return new ViewHolder(tv);
        }

        public void onBindViewHolder(ViewHolder h, int pos) {
            Assignment a = list.get(pos);
            StringBuilder displayText = new StringBuilder(a.title + "\n" + a.dueDate);

            SharedPreferences prefs = h.textView.getContext().getSharedPreferences("AssignmentPrefs", MODE_PRIVATE);
            String json = prefs.getString("uploads_" + subjectName, null);
            Type type = new TypeToken<Map<String, Map<String, AssignmentUpload>>>() {}.getType();
            Map<String, Map<String, AssignmentUpload>> allUploads = new Gson().fromJson(json, type);

            if (allUploads != null && allUploads.containsKey(a.title)) {
                Map<String, AssignmentUpload> uploads = allUploads.get(a.title);
                for (Map.Entry<String, AssignmentUpload> entry : uploads.entrySet()) {
                    AssignmentUpload u = entry.getValue();
                    if (userType.equalsIgnoreCase("Staff") || u.uploadedBy.equals(userKey) || isUploaderStaff(u.uploadedBy)) {
                        displayText.append("\n\n📎 Uploaded by ").append(u.uploadedBy).append(": ").append(u.fileName);
                    }
                }
            }

            h.textView.setText(displayText.toString());

            h.textView.setOnClickListener(v -> {
                listener.onAssignmentClick(pos);
            });

            h.textView.setOnLongClickListener(v -> {
                if (!userType.equalsIgnoreCase("Staff")) {
                    Toast.makeText(v.getContext(), "Only staff can delete uploads", Toast.LENGTH_SHORT).show();
                    return true;
                }

                new AlertDialog.Builder(v.getContext())
                        .setTitle("Delete Uploads")
                        .setMessage("Delete all uploads for this assignment?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            String key = "uploads_" + subjectName;
                            String j = prefs.getString(key, null);
                            Type t = new TypeToken<Map<String, Map<String, AssignmentUpload>>>() {}.getType();
                            Map<String, Map<String, AssignmentUpload>> m = new Gson().fromJson(j, t);
                            if (m != null && m.containsKey(a.title)) {
                                m.remove(a.title);
                                prefs.edit().putString(key, new Gson().toJson(m)).apply();
                                Toast.makeText(v.getContext(), "Uploads deleted", Toast.LENGTH_SHORT).show();
                                notifyItemChanged(h.getAdapterPosition());
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                return true;
            });
        }

        private boolean isUploaderStaff(String uploaderId) {
            return !uploaderId.toLowerCase().startsWith("s");
        }

        public int getItemCount() { return list.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView textView;
            ViewHolder(TextView v) { super(v); textView = v; }
        }
    }

    static class MaterialAdapter extends RecyclerView.Adapter<MaterialAdapter.ViewHolder> {
        ArrayList<Material> list;
        String userType, subjectName;

        MaterialAdapter(ArrayList<Material> l, String userType, String subjectName) {
            list = l;
            this.userType = userType;
            this.subjectName = subjectName;
        }

        public ViewHolder onCreateViewHolder(ViewGroup p, int v) {
            TextView tv = new TextView(p.getContext());
            tv.setPadding(32, 24, 32, 24);
            tv.setTextSize(18f);
            return new ViewHolder(tv);
        }

        public void onBindViewHolder(ViewHolder h, int pos) {
            Material m = list.get(pos);
            h.textView.setText("📘 " + m.title);

            h.textView.setOnClickListener(v -> {
                Uri uri = Uri.parse(m.fileUri);
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setDataAndType(uri, "application/pdf");
                i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                try {
                    v.getContext().startActivity(i);
                } catch (Exception e) {
                    Toast.makeText(v.getContext(), "No app to view material", Toast.LENGTH_SHORT).show();
                }
            });

            h.textView.setOnLongClickListener(v -> {
                if (!userType.equalsIgnoreCase("Staff")) {
                    Toast.makeText(v.getContext(), "Only staff can delete materials", Toast.LENGTH_SHORT).show();
                    return true;
                }

                new AlertDialog.Builder(v.getContext())
                        .setTitle("Delete Material")
                        .setMessage("Are you sure you want to delete this material?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            int position = h.getAdapterPosition();
                            list.remove(position);
                            notifyItemRemoved(position);

                            SharedPreferences prefs = v.getContext().getSharedPreferences("AssignmentPrefs", MODE_PRIVATE);
                            prefs.edit().putString("materials_" + subjectName, new Gson().toJson(list)).apply();

                            Toast.makeText(v.getContext(), "Material deleted", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();

                return true;
            });
        }

        public int getItemCount() { return list.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView textView;
            ViewHolder(TextView v) { super(v); textView = v; }
        }
    }
}
