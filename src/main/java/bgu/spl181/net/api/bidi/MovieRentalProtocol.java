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
            if ((!msg[0].equals("balance")) || (msg[0].equals("balance")&&(!msg[1].equals("add")||!msg[1].equals("info")))) {
                connections.send(connectionId, "ERROR request" + msg[0] + "failed");
            } else {
                connections.send(connectionId, "ERROR request" + msg[0]+" "+msg[1] + "failed"); }
        }
        else {
            String argument="";
            for (int i=1; i<msg.length;i++){
                argument=argument+msg[i]+" ";
            }
            argument.substring(0,argument.length()-1);

            switch (msg[0]) {//assume wont be nullpointer exception
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
                        connections.send(connectionId,"ERROR request" + msg[0] + "failed");
                    }
                    break;
                case "info":
                    if (msg.length==1 || argument.equals("")){//TODO what if we get an empty string handle """"
                        result = movieSharedData.commandRequestMovieInfo(null);
                        connections.send(connectionId,result);
                    }
                    else{

                        result = movieSharedData.commandRequestMovieInfo(argument);
                        connections.send(connectionId,result);
                    }
                    break;
                case  "rent":
                    result=movieSharedData.commandRequestMovieRent(connectionId,argument);
                    connections.send(connectionId,result);
                    if (result.substring(0,3).equals("ACK")){
                        String broadcastResult= movieSharedData.commandRequestBroad(argument);
                        broadcast(broadcastResult);
                    }
                    break;
                case "return":
                    result= movieSharedData.commandRequestReturnMovie(connectionId,argument);
                    connections.send(connectionId,result);
                    if (result.substring(0,3).equals("ACK")){
                        String broadcastResult= movieSharedData.commandRequestBroad(argument);
                        broadcast(broadcastResult);
                    }
                    break;
                case "addmovie":
                    String movieName= msg[1];
                    int i=2;
                    if(movieName.charAt(movieName.length()-1)!='"'){
                        movieName= movieName+" "+msg[2];
                        while (i<msg.length || msg[i].charAt(msg[i].length()-1)!= '"'){
                            i++;
                            movieName= movieName+" "+msg[i];
                        }
                    }
                    int amount = Integer.decode(msg[i]);
                    i++;
                    int price = Integer.decode(msg[i]);
                    if (i== msg.length-1){
                        result=movieSharedData.commandRequestAdminAddMovie(connectionId,movieName,amount,price,null);
                        connections.send(connectionId,result);
                        if (result.substring(0,3).equals("ACK")){
                            String broadcastResult= movieSharedData.commandRequestBroad(movieName);//TODO
                            broadcast(broadcastResult);
                        }
                        break;
                    }
                    else{
                        List<String> banned = new LinkedList<>();
                        while (i<msg.length){
                            String country=msg[i];
                            while (i<msg.length-1 && msg[i].charAt(msg[i].length()-1)!='"'){
                                i++;
                                country=country+" "+ msg[i];
                            }
                            banned.add(country);
                            i++;
                        }
                        result=movieSharedData.commandRequestAdminAddMovie(connectionId,movieName,amount,price,banned);
                        connections.send(connectionId,result);
                        if (result.substring(0,3).equals("ACK")){
                            String broadcastResult= movieSharedData.commandRequestBroad(movieName);//TODO
                            broadcast(broadcastResult);
                        }
                        break;
                    }

                case "remmovie":
                    result=movieSharedData.commandRequestAdminRemmovie(connectionId,argument);
                    connections.send(connectionId,result);
                    if (result.substring(0,3).equals("ACK")){
                        String broadcastResult= movieSharedData.commandRequestRemoveBroad(argument);//TODO
                        broadcast(broadcastResult);
                    }
                    break;
                case "changeprice":
                    int split= argument.lastIndexOf(" ");
                    result=movieSharedData.commandRequestAdminChangePrice(connectionId,argument.substring(0,split),Integer.decode(argument.substring(split+1)));
                    connections.send(connectionId,result);
                    if (result.substring(0,3).equals("ACK")){
                        String broadcastResult= movieSharedData.commandRequestBroad(argument.substring(0,split));//TODO
                        broadcast(broadcastResult);
                    }
                    break;
                default:
                    connections.send(connectionId,"ERROR request" + msg[0] + "failed");
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