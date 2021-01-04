package bgu.spl.net.impl.BGRSServer;

import bgu.spl.net.api.BGRSEncoderDecoder;
import bgu.spl.net.api.BGRSprotocol;
import bgu.spl.net.srv.Server;

public class ReactorMain {
    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);
        int threads = Integer.parseInt(args[1]);
        Server.reactor(
                threads,
                port, //port
                () ->  new BGRSprotocol(), //protocol factory
                BGRSEncoderDecoder::new //message encoder decoder factory
        ).serve();
    }
}
