package com.junnyjoe.gestionclasse.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/** Message local entre deux étudiants. La pièce jointe n'est qu'une référence (Uri) et un type. */
@Entity(
        tableName = "message",
        foreignKeys = {
                @ForeignKey(entity = Etudiant.class, parentColumns = "id",
                        childColumns = "expediteurId", onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Etudiant.class, parentColumns = "id",
                        childColumns = "destinataireId", onDelete = ForeignKey.CASCADE)
        },
        indices = {@Index("expediteurId"), @Index("destinataireId")})
public class Message {
    public static final String TYPE_PHOTO = "PHOTO";
    public static final String TYPE_VIDEO = "VIDEO";
    public static final String TYPE_DOCUMENT = "DOCUMENT";

    @PrimaryKey(autoGenerate = true)
    public long id;
    public long expediteurId;
    public long destinataireId;
    @NonNull
    public String contenu = "";
    /** Date d'envoi en millisecondes (epoch). */
    public long dateTime;
    @Nullable
    public String attachmentUri;
    @Nullable
    public String attachmentType;
    @Nullable
    public String attachmentMime;
    @Nullable
    public String attachmentName;

    public Message() { }

    @Ignore
    public Message(long expediteurId, long destinataireId, @NonNull String contenu, long dateTime,
                   @Nullable String attachmentUri, @Nullable String attachmentType,
                   @Nullable String attachmentMime, @Nullable String attachmentName) {
        this.expediteurId = expediteurId;
        this.destinataireId = destinataireId;
        this.contenu = contenu;
        this.dateTime = dateTime;
        this.attachmentUri = attachmentUri;
        this.attachmentType = attachmentType;
        this.attachmentMime = attachmentMime;
        this.attachmentName = attachmentName;
    }

    public boolean hasAttachment() {
        return attachmentUri != null;
    }
}
