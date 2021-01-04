package bgu.spl.net.impl.BGRSServer;

import bgu.spl.net.api.BGRSEncoderDecoder;
import bgu.spl.net.api.BGRSprotocol;
import bgu.spl.net.srv.Server;


public class TPCMain {
    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);
        Server.threadPerClient(
                port, //port
                () -> new BGRSprotocol(), //protocol factory
                BGRSEncoderDecoder::new //message encoder decoder factory
        ).serve();
    }
}

