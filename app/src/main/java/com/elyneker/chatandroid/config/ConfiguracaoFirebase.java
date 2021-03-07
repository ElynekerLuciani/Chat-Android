package com.elyneker.chatandroid.config;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ConfiguracaoFirebase {

    private static FirebaseAuth auth;
    private static DatabaseReference database;
    private static StorageReference storageReference;

    //retornar a instancia do FirebaseDatabase
    public static DatabaseReference getFirebaseDatabase() {
        if(database == null) {
            database = FirebaseDatabase.getInstance().getReference();
        }
        return database;
    }

    //retornar a instancia do FirebaseAuth
    public static FirebaseAuth getFirebaseAutenticacao() {
        if(auth == null) {
            auth = FirebaseAuth.getInstance();
        }
        return  auth;
    }

    public static StorageReference getFirebaseStorage() {
        if(storageReference == null) {
            storageReference = FirebaseStorage.getInstance().getReference();
        }
        return storageReference;
    }

}
