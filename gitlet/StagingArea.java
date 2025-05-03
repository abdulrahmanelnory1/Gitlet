package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;

import static gitlet.Utils.writeObject;

public class StagingArea implements Serializable {

    /** map represents the added/removed file with file id as a key and the file name as a value*/
    private HashMap<String, String> addedFiles;
    private HashMap<String, String> removedFiles;

    public StagingArea() {
        this.addedFiles = new HashMap<String, String>();
        this.removedFiles = new HashMap<String, String>();
    }

    public HashMap<String, String> getAddedFiles() {
        return this.addedFiles;
    }

<<<<<<< HEAD
=======
    public HashMap<String, String> getRemovedFiles() {
        return this.removedFiles;
    }
>>>>>>> master
    public boolean containsAddedFile(String fileId) {
        return addedFiles.containsKey(fileId);
    }

<<<<<<< HEAD
    public HashMap<String, String> getRemovedFiles() {
        return this.removedFiles;
    }
=======

>>>>>>> master

    public void addForAddition(String fileName, String file) {
        addedFiles.put(fileName, file);
    }

    public void markForRemoval(String fileName, String file) {
        removedFiles.put(fileName, file);
    }

<<<<<<< HEAD
    public boolean existentForEdition(String fileName) {
=======
    public boolean existentForAddition(String fileName) {
>>>>>>> master
        return addedFiles.containsKey(fileName);
    }

    public void clear() {
        addedFiles.clear();
    }

    public void unStage(String fileName) {
        addedFiles.remove(fileName);
    }

<<<<<<< HEAD
=======
    /** save the the current staging area object in the File file which is always index file*/
>>>>>>> master
    public void save(File file) {
        writeObject(file, this);
    }

}
