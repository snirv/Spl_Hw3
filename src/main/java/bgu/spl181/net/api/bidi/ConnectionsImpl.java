package bgu.spl181.net.api.bidi;

import bgu.spl181.net.srv.bidi.ConnectionHandler;

import java.sql.Connection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionsImpl<T> implements Connections<T> {

    protected ConcurrentHashMap<Integer,ConnectionHandler<T>> clients;

    public ConnectionsImpl() {
        this.clients = new ConcurrentHashMap<>();
    }

    @Override
    public boolean send(int connectionId, T msg) {
        try {
            clients.get(connectionId).send(msg);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void broadcast(T msg) {//TODO need to verify if the connection is active
        for (Map.Entry<Integer,ConnectionHandler<T>> entry : clients.entrySet()){
            send(entry.getKey(),msg);
        }

    }

    @Override
    public void disconnect(int connectionId) {
        clients.remove(connectionId);
    }


    public ConcurrentHashMap<Integer,ConnectionHandler<T>> getClients(){
        return clients;
        }
}