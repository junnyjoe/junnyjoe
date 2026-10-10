package com.junnyjoe.gestionclasse.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.junnyjoe.gestionclasse.R;
import com.junnyjoe.gestionclasse.data.Etudiant;
import com.junnyjoe.gestionclasse.databinding.ActivityEtudiantListBinding;
import com.junnyjoe.gestionclasse.util.Motion;
import com.junnyjoe.gestionclasse.util.Session;
import com.junnyjoe.gestionclasse.viewmodel.EtudiantViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Écran 4 : liste des étudiants. Réutilisé en trois modes :
 * consultation, choix du profil local (mode Étudiant) et choix du destinataire d'une conversation.
 */
public class EtudiantListActivity extends AppCompatActivity {
    public static final String MODE_BROWSE = "BROWSE";
    public static final String MODE_PICK_PROFILE = "PICK_PROFILE";
    public static final String MODE_PICK_RECIPIENT = "PICK_RECIPIENT";
    private static final String EXTRA_MODE = "mode";
    private static final String EXTRA_EXCLUDE = "exclude_id";

    private ActivityEtudiantListBinding b;
    private EtudiantViewModel vm;
    private String mode;
    private long excludeId;
    private boolean teacher;

    public static Intent intent(Context c, String mode, long excludeId) {
        return new Intent(c, EtudiantListActivity.class)
                .putExtra(EXTRA_MODE, mode)
                .putExtra(EXTRA_EXCLUDE, excludeId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityEtudiantListBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        ActionBar bar = getSupportActionBar();
        if (bar != null) bar.setDisplayHomeAsUpEnabled(true);

        String m = getIntent().getStringExtra(EXTRA_MODE);
        mode = m == null ? MODE_BROWSE : m;
        excludeId = getIntent().getLongExtra(EXTRA_EXCLUDE, Session.NO_STUDENT);
        teacher = new Session(this).isTeacher();

        switch (mode) {
            case MODE_PICK_PROFILE:
                setTitle(R.string.pick_profile_title);
                break;
            case MODE_PICK_RECIPIENT:
                setTitle(R.string.pick_recipient_title);
                break;
            default:
                setTitle(R.string.students_title);
        }

        vm = new ViewModelProvider(this).get(EtudiantViewModel.class);
        vm.setQuery("");

        EtudiantAdapter adapter = new EtudiantAdapter(this::onEtudiantClick);
        b.rv.setLayoutManager(new LinearLayoutManager(this));
        b.rv.setAdapter(adapter);

        // Seul l'enseignant peut ajouter un étudiant.
        boolean canAdd = teacher && MODE_BROWSE.equals(mode);
        b.fabAdd.setVisibility(canAdd ? View.VISIBLE : View.GONE);
        if (canAdd && savedInstanceState == null) Motion.pop(b.fabAdd);
        b.fabAdd.setOnClickListener(v -> startActivity(new Intent(this, EtudiantFormActivity.class)));

        b.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                vm.setQuery(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        vm.getEtudiants().observe(this, list -> {
            List<Etudiant> shown = new ArrayList<>();
            if (list != null) {
                for (Etudiant e : list) {
                    if (e.id != excludeId) shown.add(e);
                }
            }
            adapter.submitList(shown);
            b.tvCount.setText(getResources().getQuantityString(R.plurals.students_count,
                    shown.size(), shown.size()));
            boolean empty = shown.isEmpty();
            b.tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
            b.rv.setVisibility(empty ? View.GONE : View.VISIBLE);
            if (empty) b.tvEmpty.setText(emptyMessage());
        });
    }

    private int emptyMessage() {
        if (!vm.getQuery().trim().isEmpty()) return R.string.empty_students_search;
        return teacher ? R.string.empty_students_teacher : R.string.empty_students_pick;
    }

    private void onEtudiantClick(Etudiant e) {
        Session session = new Session(this);
        switch (mode) {
            case MODE_PICK_PROFILE:
                session.setStudent(e.id);
                Intent home = new Intent(this, HomeActivity.class);
                home.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(home);
                finish();
                break;
            case MODE_PICK_RECIPIENT:
                startActivity(ConversationActivity.intent(this, session.getStudentId(), e.id));
                break;
            default:
                startActivity(EtudiantDetailActivity.intent(this, e.id));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
