package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;

import static gitlet.Utils.readContents;
import static gitlet.Utils.writeObject;

public class StagingArea implements Serializable {

    /** map represents the added/removed file with file name as a key and the file id as a value*/
    private HashMap<String, String> addedFiles;
    private HashMap<String, String> removedFiles;

    public StagingArea() {
        this.addedFiles = new HashMap<String, String>();
        this.removedFiles = new HashMap<String, String>();
    }

    public HashMap<String, String> getAddedFiles() {
        return this.addedFiles;
    }

    public boolean containsAddedFile(String fileId) {
        return addedFiles.containsKey(fileId);
    }

    public HashMap<String, String> getRemovedFiles() {
        return this.removedFiles;
    }

    public void addForAddition(String fileName) {
        File file = new File (Repository.CWD, fileName);
        String fileId = Utils.sha1(Utils.readContentsAsString(file));
        addedFiles.put(fileName, fileId);
    }

    public void markForRemoval(String fileName) {
        File file = new File (Repository.CWD, fileName);
        String fileId = Utils.sha1(Utils.readContentsAsString(file));
        removedFiles.put(fileName, fileId);
    }

    public boolean existentForAddition(String fileName) {
        return addedFiles.containsKey(fileName);
    }

    public void clear() {
        addedFiles.clear();
    }

    public void unStage(String fileName) {
        addedFiles.remove(fileName);
    }

    /** save the the current staging area object in the File file which is always index file*/
    public void save() {
        writeObject(Repository.index, this);
    }

}
