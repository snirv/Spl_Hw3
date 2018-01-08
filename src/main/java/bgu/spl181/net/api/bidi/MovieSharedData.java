package bgu.spl181.net.api.bidi;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.concurrent.ConcurrentHashMap;

public class MovieSharedData extends SharedData{

    ConcurrentHashMap<Integer,Movie> moviesMap;
    List<Movie> movieList;


    public MovieSharedData(ConcurrentHashMap<String, User> userMovieRentalMap ,ConcurrentHashMap<Integer,Movie> moviesMap) {
        super(userMovieRentalMap);
        this.moviesMap = moviesMap;
        this.movieList = new LinkedList<>();
    }

    protected boolean isLoggedIn(Integer connectionId){
        if (mapOfLoggedInUsersByConnectedIds.contains(connectionId)) {
            return true;
        }
        else {return false;}
    }

    protected boolean isAdmin(Integer connectionId){
        if(isLoggedIn(connectionId) && ((UserMovieRental)mapOfLoggedInUsersByConnectedIds.get(connectionId)).isAdmin()){
            return true;
        }else {return false;}
    }

    protected String commandRequestBalanceInfo(Integer connectionId) {
        UserMovieRental user = (UserMovieRental)mapOfLoggedInUsersByConnectedIds.get(connectionId);
        int userBalance = user.getBalance();
        return "ACK balance " + userBalance;
    }

    protected String commandRequestBalanceAdd(Integer connectionId, int amount) {
        UserMovieRental user = (UserMovieRental)mapOfLoggedInUsersByConnectedIds.get(connectionId);
        int newBalance = user.addBalance(amount);
        user.setBalance(newBalance);
        return  "ACK balance " + newBalance + " added " + amount;
    }


    protected String commandRequestMovieInfo(String movieName) {
        String ret = "";
        if(movieName == null){
            for (Movie movie : movieList){
                ret = movie.getName() + " ";
            }
            ret = ret.substring(0, ret.length() -1);
            return "ACK info " + ret ;
        }
        else {
            for (Movie movie : movieList){
                if(movie.getName().equals(movieName)){
                    ret = movie.toString();
                    return "ACK info " + ret ;
                }
            }
            return "ERROR request info failed";
        }
    }

    protected String commandRequestMovieRent(Integer connectionId ,String movieName) {
        UserMovieRental user = (UserMovieRental) mapOfLoggedInUsersByConnectedIds.get(connectionId);
        Movie movie = getMovieFromListByMovieName(movieName);
        if (movie == null || movie.bannedCountries.contains(user.getCountry()) ||
                user.isRentingMovie(movieName) ||
                user.getBalance() < movie.getPrice()) {
            return "ERROR request rent failed";
        }
        while(!movie.lock.compareAndSet(false,true));//TODO maybe synch
        if (movie.getAvailableAmount().get() == 0){//TODO what if more then one is renting? need to lock the movie
            return "ERROR request rent failed";
        }else{
            user.getMoviesList().add(movie);
            user.setBalance(user.getBalance() - movie.getPrice());
            movie.getAvailableAmount().decrementAndGet();
            movie.lock.set(false);
            //TODO send a broadcast
            return "ACK rent "+ movieName + " success";
        }
    }

    protected String commandRequestReturnMovie(Integer connectionId, String movieName) {
        UserMovieRental user = (UserMovieRental) mapOfLoggedInUsersByConnectedIds.get(connectionId);
        Movie movie = getMovieFromListByMovieName(movieName);
        if(movie == null || !user.isRentingMovie(movieName)){
            return "ERROR request return failed";
        }
        else {
            user.getMoviesList().remove(movie);
            movie.getAvailableAmount().incrementAndGet();
            return "ACK return "+ movieName +" success";//TODO send a broadcast
        }
    }


    protected String commandRequestAdminAddMovie(Integer connectionId, String movieName , int amount , int price , List<String> bannedCountry) {
        UserMovieRental user = (UserMovieRental) mapOfLoggedInUsersByConnectedIds.get(connectionId);
        Movie movie = getMovieFromListByMovieName(movieName);
        if( !user.isAdmin() || amount <= 0 || price <= 0 || movie != null){
            return "ERROR request addmovie failed";
        }else {
            int id = 0;
            OptionalInt optionalId = movieList.stream()
                    .mapToInt(m -> m.getId())
                    .max();
            if(optionalId.isPresent()){id = optionalId.getAsInt();}
            Movie movieToAdd = new Movie(id + 1 ,movieName,price,bannedCountry,amount);
            movieList.add(movieToAdd);
            return "ACK addmovie " +movieName +" success";//TODO add a broadcast
        }
    }

    protected String commandRequestAdminRemmovie(Integer connectionId,String movieName) {
        UserMovieRental user = (UserMovieRental) mapOfLoggedInUsersByConnectedIds.get(connectionId);
        Movie movie = getMovieFromListByMovieName(movieName);
        if (movie == null || !user.isAdmin() ){return "ERROR request remmovie failed";}
        while (!movie.lock.compareAndSet(false,true));
        if(movie.getAvailableAmount() != movie.getTotalAmount()){
            return "ERROR request remmovie failed";
        }
        movieList.remove(movie);
        movie.lock.set(false);
        return "ACK remmovie " +movieName +" success";//TODO add a broadcast
    }

    protected String commandRequestAdminChangePrice(Integer connectionId , String movieName , int price) {
        UserMovieRental user = (UserMovieRental) mapOfLoggedInUsersByConnectedIds.get(connectionId);
        Movie movie = getMovieFromListByMovieName(movieName);
        if (movie == null || !user.isAdmin() || price <= 0){return "ERROR request remmovie failed";}
        while (!movie.lock.compareAndSet(false,true));
        movie.setPrice(price);
        movie.lock.set(false);
        return "ACK changeprice " +movieName +" success";//TODO add a broadcast
    }

    @Override
    protected boolean isValidDataBlock(String dataBlock) {
        String[] msg = dataBlock.split("=");
        if(msg.length < 2){return false;}
        else {return true;}
    }

    @Override
    protected void addUser(String username , String password, int connectionId, String dataBlock) {
        String[] msg = dataBlock.split("=");
        String country = msg[1];
        UserMovieRental userToAdd = new UserMovieRental(username, password, "normal" , connectionId , country, 0 , new LinkedList<>());
        mapOfRegisteredUsersByUsername.put(username,userToAdd);

    }

    private Movie getMovieFromListByMovieName(String movieName) {
        Optional<Movie> movieOptional = movieList.stream().filter((m) -> m.getName().equals(movieName)).findAny();
        if (movieOptional.isPresent()) {
            return movieOptional.get();
        } else {
            return null;
        }
    }

    public String commandRequestBroad(String movieName){
        Movie movie= getMovieFromListByMovieName(movieName);
        return "BROADCAST movie " +movie.getName() +" "+ movie.getAvailableAmount()+" "+ movie.getPrice();
    }

    public String commandRequestRemoveBroad(String movieName){
        Movie movie= getMovieFromListByMovieName(movieName);
        return "BROADCAST movie " +movie.getName() +"removed";
    }
}