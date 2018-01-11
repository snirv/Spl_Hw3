package bgu.spl181.net.api.bidi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.LongSerializationPolicy;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * a class that contain thecurrent data of the movie rental system.
 * this class received a command from the protocol and exectuing them
 * this class aslo update the json files each time a REQUEST is exectuing
 */
public class MovieSharedData extends SharedData{

    CopyOnWriteArrayList<Movie> movieList;
    Object lock;
    Object lockUpdateServiceJson;
    Object lockUpdateUserJson;


    public MovieSharedData(ConcurrentHashMap<String,User> userMovieRentalMap ,CopyOnWriteArrayList<Movie> movieList) {
        super(userMovieRentalMap);
        this.movieList = movieList;
        this.lock = new Object();
        this.lockUpdateUserJson = new Object();
        this.lockUpdateServiceJson = new Object();

    }

    protected boolean isLoggedIn(Integer connectionId){
        if (mapOfLoggedInUsersByConnectedIds.containsKey(connectionId)) {
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
        long userBalance = user.getBalance();
        return "ACK balance " + userBalance;
    }

    protected String commandRequestBalanceAdd(Integer connectionId, String amount) {
        Long amountAslong = Long.decode(amount);
        UserMovieRental user = (UserMovieRental)mapOfLoggedInUsersByConnectedIds.get(connectionId);
        long newBalance = user.addBalance(amountAslong);
        user.setBalance(newBalance);
        updateUserJson();
        updateServiceJson();
        return  "ACK balance " + newBalance + " added " + amount;
    }


    protected String commandRequestMovieInfo(String movieName) {
        String ret;
        if(movieName == null){
            ret = "\"";
            for (Movie movie : movieList){
                ret =ret + movie.getName() + "\" \"";//TODO why it doesnt keep the movie name with qoutation??
            }
            ret = ret.substring(0, ret.length() -1);
            return "ACK info " + ret ;
        }
        else {
            for (Movie movie : movieList){
                if(movie.getName().equals(movieName)){
                    ret = movie.toString();
                    return "ACK info " + ret  ;
                }
            }
            return "ERROR request info failed";
        }
    }

    protected String commandRequestMovieRent(Integer connectionId ,String movieName) {
        UserMovieRental user = (UserMovieRental) mapOfLoggedInUsersByConnectedIds.get(connectionId);
        Movie movie = getMovieFromListByMovieName(movieName);
        synchronized (lock){
        if (movie == null || movie.bannedCountries.contains(user.getWithoutQutationCountry()) ||
                user.isRentingMovie(movieName) ||
               user.getBalance() < movie.getPrice()) {
            return "ERROR request rent failed";
        }
       // while(!movie.lock.compareAndSet(false,true));
        if (movie.getAvailableAmount() == 0){
            return "ERROR request rent failed";
        }else {
            user.getMoviesList().add(movie);
            user.setBalance(user.getBalance() - movie.getPrice());
            movie.setAvailableAmount(movie.getAvailableAmount() - 1);
          //  movie.lock.set(false);
            updateUserJson();
            updateServiceJson();
            return "ACK rent " +"\"" + movieName + "\"" + " success";
        }
        }
    }

    protected String commandRequestReturnMovie(Integer connectionId, String movieName) {
        synchronized (lock) {
            UserMovieRental user = (UserMovieRental) mapOfLoggedInUsersByConnectedIds.get(connectionId);
            Movie movie = getMovieFromListByMovieName(movieName);
            if (movie == null || !user.isRentingMovie(movieName)) {
                return "ERROR request return failed";
            } else {
                user.getMoviesList().remove(movie);
                movie.setAvailableAmount(movie.getAvailableAmount() + 1);
                updateUserJson();
                updateServiceJson();
                return "ACK return " + "\"" + movieName + "\""+ " success";
            }
        }
    }


    protected String commandRequestAdminAddMovie(Integer connectionId, String movieName , int amount , int price , List<String> bannedCountry) {
        synchronized (lock) {
            Movie movie = getMovieFromListByMovieName(movieName);
            if (!isAdmin(connectionId) || amount <= 0 || price <= 0 || movie != null) {
                return "ERROR request addmovie failed";
            } else {
                long id = 0;
                OptionalLong optionalId = movieList.stream()
                        .mapToLong(m -> m.getId())
                        .max();
                if (optionalId.isPresent()) {
                    id = optionalId.getAsLong();
                }
                Movie movieToAdd = new Movie(id + 1, movieName, price, bannedCountry, amount);
                movieList.add(movieToAdd);
                updateUserJson();
                updateServiceJson();
                return "ACK addmovie " + "\"" + movieName + "\""+ " success";
            }
        }
    }

    protected String commandRequestAdminRemmovie(Integer connectionId,String movieName) {
        synchronized (lock) {
            Movie movie = getMovieFromListByMovieName(movieName);
            if (movie == null || !isAdmin(connectionId)) {
                return "ERROR request remmovie failed";
            }
           // while (!movie.lock.compareAndSet(false, true)) ;
            if (movie.getAvailableAmount() != movie.getTotalAmount()) {
                return "ERROR request remmovie failed";
            }
            movieList.remove(movie);
          //  movie.lock.set(false);
            updateUserJson();
            updateServiceJson();
            return "ACK remmovie " + "\"" + movieName + "\""+ " success";
        }
    }

    protected String commandRequestAdminChangePrice(Integer connectionId , String movieName , int price) {
        synchronized (lock) {
            Movie movie = getMovieFromListByMovieName(movieName);
            if (movie == null || !isAdmin(connectionId) || price <= 0) {
                return "ERROR request changeprice failed";
            }
           // while (!movie.lock.compareAndSet(false, true)) ;
            movie.setPrice(price);
            //movie.lock.set(false);
            updateUserJson();
            updateServiceJson();
            return "ACK changeprice " + "\"" + movieName + "\"" + " success";
        }
    }

    @Override
    protected boolean isValidDataBlock(String dataBlock) {
        if (dataBlock== null){return false;}
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

    protected Movie getMovieFromListByMovieName(String movieName) {
        Optional<Movie> movieOptional = movieList.stream().filter((m) -> m.getName().equals(movieName)).findAny();
        if (movieOptional.isPresent()) {
            return movieOptional.get();
        } else {
            return null;
        }
    }

    public String commandRequestBroad(Movie movie){
        return "BROADCAST movie " + movie.getName() +" "+ movie.getAvailableAmount()+" "+ movie.getPrice();
    }

    public String commandRequestRemoveBroad(Movie movie){
        return "BROADCAST movie " + movie.getName() +"removed";
    }
    @Override
    public  void updateServiceJson(){
        synchronized (lockUpdateServiceJson) {
            try (PrintWriter printer = new PrintWriter("Database/Movies1.json")) {
                GsonBuilder gsonBuilder = new GsonBuilder();
                gsonBuilder.setLongSerializationPolicy(LongSerializationPolicy.STRING);
                Gson writer = gsonBuilder.create();
                JsonObject jsonObject = new JsonObject();
                jsonObject.add("movies", writer.toJsonTree(movieList));
                printer.print(jsonObject);
            } catch (IOException ex) {
            }
        }
    }
    @Override
    public  void updateUserJson()  {
        synchronized (lockUpdateUserJson) {
            try (PrintWriter printer = new PrintWriter("Database/Users1.json")) {
                ArrayList<User> userAsArray = new ArrayList<>();
                for (Map.Entry<String, User> entry : mapOfRegisteredUsersByUsername.entrySet()) {
                    userAsArray.add(entry.getValue());
                }
                GsonBuilder gsonBuilder = new GsonBuilder();
                gsonBuilder.setLongSerializationPolicy(LongSerializationPolicy.STRING);
                Gson writer = gsonBuilder.create();
                JsonObject jsonObject = new JsonObject();
                jsonObject.add("users", writer.toJsonTree(userAsArray));
                printer.print(jsonObject);
            } catch (IOException ex) {
            }
        }
    }

    public List<Movie> getMovieList() {
        return movieList;
    }
}