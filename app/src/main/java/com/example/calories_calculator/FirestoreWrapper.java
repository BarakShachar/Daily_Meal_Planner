package com.example.calories_calculator;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Map;

public class FirestoreWrapper {
    private final FirebaseFirestore db;

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
}
