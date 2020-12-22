package bgu.spl.net.api;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

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
    private LinkedList<Integer> CourseByOrder;
    private HashMap<String, String> loginStudent;
    private HashMap<String, String> loginAdmin;
    private HashMap<Integer, String> CoursesName;
    private HashMap<Integer, LinkedList<Integer>> KdamCorosesList;
    private HashMap<Integer, Integer> NumOfMaxStudent;
    private HashMap<String, LinkedList<Integer>> StatStudent;
    private HashMap<Integer, LinkedList<String>> StatCourse;
    private HashMap<String, Boolean> online;

    //to prevent user from creating new Database
    private Database() {
        loginStudent = new HashMap<>();
        loginAdmin = new HashMap<>();
        CoursesName = new HashMap<>();
        KdamCorosesList = new HashMap<>();
        NumOfMaxStudent = new HashMap<>();
        StatStudent = new HashMap<>();
        StatCourse = new HashMap<>();
        CourseByOrder = new LinkedList<>();
        online = new HashMap<>();
        initialize("/Courses.txt");
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
                int numOfCourse = 0;
                int lastLineEnd = 0;
                for (int i = 0; i < line.length(); i++) {
                    if (line.charAt(i) == '|') {
                        if (numOfDownLine == 0) {
                            numOfCourse = Integer.parseInt(line.substring(0, i));
                            CourseByOrder.add(numOfCourse);
                            numOfDownLine++;
                            lastLineEnd = i;
                        } else if (numOfDownLine == 1) {
                            CoursesName.put(numOfCourse, line.substring(lastLineEnd+1, i));
                            numOfDownLine++;
                            lastLineEnd = i;
                        } else if (numOfDownLine == 2) {
                            LinkedList<Integer> list = new LinkedList();
                            int start = lastLineEnd + 2;
                            if(line.charAt(start)!=']'){
                                for (int j = lastLineEnd + 2; j < i; j++) {
                                    if (line.charAt(j) == ',') {
                                        list.add(Integer.parseInt(line.substring(start, j)));
                                        start = j + 1;
                                    } else if (line.charAt(j) == ']')
                                        list.add(Integer.parseInt(line.substring(start, j)));
                                }}
                            KdamCorosesList.put(numOfCourse, list);
                            NumOfMaxStudent.put(numOfCourse, Integer.parseInt(line.substring(i + 1)));
                            StatCourse.put(numOfCourse, new LinkedList<>());
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
    public boolean AdminRegister(String name, String pass) {
        if (loginAdmin.containsKey(name)) return false;
        loginAdmin.put(name, pass);
        online.put(name, false);
        return true;
    }

    /**
     * for STUDENTREG
     */
    public boolean studentRegister(String name, String pass) {
        if (loginStudent.containsKey(name)) return false;
        loginStudent.put(name, pass);
        online.put(name, false);
        StatStudent.put(name, new LinkedList<>());
        return true;
    }

    /**
     * for LOGIN
     */
    public boolean LoginStudent(String name, String pass) {
        if (loginStudent.get(name) == pass && !online.get(name)) {
            online.replace(name, true);
            return true;
        }
        return false;
    }
    public boolean LoginAdmin(String name, String pass) {
        if (loginAdmin.get(name) == pass && !online.get(name)) {
            online.put(name, true);
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
    public synchronized boolean CourseRegister(int numOfCourse, String name) {
        LinkedList<Integer> kdam=KdamNeeded(numOfCourse);
        boolean AllKdam=true;
        for(Integer course: kdam){
           if( !StatStudent.get(name).contains(course)) AllKdam=false;
        }
        if (StatCourse.get(numOfCourse).size() < NumOfMaxStudent.get(numOfCourse) && AllKdam) {
            StatCourse.get(numOfCourse).add(name);
            StatStudent.get(name).add(numOfCourse);
            return true;
        } else return false;
    }

    /**
     * for KDAMCHECK
     */
    public LinkedList<Integer> KdamNeeded(int numOfCourse) {
        return KdamCorosesList.get(numOfCourse);
    }

    /**
     * for COURSESTAT
     */
    public String courseName(int numOfCourse){return CoursesName.get(numOfCourse); }
    public int SeatsMax(int numOfCourse) { return NumOfMaxStudent.get(numOfCourse); }
    public synchronized int SeatsCurrent(int numOfCourse) { return StatCourse.get(numOfCourse).size(); }
    public LinkedList<String> StudentsRegisterToCourse(int numOfCourse) { return StatCourse.get(numOfCourse); }

    /**
     * for STUDENTSTAT
     */
    public synchronized LinkedList<Integer> StudentStat(String name) { return StatStudent.get(name); }

    /**
     * for ISREGISTERED
     */
    public synchronized String isRegistered(int numOfCourse, String name) {
        if (StatCourse.get(numOfCourse).contains(name)) return "REGISTERED";
        return "NOT REGISTERED";
    }

    /**
     * for UNREGISTER
     */
    public synchronized boolean unregistered(int numOfCourse,String name){
        if(StatCourse.get(numOfCourse).contains(name)){
            StatCourse.get(numOfCourse).remove(name);
            StatStudent.get(name).remove(numOfCourse);
            return true;
        }
        return false;
    }

    /**
     * for MYCOURSES
     */
    public synchronized LinkedList<Integer> myCourses(String name){
        return StatStudent.get(name);
    }
}