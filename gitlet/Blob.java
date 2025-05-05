package gitlet;

import java.io.File;
import java.io.FileWriter;
import java.io.Serializable;

public class Blob implements Serializable {

    private String content ;
    private String id;
    private  String fileName;

    public Blob(File file) {
        this.content = Utils.readContentsAsString(file); // Read file content
        this.id = Utils.sha1(this.content); // Compute SHA-1 hash of the content
        this.fileName = file.getName();
    }

    public String getContent() {
        return content;
    }
    public String getId(){
        return id;
    }

    public static Blob getBlob(String fileId){
        File blobFile = new File(Repository.BLOBS_DIR, fileId);
        if(!blobFile.exists()){
            return null;
        }
        return Utils.readObject(blobFile, Blob.class);
    }

    public void save(){
        File blobFile = new File(Repository.BLOBS_DIR, this.getId());
        Utils.writeObject(blobFile, this);
    }

}
