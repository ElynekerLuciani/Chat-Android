package com.elyneker.chatandroid.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import com.elyneker.chatandroid.adapter.GrupoSelecionadoAdapter;
import com.elyneker.chatandroid.config.ConfiguracaoFirebase;
import com.elyneker.chatandroid.helper.UsuarioFirebase;
import com.elyneker.chatandroid.model.Grupo;
import com.elyneker.chatandroid.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.elyneker.chatandroid.R;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class CadastroGrupoActivity extends AppCompatActivity {

    private static final int SELECAO_GALERIA = 20;

    private List<Usuario> listaMembrosSelecionados = new ArrayList<>();

    private TextView textTotalParticipantes;
    private RecyclerView recyclerMembrosSelecionados;
    private CircleImageView imageGrupo;
    private FloatingActionButton fabSalvarGrupo;
    private EditText editNomeGrupo;

    private GrupoSelecionadoAdapter grupoSelecionadoAdapter;
    private Grupo grupo;

    private StorageReference storageReference;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_grupo);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Novo Grupo");
        toolbar.setSubtitle("Defina um nome");
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        textTotalParticipantes = findViewById(R.id.textTotalParticipantes);
        recyclerMembrosSelecionados = findViewById(R.id.recycleMembrosGrupo);
        imageGrupo = findViewById(R.id.imageGrupo);
        fabSalvarGrupo = findViewById(R.id.fabSalvarGrupo);
        editNomeGrupo = findViewById(R.id.editNomeGrupo);

        grupo = new Grupo();

        storageReference = ConfiguracaoFirebase.getFirebaseStorage();



        //configuração de clique para inserir uma imagem no grupo
        imageGrupo.setOnClickListener(new View.OnClickListener(){

            @SuppressLint("QueryPermissionsNeeded")
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, SELECAO_GALERIA);
                }

            }
        });

        //configurar floating action button de salvar o novo grupo
        fabSalvarGrupo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nomeGrupo = editNomeGrupo.getText().toString();

                //adicionar à lista de membros o usuário que está logado
                listaMembrosSelecionados.add(UsuarioFirebase.getDadosUsuarioLogado());

                grupo.setMembros(listaMembrosSelecionados);
                grupo.setNome(nomeGrupo);
                grupo.salvar();

                Intent intent = new Intent(CadastroGrupoActivity.this, ChatActivity.class);
                intent.putExtra("chatGrupo", grupo);
                startActivity(intent);
            }
        });






        //recuperar a lista de membros selecionados
        if(getIntent().getExtras() != null) {
            List<Usuario> membros = (List<Usuario>) getIntent().getExtras().getSerializable("membros");
            listaMembrosSelecionados.addAll(membros);

           textTotalParticipantes.setText("Participantes: " + listaMembrosSelecionados.size());
        }

        //configurar recyclerview
        grupoSelecionadoAdapter = new GrupoSelecionadoAdapter(listaMembrosSelecionados, getApplicationContext());

        RecyclerView.LayoutManager layoutManagerHorizontal = new LinearLayoutManager(
                getApplicationContext(),
                LinearLayoutManager.HORIZONTAL,
                false
        );
        recyclerMembrosSelecionados.setLayoutManager(layoutManagerHorizontal);
        recyclerMembrosSelecionados.setHasFixedSize(true);
        recyclerMembrosSelecionados.setAdapter(grupoSelecionadoAdapter);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK) {
            Bitmap imagem = null;

            try {
                assert data != null;
                Uri locaImagemSelecionada = data.getData();
                imagem = MediaStore.Images.Media.getBitmap(
                        getContentResolver(),
                        locaImagemSelecionada);

                if(imagem != null) {
                    imageGrupo.setImageBitmap(imagem);

                    //Recuperar dados de imagens do firebase
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    imagem.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);
                    byte[] dadosBitmap = byteArrayOutputStream.toByteArray();

                    //salvar a imagem no firebase
                    final StorageReference imagemRef = storageReference
                            .child("imagens")
                            .child("grupos")
                            .child(grupo.getId() + "perfil.jpeg");

                    UploadTask uploadTask = imagemRef.putBytes(dadosBitmap);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(CadastroGrupoActivity.this,
                                    "Erro ao fazer upload de imagem",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(CadastroGrupoActivity.this,
                                    "Sucesso ao fazer upload de imagem",
                                    Toast.LENGTH_SHORT).show();
                            //atualizar a imagem
                            imagemRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    String url = Objects.requireNonNull(task.getResult()).toString();
                                    grupo.setFoto(url);


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
}