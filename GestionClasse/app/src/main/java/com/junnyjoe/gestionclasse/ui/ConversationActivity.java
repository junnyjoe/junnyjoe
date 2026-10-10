package com.junnyjoe.gestionclasse.ui;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.MenuItem;
import android.view.View;
import android.content.pm.PackageManager;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.junnyjoe.gestionclasse.R;
import com.junnyjoe.gestionclasse.data.Etudiant;
import com.junnyjoe.gestionclasse.data.Message;
import com.junnyjoe.gestionclasse.databinding.ActivityConversationBinding;
import com.junnyjoe.gestionclasse.util.NotificationHelper;
import com.junnyjoe.gestionclasse.util.Session;
import com.junnyjoe.gestionclasse.viewmodel.ConversationViewModel;

import java.io.IOException;
import java.io.InputStream;

/** Écran 6 (et 7 : sélection de pièce jointe) : conversation locale entre deux profils étudiants. */
public class ConversationActivity extends AppCompatActivity implements MessageAdapter.Listener {
    private static final String EXTRA_ME = "me_id";
    private static final String EXTRA_PEER = "peer_id";

    private static final String[] MIME_PHOTO = {"image/*"};
    private static final String[] MIME_VIDEO = {"video/*"};
    private static final String[] MIME_DOCUMENT = {
            "application/pdf", "text/plain", "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"};

    private ActivityConversationBinding b;
    private ConversationViewModel vm;
    private Etudiant me;

    // Pièce jointe en cours de rédaction (une seule).
    private String pickingType;
    @Nullable private Uri pendingUri;
    @Nullable private String pendingType;
    @Nullable private String pendingMime;
    @Nullable private String pendingName;
    private boolean askedNotificationPermission;

    private final ActivityResultLauncher<String[]> filePicker =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), this::onFilePicked);
    private final ActivityResultLauncher<String> notificationPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> doSend());

    public static Intent intent(Context c, long meId, long peerId) {
        return new Intent(c, ConversationActivity.class)
                .putExtra(EXTRA_ME, meId)
                .putExtra(EXTRA_PEER, peerId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        long meId = getIntent().getLongExtra(EXTRA_ME, -1);
        long peerId = getIntent().getLongExtra(EXTRA_PEER, -1);
        if (meId <= 0 || peerId <= 0) {
            finish();
            return;
        }
        // Ouverture depuis une notification : on se place dans le profil du destinataire.
        new Session(this).setStudent(meId);

        b = ActivityConversationBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        ActionBar bar = getSupportActionBar();
        if (bar != null) bar.setDisplayHomeAsUpEnabled(true);
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                leave();
            }
        });

        vm = new ViewModelProvider(this).get(ConversationViewModel.class);
        vm.init(meId, peerId);

        MessageAdapter adapter = new MessageAdapter(meId, this);
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        b.rv.setLayoutManager(lm);
        b.rv.setAdapter(adapter);

        vm.getMe().observe(this, e -> {
            if (e == null) {
                finish();
                return;
            }
            me = e;
            if (bar != null) bar.setSubtitle(getString(R.string.conversation_as, e.getNomComplet()));
        });
        vm.getPeer().observe(this, e -> {
            if (e == null) {
                finish(); // destinataire supprimé
                return;
            }
            setTitle(e.getNomComplet());
        });
        vm.getMessages().observe(this, list -> {
            boolean empty = list == null || list.isEmpty();
            b.tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
            b.rv.setVisibility(empty ? View.GONE : View.VISIBLE);
            adapter.submitList(list, () -> {
                if (adapter.getItemCount() > 0) b.rv.scrollToPosition(adapter.getItemCount() - 1);
            });
        });
        vm.getSaved().observe(this, m -> {
            if (m == null) return;
            vm.clearSaved();
            b.etMessage.setText("");
            clearAttachment();
            String sender = me != null ? me.getNomComplet() : "";
            if (!NotificationHelper.notifyNewMessage(this, m, sender)) {
                Snackbar.make(b.getRoot(), R.string.notif_denied, Snackbar.LENGTH_LONG).show();
            }
        });
        vm.getError().observe(this, err -> {
            if (err == null) return;
            vm.clearError();
            Snackbar.make(b.getRoot(), R.string.error_save, Snackbar.LENGTH_LONG).show();
        });

        b.btnAttach.setOnClickListener(v -> chooseAttachmentType());
        b.btnRemoveAttachment.setOnClickListener(v -> clearAttachment());
        b.btnSend.setOnClickListener(v -> onSendClicked());
    }

    // --- Pièce jointe ---

    private void chooseAttachmentType() {
        String[] labels = {getString(R.string.attach_photo), getString(R.string.attach_video),
                getString(R.string.attach_document)};
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.attach_choose)
                .setItems(labels, (d, which) -> {
                    if (which == 0) {
                        pickingType = Message.TYPE_PHOTO;
                        filePicker.launch(MIME_PHOTO);
                    } else if (which == 1) {
                        pickingType = Message.TYPE_VIDEO;
                        filePicker.launch(MIME_VIDEO);
                    } else {
                        pickingType = Message.TYPE_DOCUMENT;
                        filePicker.launch(MIME_DOCUMENT);
                    }
                })
                .show();
    }

    private void onFilePicked(@Nullable Uri uri) {
        if (uri == null) return;
        try {
            // Conserve l'accès à la référence locale après redémarrage.
            getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (SecurityException ignored) {
            // Certains fournisseurs n'offrent pas de permission persistante.
        }
        pendingUri = uri;
        pendingType = pickingType;
        pendingMime = getContentResolver().getType(uri);
        pendingName = queryName(uri);
        b.llPendingAttachment.setVisibility(View.VISIBLE);
        b.ivPendingAttachment.setImageResource(iconFor(pendingType));
        b.tvPendingAttachment.setText(pendingName);
        b.tilMessage.setError(null);
    }

    private void clearAttachment() {
        pendingUri = null;
        pendingType = null;
        pendingMime = null;
        pendingName = null;
        b.llPendingAttachment.setVisibility(View.GONE);
    }

    private String queryName(Uri uri) {
        try (Cursor c = getContentResolver().query(uri, null, null, null, null)) {
            if (c != null && c.moveToFirst()) {
                int idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (idx >= 0 && c.getString(idx) != null) return c.getString(idx);
            }
        } catch (RuntimeException ignored) {
            // nom inconnu : repli ci-dessous
        }
        String last = uri.getLastPathSegment();
        return last != null ? last : getString(R.string.attach_document);
    }

    private static int iconFor(@Nullable String type) {
        if (Message.TYPE_PHOTO.equals(type)) return R.drawable.ic_photo;
        if (Message.TYPE_VIDEO.equals(type)) return R.drawable.ic_video;
        return R.drawable.ic_document;
    }

    @Override
    public void onAttachmentClick(Message m) {
        if (m.attachmentUri == null) return;
        Uri uri = Uri.parse(m.attachmentUri);
        // Vérifie que le fichier existe encore avant de l'ouvrir.
        try (InputStream in = getContentResolver().openInputStream(uri)) {
            if (in == null) throw new IOException();
        } catch (IOException | RuntimeException e) {
            Snackbar.make(b.getRoot(), R.string.attachment_unavailable, Snackbar.LENGTH_LONG).show();
            return;
        }
        Intent view = new Intent(Intent.ACTION_VIEW)
                .setDataAndType(uri, m.attachmentMime != null ? m.attachmentMime : "*/*")
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            startActivity(view);
        } catch (ActivityNotFoundException | SecurityException e) {
            Snackbar.make(b.getRoot(), R.string.attachment_cannot_open, Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void onSentLongClick(Message m) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_message_title)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (d, w) -> vm.deleteMessage(m))
                .show();
    }

    // --- Envoi ---

    private void onSendClicked() {
        String text = currentText();
        if (text.isEmpty() && pendingUri == null) {
            b.tilMessage.setError(getString(R.string.error_empty_message));
            return;
        }
        b.tilMessage.setError(null);
        if (Build.VERSION.SDK_INT >= 33 && !askedNotificationPermission
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            askedNotificationPermission = true;
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS);
            return; // l'envoi reprend dans le callback, que la permission soit accordée ou non
        }
        doSend();
    }

    private void doSend() {
        String text = currentText();
        if (text.isEmpty() && pendingUri == null) return;
        vm.send(text, pendingUri == null ? null : pendingUri.toString(), pendingType, pendingMime, pendingName);
    }

    private String currentText() {
        return b.etMessage.getText() == null ? "" : b.etMessage.getText().toString().trim();
    }

    // --- Navigation ---

    /** Depuis une notification la conversation peut être la racine de la tâche : on revient à l'accueil. */
    private void leave() {
        if (isTaskRoot()) {
            startActivity(new Intent(this, HomeActivity.class));
        }
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            leave();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
