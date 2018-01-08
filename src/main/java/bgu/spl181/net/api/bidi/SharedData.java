package bgu.spl181.net.api.bidi;


import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class SharedData {

    protected  ConcurrentHashMap<String ,User> mapOfRegisteredUsersByUsername;// map userName to User
    protected  ConcurrentHashMap<Integer,User> mapOfLoggedInUsersByConnectedIds; // map connectionId to username


    public SharedData() {
        this.mapOfLoggedInUsersByConnectedIds = null;
        this.mapOfRegisteredUsersByUsername = null;
    }

    public SharedData(ConcurrentHashMap<String, User> userMap) {
        this.mapOfLoggedInUsersByConnectedIds = new ConcurrentHashMap<>();
        this.mapOfRegisteredUsersByUsername = userMap;
    }

    protected  String commandRegister(String username , String password ,String dataBlock , Integer connectionId){
        if (mapOfLoggedInUsersByConnectedIds.contains(connectionId) ||
                mapOfRegisteredUsersByUsername.contains(username) ||
                !isValidDataBlock(dataBlock)  ){
            return "ERROR registration failed";
        }
        addUser(username,password,connectionId, dataBlock);
        updateUserJson();
        updateServiceJson();
        return "ACK registration succeeded";

    }
    protected  String commandLogIn(String username , String password  ,Integer connectionId){
        if(!mapOfRegisteredUsersByUsername.contains(username) ||
                mapOfLoggedInUsersByConnectedIds.contains(connectionId) ||
                mapOfRegisteredUsersByUsername.get(username).isLoggedIn){
            return "ERROR login failed";
        }
        User user = mapOfRegisteredUsersByUsername.get(username);//TODO need to sync
        if(!user.getPassword().equals(password)){ return "ERROR login failed";}
        user.setLoggedIn(true);
        mapOfLoggedInUsersByConnectedIds.put(connectionId , user);
        return "ACK login succeeded";
    }
    protected  String commandSignOut(Integer connctionId){
        if(!mapOfLoggedInUsersByConnectedIds.contains(connctionId)){
            return "ERROR signout failed";
        }
        User user = mapOfLoggedInUsersByConnectedIds.remove(connctionId);
        user.setLoggedIn(false);
        return "ACK signout succeeded";

    }


    protected abstract boolean isValidDataBlock(String dataBlock);

    protected abstract void addUser(String username , String password, int connectionId, String dataBlock);

    public ConcurrentHashMap<String, User> getUserMap() {
        return mapOfRegisteredUsersByUsername;
    }

    public void setUserMap(ConcurrentHashMap<String, User> userMap) {
        this.mapOfRegisteredUsersByUsername = userMap;
    }

    public ConcurrentHashMap<Integer,User> getMapOfLoggedInUsersByConnectedIds() {
        return mapOfLoggedInUsersByConnectedIds;
    }
    protected abstract void updateUserJson();
    protected abstract void updateServiceJson();
}