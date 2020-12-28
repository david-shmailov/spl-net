package bgu.spl.net.api;

import java.util.Arrays;

public class BGRSEncoderDecoder implements MessageEncoderDecoder<byte[]>{

    private byte[] bytes = new byte[1 << 10]; //start with 1k
    private int len = 0;

    @Override
    public byte[] decodeNextByte(byte nextByte) {
        //notice that the top 128 ascii characters have the same representation as their utf-8 counterparts
        //this allow us to do the following comparison
        if (nextByte == '/') {
            return popString();
        }

        pushByte(nextByte);
        return null; //not a line yet
    }

    @Override
    public byte[] encode(byte[] message) {
        byte[] send=new byte[message.length+1] ;
        for(int i=0; i<message.length;i++) {send[i]=message[i];}
        send[message.length]='/';
        return send;
    }

    private void pushByte(byte nextByte) {
        if (len >= bytes.length) {
            bytes = Arrays.copyOf(bytes, len * 2);
        }

        bytes[len++] = nextByte;
    }

    private byte[] popString() {
        //notice that we explicitly requesting that the string will be decoded from UTF-8
        //this is not actually required as it is the default encoding in java.
        byte[] result=new byte[len] ;
        for(int i=0; i<len;i++) {result[i]=bytes[i];}
        len = 0;
        return result;
    }
}


