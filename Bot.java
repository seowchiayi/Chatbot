package Bot;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.lang.Math;

public class Bot {

    public Bot(){
        clearDb();

    }

    public static String getReply(String message){
        String[]posLst;
        Map<String,Double> map = new HashMap<>();
        String sen="";
        String res="";
        if(isGreeting(message)){
            return sendGreet(message);
        }
        else{
            message = stemming(message);
            System.out.println("Input: "+message);

            writeToDb(message);
            System.out.println("Get Tfidf sentence");
            map = getTFIDF(message);
            System.out.println(map);

            System.out.println("Get pos of sentence");
            sen = sortTfidfMap(map);

            posLst = storePos(sen);
            for(String a: posLst){
                System.out.println(a);
            }

            System.out.println("Get exact respond to what user asks");
            res = getRespond(posLst,message,sen);
            System.out.println(res);

            return res;

        }




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
//                        if(inputArr[i].length()>3){
//                            //Removes plural words
//                            if(inputArr[i].substring(inputArr[i].length()-1).equals("s")){
//                                inputArr[i] = inputArr[i].substring(0,inputArr[i].length()-1);
//                            }
//                            //Removes tenses with past tense
//                            else if(inputArr[i].substring(inputArr[i].length()-2,inputArr[i].length()).equals("ed")){
//                                inputArr[i] = inputArr[i].substring(0,inputArr[i].length()-2);
//                            }
//                        }
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
        String []questionWords = {"?","why","who","where"};
        for (String s:questionWords){
            if(userInput.contains(s)){
                return false;
            }

        }
        return true;

    }

    //Detect intent if is a greeting
    public static boolean isGreeting(String userInput){
        String[] greet = {"hi","hello","who are you"};
        for(String w: greet){
            if(userInput.contains(w)){
                return true;
            }
        }


        return false;


    }

    //Give respond to greeting
    public static String sendGreet(String userInput){
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date();
        Random rdn = new Random();

        String time = dateFormat.format(date);
        String[] greet = {"what's up","hello","what can I do for you"};
        int idx = rdn.nextInt(greet.length);

        int getHour = Integer.parseInt(time.substring(0,time.indexOf(":")));
        if(getHour>=6 && getHour<12){
            return "Good morning" + greet[idx];

        }
        else if(getHour>=12 && getHour<17){
            return "Good afternoon" + greet[idx];

        }
        else if (getHour>=17 && getHour<20){
            return "Good evening" + greet[idx];

        }
        else{
            return "Oh gosh it's late " + greet[idx];
        }


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


    //Return correct respond to questions that are not even relevant to database
    public static boolean isRelevant(String ques){
        boolean flag = true;
        String[] quesArr = tokenization(ques);

        String[] getInfoPosArr;
        String[] posArr = storePos(ques);
        String getInfo;
        String[]posLst;
        try{
            Scanner input = new Scanner("/home/chiayi/IdeaProjects/CYChatbot/database.txt");
            File file = new File(input.nextLine());
            input = new Scanner(file);
            while(input.hasNextLine()){
                getInfo = input.nextLine();
                //getInfoPosArr = storePos(input.nextLine());


//                for(String word:posArr){
//                    for(String term:getInfoPosArr){
//                        String a = word.substring(word.indexOf(" ")+1,word.length());
//                        String b = term.substring(term.indexOf(" ")+1,term.length());
//                        System.out.println(word);
//                        System.out.println(a);
//                        System.out.println(term);
//                        System.out.println(b);
//                        System.out.println("-------------------");
//                        if(word.equals(term)
//                                && a.equals(b)
//                                && a.equals("NOUN ") && b.equals("NOUN ")){
//
//                            return true;
//                        }
//
//                    }
//                }
                System.out.println(getInfo);
                System.out.println(stemming(ques));
                ques = stemming(ques);
                String []arr = tokenization(ques);
                String test="";
                for(int i =2 ;i<arr.length;i++){
                    test+=arr[i] + " ";
                }
                System.out.println(test);
                System.out.println(getInfo);
                if(getInfo.contains(test)){
                    return true;
                }




            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return false;

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
//                    System.out.println("Question Array: ");
//                    for (String x: quesArr){
//                        System.out.println(x);
//                    }
//                    System.out.println("Database Array: ");
//                    for (String y: getInfoArr){
//                        System.out.println(y);
//                    }
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
        //System.out.println(tfidfMap.values().toArray()[0], tfidfMap.values().toArray()[1]);
//        if(tfidfMap.values().toArray()[0].equals(0.0)){
//            return null;
//        }
//        else{
//
//        }
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
        System.out.println("Print sorted tfidfmap");
        System.out.println(sortedEntries);
        return sortedEntries.get(0).getKey();
    }

    //Take highest tfidf and return part of speech of the words
    public static String[] storePos(String userInput) {
        String getWordPos;
        String[] matches=tokenization(userInput);
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

        for(int i=0; i<matches.length;i++){
            matches[i]= matches[i] + " ";
        }

        return matches;
    }

    //Get respond by classifying situations asking where/who/why
    public static String getRespond(String[] posLst, String ques,String userInput){
        String[]quesArr = tokenization(ques);
        String res="";
        String term,pos;
        String checkQues = ques.substring(ques.indexOf("who")+4,ques.length());
        Boolean because=false;
        if (!isInfo(ques)){

            for(String word: quesArr){
                if(word.equals("where") && isRelevant(ques)){
                    for(int i=0; i<posLst.length;i++){
                        term = posLst[i].substring(0, posLst[i].indexOf(" "));
                        pos = posLst[i].substring(posLst[i].indexOf(" ")+1,posLst[i].length());
                        if(i>=1){
                            if(pos.equals("NOUN ") && (posLst[i-1].substring(posLst[i-1].indexOf(" ")+1,posLst[i-1].length())).contains("ADP")){
                                res=term;
                                return res;
                            }
                        }

                    }
                    return "Sorry I don't know";

                }
                else if(word.equals("who") && isRelevant(ques)){
                    for(int i=0; i<posLst.length;i++){
                        pos = posLst[i].substring(posLst[i].indexOf(" ")+1,posLst[i].length());
                        if(i>=1){
                            term = posLst[i-1].substring(0, posLst[i-1].indexOf(" "));
                            System.out.println("TERM: " + term);
                            if(pos.contains("VERB") && (posLst[i-1].substring(posLst[i-1].indexOf(" ")+1,posLst[i-1].length())).equals("NOUN ")){
                                res=term;

                            }
                        }
                    }
//                    checkQues = res + " " + checkQues;
//                    System.out.println("CHECK QUES: " + checkQues);
//                    System.out.println("USER INPUT: " + userInput);
//                    if (!checkQues.equals(userInput)){
//                        return "Sorry I don't understand";
//                    }
//                    else {
//                        return res;
//                    }
                    return res;

                }
                else if(word.equals("why")){
                    int start =0;
                    for(int i=0; i<posLst.length;i++){
                        term = posLst[i].substring(0, posLst[i].indexOf(" "));
                        if(term.equals("because")){
                            because = true;
                            start = i;
                        }
                    }
                    if(because){
                        for(int i=0; i<posLst.length;i++){
                            term = posLst[i].substring(0, posLst[i].indexOf(" "));
                            if(i>=start){
                                res += term + " ";

                            }
                        }
                        return res;

                    }
                    else{
                        return "Sorry the info you gave is insufficient";
                    }

                }
            }
            System.out.println(res);
            return "Sorry I don't know";

        } else {
            return "Thank you for your info~!";
        }

    }

    //private static String read = "Harry ran away from school because he wanted to go home";
    //private static String test = "Harry is tall";
    public static void main(String[]args){
        String[]posLst;
        String db = "harry ran away from school because he wanted to go home";
        String ques = "where did harry go";
        clearDb();
        writeToDb(stemming(db));
        //writeToDb(stemming(test));
//        String sen = sortTfidfMap(getTFIDF(ques));
//        posLst = storePos(sen);
//        getRespond(posLst,ques,sen);
        //System.out.println(isRelevant(ques));
        boolean flag = false;
        String [] t1 = {"wanted","go","home","susan"};
        String [] t2 = {"harry" ,"wanted", "to", "go" ,"home"};
        for (String a: t2){
            for(String b: t1){
                System.out.println(a);
                System.out.println(b);
                if(b.contains(a)){
                    flag = true;
                }
                else if(!b.contains(a)){
                    break;
                }
            }

        }
        System.out.println(flag);




    }

}
