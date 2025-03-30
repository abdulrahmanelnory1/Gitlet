package gitlet;

import java.io.Serializable;

public class Branch implements Serializable {

    private String name;
    private String id;

    public Branch(String name, String id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
       return name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


}
