package com.junnyjoe.gestionclasse.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.junnyjoe.gestionclasse.R;
import com.junnyjoe.gestionclasse.databinding.ActivityHomeBinding;
import com.junnyjoe.gestionclasse.util.Session;
import com.junnyjoe.gestionclasse.viewmodel.ClasseViewModel;
import com.junnyjoe.gestionclasse.viewmodel.EtudiantViewModel;

/** Écran 2 : présentation de la classe et accès aux étudiants et aux conversations. */
public class HomeActivity extends AppCompatActivity {
    private Session session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        session = new Session(this);
        if (session.getRole() == null) {
            goToRole();
            return;
        }
        ActivityHomeBinding b = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        setTitle(R.string.app_name);

        boolean teacher = session.isTeacher();
        ClasseViewModel classeVm = new ViewModelProvider(this).get(ClasseViewModel.class);

        if (teacher) {
            b.tvWelcome.setText(R.string.welcome_teacher);
            b.btnMessages.setVisibility(View.GONE);
        } else {
            EtudiantViewModel etudiantVm = new ViewModelProvider(this).get(EtudiantViewModel.class);
            etudiantVm.getEtudiant(session.getStudentId()).observe(this, e -> {
                if (e == null) {
                    // Profil supprimé par l'enseignant : retour au choix du mode.
                    goToRole();
                } else {
                    b.tvWelcome.setText(getString(R.string.welcome_student, e.getNomComplet()));
                }
            });
        }

        classeVm.getClasse().observe(this, classe -> {
            boolean exists = classe != null;
            b.cardClasse.setVisibility(exists ? View.VISIBLE : View.GONE);
            b.tvNoClasse.setVisibility(exists ? View.GONE : View.VISIBLE);
            b.tvNoClasse.setText(teacher ? R.string.no_classe_teacher : R.string.no_classe_student);
            if (exists) {
                b.tvClasseTitle.setText(classe.intitule);
                b.tvClasseDesc.setText(classe.description);
                b.tvClasseYear.setText(classe.anneeUniversitaire);
            }
            if (teacher) {
                b.btnClasse.setText(exists ? R.string.classe_edit : R.string.classe_create);
                b.btnClasse.setVisibility(View.VISIBLE);
            } else {
                b.btnClasse.setText(R.string.classe_view);
                b.btnClasse.setVisibility(exists ? View.VISIBLE : View.GONE);
            }
        });

        b.btnClasse.setOnClickListener(v -> startActivity(new Intent(this, ClasseActivity.class)));
        b.btnStudents.setOnClickListener(v -> startActivity(
                EtudiantListActivity.intent(this, EtudiantListActivity.MODE_BROWSE, Session.NO_STUDENT)));
        b.btnMessages.setOnClickListener(v -> startActivity(
                EtudiantListActivity.intent(this, EtudiantListActivity.MODE_PICK_RECIPIENT,
                        session.getStudentId())));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_change_mode) {
            goToRole();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void goToRole() {
        new Session(this).clear();
        Intent i = new Intent(this, RoleActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}
