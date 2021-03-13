package com.elyneker.chatandroid.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import com.elyneker.chatandroid.adapter.ContatosAdapter;
import com.elyneker.chatandroid.adapter.GrupoSelecionadoAdapter;
import com.elyneker.chatandroid.config.ConfiguracaoFirebase;
import com.elyneker.chatandroid.helper.RecyclerItemClickListener;
import com.elyneker.chatandroid.helper.UsuarioFirebase;
import com.elyneker.chatandroid.model.Usuario;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.AdapterView;

import com.elyneker.chatandroid.R;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GrupoActivity extends AppCompatActivity {

    private RecyclerView recyclerMembroSelecionado;
    private RecyclerView recyclerMembro;
    private Toolbar toolbar;
    private FloatingActionButton fabAvancarCadastro;

    private ContatosAdapter contatosAdapter;
    private GrupoSelecionadoAdapter grupoSelecionadoAdapter;
    private List<Usuario> listaMembros = new ArrayList<>();
    private List<Usuario> listaMembrosSelecionados = new ArrayList<>();
    private ValueEventListener valueEventListenerMembros;

    private DatabaseReference usuarioRef;
    private FirebaseUser usuarioAtual;

    @SuppressLint("CutPasteId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grupo);
        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);


        //configuração inicial
        recyclerMembro = findViewById(R.id.recyclerMembros);
        recyclerMembroSelecionado = findViewById(R.id.recyclerMembrosSelecionados);
        fabAvancarCadastro = findViewById(R.id.fabSalvarGrupo);

        usuarioRef = ConfiguracaoFirebase.getFirebaseDatabase().child("usuarios");
        usuarioAtual = UsuarioFirebase.getUsuarioAtual();

        //configuração adapter
        contatosAdapter = new ContatosAdapter(listaMembros, getApplicationContext());

        //configuração recyclerview
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerMembro.setLayoutManager(layoutManager);
        recyclerMembro.setHasFixedSize(true);
        recyclerMembro.setAdapter(contatosAdapter);

        recyclerMembro.addOnItemTouchListener(new RecyclerItemClickListener(
                getApplicationContext(),
                recyclerMembro,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Usuario usuarioSelecionado = listaMembros.get(position);

                        //remover usuario selacionado
                        listaMembros.remove(usuarioSelecionado);
                        contatosAdapter.notifyDataSetChanged();

                        //adicionar usuario na lista de usuarios selecionados para o grupo
                        listaMembrosSelecionados.add(usuarioSelecionado);
                        grupoSelecionadoAdapter.notifyDataSetChanged();

                        //atualizar a quantidade de menbros na toolbar
                        atualizarMembrosToolbar();

                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

                    }

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    }
                }
        ));

        //configurar o recyclerview para os membros selecionados para o grupo
        grupoSelecionadoAdapter = new GrupoSelecionadoAdapter(listaMembrosSelecionados, getApplicationContext());

        RecyclerView.LayoutManager layoutManagerHorizontal = new LinearLayoutManager(
                getApplicationContext(),
                LinearLayoutManager.HORIZONTAL,
                false
        );
        recyclerMembroSelecionado.setLayoutManager(layoutManagerHorizontal);
        recyclerMembroSelecionado.setHasFixedSize(true);
        recyclerMembroSelecionado.setAdapter(grupoSelecionadoAdapter);

        recyclerMembroSelecionado.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        getApplicationContext(),
                        recyclerMembroSelecionado,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                Usuario usuarioSelecionado = listaMembrosSelecionados.get(position);

                                //Remover membro selecionado da lista
                                listaMembrosSelecionados.remove(usuarioSelecionado);
                                grupoSelecionadoAdapter.notifyDataSetChanged();

                                //adicionar membro na lista
                                listaMembros.add(usuarioSelecionado);
                                contatosAdapter.notifyDataSetChanged();

                                //atualizar a quantidade de menbros na toolbar
                                atualizarMembrosToolbar();
                            }

                            @Override
                            public void onLongItemClick(View view, int position) {

                            }

                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            }
                        }
                )
        );

        //configurar FAB
        fabAvancarCadastro.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent i = new Intent(GrupoActivity.this, CadastroGrupoActivity.class);
                i.putExtra("membros",(Serializable) listaMembrosSelecionados);
                startActivity(i);
            }

        });

    }


    @Override
    public void onStart() {
        super.onStart();

        recuperarContatos();
    }

    @Override
    public void onStop() {
        super.onStop();

        usuarioRef.removeEventListener(valueEventListenerMembros);
    }

    public void recuperarContatos() {
        valueEventListenerMembros = usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot dados : snapshot.getChildren()) {
                    Usuario usuario = dados.getValue(Usuario.class);

                    String emailAtual = usuarioAtual.getEmail();
                    assert emailAtual != null;
                    assert usuario != null;
                    if (!emailAtual.equals(usuario.getEmail())) {
                        listaMembros.add(usuario);
                    }
                }

                contatosAdapter.notifyDataSetChanged();

                //atualizar membros na toolbar
                atualizarMembrosToolbar();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void atualizarMembrosToolbar() {
        int totalSelecionados = listaMembrosSelecionados.size();
        int total = listaMembros.size() + totalSelecionados;

        toolbar.setSubtitle(totalSelecionados + " de " + total + " selecionados");
    }

}