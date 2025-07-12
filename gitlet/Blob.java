package gitlet;

import java.io.File;
import java.io.Serializable;

public class Blob implements Serializable {

    private String content;
    private String ID;

    public Blob(String content) {
        this.content = content;
        this.ID = Utils.sha1(this.content); // Compute SHA-1 hash of the content

    }

    /**
     * Return the file content.
     */
    public String getContent() {

        return content;
    }

    public String getID() {
        return ID;
    }

    /**
     * Return the file ID.
     */
    public static Blob getBlob(String fileId) {
        File blobFile = new File(Repository.BLOBS_DIR, fileId);
        if (!blobFile.exists()) {
            return null;
        }
        return Utils.readObject(blobFile, Blob.class);
    }

    /**
     * save the blob in BLOBS_DIR.
     */
    public void save() {
        File blobFile = new File(Repository.BLOBS_DIR, this.getID());
        if (!blobFile.exists())
            Utils.writeObject(blobFile, this);
    }
}
