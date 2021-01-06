package bgu.spl.net.api;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Vector;

/**
 * Passive object representing the Database where all courses and users are stored.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You can add private fields and methods to this class as you see fit.
 */
public class Database {
    private static class SingletonHolder {
        private static Database database = new Database();
    }
    private Vector<Short> CourseByOrder;
    private HashMap<String, String> loginStudent;
    private HashMap<String, String> loginAdmin;
    private HashMap<Short, String> CoursesName;
    private HashMap<String, Short> CoursesNum;
    private HashMap<Short, Vector<Short>> KdamCoursesList;
    private HashMap<Short, Short> NumOfMaxStudent;
    private HashMap<String, Vector<String>> StatStudent;
    private HashMap<Short, Vector<String>> StatCourse;
    private HashMap<String, Boolean> online;

    //to prevent user from creating new Database
    private Database() {
        loginStudent = new HashMap<>();
        loginAdmin = new HashMap<>();
        CoursesName = new HashMap<>();
        CoursesNum =new HashMap<>();
        KdamCoursesList = new HashMap<>();
        NumOfMaxStudent = new HashMap<>();
        StatStudent = new HashMap<>();
        StatCourse = new HashMap<>();
        CourseByOrder = new Vector<>();
        online = new HashMap<>();
        initialize("Courses.txt");
    }

    /**
     * Retrieves the single instance of this class.
     */
    public static Database getInstance() {
        return SingletonHolder.database;
    }

    /**
     * loades the courses from the file path specified
     * into the Database, returns true if successful.
     */
    boolean initialize(String coursesFilePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(coursesFilePath))) {
            String line = reader.readLine();
            while (line != null) {
                int numOfDownLine = 0;
                short numOfCourse = 0;
                int lastLineEnd = 0;
                for (int i = 0; i < line.length(); i++) {
                    if (line.charAt(i) == '|') {
                        if (numOfDownLine == 0) {
                            numOfCourse = Short.parseShort(line.substring(0, i));
                            CourseByOrder.add(numOfCourse);
                            numOfDownLine++;
                            lastLineEnd = i;
                        } else if (numOfDownLine == 1) {
                            CoursesName.put(numOfCourse, line.substring(lastLineEnd+1, i));
                            CoursesNum.put(line.substring(lastLineEnd+1, i),numOfCourse);
                            numOfDownLine++;
                            lastLineEnd = i;
                        } else if (numOfDownLine == 2) {
                            Vector<Short> list = new Vector<>();
                            int start = lastLineEnd + 2;
                            if(line.charAt(start)!=']'){
                                for (int j = lastLineEnd + 2; j < i; j++) {
                                    if (line.charAt(j) == ',') {
                                        list.add(Short.parseShort(line.substring(start, j)));
                                        start = j + 1;
                                    } else if (line.charAt(j) == ']')
                                        list.add(Short.parseShort(line.substring(start, j)));
                                }}
                            KdamCoursesList.put(numOfCourse, list);
                            NumOfMaxStudent.put(numOfCourse,  Short.parseShort(line.substring(i + 1)));
                            StatCourse.put(numOfCourse, new Vector<>());
                            break;
                        }
                    }
                }
                line=reader.readLine();
            } return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * for ADMINREG
     */
    public synchronized boolean AdminRegister(String name, String pass) {
        if (loginStudent.containsKey(name) || loginAdmin.containsKey(name)) return false;
        loginAdmin.put(name, pass);
        online.put(name, false);
        return true;
    }

    /**
     * for STUDENTREG
     */
    public synchronized boolean studentRegister(String name, String pass) {
        if (loginStudent.containsKey(name) || loginAdmin.containsKey(name)) return false;
        loginStudent.put(name, pass);
        online.put(name, false);
        StatStudent.put(name, new Vector<>());
        return true;
    }

    /**
     * for LOGIN
     */
    public boolean isStudent(String name){return loginStudent.containsKey(name);}
    public synchronized boolean LoginStudent(String name, String pass) {
        if (loginStudent.get(name).equals(pass) && !online.get(name)) {
            online.replace(name, true);
            return true;
        }
        return false;
    }
    public synchronized boolean LoginAdmin(String name, String pass) {
        if (loginAdmin.get(name).equals(pass) && !online.get(name)) {
            online.replace(name, true);
            return true;
        }
        return false;
    }

    /**
     * for LOGOUT
     */
    public void Logout(String name) {
        online.replace(name, false);
    }

    /**
     * for COURSEREG
     */
    public synchronized boolean CourseRegister(short numOfCourse, String name) {
        if(CoursesName.containsKey(numOfCourse)){
        Vector<Short> kdam=KdamNeeded(numOfCourse);
        boolean AllKdam=true;
        for(Short course: kdam){
           if( !StatStudent.get(name).contains(CoursesName.get(course))) AllKdam=false;
        }
        if (StatCourse.get(numOfCourse).size() < NumOfMaxStudent.get(numOfCourse) && AllKdam) {
            StatCourse.get(numOfCourse).add(name);
            StatStudent.get(name).add(CoursesName.get(numOfCourse));
            return true;
        }}  return false;
    }

    /**
     * for KDAMCHECK
     */
    public Vector<Short> KdamNeeded(short numOfCourse) {
        return KdamCoursesList.get(numOfCourse);
    }

    /**
     * for COURSESTAT
     */
    public boolean courseExist(short numOfCourse){return CoursesName.containsKey(numOfCourse); }
    public String courseName(short numOfCourse){return CoursesName.get(numOfCourse); }

    public short SeatsMax(short numOfCourse) { return NumOfMaxStudent.get(numOfCourse); }

    public synchronized short SeatsCurrent(short numOfCourse) { return (short) StatCourse.get(numOfCourse).size(); }

    public Vector<String> StudentsRegisterToCourse(short numOfCourse) {
        StatCourse.get(numOfCourse).sort(Comparator.naturalOrder());
        return StatCourse.get(numOfCourse);
    }

    /**
     * for STUDENTSTAT
     */
    public synchronized Vector<Short> StudentStat(String name) {
        if (StatStudent.containsKey(name)) {
            Vector<String> na = StatStudent.get(name);
            Vector<Short> send = new Vector<>();
            for (int i = 0; i < CourseByOrder.size(); i++) {
                String course = CoursesName.get(CourseByOrder.get(i));
                if (na.contains(course)) send.add(CourseByOrder.get(i));
            }
            return send;
        }
        return null;
    }

    /**
     * for ISREGISTERED
     */
    public boolean IsCourseExist(short numOfCourse){return CourseByOrder.contains(numOfCourse);}
    public String isRegistered(short numOfCourse, String name) {
        if (StatCourse.get(numOfCourse).contains(name)) return "REGISTERED";
        return "NOT REGISTERED";
    }

    /**
     * for UNREGISTER
     */
    public synchronized boolean unregistered(short numOfCourse,String name){
        if(StatCourse.get(numOfCourse).contains(name)){
            StatCourse.get(numOfCourse).remove(name);
            StatStudent.get(name).remove(CoursesName.get(numOfCourse));
            return true;
        }
        return false;
    }

    /**
     * for MYCOURSES
     */
    public synchronized Vector<Short> myCourses(String name){
        Vector<String> na=StatStudent.get(name);
        Vector<Short> send=new Vector<>();
        for(String a:na){
            send.add(CoursesNum.get(a));
        }
        return send;
    }
}