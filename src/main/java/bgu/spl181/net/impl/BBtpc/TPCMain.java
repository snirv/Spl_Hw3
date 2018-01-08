package bgu.spl181.net.impl.BBtpc;

import bgu.spl181.net.api.MessageEncoderDecoderImpl;
import bgu.spl181.net.api.bidi.bidiMessagingProtocolImpl;
import bgu.spl181.net.srv.Server;

public class TPCMain {
    public static void main(String[] args){
        Server tpcServer = Server.threadPerClient(
                Integer.decode(args[0]).intValue(),
                ()-> new bidiMessagingProtocolImpl<>(),
                ()-> new MessageEncoderDecoderImpl<>()
        );
        tpcServer.serve();

    }


}
