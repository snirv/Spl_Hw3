package bgu.spl181.net.api.bidi;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

import java.util.List;

public class JsonUsers {

    /**
     * This class use to read the input from the Json file.
     */


    public ArrayList<UserMovieRental> users;




    public void setUsers(ArrayList<UserMovieRental> users) {
        this.users = users;
    }




    public ArrayList<UserMovieRental> getUsers() {
            return users;
        }

}
