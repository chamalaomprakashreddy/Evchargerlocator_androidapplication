package com.example.evchargerlocator_androidapplication.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    private RecyclerView recyclerViewUsers;
    private UsersAdapter usersAdapter;
    private List<User> userList;
    private DatabaseReference usersRef;
    private String currentUserId;

    private static final String TAG = "UsersFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_users, container, false);

        recyclerViewUsers = view.findViewById(R.id.recyclerViewUsers);
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(getContext()));

        userList = new ArrayList<>();
        usersAdapter = new UsersAdapter(userList, getContext(), user -> openChat(user));
        recyclerViewUsers.setAdapter(usersAdapter);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        if (currentUserId != null) {
            usersRef = FirebaseDatabase.getInstance().getReference("users");
            loadUsers();
        } else {
            Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
        }

        return view;
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
                    String email = dataSnapshot.child("email").getValue(String.class);
                    String phoneNumber = dataSnapshot.child("phoneNumber").getValue(String.class);
                    String vehicle = dataSnapshot.child("vehicle").getValue(String.class);
                    String lastSeen = dataSnapshot.child("lastSeen").getValue(String.class);

                    // ✅ If lastSeen is missing, set default to "Offline"
                    if (lastSeen == null) {
                        lastSeen = "Offline";
                    }

                    User user = new User(userId, email, fullName, phoneNumber, vehicle, lastSeen);
                    userList.add(user);
                }
                usersAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error fetching users: " + error.getMessage());
            }
        });
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
