package com.elyneker.chatandroid.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.elyneker.chatandroid.R;
import com.elyneker.chatandroid.config.ConfiguracaoFirebase;
import com.elyneker.chatandroid.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText emailLogin;
    private EditText senhaLogin;

    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

        emailLogin = findViewById(R.id.editEmailLogin);
        senhaLogin = findViewById(R.id.editSenhaLogin);
    }

    //Iniciar com usuário logado
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = autenticacao.getCurrentUser();
        if(user != null) {
            abrirTelaPrincipal();
        }
    }

    public void abrirTelaCadastro(View view) {
        Intent intent = new Intent(LoginActivity.this, CadastroActivity.class);
        startActivity(intent);
    }

    public void abrirTelaPrincipal() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
    }



    //TODO: REFATORAR PARA TIRAR AS VALIDAÇÕES E COLOCAR EM UMA CLASSE RESPONSÁVEL
    public void autenticarUsuario(View view) {
        String email = emailLogin.getText().toString();
        String senha = senhaLogin.getText().toString();

        if (!email.isEmpty() && senha.length() >= 6) {
            Usuario usuario = new Usuario();
            usuario.setEmail(email);
            usuario.setSenha(senha);

            logarUsuario(usuario);
        } else {
            Toast.makeText(LoginActivity.this, "Preencha os campos corretamente",
                    Toast.LENGTH_SHORT).show();
        }
    }

    //TODO: REMOVER TESTES PARA CLASSE RESPONSÁVEL
    public void logarUsuario(Usuario usuario) {

        autenticacao.signInWithEmailAndPassword(
                usuario.getEmail(), usuario.getSenha()
        ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {
                   abrirTelaPrincipal();
                } else {
                    String excecao = "";
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthWeakPasswordException e) {
                        excecao = "Digite uma senha mais forte";
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        excecao = "Por favor, digite uma e-mail válido";
                    } catch (FirebaseAuthUserCollisionException e) {
                        excecao = "Esta conta já utiliza este email";
                    } catch (Exception e) {
                        excecao = "Erro ao cadastrar usuário: " + e.getMessage();
                        e.printStackTrace();
                    }

                    Toast.makeText(LoginActivity.this, excecao,
                            Toast.LENGTH_SHORT).show();

                }

            }
        });

    }
}