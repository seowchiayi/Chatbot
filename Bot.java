package Bot;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.lang.Math;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class Bot {

    public Bot(){
        //clearDb();

    }

    public static String getReply(String message){
        ArrayList<String> posLst;
        Map<String,Double> map = new HashMap<>();
        String sen="";
        String res="";

        if(isGreeting(message)){
            return sendGreet(message);
        }
        else if(isNumber(message)){
            return calculator(message);
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
    public static ArrayList<String> tokenization(String userInput){
        userInput = lowerCase(userInput);
        String[] initSplit;
        initSplit = userInput.split(" ");
        ArrayList<String> finalSplit = new ArrayList<>();
        for(String w:initSplit){
            finalSplit.add(w);
        }
        return finalSplit;
    }

    //Converts user info and question into sentence without past tense/plural
    public static String stemming(String userInput){
        userInput = lowerCase(userInput);
        ArrayList<String> inputArr = tokenization(userInput);
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
                for (int i=0; i<inputArr.size();i++){
                    for(String w:arr){
                        //Converts past tense to present tense
                        if (w.equals(inputArr.get(i))){
                            inputArr.set(i,s.substring(0,s.indexOf(",")));
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
        result = result.replaceAll("\\p{P}", "");

        return result;

    }
    public static boolean isNumber(String userInput){
        ArrayList<String> quesArr = tokenization(userInput);
        for(String findDigit: quesArr) {
            if (findDigit.matches(".*\\d+.*")) {
                return true;

            }
        }
        return false;

    }

    public static String calculator(String userInput){
        Object result="";
        ArrayList<String> quesArr = tokenization(userInput);
        for(String findDigit: quesArr) {
            if (findDigit.matches(".*\\d+.*")) {
                ScriptEngineManager manager = new ScriptEngineManager();
                ScriptEngine engine = manager.getEngineByName("js");
                try {
                    String expression = findDigit;
                    result = engine.eval(expression);
                } catch(ScriptException se) {
                    se.printStackTrace();
                }

            }

        }
        return String.valueOf(result);
    }

    //Check if what user entered is an info. Only store info because tfidf only uses documents
    public static boolean isInfo(String userInput){
        String []questionWords = {"why","who","where","do","what"};
        ArrayList <String> arr = tokenization(userInput);
        for (String s:questionWords){
            if(arr.get(0).equals(s)){
                return false;
            }

        }
        return true;

    }

    //Detect intent if is a greeting
    public static boolean isGreeting(String userInput){
        String[] greet = {"hi","hello","who are you"};
        for(String w: greet){
            if(userInput.equals(w)){
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
            return "Good morning " + greet[idx];

        }
        else if(getHour>=12 && getHour<17){
            return "Good afternoon " + greet[idx];

        }
        else if (getHour>=17 && getHour<20){
            return "Good evening " + greet[idx];

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
    //Remove stopwords
    public static String removeStopwords(String ques){
        String getInfo;
        String result="";
        ArrayList<String> arr= tokenization(ques);
        try{
            Scanner input = new Scanner("/home/chiayi/IdeaProjects/CYChatbot/stopwords_eng.txt");
            File file = new File(input.nextLine());
            input = new Scanner(file);
            //Loop through database
            while(input.hasNextLine()){
                getInfo = input.nextLine();
                ques = stemming(ques);
                for(int i =0 ;i<arr.size();i++){
                    if(getInfo.equals(arr.get(i))){
                        arr.set(i,"");
                    }
                }

            }
        } catch (Exception e){
            e.printStackTrace();
        }
        for(int i =0 ;i<arr.size();i++){
            result += arr.get(i) + " ";
        }
        return result.replaceAll("\\s{2,}", " ").trim();

    }


    //Return correct respond to questions that are not even relevant to database
    public static boolean isRelevant(String ques){
        String getInfo = sortTfidfMap(getTFIDF(ques));
        boolean flag = false;
        ques = removeStopwords(stemming(ques));
        ArrayList<String> t = tokenization(ques);
        for(String a: t){
            if(getInfo.contains(a)){
                flag = true;
            }
            else{
                return false;
            }
        }

        return flag;

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
        ArrayList<String> quesArr = tokenization(ques);
        ArrayList<String> getInfoArr;
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
        //Split ques into words
        ArrayList<String> quesArr = tokenization(ques);

        ArrayList<String> getInfoArr;
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
            //Loop through documents
            while(input.hasNextLine()){
                getInfo = input.nextLine();

                //Example Question: who went to bangkok
                for(String s:quesArr){
                    //Split document into words
                    getInfoArr = tokenization(getInfo);
                    //Example Document: harry went to bangkok
                    for (String doc:getInfoArr){
                        if (doc.equals(s)){
                            tfCount +=1;
                        }
                    }
                    if(tfCount>=1){
                        storeTf.add(Math.log10(totalDoc/idfMap.get(s)+1)*tfCount/getInfoArr.size());
                    }

                    tfCount = 0;
                }
                for(double tfidf: storeTf){
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
    String sortTfidfMap(Map<K,V> map) {

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
        if(sortedEntries.size()>1){
            if(sortedEntries.get(0).getValue().equals(sortedEntries.get(1).getValue())){
                return sortedEntries.get(0).getKey().toString() + "and " + sortedEntries.get(1).getKey().toString() + "both";
            }
        }

        return sortedEntries.get(0).getKey().toString();
    }

    //Take highest tfidf and return part of speech of the words
    public static ArrayList<String> storePos(String userInput) {
        String getWordPos;
        ArrayList<String> matches=tokenization(userInput);
        try {
            Scanner input = new Scanner("/home/chiayi/IdeaProjects/CYChatbot/src/Bot/pos_tag.txt");
            File file = new File(input.nextLine());
            input = new Scanner(file);
            while (input.hasNextLine()) {
                getWordPos = input.nextLine();
                for (int i=0; i<matches.size();i++) {
                    if (getWordPos.substring(0, getWordPos.indexOf(" ")).equals(matches.get(i))) {
                        matches.set(i, matches.get(i)+" " + getWordPos.substring(getWordPos.indexOf(" ")+1,getWordPos.length()));
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        for(int i=0; i<matches.size();i++){
            matches.set(i,matches.get(i)+" ");
            if(matches.get(i).substring(matches.get(i).indexOf(" ")+1,matches.get(i).length()).equals("")){
                matches.set(i,matches.get(i)+ "NOUN ");
            }
        }
        return matches;
    }

    //Get respond by classifying situations asking where/who/why
    public static String getRespond(ArrayList<String> posLst, String ques,String userInput){
        ArrayList<String> quesArr = tokenization(ques);
        ArrayList<String> quesPos = storePos(ques);
        ArrayList<String> storePos = new ArrayList<>();
        String res="";
        String term,pos;
        boolean because=false;
        boolean conj = false;
        if (!isInfo(ques)){
            if(posLst.contains("and CONJ X ") && isRelevant(ques) && posLst.get(posLst.size()-1).contains("both DET ")){
                for(int i=0;i<posLst.size()-1;i++){
                    term = posLst.get(i).substring(0,posLst.get(i).indexOf(" ")+1);
                    res+=term;
                }
                return res;
            }
            else if(quesArr.get(0).equals("where") && isRelevant(ques)){
                for(String a: quesPos){
                    if(a.contains("ADP")){
                        storePos.add(a);
                    }
                }
                for(String adp: storePos){
                    for(int i=posLst.indexOf(adp); i<posLst.size();i++){
                        if(i+1<posLst.size()){
                            term = posLst.get(i+1).substring(0, posLst.get(i+1).indexOf(" ")+1);
                            pos = posLst.get(i+1).substring(posLst.get(i+1).indexOf(" ")+1,posLst.get(i+1).length());
                            if(i<posLst.size()){
                                if(!term.equals("to ") && pos.contains("NOUN ") || pos.contains("PRON") || pos.contains("ADJ")){
                                    res+=term;
                                }
                                else{
                                    return res;
                                }

                            }

                        }
                        else{
                            return res;
                        }


                    }

                }


            }

            else if(quesArr.get(0).equals("who") && isRelevant(ques)){
                for(String a:quesPos){
                    if(a.contains("VERB")){

                        storePos.add(a);
                    }
                }
                if(storePos.size()==1 && storePos.contains("is VERB X ")) {
                    for(int i=0; i<posLst.size(); i++){
                        res +=posLst.get(i).substring(0,posLst.get(i).indexOf(" ") + 1);
                    }
                    return res;
                } else{
                    for(String verb: storePos){
                        System.out.println("VERBBBB: " + verb);
                        for(int i=posLst.indexOf(verb); i>=0; i--){
                            if(i-1>=0){
                                pos = posLst.get(i-1).substring(posLst.get(i-1).indexOf(" ")+1,posLst.get(i-1).length());
                                term = posLst.get(i-1).substring(0, posLst.get(i-1).indexOf(" ")+1);
                                System.out.println("POS: " + pos);
                                System.out.println("TERM: " +term);
                                if(!term.contains("a ") && pos.contains("NOUN ") && !pos.contains("ADP ") && !posLst.get(i).substring(posLst.get(i).indexOf(" ")+1,posLst.get(i).length()).contains("ADP ")){
                                    res = term+res;
                                }
                            }
                            else if(ques.contains(res)){
                                String[] split = res.split(" ");
                                ArrayList<String> temp = storePos(split[split.length-1]);
                                for(int j=posLst.indexOf(temp.get(0))+1; j<posLst.size();j++){
                                    res+=posLst.get(j).substring(0,posLst.get(j).indexOf(" ")+1);
                                }
                                return res;

                            }
                            else{
                                return res;
                            }


                        }
                    }
                }

            }

            else if(quesArr.get(0).equals("why")){
                int start =0;
                for(int i=0; i<posLst.size();i++){
                    term = posLst.get(i).substring(0, posLst.get(i).indexOf(" "));
                    if(term.equals("because") || term.equals("as") || term.equals("so")){
                        because = true;
                        start = i;
                    }
                }
                if(because){
                    for(int i=0; i<posLst.size();i++){
                        term = posLst.get(i).substring(0, posLst.get(i).indexOf(" "));
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
            else if(quesArr.get(0).equals("do") || quesArr.get(0).equals("can")){
                if(isRelevant(ques)){
                    if(posLst.contains("not ADV ")){
                        if(ques.contains("not ")){
                            return "Yes !";
                        }
                        return "No. ";
                    }
                    if(ques.contains("not") && !posLst.contains("not ADV ")){
                        return "No !";
                    }
                    else{
                        return "Yes !";
                    }

                }
                else{
                    return "No.";
                }
            }
            else if(quesArr.get(0).equals("what") && isRelevant(ques)){
                String[] split = ques.split(" ");
                ArrayList<String> temp = storePos(split[split.length-1]);
                for(int j=posLst.indexOf(temp.get(0))+1; j<posLst.size();j++){
                    pos = posLst.get(j).substring(posLst.get(j).indexOf(" ")+1,posLst.get(j).length());
//                    if(!pos.contains("ADJ") && !pos.contains("ADP") &&!pos.contains("DET") && !pos.contains("X")){
                    res+=posLst.get(j).substring(0,posLst.get(j).indexOf(" ")+1);
//                    }

                }
                return res;

            }

            System.out.println(res);
            return "Sorry I don't know";

        } else {
            return "Thank you for your info~!";
        }

    }
    public static void main(String[]args){


    }

}
