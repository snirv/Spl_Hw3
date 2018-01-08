package bgu.spl181.net.api.bidi;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

import java.util.List;

public class JsonShardData {

    /**
     * This class use to read the input from the Json file.
     */

        @SerializedName("users")
        public ArrayList<UserMovieRental> users;


        public List getUsers() {
            return users;
        }

}
