package Bot;

import java.io.File;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Scanner;
import java.util.ArrayList;


public class PosTagging {
    //part of speech indicates how word function in meaning and grammatically
    /**
     * noun name of person, place, thing or idea
     * proper nouns start with captial letter, common nouns do not
     * nouns can be singular or plural
     * nouns show possession by adding 's
     * noun can be subject, direct object, indirect object, subject complement, object of a preposition
     */

    /**
     * pronoun used in place of noun
     * the girl (noun) --> she (pronoun)
     */

    /**
     * verb expresses action or being
     * 'she can sing' -> 'Sing' is the main verb, 'can' is the helping verb
     * verb must agree with its subject
     */

    /**
     * adjective modifies/describes noun or pronoun
     *  answer question of which one, what kind, how many
     */

    /**
     * adverb modifies/describes a verb/adjective
     * answer questions of when, where, how, why, what to what degree
     */

    /**
     * preposition is a word before noun/pronoun
     * form a phrase modifying another word in sentence
     * by,with,about,until,from
     */

    /**
     * conjunction joins words/phrases/clauses
     * and,but,or,while,because
     */

    /**
     * interjection expresses emotions
     * wow! oops! oh!
     */

    /**
     * FIND SUBJECT OF SENTENCE if you can find the verb
     */
    private static String wordPosArr[];
    private static String getWordPos;
    private static String[] matches;
    private static String read = "Harry runs away from school because he wants to go home";
    private static ArrayList<String> storeMatches = new ArrayList<>(100);

    public static void main(String[]args){
        try{
            Scanner input = new Scanner("/home/chiayi/IdeaProjects/CYChatbot/src/Bot/pos_tag.txt");
            File file = new File(input.nextLine());
            input = new Scanner(file);
            while(input.hasNextLine()){
                getWordPos = input.nextLine();
                wordPosArr = getWordPos.split("\n");
                matches = read.split(" ");

                for(String s:matches){
                    if (getWordPos.substring(0,getWordPos.indexOf(" ")).equals(s)){
                        storeMatches.add(getWordPos);
                    }
                }

            }
            for (String s: storeMatches){
                System.out.println(s);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }


}
