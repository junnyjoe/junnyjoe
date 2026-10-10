package com.junnyjoe.gestionclasse.ui;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.junnyjoe.gestionclasse.R;
import com.junnyjoe.gestionclasse.data.Callback;
import com.junnyjoe.gestionclasse.data.Etudiant;
import com.junnyjoe.gestionclasse.databinding.ActivityEtudiantDetailBinding;
import com.junnyjoe.gestionclasse.util.Motion;
import com.junnyjoe.gestionclasse.util.Session;
import com.junnyjoe.gestionclasse.viewmodel.EtudiantViewModel;

/** Détail d'un étudiant : appel (composeur), modification et suppression (enseignant), message (étudiant). */
public class EtudiantDetailActivity extends AppCompatActivity {
    private static final String EXTRA_ID = "etudiant_id";

    private ActivityEtudiantDetailBinding b;
    private EtudiantViewModel vm;
    private Etudiant current;
    private boolean deleting;

    public static Intent intent(Context c, long etudiantId) {
        return new Intent(c, EtudiantDetailActivity.class).putExtra(EXTRA_ID, etudiantId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityEtudiantDetailBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        setTitle(R.string.student_detail_title);
        ActionBar bar = getSupportActionBar();
        if (bar != null) bar.setDisplayHomeAsUpEnabled(true);

        long id = getIntent().getLongExtra(EXTRA_ID, 0);
        Session session = new Session(this);
        boolean teacher = session.isTeacher();
        long meId = session.getStudentId();

        // Modification et suppression : enseignant uniquement.
        int teacherVisibility = teacher ? View.VISIBLE : View.GONE;
        b.btnEdit.setVisibility(teacherVisibility);
        b.btnDelete.setVisibility(teacherVisibility);
        boolean canMessage = session.isStudent() && meId != Session.NO_STUDENT && meId != id;
        b.btnMessage.setVisibility(canMessage ? View.VISIBLE : View.GONE);

        if (savedInstanceState == null) {
            Motion.enter(b.tvName, b.cardInfo, b.btnCall, b.btnMessage, b.btnEdit, b.btnDelete);
        }

        vm = new ViewModelProvider(this).get(EtudiantViewModel.class);
        vm.getEtudiant(id).observe(this, e -> {
            if (e == null) {
                if (!deleting) finish(); // supprimé entre-temps
                return;
            }
            current = e;
            b.tvName.setText(e.getNomComplet());
            b.tvEmail.setText(e.email);
            b.tvPhone.setText(e.telephone);
        });

        b.btnCall.setOnClickListener(v -> call());
        b.btnEdit.setOnClickListener(v -> startActivity(EtudiantFormActivity.intent(this, id)));
        b.btnDelete.setOnClickListener(v -> confirmDelete());
        b.btnMessage.setOnClickListener(v -> startActivity(ConversationActivity.intent(this, meId, id)));
    }

    /** Ouvre le composeur avec le numéro : l'appel n'est jamais lancé automatiquement. */
    private void call() {
        if (current == null) return;
        Intent dial = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", current.telephone.trim(), null));
        try {
            startActivity(dial);
        } catch (ActivityNotFoundException e) {
            Snackbar.make(b.getRoot(), R.string.call_unavailable, Snackbar.LENGTH_LONG).show();
        }
    }

    private void confirmDelete() {
        if (current == null) return;
        Etudiant e = current;
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_student_title)
                .setMessage(getString(R.string.delete_student_message, e.getNomComplet()))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (d, w) -> {
                    deleting = true;
                    vm.delete(e, new Callback<Long>() {
                        @Override
                        public void onSuccess(Long result) {
                            Toast.makeText(EtudiantDetailActivity.this, R.string.student_deleted,
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        }

                        @Override
                        public void onError(Exception ex) {
                            deleting = false;
                            Snackbar.make(b.getRoot(), R.string.error_save, Snackbar.LENGTH_LONG).show();
                        }
                    });
                })
                .show();
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
