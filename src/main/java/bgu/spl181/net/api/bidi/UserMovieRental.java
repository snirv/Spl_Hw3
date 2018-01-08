package bgu.spl181.net.api.bidi;


import java.util.List;
import java.util.Optional;

public class UserMovieRental extends User{

    private String country;
    private int balance;
    private List<Movie> moviesList;

    public UserMovieRental(String userName, String password, String type , int connectionId, String country, int balance, List<Movie> moviesList) { //TODO connection id where to initiate
        super(userName, password, type , connectionId);
        this.country = country;
        this.balance = balance;
        this.moviesList = moviesList;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    public List<Movie> getMoviesList() {
        return moviesList;
    }

    public void setMoviesList(List<Movie> moviesList) {
        this.moviesList = moviesList;
    }

    protected int addBalance(int balanceToAdd){
        return balance + balanceToAdd;

    }

    public boolean isAdmin(){
        if(type.equals("admin")){
            return true;
        }else {
            return false;}
    }

    public boolean isRentingMovie(String movieName){
        Optional<Movie> movieOptional = moviesList.stream().filter((m)-> m.getName().equals(movieName)).findAny();
        if(movieOptional.isPresent()){
            return true;
        }
        else {
            return false;
        }
    }
}