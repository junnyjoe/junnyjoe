package com.junnyjoe.gestionclasse.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.junnyjoe.gestionclasse.R;
import com.junnyjoe.gestionclasse.data.Message;
import com.junnyjoe.gestionclasse.databinding.ItemMessageReceivedBinding;
import com.junnyjoe.gestionclasse.databinding.ItemMessageSentBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

/** Deux types de lignes : messages envoyés (à droite) et reçus (à gauche). */
public class MessageAdapter extends ListAdapter<Message, RecyclerView.ViewHolder> {
    private static final int SENT = 1;
    private static final int RECEIVED = 2;

    public interface Listener {
        void onAttachmentClick(Message m);

        void onSentLongClick(Message m);
    }

    private final long meId;
    private final Listener listener;
    private final SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE);

    public MessageAdapter(long meId, Listener listener) {
        super(new DiffUtil.ItemCallback<Message>() {
            @Override
            public boolean areItemsTheSame(@NonNull Message a, @NonNull Message b) {
                return a.id == b.id;
            }

            @Override
            public boolean areContentsTheSame(@NonNull Message a, @NonNull Message b) {
                return a.contenu.equals(b.contenu) && Objects.equals(a.attachmentUri, b.attachmentUri);
            }
        });
        this.meId = meId;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).expediteurId == meId ? SENT : RECEIVED;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == SENT) {
            return new Holder(ItemMessageSentBinding.inflate(inflater, parent, false).getRoot());
        }
        return new Holder(ItemMessageReceivedBinding.inflate(inflater, parent, false).getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message m = getItem(position);
        View root = holder.itemView;
        TextView content = root.findViewById(R.id.tvContent);
        content.setVisibility(m.contenu.isEmpty() ? View.GONE : View.VISIBLE);
        content.setText(m.contenu);
        ((TextView) root.findViewById(R.id.tvTime)).setText(format.format(new Date(m.dateTime)));

        LinearLayout attachment = root.findViewById(R.id.llAttachment);
        if (m.hasAttachment()) {
            attachment.setVisibility(View.VISIBLE);
            ((ImageView) root.findViewById(R.id.ivAttachment)).setImageResource(iconFor(m.attachmentType));
            ((TextView) root.findViewById(R.id.tvAttachmentName))
                    .setText(m.attachmentName != null ? m.attachmentName : root.getContext().getString(R.string.attach_document));
            attachment.setOnClickListener(v -> listener.onAttachmentClick(m));
        } else {
            attachment.setVisibility(View.GONE);
            attachment.setOnClickListener(null);
        }

        View bubble = root.findViewById(R.id.bubble);
        if (holder.getItemViewType() == SENT) {
            bubble.setOnLongClickListener(v -> {
                listener.onSentLongClick(m);
                return true;
            });
        }
    }

    private static int iconFor(String type) {
        if (Message.TYPE_PHOTO.equals(type)) return R.drawable.ic_photo;
        if (Message.TYPE_VIDEO.equals(type)) return R.drawable.ic_video;
        return R.drawable.ic_document;
    }

    static class Holder extends RecyclerView.ViewHolder {
        Holder(View v) {
            super(v);
        }
    }
}
