package bgu.spl.net.srv;

import bgu.spl.net.api.BGRSEncoderDecoder;
import bgu.spl.net.api.BGRSprotocol;
import bgu.spl.net.impl.newsfeed.NewsFeed;


public class BGRSSeverMain {
    public static void main(String[] args) {

        Server.threadPerClient(
                7777, //port
              () -> new BGRSprotocol(), //protocol factory
              BGRSEncoderDecoder::new //message encoder decoder factory
        ).serve();
    }
}

