package bgu.spl181.net.api.bidi;

public abstract class User {
    //I am sharon
    protected String userName;
    protected String password;
    protected String type;


    protected boolean isLoggedIn;
    protected int connectionId;//TODO check if necessary


    public User(String userName, String password, String type ,int connectionId) {
        this.userName = userName;
        this.password = password;
        this.type = type;
        isLoggedIn = false;
        this.connectionId =connectionId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        isLoggedIn = loggedIn;
    }


}
