package com.example.evchargerlocator_androidapplication.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.evchargerlocator_androidapplication.MessageActivity;
import com.example.evchargerlocator_androidapplication.R;
import com.example.evchargerlocator_androidapplication.User;
import com.example.evchargerlocator_androidapplication.UsersAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class UsersFragment extends Fragment {

    private EditText searchUser;
    private RecyclerView recyclerViewUsers;
    private UsersAdapter usersAdapter;
    private List<User> userList, filteredUserList;
    private DatabaseReference usersRef;
    private FirebaseAuth auth;
    private String currentUserId;

    private static final String TAG = "UsersFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_users, container, false);

        searchUser = view.findViewById(R.id.search_user);
        recyclerViewUsers = view.findViewById(R.id.recyclerViewUsers);
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(getContext()));

        userList = new ArrayList<>();
        filteredUserList = new ArrayList<>();
        usersAdapter = new UsersAdapter(filteredUserList, getContext(), this::openChat);
        recyclerViewUsers.setAdapter(usersAdapter);

        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        if (currentUserId != null) {
            usersRef = FirebaseDatabase.getInstance().getReference("users");
            setUserOnline();
            loadUsers();
        } else {
            Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
        }

        searchUser.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        return view;
    }

    private void setUserOnline() {
        if (currentUserId == null) return;
        DatabaseReference currentUserRef = usersRef.child(currentUserId);
        currentUserRef.child("status").setValue("Online");
        currentUserRef.child("status").onDisconnect().setValue("Offline");
    }

    private void loadUsers() {
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String userId = dataSnapshot.getKey();
                    if (userId == null || userId.equals(currentUserId)) {
                        continue;
                    }

                    String fullName = dataSnapshot.child("fullName").getValue(String.class);
                    String status = dataSnapshot.child("status").getValue(String.class);

                    if (fullName == null) {
                        fullName = "Unknown"; // Prevent null name crashes
                    }

                    String displayStatus = (status != null && status.equals("Online")) ? "Online" : "Offline";

                    User user = new User(userId, fullName, displayStatus);
                    userList.add(user);
                }
                filterUsers(searchUser.getText().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error fetching users: " + error.getMessage());
            }
        });
    }

    private void filterUsers(String query) {
        filteredUserList.clear();
        if (query.isEmpty()) {
            filteredUserList.addAll(userList);
        } else {
            for (User user : userList) {
                if (user.getFullName().toLowerCase().contains(query.toLowerCase())) {
                    filteredUserList.add(user);
                }
            }
        }
        usersAdapter.notifyDataSetChanged();
    }

    private void openChat(User user) {
        if (user == null || user.getId() == null) {
            Toast.makeText(getContext(), "User not found!", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(getContext(), MessageActivity.class);
        intent.putExtra("receiverUserId", user.getId());
        intent.putExtra("receiverUserName", user.getFullName());
        startActivity(intent);
    }
}
