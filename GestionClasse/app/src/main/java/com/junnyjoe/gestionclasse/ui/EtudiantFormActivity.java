package com.junnyjoe.gestionclasse.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.junnyjoe.gestionclasse.R;
import com.junnyjoe.gestionclasse.data.Callback;
import com.junnyjoe.gestionclasse.data.Etudiant;
import com.junnyjoe.gestionclasse.databinding.ActivityEtudiantFormBinding;
import com.junnyjoe.gestionclasse.util.Session;
import com.junnyjoe.gestionclasse.util.Validators;
import com.junnyjoe.gestionclasse.viewmodel.EtudiantViewModel;

/** Écran 5 : ajout ou modification d'un étudiant (réservé à l'enseignant). */
public class EtudiantFormActivity extends AppCompatActivity {
    private static final String EXTRA_ID = "etudiant_id";

    private ActivityEtudiantFormBinding b;
    private EtudiantViewModel vm;
    private long etudiantId;
    private boolean filled;

    public static Intent intent(Context c, long etudiantId) {
        return new Intent(c, EtudiantFormActivity.class).putExtra(EXTRA_ID, etudiantId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!new Session(this).isTeacher()) {
            finish(); // accès interdit en mode Étudiant
            return;
        }
        b = ActivityEtudiantFormBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        ActionBar bar = getSupportActionBar();
        if (bar != null) bar.setDisplayHomeAsUpEnabled(true);

        etudiantId = getIntent().getLongExtra(EXTRA_ID, 0);
        setTitle(etudiantId > 0 ? R.string.student_form_edit : R.string.student_form_add);
        vm = new ViewModelProvider(this).get(EtudiantViewModel.class);

        if (etudiantId > 0) {
            vm.getEtudiant(etudiantId).observe(this, e -> {
                if (!filled && e != null) {
                    b.etNom.setText(e.nom);
                    b.etPrenom.setText(e.prenom);
                    b.etEmail.setText(e.email);
                    b.etPhone.setText(e.telephone);
                    filled = true;
                }
            });
        }
        b.btnSave.setOnClickListener(v -> save());
    }

    private void save() {
        String nom = text(b.etNom);
        String prenom = text(b.etPrenom);
        String email = text(b.etEmail);
        String phone = text(b.etPhone);

        b.tilNom.setError(Validators.isBlank(nom) ? getString(R.string.error_required) : null);
        b.tilPrenom.setError(Validators.isBlank(prenom) ? getString(R.string.error_required) : null);
        b.tilEmail.setError(Validators.isEmailValid(email) ? null : getString(R.string.error_email));
        if (Validators.isBlank(phone)) {
            b.tilPhone.setError(getString(R.string.error_required));
        } else {
            b.tilPhone.setError(Validators.isPhoneValid(phone) ? null : getString(R.string.error_phone));
        }
        if (b.tilNom.getError() != null || b.tilPrenom.getError() != null
                || b.tilEmail.getError() != null || b.tilPhone.getError() != null) return;

        vm.save(new Etudiant(etudiantId, nom, prenom, email, phone), new Callback<Long>() {
            @Override
            public void onSuccess(Long id) {
                Toast.makeText(EtudiantFormActivity.this, R.string.student_saved, Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(Exception e) {
                Snackbar.make(b.getRoot(), R.string.error_save, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private static String text(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
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
