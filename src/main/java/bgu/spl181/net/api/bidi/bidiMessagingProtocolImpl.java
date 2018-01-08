package bgu.spl181.net.api.bidi;

import java.util.ArrayList;

public abstract class bidiMessagingProtocolImpl<T> implements bidiMessagingProtocol<T> {

    protected int connectionId;
    protected Connections<T> connections;
    protected SharedData sharedData;
    protected boolean shouldTerminated;

    public bidiMessagingProtocolImpl(SharedData sharedData) {
        this.sharedData = sharedData;
        shouldTerminated=false;
    }


    @Override
    public void start(int connectionId, Connections<T> connections) {
        this.connectionId = connectionId;
        this.connections = connections;
    }

    @Override
    public void process(T message) {
        if (message instanceof String) {
            String[] msg = ((String) message).split(" ");
                switch (msg[0]) {
                    case "REGISTER":
                        register(msg);
                        break;
                    case "LOGIN":
                       login(msg);
                        break;
                    case "SIGNOUT":
                        signout();
                        break;
                    case "REQUEST":
                        String requestArgs = ((String) message).substring(((String) message).indexOf(" ")+1);
                        parseringRequest(requestArgs);
                        break;
                    default:
                        connections.send(connectionId,(T)"ERROR unknown command");
                        break;

                }
            }

        }





        /**
         * @return true if the connection should be terminated
         */
        @Override
        public boolean shouldTerminate () {
            return shouldTerminated;
        }


        public abstract void parseringRequest(String args);

        public void register (String[] msg){
            String result;
            if (msg.length == 3) {
                result = sharedData.commandRegister(msg[1], msg[2], null, connectionId);
                connections.send(connectionId, (T) result);
            } else if (msg.length > 3) {
                String datablock="";
                for (int i=3; i<msg.length;i++){
                    datablock= datablock + msg[i]+" ";
                }
                datablock=datablock.substring(0,datablock.length()-1);
                result = sharedData.commandRegister(msg[1], msg[2], datablock, connectionId);
                connections.send(connectionId, (T) result);
            }else {
                connections.send(connectionId, (T) "ERROR registration failed");
            }
        }


        public void  login (String[] msg){
            String result;
            if (msg.length == 3) {
                result = sharedData.commandLogIn(msg[1], msg[2],connectionId);
                connections.send(connectionId, (T) result);
            } else {
                connections.send(connectionId, (T) "ERROR login failed");
            }
        }


        public void signout() {
        String result = sharedData.commandSignOut(connectionId);
        if(result.equals("ACK signout succeeded")){
            shouldTerminated=true;
            connections.disconnect(connectionId);
            connections.send(connectionId,(T)result);
        }
        else {
            connections.send(connectionId,(T)result);
        }

    }

    }


