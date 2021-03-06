package com.elyneker.chatandroid.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.elyneker.chatandroid.R;
import com.elyneker.chatandroid.config.ConfiguracaoFirebase;
import com.elyneker.chatandroid.helper.Base64Custom;
import com.elyneker.chatandroid.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

public class CadastroActivity extends AppCompatActivity {

    private EditText nome;
    private EditText email;
    private EditText senha;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        nome = findViewById(R.id.editNome);
        email = findViewById(R.id.editEmail);
        senha = findViewById(R.id.editSenha);
    }

    public void validarDadosUsuario(View view) {
        String textNome = nome.getText().toString();
        String textEmail = email.getText().toString();
        String textSenha = senha.getText().toString();

        if (!textNome.isEmpty() && !textEmail.isEmpty() && textSenha.length() >= 6) {
            Usuario usuario = new Usuario();
            usuario.setNome(textNome);
            usuario.setEmail(textEmail);
            usuario.setSenha(textSenha);

            cadastrarUsuario(usuario);

        } else {
            Toast.makeText(CadastroActivity.this, "Preencha os campos corretamente",
                    Toast.LENGTH_SHORT).show();
        }
    }

    //TODO: REFATORAR OS ERROS PAR UMA CLASSE DE TESTE DE ERROS
    public void cadastrarUsuario(Usuario usuario) {
        auth = ConfiguracaoFirebase.getFirebaseAutenticacao();
        auth.createUserWithEmailAndPassword(
                usuario.getEmail(), usuario.getSenha()
        ).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {

                    try {
                        String identificadorUsuario = Base64Custom.codificarBase64(usuario.getEmail());
                        usuario.setIdUser(identificadorUsuario);
                        usuario.salvar();
                    }catch (Exception e) {
                        e.printStackTrace();
                    }

                    Toast.makeText(CadastroActivity.this, "Sucesso ao realizar o Cadastro",
                            Toast.LENGTH_SHORT).show();

                    finish();
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

                }

            }
        });

    }
}