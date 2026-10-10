package com.junnyjoe.gestionclasse.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.junnyjoe.gestionclasse.databinding.ActivityRoleBinding;
import com.junnyjoe.gestionclasse.util.Session;

/** Écran 1 : choix du mode Enseignant ou Étudiant. */
public class RoleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityRoleBinding b = ActivityRoleBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        Session session = new Session(this);

        b.btnTeacher.setOnClickListener(v -> {
            session.setTeacher();
            startActivity(new Intent(this, HomeActivity.class));
        });
        b.btnStudent.setOnClickListener(v ->
                startActivity(EtudiantListActivity.intent(this, EtudiantListActivity.MODE_PICK_PROFILE,
                        Session.NO_STUDENT)));
    }
}
