package bgu.spl181.net.api.bidi;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class MovieRentalProtocol extends bidiMessagingProtocolImpl {


    public MovieRentalProtocol(MovieSharedData movieSharedData) {
        super(movieSharedData);
    }


    @Override
    public void parseringRequest(String args) {
        String result;
        MovieSharedData movieSharedData = (MovieSharedData)sharedData;
        String[] msg= args.split(" ");
        if (msg.length==0){
            connections.send(connectionId,"ERROR request failed");
            return;
        } else if(!movieSharedData.isLoggedIn(connectionId)) {
            connections.send(connectionId, "ERROR request " + msg[0] + " failed");
        }
        else {
            String argument=args.substring(args.indexOf(" ")+1);
            switch (msg[0]) {
                case "balance":
                    if(msg[1].equals("info")){
                        result= movieSharedData.commandRequestBalanceInfo(connectionId);
                        connections.send(connectionId,result);

                    }
                    else if (msg[1].equals("add")){
                        result=movieSharedData.commandRequestBalanceAdd(connectionId,msg[2]);
                        connections.send(connectionId,result);

                    }
                    else {
                        connections.send(connectionId,"ERROR request " + msg[0] + " failed");
                    }
                    break;
                case "info":
                    if (msg.length==1){//TODO what if we get an empty string handle """"
                        result = movieSharedData.commandRequestMovieInfo(null);
                        connections.send(connectionId,result);
                    }
                    else{
                        argument= argument.substring(argument.indexOf("\"")+1, argument.lastIndexOf("\""));
                        result = movieSharedData.commandRequestMovieInfo(argument);
                        connections.send(connectionId,result);
                    }
                    break;
                case  "rent":
                    argument= argument.substring(argument.indexOf("\"")+1, argument.lastIndexOf("\""));
                    result=movieSharedData.commandRequestMovieRent(connectionId,argument);
                    connections.send(connectionId,result);
                    if (result.substring(0,3).equals("ACK")){
                        Movie movie = movieSharedData.getMovieFromListByMovieName(argument);
                        String broadcastResult= movieSharedData.commandRequestBroad(movie);
                        broadcast(broadcastResult);
                    }
                    break;
                case "return":
                    argument= argument.substring(argument.indexOf("\"")+1, argument.lastIndexOf("\""));
                    result= movieSharedData.commandRequestReturnMovie(connectionId,argument);
                    connections.send(connectionId,result);
                    if (result.substring(0,3).equals("ACK")){
                        Movie movie = movieSharedData.getMovieFromListByMovieName(argument);
                        String broadcastResult= movieSharedData.commandRequestBroad(movie);
                        broadcast(broadcastResult);
                    }
                    break;
                case "addmovie":
                    args= args.substring(args.indexOf(" ")+2);
                    String movieName=  args.substring(0,args.indexOf("\""));
                    args= args.substring(args.indexOf("\"")+2);
                    int amount = Integer.decode(args.substring(0,args.indexOf(" ")));
                    args= args.substring(args.indexOf(" ")+1);
                    int price;
                    if (args.contains(" ")) {
                         price = Integer.decode(args.substring(0, args.indexOf(" ")));
                    }else { price = Integer.decode(args);

                    }
                    if(args.indexOf(" ")==-1){
                        result=movieSharedData.commandRequestAdminAddMovie(connectionId,movieName,amount,price,null);
                        connections.send(connectionId,result);
                        if (result.substring(0,3).equals("ACK")){
                            Movie movie = movieSharedData.getMovieFromListByMovieName(movieName);
                            String broadcastResult= movieSharedData.commandRequestBroad(movie);//TODO
                            broadcast(broadcastResult);
                        }
                        break;
                    }
                    else{
                        args= args.substring(args.indexOf(" ")+1);
                        List<String> banned = new LinkedList<>();
                        while(args.indexOf("\"")!= -1 ){
                            args = args.substring(1);
                            String country = args.substring(0,args.indexOf("\""));
                            banned.add(country);
                            args = args.substring(args.indexOf("\""));
                            if(args.length()>1){
                                args = args.substring(2);
                            }else {
                                args = "";
                            }
                        }
                        result=movieSharedData.commandRequestAdminAddMovie(connectionId,movieName,amount,price,banned);
                        connections.send(connectionId,result);
                        if (result.substring(0,3).equals("ACK")){
                            Movie movie = movieSharedData.getMovieFromListByMovieName(movieName);
                            String broadcastResult= movieSharedData.commandRequestBroad(movie);//TODO
                            broadcast(broadcastResult);
                        }
                        break;
                    }

                case "remmovie":
                    argument= argument.substring(argument.indexOf("\"")+1, argument.lastIndexOf("\""));
                    Movie movieToBeRemoved = movieSharedData.getMovieFromListByMovieName(argument);
                    result = movieSharedData.commandRequestAdminRemmovie(connectionId,argument);
                    connections.send(connectionId,result);
                    if (result.substring(0,3).equals("ACK")){
                        String broadcastResult = movieSharedData.commandRequestRemoveBroad(movieToBeRemoved);//TODO
                        broadcast(broadcastResult);
                    }
                    break;
                case "changeprice":
                    int split = argument.lastIndexOf(" ");
                    Integer pricetobe= Integer.decode(argument.substring(split+1));
                    argument = argument.substring(0,split);
                    String movieNameToSearch = argument.substring(argument.indexOf("\"")+1, argument.lastIndexOf("\""));
                    Movie movie = movieSharedData.getMovieFromListByMovieName(movieNameToSearch);
                    result = movieSharedData.commandRequestAdminChangePrice(connectionId,movieNameToSearch,pricetobe);
                    connections.send(connectionId,result);
                    if (result.substring(0,3).equals("ACK")){
                        String broadcastResult= movieSharedData.commandRequestBroad(movie);//TODO
                        broadcast(broadcastResult);
                    }
                    break;
                default:
                    connections.send(connectionId,"ERROR request " + msg[0] + " failed");
                    break;
            }
        }


    }

    public void broadcast(String msg){
        ConcurrentHashMap<Integer, User>  map = sharedData.getMapOfLoggedInUsersByConnectedIds();
        for (ConcurrentHashMap.Entry<Integer, User> entry : map.entrySet()){
            UserMovieRental user = (UserMovieRental)entry.getValue();
            connections.send(user.connectionId,msg);
        }
    }
}