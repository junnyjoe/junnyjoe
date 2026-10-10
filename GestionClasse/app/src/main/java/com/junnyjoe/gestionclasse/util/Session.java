package com.junnyjoe.gestionclasse.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

/** Rôle actif (enseignant / étudiant) et profil étudiant local choisi. */
public final class Session {
    public static final String ROLE_TEACHER = "TEACHER";
    public static final String ROLE_STUDENT = "STUDENT";
    public static final long NO_STUDENT = -1;

    private static final String KEY_ROLE = "role";
    private static final String KEY_STUDENT = "student_id";

    private final SharedPreferences prefs;

    public Session(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences("session", Context.MODE_PRIVATE);
    }

    @Nullable
    public String getRole() {
        return prefs.getString(KEY_ROLE, null);
    }

    public boolean isTeacher() {
        return ROLE_TEACHER.equals(getRole());
    }

    public boolean isStudent() {
        return ROLE_STUDENT.equals(getRole());
    }

    public long getStudentId() {
        return prefs.getLong(KEY_STUDENT, NO_STUDENT);
    }

    public void setTeacher() {
        prefs.edit().putString(KEY_ROLE, ROLE_TEACHER).putLong(KEY_STUDENT, NO_STUDENT).apply();
    }

    public void setStudent(long studentId) {
        prefs.edit().putString(KEY_ROLE, ROLE_STUDENT).putLong(KEY_STUDENT, studentId).apply();
    }

    public void clear() {
        prefs.edit().clear().apply();
    }
}
