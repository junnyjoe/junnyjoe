package com.junnyjoe.gestionclasse.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.junnyjoe.gestionclasse.data.Etudiant;
import com.junnyjoe.gestionclasse.databinding.ItemEtudiantBinding;

public class EtudiantAdapter extends ListAdapter<Etudiant, EtudiantAdapter.Holder> {

    public interface OnClick {
        void onClick(Etudiant etudiant);
    }

    private final OnClick listener;

    public EtudiantAdapter(OnClick listener) {
        super(new DiffUtil.ItemCallback<Etudiant>() {
            @Override
            public boolean areItemsTheSame(@NonNull Etudiant a, @NonNull Etudiant b) {
                return a.id == b.id;
            }

            @Override
            public boolean areContentsTheSame(@NonNull Etudiant a, @NonNull Etudiant b) {
                return a.nom.equals(b.nom) && a.prenom.equals(b.prenom)
                        && a.email.equals(b.email) && a.telephone.equals(b.telephone);
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemEtudiantBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        Etudiant e = getItem(position);
        h.b.tvInitials.setText(e.getInitiales());
        h.b.tvName.setText(e.getNomComplet());
        h.b.tvPhone.setText(e.telephone);
        h.b.getRoot().setOnClickListener(v -> listener.onClick(e));
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ItemEtudiantBinding b;

        Holder(ItemEtudiantBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }
}
