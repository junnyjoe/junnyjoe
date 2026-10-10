package com.junnyjoe.gestionclasse.util;

import android.animation.TimeInterpolator;
import android.view.View;
import android.view.animation.PathInterpolator;

/**
 * Animations d'entrée courtes (≤ 320 ms) avec une courbe « ease-out » marquée : l'élément démarre vite
 * puis se pose en douceur. Les durées suivent le réglage système « échelle de durée d'animation ».
 */
public final class Motion {
    private static final TimeInterpolator EASE_OUT = new PathInterpolator(0.23f, 1f, 0.32f, 1f);
    private static final long DURATION = 320;
    private static final long STAGGER = 55;
    private static final float RISE_DP = 20f;

    private Motion() { }

    /** Fait apparaître les vues l'une après l'autre (fondu + léger glissement vers le haut). */
    public static void enter(View... views) {
        long delay = 0;
        for (View v : views) {
            if (v == null || v.getVisibility() == View.GONE) continue;
            rise(v, delay);
            delay += STAGGER;
        }
    }

    /** Entrée d'une ligne de liste ; {@code delay} permet d'étager les premières lignes. */
    public static void rise(View v, long delay) {
        v.animate().cancel();
        v.setAlpha(0f);
        v.setTranslationY(RISE_DP * v.getResources().getDisplayMetrics().density);
        v.animate().alpha(1f).translationY(0f).setStartDelay(delay)
                .setDuration(DURATION).setInterpolator(EASE_OUT).start();
    }

    /** Apparition d'un bouton flottant : part de 0,8 (jamais de 0) pour rester naturel. */
    public static void pop(View v) {
        v.animate().cancel();
        v.setScaleX(0.8f);
        v.setScaleY(0.8f);
        v.setAlpha(0f);
        v.animate().scaleX(1f).scaleY(1f).alpha(1f).setStartDelay(150)
                .setDuration(240).setInterpolator(EASE_OUT).start();
    }

    /** Remet une vue recyclée dans son état normal. */
    public static void reset(View v) {
        v.animate().cancel();
        v.setAlpha(1f);
        v.setTranslationY(0f);
        v.setScaleX(1f);
        v.setScaleY(1f);
    }
}
