package com.example.calories_calculator;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class FirestoreWrapper {
    private final FirebaseFirestore db;
    String userMail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

    public FirestoreWrapper(){
        this.db = FirebaseFirestore.getInstance();
    }

    public Task<DocumentSnapshot> getDocument(String pathToDoc){
        DocumentReference docRef = this.db.document(pathToDoc);
        return docRef.get();
    }

    public DocumentReference getDocumentRef(String pathToDoc){
        return this.db.document(pathToDoc);
    }

    public CollectionReference getCollectionRef(String pathToCollection){
        return this.db.collection(pathToCollection);
    }

    public Task<Void> setDocument(String path, Map<String, Object> data){
        return this.db.document(path).set(data);
    }

    public static class UserMainScreenWrapper extends FirestoreWrapper{
        UserMainScreenWrapper thisScreenWrapper = this;
        public UserMainScreenWrapper(){
            super();
        }

        public void getUserMenus(UserMainScreen screen){
            this.getCollectionRef("users/"+userMail+"/menus")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                Log.d("mainActivity", "success get menus");
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    screen.userMenus.put(document.getId(), document.getData());
                                }
                                screen.mainFunction();
                            } else {
                                Log.d("mainActivity", "Error getting documents: ", task.getException());
                            }
                        }
                    });
        }

        public void removeMenu(String menuName){
            this.getCollectionRef("users/" + userMail + "/menus/" + menuName + "/meals")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                Log.d("mainActivity", "success get meals to delete");
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    document.getReference().delete();
                                }
                            } else {
                                Log.d("mainActivity", "Error getting documents: ", task.getException());
                            }
                        }
                    });

            this.getDocumentRef("users/" + userMail + "/menus/"+menuName)
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("mainActivity", "DocumentSnapshot successfully deleted!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("mainActivity", "Error deleting document", e);
                        }
                    });
        }

        void addNewMenu(UserMainScreen screen,String menuName){
            Map<String, Object> menu = new HashMap<>();
            menu.put("totalCals", 0);
            this.setDocument("users/" + userMail + "/menus/"+menuName,menu)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            thisScreenWrapper.getUserMenus(screen);
                        }
                    });
        }

        public void getUserName(UserMainScreen screen){
            this.getDocument("users/"+userMail)
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    screen.userName = (String) document.getData().get("name");
                                    String adminRequest = (String) document.getData().get("message");
                                    if (adminRequest != null){
                                        screen.AdminRequest(adminRequest);
                                    }
                                    thisScreenWrapper.getUserMenus(screen);
                                }
                            }
                        }
                    });
        }

    }



}
