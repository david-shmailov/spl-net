package bgu.spl.net.api;

import java.nio.charset.StandardCharsets;

public class BGRSprotocol implements MessagingProtocol<byte[]> {
    private boolean isStudent=false;
    private boolean isLogin=false;
    private boolean isAdmin=false;
    private String UserName=null;
    private boolean shouldTerminate = false;
    private Database database=Database.getInstance();

    @Override
    public byte[] process(byte[] msg) {
        short result = (short)((msg[0] & 0xff) << 8); //convert the first 2 byte
        result += (short)(msg[1] & 0xff);             //convert the first 2 byte
        if(result==1) return AdminReg(msg);
        if(result==2) return StudentRed(msg);
        if(result==3) return Login(msg);
        if(result==4) return Logout(msg);
        if(result==5) return CourseReg(msg);
        if(result==6) return KdamCheck(msg);
        if(result==7) return CourseStat(msg);
        if(result==8) return StudentStat(msg);
        if(result==9) return IsRegistered(msg);
        if(result==10) return UnRegister(msg);
        if(result==11) return MyCourses(msg);
        if(result==12) return ACK(msg);
        if(result==13) return ERROR(msg);
        else return null;
    }

    private byte[] AdminReg(byte[] msg) {
        String[] str =bytesToString(msg);
        UserName=str[0];

    }

    private byte[] StudentRed(byte[] msg) {
    }

    private byte[] Login(byte[] msg) {
    }

    private byte[] Logout(byte[] msg) {
    }

    private byte[] CourseReg(byte[] msg) {
    }

    private byte[] KdamCheck(byte[] msg) {
    }

    private byte[] CourseStat(byte[] msg) {
    }

    private byte[] StudentStat(byte[] msg) {
    }

    private byte[] IsRegistered(byte[] msg) {
    }

    private byte[] UnRegister(byte[] msg) {
    }

    private byte[] MyCourses(byte[] msg) {
    }

    private byte[] ACK(byte[] msg) {
    }

    private byte[] ERROR(byte[] msg) {
    }

    private String[] bytesToString(byte[] msg){
        int start=0;
        int index=0;
        String[] str=new String[2];
        for(int i=2;i<=msg.length;i++){
            if(msg[i]==0) {
                str[index]=new String(msg,start,i,StandardCharsets.UTF_8);
                start=i+1;
                index++;
            }
        }
        return str;
    }


    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }
}

