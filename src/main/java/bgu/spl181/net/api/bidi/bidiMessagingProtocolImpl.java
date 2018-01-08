package bgu.spl181.net.api.bidi;

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
        String result;
        if (message instanceof String) {
            String[] msg = ((String) message).split(" ");
                switch (msg[0]) {//assume wont be nullpointer exception
                    case "REGISTER":

                        if (msg.length == 3) {
                            result = sharedData.commandRegister(msg[1], msg[2], null, connectionId);
                            connections.send(connectionId, (T) result);
                        } else if (msg.length == 4) {
                            result = sharedData.commandRegister(msg[1], msg[2], msg[3], connectionId);
                            connections.send(connectionId, (T) result);
                        } else {
                            connections.send(connectionId, (T) "ERROR registration failed");
                        }
                        break;
                    case "LOGIN":
                        if (msg.length == 3) {
                            result = sharedData.commandLogIn(msg[1], msg[2],connectionId);
                            connections.send(connectionId, (T) result);
                        } else {
                            connections.send(connectionId, (T) "ERROR login failed");
                        }
                        break;
                    case "SIGNOUT":
                        result = sharedData.commandSignOut(connectionId);
                        if(result.equals("ACK signout succeeded")){
                            shouldTerminated=true;
                            connections.disconnect(connectionId);
                            connections.send(connectionId,(T)result);
                        }
                        else {
                            connections.send(connectionId,(T)result);
                        }
                        break;

                    case "REQUEST":
                        String requestArgs = ((String) message).substring(((String) message).indexOf(" ")+1);
                        parseringRequest(requestArgs);
                        break;

                    default:
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
    }


