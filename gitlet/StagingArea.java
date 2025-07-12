package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;

import static gitlet.Repository.HEAD;
import static gitlet.Utils.readContentsAsString;
import static gitlet.Utils.writeObject;

public class StagingArea implements Serializable {

    /**
     * map represents the added files with file name as a key and the file id/blob as a value
     */
    private HashMap<String, String> stagedForAddition;

    /**
     * map represents the removed files with file name as a key and the file id/blob as a value
     */
    private HashMap<String, String> stagedForRemoval;

    public StagingArea() {
        this.stagedForAddition = new HashMap<String, String>();
        this.stagedForRemoval = new HashMap<String, String>();
    }

    /**
     * Return the files staged for addition.
     */
    public HashMap<String, String> getStagedForAddition() {
        return this.stagedForAddition;
    }

    /**
     * Return the files marked for removal.
     */
    public HashMap<String, String> getRemovedFiles() {
        return this.stagedForRemoval;
    }

    /**
     * Stage the file for addition.
     */
    public void stageForAddition(String fileName) {

        if (stagedForRemoval.containsKey(fileName))
            stagedForRemoval.remove(fileName);

        String newFileContent = readContentsAsString(new File(Repository.CWD, fileName));

        Blob blob = new Blob(newFileContent);
        String blobId = blob.getID();

        stagedForAddition.put(fileName, blob.getID());

        // Save blob only if it doesn't exist.
        if (!new File(Repository.BLOBS_DIR, blobId).exists())
            blob.save();
    }

    /**
     * Stage the file for removal.
     */
    public void markForRemoval(String fileName) {

        File file = new File(Repository.CWD, fileName);

        String fileId = Utils.sha1(Utils.readContentsAsString(file));

        // Unstage the file if it is currently staged for addition.
        if (stagedForAddition.containsKey(fileId))
            unStage(fileName);

        Commit headCOmmit = Commit.getCommit(readContentsAsString(HEAD));

        // If the file is tracked in the current commit, delete it from the CWD.
        if (headCOmmit.containsFile(fileName)) {

            // Delete the file from CWD.
            if (file.exists())
                Utils.restrictedDelete(fileName);
        }
        // mark the file for removal.
        stagedForRemoval.put(fileName, fileId);
    }

    /**
     * Return true if the file is staged for addition.
     */
    public boolean existentForAddition(String fileName) {
        return stagedForAddition.containsKey(fileName);
    }

    /**
     * Clear the staging area.
     */
    public void clear() {
        stagedForAddition.clear();
        stagedForRemoval.clear();
    }

    /**
     * Unstage a file (unstage it for addition, unmark it for removal).
     */
    public void unStage(String fileName) {
        if (stagedForRemoval.containsKey(fileName))
            stagedForRemoval.remove(fileName);

        if (stagedForAddition.containsKey(fileName))
            stagedForAddition.remove(fileName);
    }

    /**
     * save the current staging area object in the File file which is always index file.
     */
    public void save() {
        writeObject(Repository.index, this);
    }

}
