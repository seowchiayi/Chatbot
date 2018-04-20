package Bot;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.*;
import java.lang.Math;

public class Bot {
    public Bot(){

    }

    public static String getReply(String message){
        String[]posLst;
        //String ques = "who run away from school";
        //clearDb();
        //writeToDb(stemming(message));
        System.out.println(1);
        //writeToDb(stemming(message));
        System.out.println(2);
        String sen = sortTfidfMap(getTFIDF(message));
        System.out.println(3);
        posLst = storePos(sen);
        System.out.println(4);
        String res = getRespond(posLst,message,sen);
        System.out.println(5);

        return res;
    }

    //Preprocessing: convert to lower case
    public static String lowerCase(String userInput){
        return userInput.toLowerCase();
    }

    //Preprocessing: Split sentence into an array of words
    public static String[] tokenization(String userInput){
        userInput = lowerCase(userInput);
        String[]arr;
        arr = userInput.split(" ");
        return arr;
    }

    //Converts user info and question into sentence without past tense/plural
    public static String stemming(String userInput){
        userInput = lowerCase(userInput);
        String [] inputArr = tokenization(userInput);
        String s;
        String[] arr;
        String result = "";
        try{
            Scanner input = new Scanner("/home/chiayi/IdeaProjects/CYChatbot/stemming.txt");
            File file = new File(input.nextLine());
            input = new Scanner(file);
            while(input.hasNextLine()){
                s = input.nextLine();
                arr = s.split(",");
                for (int i=0; i<inputArr.length;i++){

                    for(String w:arr){
                        //Converts past tense to present tense
                        if (w.equals(inputArr[i])){
                            inputArr[i] = s.substring(0,s.indexOf(","));
                        }
                        if(inputArr[i].length()>3){
                            //Removes plural words
                            if(inputArr[i].substring(inputArr[i].length()-1).equals("s")){
                                inputArr[i] = inputArr[i].substring(0,inputArr[i].length()-1);
                            }
                            //Removes tenses with past tense
                            else if(inputArr[i].substring(inputArr[i].length()-2,inputArr[i].length()).equals("ed")){
                                inputArr[i] = inputArr[i].substring(0,inputArr[i].length()-2);
                            }
                        }
                    }

                }

            }
        } catch (Exception e){
            e.printStackTrace();
        }

        for (String c: inputArr){
            result+=c + " ";
        }

        return result;

    }

    //Check if what user entered is an info. Only store info because tfidf only uses documents
    public static boolean isInfo(String userInput){
        String []questionWords = {"?","why","who","when","where","what","how"};
        for (String s:questionWords){
            if(userInput.contains(s)){
                return false;
            }

        }
        return true;

    }

    //Clear database everytime the program refreshes
    public static void clearDb(){
        File f = new File("database.txt");
        if(f.exists()) f.delete();
    }

    //Stores user info for tfidf
    public static String writeToDb(String userInput){
        if(isInfo(userInput)){
            try{
                BufferedWriter writer = new BufferedWriter(new FileWriter("database.txt", true));
                writer.write(userInput);
                writer.write("\n");
                writer.close();

            } catch (Exception e){
                e.printStackTrace();
            }

        }

        return "Thank you for your info";

    }

    //Get total number of lines in database
    public static int totalDoc(){
        int totalDoc = 0;
        String getInfo;

        try{
            Scanner input = new Scanner("/home/chiayi/IdeaProjects/CYChatbot/database.txt");
            File file = new File(input.nextLine());
            input = new Scanner(file);
            while(input.hasNextLine()){
                getInfo = input.nextLine();
                totalDoc+=1;


            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return totalDoc;
    }

    //Get Inverse Document Frequency (IDF) from user question (formula: total num of db lines/occurence of term in whole db)
    public static Map<String, Integer> getIdf(String ques){
        String[] quesArr = tokenization(ques);
        String[] getInfoArr;
        String getInfo;
        Map<String,Integer> idfMap = new HashMap<>();

        try{
            Scanner input = new Scanner("/home/chiayi/IdeaProjects/CYChatbot/database.txt");
            File file = new File(input.nextLine());
            input = new Scanner(file);
            while(input.hasNextLine()){
                getInfo = input.nextLine();
                getInfoArr = tokenization(getInfo);
                for (String doc:getInfoArr){
                    for (String s:quesArr){
                        if(doc.equals(s)){
                            if(idfMap.get(s)!=null){
                                idfMap.put(s,idfMap.get(s)+1);
                            }
                            else{
                                idfMap.put(s,1);
                            }
                        }
                    }

                }


            }
        } catch (Exception e){
            e.printStackTrace();
        }

        return idfMap;

    }

    //Calculate tfidf for user question for each document in database
    public static Map<String,Double> getTFIDF(String ques){
        int totalDoc = totalDoc();
        String[] quesArr = tokenization(ques);
        String[]getInfoArr;
        String getInfo;
        double tfCount = 0;
        double sum = 0;
        Map<String,Integer> idfMap = getIdf(ques);
        Map<String,Double> tfidfMap = new HashMap<>(100);
        ArrayList<Double> storeTf = new ArrayList<>(100);

        try{
            Scanner input = new Scanner("/home/chiayi/IdeaProjects/CYChatbot/database.txt");
            File file = new File(input.nextLine());
            input = new Scanner(file);
            while(input.hasNextLine()){
                getInfo = input.nextLine();
                for(String s:quesArr){
                    getInfoArr = tokenization(getInfo);
                    for (String doc:getInfoArr){
                        if (doc.equals(s)){
                            tfCount +=1;
                        }

                    }
                    if(tfCount>=1){
                        storeTf.add(Math.log10(totalDoc/idfMap.get(s))*tfCount/getInfoArr.length);
                    }

                    tfCount = 0;
                }
                for (double tfidf: storeTf){
                    sum+=tfidf;
                }
                tfidfMap.put(getInfo,sum);
                storeTf.clear();
                sum=0;


            }
        } catch (Exception e){
            e.printStackTrace();
        }

        return tfidfMap;
    }

    public static <K,V extends Comparable<? super V>>
    K sortTfidfMap(Map<K,V> map) {

        List<Map.Entry<K,V>> sortedEntries = new ArrayList<Map.Entry<K,V>>(map.entrySet());

        Collections.sort(sortedEntries,
                new Comparator<Map.Entry<K,V>>() {
                    @Override
                    public int compare(Map.Entry<K,V> e1, Map.Entry<K,V> e2) {
                        return e2.getValue().compareTo(e1.getValue());
                    }
                }
        );
        return sortedEntries.get(0).getKey();
    }

    //Take highest tfidf and return part of speech of the words
    public static String[] storePos(String userInput) {
        String getWordPos;
        String[] matches=tokenization(userInput);
        ArrayList<String> storeMatches = new ArrayList<>(100);

        try {
            Scanner input = new Scanner("/home/chiayi/IdeaProjects/CYChatbot/src/Bot/pos_tag.txt");
            File file = new File(input.nextLine());
            input = new Scanner(file);
            while (input.hasNextLine()) {
                getWordPos = input.nextLine();
                for (int i=0; i<matches.length;i++) {
                    if (getWordPos.substring(0, getWordPos.indexOf(" ")).equals(matches[i])) {
                        matches[i]= matches[i]+" " + getWordPos.substring(getWordPos.indexOf(" ")+1,getWordPos.length());
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return matches;
    }

    //Get respond by classifying situations asking where/who/why
    public static String getRespond(String[] posLst, String ques,String userInput){
        String[]quesArr = tokenization(ques);
        String res="";
        String term,pos;
        String checkQues = ques.substring(ques.indexOf(" ")+1,ques.length());

        for(String word: quesArr){
            if(word.equals("where")){
                for(int i=0; i<posLst.length;i++){
                    term = posLst[i].substring(0, posLst[i].indexOf(" "));
                    pos = posLst[i].substring(posLst[i].indexOf(" ")+1,posLst[i].length());
                    if(i>=1){
                        if(pos.contains("NOUN VERB") && (posLst[i-1].substring(posLst[i-1].indexOf(" ")+1,posLst[i-1].length())).equals("ADP")){
                            res=term;
                        }
                    }

                }
            }
            else if(word.equals("who")){
                if (checkQues==ques.substring(ques.indexOf("who")+4,ques.length())){
                    System.out.println("Sorry I don't understand");
                }
                else{
                    for(int i=0; i<posLst.length;i++){

                        pos = posLst[i].substring(posLst[i].indexOf(" ")+1,posLst[i].length());
                        if(i>=1){
                            term = posLst[i-1].substring(0, posLst[i-1].indexOf(" "));
                            if(pos.contains("VERB") && (posLst[i-1].substring(posLst[i-1].indexOf(" ")+1,posLst[i-1].length())).equals("NOUN VERB")){
                                res=term;
                            }
                        }

                    }

                }

            }
        }
        System.out.println(res);
        return res;
    }

    //private static String read = "Harry ran away from school because he wanted to go home";
    //private static String test = "Harry is tall";
    public static void main(String[]args){
        String[]posLst;
        String ques = "who run away from school";
        clearDb();
        //writeToDb(stemming(read));
        //writeToDb(stemming(test));
        String sen = sortTfidfMap(getTFIDF(ques));
        posLst = storePos(sen);
        getRespond(posLst,ques,sen);

    }

}
