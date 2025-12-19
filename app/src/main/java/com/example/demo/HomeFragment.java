package com.example.demo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.*;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.text.SimpleDateFormat;
import java.util.*;

public class HomeFragment extends Fragment {

    private RecyclerView announcementRecyclerView;
    private AnnouncementAdapter adapter;
    private SharedPreferences prefs;
    private List<Announcement> announcementList;
    private FloatingActionButton fabAdd;
    private String userType;

    private static final String PREFS_NAME = "AnnouncementsPrefs";
    private static final String ANNOUNCEMENTS_KEY = "announcements";
    private static final String STAFF = "Staff";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        announcementRecyclerView = view.findViewById(R.id.announcementRecyclerView);
        fabAdd = view.findViewById(R.id.fab_add_announcement);

        SharedPreferences loginPrefs = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        userType = loginPrefs.getString("userType", "Student");

        if (!STAFF.equals(userType)) {
            fabAdd.setVisibility(View.GONE);
        }

        prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(ANNOUNCEMENTS_KEY, "");
        announcementList = json.isEmpty() ? new ArrayList<>() :
                new Gson().fromJson(json, new TypeToken<List<Announcement>>() {}.getType());

        adapter = new AnnouncementAdapter(announcementList, this::deleteAnnouncement, this::showEditDialog, userType);
        announcementRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        announcementRecyclerView.setAdapter(adapter);

        fabAdd.setOnClickListener(v -> showAddAnnouncementDialog());

        return view;
    }

    private void showAddAnnouncementDialog() {
        if (!STAFF.equals(userType)) return;

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog, null);
        EditText headingInput = dialogView.findViewById(R.id.editHeading);
        EditText bodyInput = dialogView.findViewById(R.id.editBody);

        new AlertDialog.Builder(getContext())
                .setTitle("Create Announcement")
                .setView(dialogView)
                .setPositiveButton("Create", (dialog, which) -> {
                    String heading = headingInput.getText().toString().trim();
                    String body = bodyInput.getText().toString().trim();
                    if (!heading.isEmpty() && !body.isEmpty()) {
                        String timestamp = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(new Date());
                        Announcement a = new Announcement(heading, body, timestamp);
                        announcementList.add(a);
                        saveAnnouncements();
                        adapter.notifyItemInserted(announcementList.size() - 1);
                    } else {
                        Toast.makeText(getContext(), "Both fields required", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditDialog(Announcement a, int position) {
        if (!STAFF.equals(userType)) return;

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog, null);
        EditText headingInput = dialogView.findViewById(R.id.editHeading);
        EditText bodyInput = dialogView.findViewById(R.id.editBody);

        headingInput.setText(a.heading);
        bodyInput.setText(a.body);

        new AlertDialog.Builder(getContext())
                .setTitle("Edit Announcement")
                .setView(dialogView)
                .setPositiveButton("Update", (dialog, which) -> {
                    String newHeading = headingInput.getText().toString().trim();
                    String newBody = bodyInput.getText().toString().trim();
                    if (!newHeading.isEmpty() && !newBody.isEmpty()) {
                        a.heading = newHeading;
                        a.body = newBody;
                        a.timestamp = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(new Date());
                        saveAnnouncements();
                        adapter.notifyItemChanged(position);
                    } else {
                        Toast.makeText(getContext(), "Both fields required", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteAnnouncement(Announcement a) {
        if (!STAFF.equals(userType)) return;

        int index = announcementList.indexOf(a);
        if (index >= 0) {
            announcementList.remove(index);
            saveAnnouncements();
            adapter.notifyItemRemoved(index);
            Toast.makeText(getContext(), "Deleted announcement", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveAnnouncements() {
        String json = new Gson().toJson(announcementList);
        prefs.edit().putString(ANNOUNCEMENTS_KEY, json).apply();
    }

    public static class Announcement {
        String heading;
        String body;
        String timestamp;

        public Announcement(String heading, String body, String timestamp) {
            this.heading = heading;
            this.body = body;
            this.timestamp = timestamp;
        }
    }

    public static class AnnouncementAdapter extends RecyclerView.Adapter<AnnouncementAdapter.ViewHolder> {

        private final List<Announcement> announcements;
        private final DeleteCallback deleteCallback;
        private final EditCallback editCallback;
        private final String userType;

        public interface DeleteCallback {
            void onDelete(Announcement a);
        }

        public interface EditCallback {
            void onEdit(Announcement a, int position);
        }

        public AnnouncementAdapter(List<Announcement> announcements, DeleteCallback deleteCallback, EditCallback editCallback, String userType) {
            this.announcements = announcements;
            this.deleteCallback = deleteCallback;
            this.editCallback = editCallback;
            this.userType = userType;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bg, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Announcement a = announcements.get(position);
            holder.heading.setText(a.heading);
            holder.body.setText(a.body);
            holder.timestamp.setText(a.timestamp);

            holder.itemView.setOnLongClickListener(v -> {
                if (!STAFF.equals(userType)) return false;

                String[] options = {"Edit", "Delete"};
                new AlertDialog.Builder(v.getContext())
                        .setTitle("Announcement Options")
                        .setItems(options, (dialog, which) -> {
                            if (which == 0) editCallback.onEdit(a, holder.getAdapterPosition());
                            else deleteCallback.onDelete(a);
                        })
                        .show();
                return true;
            });
        }

        @Override
        public int getItemCount() {
            return announcements.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView heading, body, timestamp;

            ViewHolder(View view) {
                super(view);
                heading = view.findViewById(R.id.headingText);
                body = view.findViewById(R.id.bodyText);
                timestamp = view.findViewById(R.id.timestampText);

            }
        }
    }
}
