package gitlet;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class Commit implements Serializable {

    /**
     * The message of this Commit.
     */
    private String message;

    /**
     * The date when this Commit was created.
     */
    private Date timestamp;

    /**
     * Data structure(map) represents the files this commit has with file name as a key and file blob/version/its content Id as a value.
     */
    private HashMap<String, String> files;

    /**
     * The first parent of this Commit.
     */
    private String parent1;

    /**
     * The second parent of this Commit in case merged commit.
     * Unlike real git, our Git-let just maintain two parents for merged commit.
     */
    private String parent2;

    /**
     * Create commit with one parent.
     */
    public Commit(String message, String parent1, String parent2) {
        this.message = message;
        this.parent1 = parent1;
        this.parent2 = parent2;
        this.timestamp = new Date(); // set to the current time.
        files = new HashMap<String, String>();

        if (parent1 == null)
            this.timestamp = new Date(0);
    }

    /**
     * Find a commit by it`s id and return it.
     */
    public static Commit getCommit(String commitId) {

        File commitFile = new File(Repository.COMMITS_DIR, commitId);
        if (!commitFile.exists()) {
            return null;
        }
        return Utils.readObject(commitFile, Commit.class);
    }

    /**
     * return true if the file is tracked in the commit.
     */
    public boolean containsFile(String name) {
        return files.containsKey(name);
    }

    /**
     * return true if the file is tracked in the commit.
     */
    public HashMap<String, String> getFiles() {
        return new HashMap<String, String>(this.files); // Shallow copy
    }

    /**
     * Cloning the parent files.
     */
    public void setFiles(HashMap<String, String> parentFiles) {
        this.files = new HashMap<String, String>(parentFiles);
    }

    /**
     * Remove a file from tracked files.
     */
    public void removeFile(String fileName) {
        files.remove(fileName);
    }

    /**
     * add reference to new/modified file added(staged)
     */
    public void addFile(String fileName, String fileId) {
        files.put(fileName, fileId);
    }

    /**
     * Return the commit message.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Write this commit in a file and save it in COMMITS_DIR.
     */
    public void save() {
        File commitFile = new File(Repository.COMMITS_DIR, this.getId());
        Utils.writeObject(commitFile, this);
    }

    /**
     * Return the SA1 id for this commit.
     */
    public String getId() {
        return Utils.sha1(
                message,
                timestamp.toString(),
                Utils.serialize(files),
                parent1 != null ? parent1 : "null", // Assuming parent is already the ID string
                parent2 != null ? parent2 : "null"  // Assuming parent is already the ID string

        );
    }

    /**
     * Find a file by it`s id and return it.
     */
    public String getFileId(String fileName) {
        return files.get(fileName);
    }

    /**
     * Return the content of a file has name fileName.
     */
    public String getFileContent(String fileName) {
        String fileId = files.get(fileName);
        if (fileId == null)
            return null;
        Blob blob = Blob.getBlob(fileId);

        return blob.getContent();
    }

    /**
     * return the first parent of this commit.
     */
    public String getParent1() {
        return parent1;
    }

    /**
     * return the Second parent of this commit.
     */
    public String getParent2() {
        return parent2;
    }

    public String toString() {

        SimpleDateFormat timeForamt = new SimpleDateFormat("EEE, MMMM dd, yyyy HH:mm:ss z");

        StringBuilder printedCommit = new StringBuilder();

        printedCommit.append("===\n")
                .append("commit ").append(this.getId()).append("\n");

        if (parent2 != null)
            printedCommit.append("Merge: ").append(parent1, 0, 7).append(" ").append(parent2, 0, 7).append("\n");

        printedCommit.
                append("Date: ").append(timeForamt.format(timestamp)).append("\n")
                .append(message).append("\n");

        return printedCommit.toString();
    }
}
