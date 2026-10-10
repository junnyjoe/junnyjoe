package com.junnyjoe.gestionclasse.ui;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.junnyjoe.gestionclasse.R;
import com.junnyjoe.gestionclasse.data.Callback;
import com.junnyjoe.gestionclasse.data.Classe;
import com.junnyjoe.gestionclasse.databinding.ActivityClasseBinding;
import com.junnyjoe.gestionclasse.util.Session;
import com.junnyjoe.gestionclasse.util.Validators;
import com.junnyjoe.gestionclasse.viewmodel.ClasseViewModel;

/** Écran 3 : création / modification (enseignant) ou consultation (étudiant) de la classe. */
public class ClasseActivity extends AppCompatActivity {
    private ActivityClasseBinding b;
    private ClasseViewModel vm;
    private boolean filled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityClasseBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        setTitle(R.string.classe_title);
        ActionBar bar = getSupportActionBar();
        if (bar != null) bar.setDisplayHomeAsUpEnabled(true);

        boolean teacher = new Session(this).isTeacher();
        vm = new ViewModelProvider(this).get(ClasseViewModel.class);

        if (!teacher) {
            // Consultation seule : champs verrouillés, pas de bouton d'enregistrement.
            b.etIntitule.setEnabled(false);
            b.etDescription.setEnabled(false);
            b.etAnnee.setEnabled(false);
            b.btnSave.setVisibility(View.GONE);
        }

        vm.getClasse().observe(this, c -> {
            if (!filled && c != null) {
                b.etIntitule.setText(c.intitule);
                b.etDescription.setText(c.description);
                b.etAnnee.setText(c.anneeUniversitaire);
            }
            filled = true;
        });

        b.btnSave.setOnClickListener(v -> save());
    }

    private void save() {
        String intitule = text(b.etIntitule);
        String description = text(b.etDescription);
        String annee = text(b.etAnnee);
        b.tilIntitule.setError(Validators.isBlank(intitule) ? getString(R.string.error_required) : null);
        b.tilDescription.setError(Validators.isBlank(description) ? getString(R.string.error_required) : null);
        b.tilAnnee.setError(Validators.isBlank(annee) ? getString(R.string.error_required) : null);
        if (Validators.isBlank(intitule) || Validators.isBlank(description) || Validators.isBlank(annee)) return;

        vm.save(new Classe(intitule, description, annee), new Callback<Long>() {
            @Override
            public void onSuccess(Long result) {
                Toast.makeText(ClasseActivity.this, R.string.classe_saved, Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(Exception e) {
                Snackbar.make(b.getRoot(), R.string.error_save, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private static String text(com.google.android.material.textfield.TextInputEditText et) {
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
