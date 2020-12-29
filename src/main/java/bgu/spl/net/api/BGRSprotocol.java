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

        short result=bytesToShort(take2Bytes(msg,0)); ///covert the first 2 byte[] to short
        if(result==1) return AdminReg(msg);
        if(result==2) return StudentReg(msg);
        if(result==3) return Login(msg);
        if(result==4) return Logout(msg);
        if(result==5) return CourseReg(msg);
        if(result==6) return KdamCheck(msg);
        if(result==7) return CourseStat(msg);
        if(result==8) return StudentStat(msg);
        if(result==9) return IsRegistered(msg);
        if(result==10) return UnRegister(msg);
        if(result==11) return MyCourses(msg);
        else return sendError((short) 13);
    }

    private byte[] AdminReg(byte[] msg) {
        String[] str=bytesToString(msg);
        if(str[1]!=null){
        UserName=str[0];
        if(database.AdminRegister(str[0],str[1])) return sendACK((short) 1);
        }
        return sendError((short) 1);
    }

    private byte[] StudentReg(byte[] msg) {
        String[] str=bytesToString(msg);
        if(str[1]!=null){
            UserName = str[0];
            if (database.studentRegister(str[0], str[1])) return sendACK((short) 2);
        }
        return sendError((short) 2);
    }

    private byte[] Login(byte[] msg) {
        String[] str=bytesToString(msg);
        UserName=str[0];
        if (!isLoginAdmin && !isLoginStudent) {
            if (database.isStudent(str[0])) {
                if (database.LoginStudent(str[0], str[1])) {
                    isLoginStudent = true;
                    return sendACK((short) 3);
                }
            } else {
                if (database.LoginAdmin(str[0], str[1])) {
                    isLoginAdmin = true;
                    return sendACK((short) 3);
                }
            }
            return sendError((short) 3);
        }return sendError((short) 3);
    }

    private byte[] Logout(byte[] msg) {
        if(msg.length==2){
        if(isLoginAdmin|| isLoginStudent){
            database.Logout(UserName);
            shouldTerminate=true;
            return sendACK((short) 4);
        }}
        return sendError((short) 4);
    }

    private byte[] CourseReg(byte[] msg) {
        if(msg.length==4) {
            short courseNum = bytesToShort(take2Bytes(msg, 2));
            if (isLoginStudent && database.CourseRegister(courseNum, UserName)) return sendACK((short) 5);
        }
        return sendError((short) 5);
    }

    private byte[] KdamCheck(byte[] msg) {
        if(msg.length==4){
        if(isLoginStudent|| isLoginAdmin) {
            short courseNum= bytesToShort(take2Bytes(msg, 2));
            if(database.KdamNeeded(courseNum)!=null)
            return sendACKOptionalList((short) 6, database.KdamNeeded(courseNum)); //todo list of kdam need to be order
        }}
        return sendError((short) 6);
    }

    private byte[] CourseStat(byte[] msg) {
        if(msg.length==4){
        if(isLoginAdmin){
            short courseNum= bytesToShort(take2Bytes(msg, 2));
            return sendACKCourseStat((short) 7,courseNum,database.courseName(courseNum),database.SeatsMax(courseNum),
                    database.SeatsCurrent(courseNum),database.StudentsRegisterToCourse(courseNum));//todo list of student need to be order alphabetic
        }}
        return sendError((short) 7);
    }

    private byte[] StudentStat(byte[] msg) {
        String[] str=bytesToString(msg);
        if(str[1]==null){
        if(isLoginAdmin){
            String name=str[0];
            return sendACKStringAndList((short) 8,name,database.StudentStat(name));//todo list of Integer need to be order
        }}
        return sendError((short) 8);
    }

    private byte[] IsRegistered(byte[] msg) {
        if(msg.length==4){
        if(isLoginStudent){
            short courseNum= bytesToShort(take2Bytes(msg, 2));
            if(database.IsCourseExist(courseNum))return sendACKOptionalString((short) 9,database.isRegistered(courseNum,UserName));
        }}
        return sendError((short) 9);
    }

    private byte[] UnRegister(byte[] msg) {
        if(msg.length==4){
        if (isLoginStudent){
            short courseNum= bytesToShort(take2Bytes(msg, 2));
            if (database.unregistered(courseNum,UserName)) return sendACK((short) 10);
        }}
        return sendError((short) 10);
    }

    private byte[] MyCourses(byte[] msg) {
        if(msg.length==2){
        if(isLoginStudent){
            return sendACKOptionalList((short) 11,database.myCourses(UserName));
        }}
        return sendError((short) 11);
    }

    private byte[] sendACK(short commend){
        byte[] send=shortToBytes((short) 12);
        return unionByte(send,shortToBytes(commend));
    }
    private byte[] sendACKCourseStat(short commend,short numCourse,String courseName, short seatsMax, short seatsCurrent, LinkedList<String> list) {
        byte[] send=sendACK_Short_String(commend,numCourse,courseName+' ');
        byte[] curr=(String.valueOf(seatsCurrent)+' ').getBytes();
        byte[] max=(String.valueOf(seatsMax)+' ').getBytes();
        send=unionByte(send,curr);
        send=unionByte(send,max);
        for(String name: list){
            byte[] nam=(name+'\0').getBytes();
            send=unionByte(send,nam);
        }
        return send;
    }


    private byte[] sendACKStringAndList(short commend, String name, LinkedList<Short> list) {
        byte[] send=sendACKOptionalString(commend,name);
        for (Short course: list){
            byte[] cours=(String.valueOf(course)+' ').getBytes();
            send=unionByte(send,cours);
        }
        return send;
    }

    private byte[] sendACKOptionalList(short commend, LinkedList<Short> list) {
        byte[] send =sendACK(commend);
        for (Short course: list){
            byte[] cours=(String.valueOf(course)+' ').getBytes();
            send=unionByte(send,cours);
        }
        return send;
    }
    private byte[] sendACKOptionalString(short commend, String name) {
        byte[] st=(name+"\0").getBytes();
        byte[] send =sendACK(commend);
        return unionByte(send,st);
    }
    private byte[] sendACK_Short_String(short commend,short numCourse, String name) {
        byte[] name1=(name+"\0").getBytes();
        byte[] num=(String.valueOf(numCourse)+' ').getBytes();
        byte[] send =sendACK(commend);
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
    private byte[] sendError(short commend){
        byte[] send=shortToBytes((short) 13);
        return unionByte(send,shortToBytes(commend));
    }

    private String[] bytesToString(byte[] msg){
        int start=2;
        int index=0;
        String[] str=new String[2];
        for(int i=2;i<msg.length;i++){
            if(msg[i]=='\0') { // if it is find '\0' namespace
                str[index]=new String(msg,start,i-start,StandardCharsets.UTF_8);
                start=i+1;
                index++;
            }
            if(index==str.length) str=Arrays.copyOf(str,str.length+1);
            if(i==msg.length-1 && msg[i]!=' ') str[index]=new String(msg,start,i+1-start,StandardCharsets.UTF_8);
        }
        return str;
    }

    public short bytesToShort(byte[] byteArr)
    {
        short result = (short)((byteArr[0] & 0xff) << 8);
        result += (short)(byteArr[1] & 0xff);
        return result;
    }
    public byte[] shortToBytes(short num)
    {
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte)((num >> 8) & 0xFF);
        bytesArr[1] = (byte)(num & 0xFF);
        return bytesArr;
    }
    public byte[] take2Bytes(byte[] msg,int i){
        byte[] bytesArr = new byte[2];
        bytesArr[0]=msg[0+i];
        bytesArr[1]=msg[1+i];
        return bytesArr;
    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }
}

