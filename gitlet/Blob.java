package gitlet;

import java.io.File;
import java.io.Serializable;

public class Blob implements Serializable {

    private String content ;
    private String id;
    private  String fileName;

    public Blob(File file ,String name) {
        this.content = Utils.readContentsAsString(file); // Read file content
        this.id = Utils.sha1(this.content); // Compute SHA-1 hash of the content
        this.fileName = name;
    }

    public String getContent() {
        return content;
    }
    public String getId(){
        return id;
    }
}
