package gitlet;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class Commit implements Serializable {

    /** The message of this Commit. */
    private String message;

    /** The date when this Commit was created. */
    private Date timestamp;

    /** Data structure(map) represents the blobs/files this commit references to with file name as a key and file blob/version/its content as a value. */
    private HashMap<String, String> files;

    /** The parent this Commit came from. */
    private String parent;

    public Commit(String message, String parent) {
        this.message = message;
        this.parent = parent;
        this.timestamp = new Date(); // set to the current time.
        files =  new HashMap<String , String>();

        if(parent == null)
            this.timestamp = new Date(0);
    }

    public boolean containsFile(String name) {
        return files.containsKey(name);
    }

    public HashMap<String, String> getFiles() {
        return new HashMap<String, String>(this.files); // Shallow copy
    }
    public String getFile(String fileName) {
        return files.get(fileName); // Shallow copy
    }

    public void setFiles(HashMap<String, String> files) {
        this.files = new HashMap<String, String>(files);
    }

    public void removeFile(String fileName) {
        files.remove(fileName);
    }

    /** add reference to new/modified file added(staged)  */
    public void addFile(String fileName ,  String content) {
        files.put(fileName, content);
    }

    public String getMessage() {
        return message;
    }

    public String getId() {
        return Utils.sha1(
                message,
                timestamp.toString(),
                Utils.serialize(files),
                parent != null ? parent : "null"  // Assuming parent is already the ID string
        );
    }

    public String getFileId(String fileName) {
        return files.get(fileName);
    }

    public String getParent() {
        return parent;
    }

    public String toString(){

        SimpleDateFormat timeForamt = new SimpleDateFormat("EEE, MMMM dd, yyyy HH:mm:ss z");

        StringBuilder printedCommit = new StringBuilder();
        printedCommit.append("===\n")
                .append("commit ").append(this.getId()).append("\n")
                .append("Date:   ").append(timeForamt.format(timestamp)).append("\n")
                .append("\n")
                .append(message).append("\n");

        // if (commit is merged){......}

        return printedCommit.toString();
    }

}
