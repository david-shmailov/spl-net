package bgu.spl.net.api;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;

public class BGRSprotocol implements MessagingProtocol<byte[]> {
    private boolean isStudent=false;
    private boolean isLoginStudent=false;
    private boolean isLoginAdmin=false;
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
    //    if(result==12) return ACK(msg); // todo check if it is needed to do
    //    if(result==13) return ERROR(msg);
        else return sendError(msg);
    }

    private byte[] AdminReg(byte[] msg) {
        String[] str =bytesToString(msg);
        UserName=str[0];
        isAdmin=true;
        if(database.AdminRegister(str[0],str[1])) return sendACK(msg);
        return sendError(msg);
    }

    private byte[] StudentRed(byte[] msg) {
        String[] str =bytesToString(msg);
        UserName=str[0];
        isStudent=true;
        if(database.studentRegister(str[0],str[1])) return sendACK(msg);
        return sendError(msg);
    }

    private byte[] Login(byte[] msg) {
        String[] str =bytesToString(msg);
        if(isAdmin){
            if (database.LoginAdmin(str[0],str[1])) {
                isLoginAdmin=true;
                return sendACK(msg);
            }
        }
        if(isStudent){
            if(database.LoginStudent(str[0],str[1])) {
                isLoginStudent=true;
                return sendACK(msg);
            }
        }
        return sendError(msg);
    }

    private byte[] Logout(byte[] msg) {
        if(isLoginAdmin|| isLoginStudent){
            database.Logout(UserName);
            return sendACK(msg);
        }
        return sendError(msg);
    }

    private byte[] CourseReg(byte[] msg) {
        short courseNum = (short)((msg[2] & 0xff) << 8); //convert the 2 byte
        courseNum += (short)(msg[3] & 0xff);             //convert the 2 byte
        if(isLoginStudent && database.CourseRegister(courseNum,UserName))return sendACK(msg);
        return sendError(msg);
    }

    private byte[] KdamCheck(byte[] msg) {
        if(isLoginStudent) {
            short courseNum = (short) ((msg[2] & 0xff) << 8); //convert the 2 byte
            courseNum += (short) (msg[3] & 0xff);             //convert the 2 byte
            return sendACKOptionalList(msg, database.KdamNeeded(courseNum));
        }
        return sendError(msg);
    }

    private byte[] CourseStat(byte[] msg) {
        if(isLoginAdmin){
            short courseNum = (short) ((msg[2] & 0xff) << 8); //convert the 2 byte
            courseNum += (short) (msg[3] & 0xff);             //convert the 2 byte
            return sendACKCourseStat(msg,database.courseName(courseNum),database.SeatsMax(courseNum),database.SeatsCurrent(courseNum),database.StudentsRegisterToCourse(courseNum));
        }
        return sendError(msg);
    }

    private byte[] StudentStat(byte[] msg) {
        if(isLoginAdmin){
            String name=bytesToString(msg)[0];
            return sendACKStringAndList(msg,name,database.StudentStat(name));
        }
        return sendError(msg);
    }

    private byte[] IsRegistered(byte[] msg) {
        if(isLoginStudent){
            short courseNum = (short) ((msg[2] & 0xff) << 8); //convert the 2 byte
            courseNum += (short) (msg[3] & 0xff);             //convert the 2 byte
            return sendACKOptionalString(msg,database.isRegistered(courseNum,UserName));
        }
        return sendError(msg);
    }

    private byte[] UnRegister(byte[] msg) {
        if (isLoginStudent){
            short courseNum = (short) ((msg[2] & 0xff) << 8); //convert the 2 byte
            courseNum += (short) (msg[3] & 0xff);             //convert the 2 byte
            if (database.unregistered(courseNum,UserName)) return sendACK(msg);
        }
        return sendError(msg);
    }

    private byte[] MyCourses(byte[] msg) {
        if(isLoginStudent){
            return sendACKOptionalList(msg,database.myCourses(UserName));
        }
        return sendError(msg);
    }

    private byte[] sendACK(byte[] msg){
        byte[] send=new byte[4];
        short num=12;
        send[0] = (byte)((num >> 8) & 0xFF);
        send[1] = (byte)(num & 0xFF);
        send[2]=msg[0];
        send[3]=msg[1];
        return send;
    }
    private byte[] sendACKCourseStat(byte[] msg, String courseName, int seatsMax, int seatsCurrent, LinkedList<String> list) {
        byte[] send=sendACKOptionalString(msg,courseName);
        send= Arrays.copyOf(send, send.length+4);
        int index=0;
        for (int j=0;j<send.length;j++){
            if(send[j]==0) {index =j; break;}
        }
        send[index+1] = (byte)(((seatsCurrent >> 8) & 0xFF));
        send[index+2] = (byte)(seatsCurrent & 0xFF);
        send[index+3]=0;
        send[index+4] = (byte)(((seatsMax >> 8) & 0xFF));
        send[index+5] = (byte)(seatsMax & 0xFF);
        send[index+6]=0;
        return getlistOfStringToBytes(list,send,index+7);
    }

    private byte[] getlistOfStringToBytes(LinkedList<String> list, byte[] send, int index) {
        for(String name:list){
            byte[] Name=name.getBytes();
            send=Arrays.copyOf(send,send.length+Name.length);//todo mybe to copy the array different
            for(int i=0;i<Name.length;i++){
                send[i+index]=Name[i];
            }
            index=index+Name.length;
        }
        return send;
    }

    private byte[] sendACKStringAndList(byte[] msg, String name, LinkedList<Integer> list) {
        byte[] send=sendACKOptionalString(msg,name);
        send= Arrays.copyOf(send, send.length+3*list.size());
        int index=0;
        for (int j=0;j<send.length;j++){
            if(send[j]==0) {index =j; break;}
        }
        return getListToBytes(list, send, index+1);
    }

    private byte[] getListToBytes(LinkedList<Integer> list, byte[] send, int index) {
        for (Integer course:list){
            short sCourse= (short) (course& 0x00FF);
            send[index] = (byte)(((sCourse >> 8) & 0xFF));
            send[index+1] = (byte)(sCourse & 0xFF);
            send[index+2]=0;
            index=index+3;
        }
        return send;
    }

    private byte[] sendACKOptionalList(byte[] msg, LinkedList<Integer> list) {
        byte[] send =new byte[4+3*list.size()];
        short num=12;
        send[0] = (byte)((num >> 8) & 0xFF);
        send[1] = (byte)(num & 0xFF);
        send[2]=msg[0];
        send[3]=msg[1];
        int index=4;        ///optional part
        return getListToBytes(list, send, index+1);
    }
    private byte[] sendACKOptionalString(byte[] msg, String str) {
        byte[] st=str.getBytes();
        byte[] send =new byte[4+st.length];
        short num=12;
        send[0] = (byte)((num >> 8) & 0xFF);
        send[1] = (byte)(num & 0xFF);
        send[2]=msg[0];
        send[3]=msg[1];
        for (int i=4;i<4+st.length;i++){    ///optional part
           send[i]=st[i-4];
        }
        return send;
    }
    private byte[] sendError(byte[] msg){
        byte[] send=new byte[4];
        short num=13;
        send[0] = (byte)((num >> 8) & 0xFF);
        send[1] = (byte)(num & 0xFF);
        send[2]=msg[0];
        send[3]=msg[1];
        return send;
    }

    private String[] bytesToString(byte[] msg){
        int start=0;
        int index=0;
        String[] str=new String[2];
        for(int i=2;i<msg.length;i++){
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

