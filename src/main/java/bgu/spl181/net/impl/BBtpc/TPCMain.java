package bgu.spl181.net.impl.BBtpc;

import bgu.spl181.net.api.MessageEncoderDecoderImpl;
import bgu.spl181.net.api.bidi.*;
import bgu.spl181.net.srv.Server;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class TPCMain {
    public static void main(String[] args) {
        /*Server tpcServer = Server.threadPerClient(
                Integer.decode(args[0]).intValue(),
                ()-> new bidiMessagingProtocolImpl<>(),
                ()-> new MessageEncoderDecoderImpl<>()
        );
        tpcServer.serve();*/

      /*  JsonShardData jsonShardData;
        Gson gson = new Gson();
        try {
            FileReader fileReader = new FileReader("Database/Users.json");
            jsonShardData = gson.fromJson(fileReader, JsonShardData.class);
            System.out.println(jsonShardData);
        } catch (FileNotFoundException ex) {

        }*/


   /*     MovieSharedData jsonShardData;
        Gson gson = new Gson();
        try {
            FileReader fileReader = new FileReader("Database/Users.json");
            jsonShardData = gson.fromJson(fileReader, MovieSharedData.class);
            System.out.println(jsonShardData);
        } catch (FileNotFoundException ex) {

        }*/
    /*    ConcurrentHashMap<Integer,Movie> moviesMap;//TODO delete
        List<Movie> movieList;
        protected  ConcurrentHashMap<String ,User> mapOfRegisteredUsersByUsername;
        protected  ConcurrentHashMap<Integer,User> mapOfLoggedInUsersByConnectedIds;*/
        User u1 = new UserMovieRental("snir" , "1" , "normal", 1, "conutry=israel", 0 , new LinkedList<>());
    User u2 = new UserMovieRental("snir1" , "2" , "normal", 2, "conutry=israel", 1, new LinkedList<>());
    User u3 = new UserMovieRental("snir2" , "3" , "admin", 3, "conutry=israel", 423, new LinkedList<>());
    User u4 = new UserMovieRental("snir3" , "4" , "normal", 4, "conutry=israel", 23 , new LinkedList<>());

    LinkedList<String> list = new LinkedList();
    list.add("israel");
        list.add("israel2");
        list.add("israel3");
    Movie m1 = new Movie(1,"titanic",50, list, 5);
    Movie m2 = new Movie(2,"titanic2",51, new LinkedList<>(), 4);
    Movie m3 = new Movie(3,"titanic3",52, new LinkedList<>(), 7);

        MovieSharedData movieSharedData = new MovieSharedData(new ConcurrentHashMap<>(), new  ConcurrentHashMap<>());
        movieSharedData.getMovieList().add(m1);
        movieSharedData.getMovieList().add(m2);
        movieSharedData.getMovieList().add(m3);
    movieSharedData.getUserMap().put(u1.getUserName() ,u1);
        movieSharedData.getUserMap().put(u2.getUserName() ,u2);
        movieSharedData.getUserMap().put(u3.getUserName() ,u3);
        movieSharedData.getUserMap().put(u4.getUserName() ,u4);
        movieSharedData.updateUserJson();
        movieSharedData.updateServiceJson();
    }
}
