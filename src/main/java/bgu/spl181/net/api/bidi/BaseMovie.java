package bgu.spl181.net.api.bidi;


import java.io.Serializable;

public class BaseMovie implements Serializable {
    protected long id;
    protected String name;

    public BaseMovie(long id, String name) {
        this.id = id;
        this.name = name;
    }
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
