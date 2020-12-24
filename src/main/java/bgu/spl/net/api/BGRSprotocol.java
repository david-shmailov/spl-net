package bgu.spl.net.api;


import java.nio.ByteBuffer;
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
        String str[]=bytesToString(msg);    ///covert the byte[] to string []
        int result =Integer.parseInt(str[0]);      ////convert the commend string to int
        if(result==1) return AdminReg(str);
        if(result==2) return StudentRed(str);
        if(result==3) return Login(str);
        if(result==4) return Logout(str);
        if(result==5) return CourseReg(str);
        if(result==6) return KdamCheck(str);
        if(result==7) return CourseStat(str);
        if(result==8) return StudentStat(str);
        if(result==9) return IsRegistered(str);
        if(result==10) return UnRegister(str);
        if(result==11) return MyCourses(str);
    //    if(result==12) return ACK(msg); // todo check if it is needed to do
    //    if(result==13) return ERROR(msg);
        else return sendError(str);
    }

    private byte[] AdminReg(String[] str) {
        UserName=str[1];
        if(database.AdminRegister(str[1],str[2])) return sendACK(str);
        return sendError(str);
    }

    private byte[] StudentRed(String[] str) {
        UserName=str[0];
        if(database.studentRegister(str[1],str[2])) return sendACK(str);
        return sendError(str);
    }

    private byte[] Login(String[] str) {
        if(database.isStudent(str[1])){
            if (database.LoginStudent(str[1],str[2])) {
                isLoginStudent=true;
                return sendACK(str);
            }
        }
        else{
            if(database.LoginAdmin(str[1],str[2])) {
                isLoginAdmin=true;
                return sendACK(str);
            }
        }
        return sendError(str);
    }

    private byte[] Logout(String[] str) {
        if(isLoginAdmin|| isLoginStudent){
            database.Logout(UserName);
            shouldTerminate=true;
            return sendACK(str);
        }
        return sendError(str);
    }

    private byte[] CourseReg(String[] str) {
        int courseNum=Integer.parseInt(str[1]);
        if(isLoginStudent && database.CourseRegister(courseNum,UserName))return sendACK(str);
        return sendError(str);
    }

    private byte[] KdamCheck(String[] str) {
        if(isLoginStudent) {
            int courseNum=Integer.parseInt(str[1]);
            if(database.KdamNeeded(courseNum)!=null)
            return sendACKOptionalList(str, database.KdamNeeded(courseNum)); //todo list of kdam need to be order
        }
        return sendError(str);
    }

    private byte[] CourseStat(String[] str) {
        if(isLoginAdmin){
            int courseNum=Integer.parseInt(str[1]);
            return sendACKCourseStat(str,courseNum,database.courseName(courseNum),database.SeatsMax(courseNum),
                    database.SeatsCurrent(courseNum),database.StudentsRegisterToCourse(courseNum));//todo list of student need to be order alphabetic
        }
        return sendError(str);
    }

    private byte[] StudentStat(String[] str) {
        if(isLoginAdmin){
            String name=str[1];
            return sendACKStringAndList(str,name,database.StudentStat(name));//todo list of Integer need to be order
        }
        return sendError(str);
    }

    private byte[] IsRegistered(String[] str) {
        if(isLoginStudent){
            int courseNum=Integer.parseInt(str[1]);
            return sendACKOptionalString(str,database.isRegistered(courseNum,UserName));
        }
        return sendError(str);
    }

    private byte[] UnRegister(String[] str) {
        if (isLoginStudent){
            int courseNum=Integer.parseInt(str[1]);
            if (database.unregistered(courseNum,UserName)) return sendACK(str);
        }
        return sendError(str);
    }

    private byte[] MyCourses(String[] str) {
        if(isLoginStudent){
            return sendACKOptionalList(str,database.myCourses(UserName));
        }
        return sendError(str);
    }

    private byte[] sendACK(String[] str){
        byte[] send=new byte[4];
        short num=12;
        send[0] = (byte)((num >> 8) & 0xFF);
        send[1] = (byte)(num & 0xFF);
        byte[] b=str[0].getBytes();
        send= Arrays.copyOf(send, send.length+b.length);
        for(int i=send.length-b.length;i<send.length;i++)
            send[i+b.length]=b[i];
        return send;
    }
    private byte[] sendACKCourseStat(String[] str,int numCourse,String courseName, int seatsMax, int seatsCurrent, LinkedList<String> list) {
        byte[] send=sendACK_Short_String(str,numCourse,courseName);
        byte[] curr=String.valueOf(seatsCurrent).getBytes();
        byte[] max=String.valueOf(seatsMax).getBytes();
        send=unionByte(send,curr);
        send=unionByte(send,max);
        for(String name: list){
            byte[] nam=name.getBytes();
            send=unionByte(send,nam);
        }
        return send;
    }


    private byte[] sendACKStringAndList(String[] str, String name, LinkedList<Integer> list) {
        byte[] send=sendACKOptionalString(str,name);
        for (Integer course: list){
            byte[] cours=String.valueOf(course).getBytes();
            send=unionByte(send,cours);
        }
        return send;
    }

    private byte[] sendACKOptionalList(String[] str, LinkedList<Integer> list) {
        byte[] send =sendACK(str);
        for (Integer course: list){
            byte[] cours=String.valueOf(course).getBytes();
            send=unionByte(send,cours);
        }
        return send;
    }
    private byte[] sendACKOptionalString(String[] str, String name) {
        byte[] st=name.getBytes();
        byte[] send =sendACK(str);
        return unionByte(send,st);
    }
    private byte[] sendACK_Short_String(String[] str,int numCourse, String name) {
        byte[] name1=name.getBytes();
        byte[] num=String.valueOf(numCourse).getBytes();
        byte[] send =sendACK(str);
        send=unionByte(send,num);
        return unionByte(send,name1);
    }
    private byte[] unionByte(byte[] one,byte[] two){
        int index=one.length;
        one= Arrays.copyOf(one, one.length+two.length);
        for(int i=index;i<one.length;i++){
            one[i]=two[i-index];
        }
        return one;
    }
    private byte[] sendError(String[] str){
        byte[] send=new byte[4];
        short num=13;
        send[0] = (byte)((num >> 8) & 0xFF);
        send[1] = (byte)(num & 0xFF);
        byte[] b=str[0].getBytes();
        return unionByte(send,b);
    }

    private String[] bytesToString(byte[] msg){
        int start=0;
        int index=0;
        String[] str=new String[2];
        for(int i=0;i<msg.length;i++){
            if(msg[i]==32) { // if it is find ' ' namespace
                str[index]=new String(msg,start,i-start,StandardCharsets.UTF_8);
                start=i+1;
                index++;
            }
            if(i==msg.length-1) str[index]=new String(msg,start,i+1-start,StandardCharsets.UTF_8);
        }
        return str;
    }


    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }
}

