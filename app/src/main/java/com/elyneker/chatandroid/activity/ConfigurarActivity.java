package com.elyneker.chatandroid.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.elyneker.chatandroid.R;
import com.elyneker.chatandroid.config.ConfiguracaoFirebase;
import com.elyneker.chatandroid.helper.Permissao;
import com.elyneker.chatandroid.helper.UsuarioFirebase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ConfigurarActivity extends AppCompatActivity {

    private StorageReference storageReference;
    private String identificadorUsuario;

    private final String[] permissoesNecessarias = new String[] {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    private ImageButton imageButtonCamera, imageButtonGaleria;
    private EditText nomePerfilUsuario;
    private static final int SELECAO_CAMERA = 10;
    private static final int SELECAO_GALERIA = 20;
    private CircleImageView circleImageViewPerfil;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configurar);

        storageReference = ConfiguracaoFirebase.getFirebaseStorage();
        identificadorUsuario = UsuarioFirebase.getIdentificadorUsuario();

        //validar permissões do app
        Permissao.validarPermissoes(permissoesNecessarias, this, 1);

        nomePerfilUsuario = findViewById(R.id.editNomePerfilUsuario);
        imageButtonCamera = findViewById(R.id.imageButtonCamera);
        imageButtonGaleria = findViewById(R.id.imageButtonGaleria);
        circleImageViewPerfil = findViewById(R.id.circleImageViewFotoPerfil);
        imageButtonCamera.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("QueryPermissionsNeeded")
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if(intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, SELECAO_CAMERA);
                }


            }
        });

        imageButtonGaleria.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("QueryPermissionsNeeded")
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                if(intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, SELECAO_GALERIA);
                }
            }
        });


        //configurar a toolbar personalizada
        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        toolbar.setTitle("Configurar");
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        //recuperar a foto do usuário atual
        FirebaseUser user = UsuarioFirebase.getUsuarioAtual();
        Uri url = user.getPhotoUrl();

        if (url != null) {
            Glide.with(ConfigurarActivity.this)
                    .load(url)
                    .into(circleImageViewPerfil);

        } else {
            circleImageViewPerfil.setImageResource(R.drawable.padrao);
        }

        nomePerfilUsuario.setText(user.getDisplayName());

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            Bitmap bitmap = null;

            try {
                switch (requestCode) {
                    case SELECAO_CAMERA:
                        assert data != null;
                        bitmap = (Bitmap) data.getExtras().get("data");
                        break;
                    case SELECAO_GALERIA:
                        assert data != null;
                        Uri locaImagemSelecionada = data.getData();
                        bitmap = MediaStore.Images.Media.getBitmap(
                                getContentResolver(),
                                locaImagemSelecionada
                        );
                        break;
                }

                if(bitmap != null) {
                    circleImageViewPerfil.setImageBitmap(bitmap);
                    //Recuperar dados de imagens do firebase
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream );
                    byte[] dadosBitmap = byteArrayOutputStream.toByteArray();
                    //salvar a imagem no firebase
                    final StorageReference imagemRef = storageReference
                            .child("imagens")
                            .child("perfil")
                            .child(identificadorUsuario + "perfil.jpeg");

                    UploadTask uploadTask = imagemRef.putBytes(dadosBitmap);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ConfigurarActivity.this,
                                    "Erro ao fazer upload de imagem",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(ConfigurarActivity.this,
                                    "Sucesso ao fazer upload de imagem",
                                    Toast.LENGTH_SHORT).show();
                            //atualizar a imagem
                            imagemRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    Uri url = task.getResult();

                                    atualizaFotoUsuario(url);
                                }
                            });


                        }
                    });

                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private void atualizaFotoUsuario(Uri url) {
        UsuarioFirebase.atualizarFotoUsuario(url);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for (int permissaoResultado : grantResults) {
            if(permissaoResultado == PackageManager.PERMISSION_DENIED) {
                altertaValidacaoPermissao();
            }
        }
    }

    private void altertaValidacaoPermissao() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissões Negadas");
        builder.setMessage("Você precisa aceitar as permissões para continuar usando o Chat Android");
        builder.setCancelable(false);
        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}