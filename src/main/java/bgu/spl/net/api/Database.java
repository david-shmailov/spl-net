package bgu.spl.net.api;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
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
    private static class SingletonHolder{
        private static Database database=new Database();
    }
    private HashMap<String,String> login;
    private HashMap<Integer,String> CoursesName;
    private HashMap<Integer, LinkedList<Integer>> KdamCorosesList;
    private HashMap<Integer,Integer> NumOfMaxStudent;
    private HashMap<Integer,Integer> NumOfCurrentStudent;
    private HashMap<String,Integer[]> StatStudent;


    //to prevent user from creating new Database
    private Database() {
       login=new HashMap<>();
       CoursesName=new HashMap<>();
       KdamCorosesList=new HashMap<>();
       NumOfCurrentStudent=new HashMap<>();
       NumOfMaxStudent=new HashMap<>();
       StatStudent=new HashMap<>();
       initialize("/Coursrs.txt");
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
        try(BufferedReader reader=new BufferedReader(new FileReader(coursesFilePath))){
            String line = reader.readLine();
            while (line!=null){
                int numOfDownLine=0;
                int numOfCourse=0;
                int lastLineEnd=0;
                for(int i=0;i<line.length();i++){
                    if(line.charAt(i)=='|'){
                        if(numOfDownLine==0){
                            numOfCourse=Integer.parseInt(line.substring(0,i-1));
                            numOfDownLine++;
                            lastLineEnd=i;
                        }
                        else if(numOfDownLine==1){
                            CoursesName.put(numOfCourse,line.substring(lastLineEnd,i-1));
                            numOfDownLine++;
                            lastLineEnd=i;
                        }
                        else if(numOfDownLine==2){
                            LinkedList<Integer> list=new LinkedList();
                            int start=lastLineEnd+2;
                            for(int j=lastLineEnd+2;j<i;j++){
                                if(line.charAt(j)==','){
                                   list.add(Integer.parseInt(line.substring(start,j-1)));
                                   start=j+1;
                                }
                                else if(line.charAt(j)==']')
                                    list.add(Integer.parseInt(line.substring(start,j-1)));
                            }
                            KdamCorosesList.put(numOfCourse,list);
                           NumOfMaxStudent.put(numOfCourse,Integer.parseInt(line.substring(i+1)));
                           NumOfCurrentStudent.put(numOfCourse,0);
                           break;
                        }

                    }

                }
            }


        }catch (IOException e){e.printStackTrace();}
        // TODO: implement
        return false;
    }


}