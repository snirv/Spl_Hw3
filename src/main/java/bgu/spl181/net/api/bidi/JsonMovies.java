package bgu.spl181.net.api.bidi;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

import java.util.List;

public class JsonMovies {

    /**
     * This class use to read the input from the Json file.
     */


    public ArrayList<Movie> movies;


    public ArrayList<Movie> getMovies() {
        return movies;
    }

    public void setMovies(ArrayList<Movie> movies) {
        this.movies = movies;
    }

}


