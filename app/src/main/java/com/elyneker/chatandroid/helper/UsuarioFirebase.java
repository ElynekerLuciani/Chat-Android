package com.elyneker.chatandroid.helper;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.elyneker.chatandroid.config.ConfiguracaoFirebase;
import com.elyneker.chatandroid.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.Objects;

public class UsuarioFirebase {

    public static String getIdentificadorUsuario() {

        FirebaseAuth auth = ConfiguracaoFirebase.getFirebaseAutenticacao();
        String email = Objects.requireNonNull(auth.getCurrentUser()).getEmail();
        assert email != null;

        return Base64Custom.codificarBase64(email);
    }

    public static FirebaseUser getUsuarioAtual() {
        FirebaseAuth auth = ConfiguracaoFirebase.getFirebaseAutenticacao();
        return auth.getCurrentUser();
    }

    public static boolean atualizarFotoUsuario(Uri url) {
        try {
            FirebaseUser user = getUsuarioAtual();
            UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                    .setPhotoUri(url)
                    .build();
            user.updateProfile(profile).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(!task.isSuccessful()) {
                        Log.d("Perfil", "Erro ao atualizar foto de usuário");
                    }
                }
            });
            return true;

        }catch (Exception e) {
            e.printStackTrace();
        }
        return false;

    }

    public static boolean atualizarNomeUsuario(String nome) {
        try {
            FirebaseUser user = getUsuarioAtual();
            UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                    .setDisplayName(nome)
                    .build();
            user.updateProfile(profile).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(!task.isSuccessful()) {
                        Log.d("Perfil", "Erro ao atualizar nome de usuário");
                    }
                }
            });
            return true;

        }catch (Exception e) {
            e.printStackTrace();
        }
        return false;

    }

    public static Usuario getDadosUsuarioLogado() {
        FirebaseUser firebaseUser = getUsuarioAtual();
        Usuario dadosUsuario = new Usuario();
        dadosUsuario.setEmail(firebaseUser.getEmail());
        dadosUsuario.setNome(firebaseUser.getDisplayName());

        if(firebaseUser.getPhotoUrl() == null) {
            dadosUsuario.setFoto("");
        } else {
            dadosUsuario.setFoto(firebaseUser.getPhotoUrl().toString());
        }

        return dadosUsuario;
    }




}
