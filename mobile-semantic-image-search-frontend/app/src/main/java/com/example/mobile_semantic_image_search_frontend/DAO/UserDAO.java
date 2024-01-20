package com.example.mobile_semantic_image_search_frontend.DAO;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.mobile_semantic_image_search_frontend.Object.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserDAO {

    public interface UserCallback {
        void onUserRetrieved(User user);
    }
    public static void deleteUser (FirebaseUser user){
        user.delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("DELETE USER", "User account deleted.");
                        }
                    }
                });
    }

    public static void getUserbyUID(String UID, UserDAO.UserCallback callback){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference seatsRef = database.getReference("users");

        seatsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot seatSnapshot : dataSnapshot.getChildren()) {
                    User m_user = seatSnapshot.getValue(User.class);

                    if (m_user.getUserID().equals(UID)) {
                        callback.onUserRetrieved(m_user);
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("GETUSERS", "Failed to read value.", databaseError.toException());
            }
        });
    }
}
