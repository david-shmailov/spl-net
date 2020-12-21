package bgu.spl.net.api;

import java.time.LocalDateTime;

public class BGRSprotocol implements MessagingProtocol<String> {
    private boolean isStudent=false;
    private boolean isLogin=false;
    private boolean isAdmin=false;
    private String UserName=null;
    private boolean shouldTerminate = false;

    @Override
    public String process(String msg) {

        return createEcho(msg);
    }

    private String createEcho(String message) {
        String echoPart = message.substring(Math.max(message.length() - 2, 0), message.length());
        return message + " .. " + echoPart + " .. " + echoPart + " ..";
    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }
}

