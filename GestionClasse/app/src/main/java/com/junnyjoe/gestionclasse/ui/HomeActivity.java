package com.junnyjoe.gestionclasse.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.junnyjoe.gestionclasse.R;
import com.junnyjoe.gestionclasse.databinding.ActivityHomeBinding;
import com.junnyjoe.gestionclasse.util.Motion;
import com.junnyjoe.gestionclasse.util.Session;
import com.junnyjoe.gestionclasse.viewmodel.ClasseViewModel;
import com.junnyjoe.gestionclasse.viewmodel.EtudiantViewModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** Écran 2 : tableau de bord (classe, accès rapide aux étudiants et aux conversations). */
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
        ActionBar bar = getSupportActionBar();
        if (bar != null) bar.hide(); // l'en-tête est dans la mise en page

        boolean teacher = session.isTeacher();
        ClasseViewModel classeVm = new ViewModelProvider(this).get(ClasseViewModel.class);
        EtudiantViewModel etudiantVm = new ViewModelProvider(this).get(EtudiantViewModel.class);
        etudiantVm.setQuery("");

        String date = new SimpleDateFormat("EEEE d MMMM", Locale.FRANCE).format(new Date());
        b.tvDate.setText(date.substring(0, 1).toUpperCase(Locale.FRANCE) + date.substring(1));

        if (teacher) {
            b.tvGreeting.setText(R.string.greeting_teacher);
            b.tvAvatar.setText("E");
            b.cardMessages.setVisibility(View.GONE);
        } else {
            etudiantVm.getEtudiant(session.getStudentId()).observe(this, e -> {
                if (e == null) {
                    // Profil supprimé par l'enseignant : retour au choix du mode.
                    goToRole();
                } else {
                    b.tvGreeting.setText(getString(R.string.greeting_student, e.prenom));
                    b.tvAvatar.setText(e.getInitiales());
                }
            });
        }

        etudiantVm.getEtudiants().observe(this, list -> {
            int n = list == null ? 0 : list.size();
            b.tvCount.setText(String.valueOf(n));
            b.tvCountLabel.setText(getResources().getQuantityString(R.plurals.enrolled_label, n));
            b.tvStudentsSub.setText(getResources().getQuantityString(R.plurals.students_count, n, n));
        });

        classeVm.getClasse().observe(this, classe -> {
            boolean exists = classe != null;
            boolean appearing = exists && b.cardClasse.getVisibility() != View.VISIBLE;
            b.cardClasse.setVisibility(exists ? View.VISIBLE : View.GONE);
            if (appearing && savedInstanceState == null) Motion.enter(b.cardClasse);
            b.tvNoClasse.setVisibility(exists ? View.GONE : View.VISIBLE);
            b.tvNoClasse.setText(teacher ? R.string.no_classe_teacher : R.string.no_classe_student);
            if (exists) {
                b.tvClasseTitle.setText(classe.intitule);
                b.tvClasseDesc.setText(classe.description);
                b.tvClasseYear.setText(classe.anneeUniversitaire);
            }
            if (teacher) {
                b.tvClasseSub.setText(exists ? R.string.sub_edit : R.string.sub_create);
                b.cardClasseAction.setVisibility(View.VISIBLE);
            } else {
                b.tvClasseSub.setText(R.string.sub_view);
                b.cardClasseAction.setVisibility(exists ? View.VISIBLE : View.GONE);
            }
        });

        if (savedInstanceState == null) {
            Motion.enter(b.llHeader, b.tvQuick, b.cardStudents, b.cardMessages, b.cardClasseAction);
            Motion.pop(b.btnFab);
        }

        View.OnClickListener openStudents = v -> startActivity(
                EtudiantListActivity.intent(this, EtudiantListActivity.MODE_BROWSE, Session.NO_STUDENT));
        b.cardStudents.setOnClickListener(openStudents);
        b.navStudents.setOnClickListener(openStudents);
        b.cardClasseAction.setOnClickListener(v -> startActivity(new Intent(this, ClasseActivity.class)));
        b.cardMessages.setOnClickListener(v -> openRecipientPicker());
        b.ibMode.setOnClickListener(v -> goToRole());
        // Bouton central : ajouter un étudiant (enseignant) ou écrire un message (étudiant).
        b.btnFab.setOnClickListener(v -> {
            if (teacher) {
                startActivity(new Intent(this, EtudiantFormActivity.class));
            } else {
                openRecipientPicker();
            }
        });
    }

    private void openRecipientPicker() {
        startActivity(EtudiantListActivity.intent(this, EtudiantListActivity.MODE_PICK_RECIPIENT,
                session.getStudentId()));
    }

    private void goToRole() {
        new Session(this).clear();
        Intent i = new Intent(this, RoleActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}
