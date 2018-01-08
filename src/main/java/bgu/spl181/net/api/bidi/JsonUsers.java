package bgu.spl181.net.api.bidi;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

import java.util.List;

public class JsonUsers {

    /**
     * This class use to read the input from the Json file.
     */


    public ArrayList<UserMovieRental> users;

    public ArrayList<Movie> movies;


    public void setUsers(ArrayList<UserMovieRental> users) {
        this.users = users;
    }

    public ArrayList<Movie> getMovies() {
        return movies;
    }

    public void setMovies(ArrayList<Movie> movies) {
        this.movies = movies;
    }


    public List getUsers() {
            return users;
        }

}
