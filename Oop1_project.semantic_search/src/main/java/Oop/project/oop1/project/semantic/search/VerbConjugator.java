package Oop.project.oop1.project.semantic.search;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


/**
 * get base form, past tense and present participle of a verb
 */
public class VerbConjugator extends QueryImprover {


    static String[] irregularVerbs = {
            "be:was/were:being", "become:became:becoming", "feel:felt:feeling", "break:broke:breaking", "choose:chose:choosing", "do:did:doing",
            "eat:ate:eating", "find:found:finding", "fly:flew:flying", "get:got:getting", "give:gave:giving", "go:went:going", "have:had:having",
            "know:knew:knowing", "learn:learned:learning", "leave:left:leaving", "lose:lost:losing", "make:made:making", "meet:met:meeting",
            "pay:paid:paying", "put:put:putting", "read:read:reading", "run:ran:running", "say:said:saying", "see:saw:seeing", "sell:sold:selling", "send:sent:sending",
            "speak:spoke:speaking", "take:took:taking", "think:thought:thinking", "understand:understood:understanding", "wear:wore:wearing", "write:wrote:writing",
            "begin:began:beginning", "bend:bent:bending", "bid:bid:bidding", "bind:bound:binding", "bite:bit:biting", "bleed:bled:bleeding", "blow:blew:blowing",
            "break:broke:breaking", "bring:brought:bringing", "build:built:building", "burn:burnt:burning", "burst:burst:bursting", "buy:bought:buying",
             "catch:caught:catching", "choose:chose:choosing", "come:came:coming", "cost:cost:costing", "cut:cut:cutting", "dig:dug:digging",
            "draw:drew:drawing", "drink:drank:drinking", "drive:drove:driving", "eat:ate:eating", "fall:fell:falling", "feed:fed:feeding", "feel:felt:feeling",
            "fight:fought:fighting", "find:found:finding", "fly:flew:flying", "forget:forgot:forgetting", "freeze:froze:freezing", "get:got:getting",
            "give:gave:giving", "go:went:going", "grow:grew:growing", "hang:hung:hanging", "have:had:having", "hear:heard:hearing", "hide:hid:hiding",
            "hit:hit:hitting", "hold:held:holding", "hurt:hurt:hurting", "keep:kept:keeping", "know:knew:knowing", "lay:laid:laying", "lead:led:leading",
            "learn:learned:learning", "leave:left:leaving", "lend:lent:lending", "let:let:letting", "lose:lost:losing", "make:made:making", "mean:meant:meaning",
            "meet:met:meeting", "pay:paid:paying", "put:put:putting",
            "quit:quit:quitting", "read:read:reading", "ride:rode:riding", "ring:rang:rining", "rise:rose:rising", "run:ran:running", "say:said:saying",
            "see:saw:seeing", "sell:sold:selling", "send:sent:sending", "set:set:setting", "shake:shook:shaking", "shine:shone:shining", "shoot:shot:shooting",
            "show:showed:showing", "shut:shut:shutting", "sing:sang:singing", "sink:sank:sinking", "sit:sat:sitting", "sleep:slept:sleeping", "speak:spoke:speaking",
            "spend:spent:spending", "spin:spun:spinning", "spread:spread:spreading", "stand:stood:standing", "steal:stole:stealing", "stick:stuck:sticking", "strike:struck:striking",
            "swear:swore:swearing", "swim:swam:swimming", "swing:swung:swinging", "take:took:taking", "teach:taught:teaching", "tear:tore:tearing", "tell:told:telling",
            "think:thought:thinking", "throw:threw:throwing", "understand:understood:understanding", "wake:woke:waking", "wear:wore:wearing", "win:won:winning", "write:wrote:writing"
    };

    /**
     * get base form of a verb
     * @param verb to get base form of
     * @return base form
     */
    public static String getBaseForm(String verb) {
        //Irregular verbs
        for (String irregularVerb : irregularVerbs) {
            String[] parts = irregularVerb.split(":");
            String[] irregularForms = parts[1].split("/");
            for (String irregularForm : irregularForms) {
                if (irregularForm.equals(verb)) {
                    return parts[0];
                }
            }
        }


        if (verb.endsWith("ing")) {
            // Remove "ing" for present participle
            String baseForm = verb.substring(0, verb.length() - 3);

            if (baseForm.length() >= 2 && baseForm.charAt(baseForm.length() - 1) == baseForm.charAt(baseForm.length() - 2)) {
                return baseForm.substring(0, baseForm.length() - 1); // Remove doubled consonant
            } else if (baseForm.endsWith("i")) {
                return baseForm.substring(0, baseForm.length() - 1) + "y"; // Change "i" to "y"
            } else {
                return baseForm;
            }


        } else if (verb.endsWith("ed")) {
            String baseForm = verb.substring(0, verb.length() - 2);

            if (baseForm.length() >= 2 && baseForm.charAt(baseForm.length() - 1) == baseForm.charAt(baseForm.length() - 2)) {
                return baseForm.substring(0, baseForm.length() - 1); // Remove doubled consonant
            } else if (baseForm.endsWith("i")) {
                return baseForm.substring(0, baseForm.length() - 1) + "y"; // Change "i" to "y"
            } else {
                return baseForm;
            }


        } else if (verb.endsWith("s")) {

            if (verb.endsWith("ies")) {
                return verb.substring(0, verb.length() - 3) + "y";

            } else if (verb.endsWith("es")) {
                return verb.substring(0, verb.length() - 2);

            } else {
                return verb.substring(0, verb.length() - 1);
            }

        } else {
            return verb;
        }
    }

    /**
     * get past tense of a verbe
     * @param verb to get past tense of
     * @return past tense
     */
    public static String getPastTense(String verb) {
        for (String irregularVerb : irregularVerbs) {
            String[] parts = irregularVerb.split(":");
            if (parts[0].equals(verb)) {
                return parts[1].split("/")[0];
            }
        }


        if (verb.endsWith("e")) {
            return verb + "d";

        } else if (verb.endsWith("y") && !isVowel(verb.charAt(verb.length() - 2))) {
            return verb.substring(0, verb.length() - 1) + "ied";

        } else {
            return verb + "ed";
        }
    }


    /**
     * get present participle of a verb
     * @param verb to get present participle of
     * @return present participle
     */
    public static String getPresentParticiple(String verb) {
        for (String irregularVerb : irregularVerbs) {
            String[] parts = irregularVerb.split(":");
            String baseForm = parts[0];
            String presentParticiple = parts[2];
            if (baseForm.equals(verb)) {
                return presentParticiple;
            }
        }

        if (verb.endsWith("ie")) {
            return verb.substring(0, verb.length() - 2) + "ying";

        } else if (verb.endsWith("e")) {
        return verb.substring(0, verb.length() - 1) + "ing";

        } else if (verb.length() >= 3 && isConsonant(verb.charAt(verb.length() - 1)) && isVowel(verb.charAt(verb.length() - 2)) &&
                isConsonant(verb.charAt(verb.length() - 3)) && !(verb.endsWith("w") || verb.endsWith("x") || verb.endsWith("y")))
        {
            return verb + verb.charAt(verb.length() - 1) + "ing";

        } else {
            return verb + "ing";
        }
    }

    /**
     * check is a letter is consonant
     * @param ch character to check if it is a consonant
     * @return true or false
     */
    public static boolean isConsonant(char ch) {
        char lowerCh = Character.toLowerCase(ch);
        return lowerCh != 'a' && lowerCh != 'e' && lowerCh != 'i' && lowerCh != 'o' && lowerCh != 'u';
    }

    //using opennlp pos to check if the word is a verb then doing the conjugation
    private static final String POS_MODEL_PATH = "C:\\Users\\Lenovo\\Downloads\\Oop1_project.semantic_search\\en-pos-maxent.bin";
    private static POSModel posModel;
    private static POSTaggerME posTagger;

    static {
        try {
            InputStream inputStream = new FileInputStream(POS_MODEL_PATH);
            posModel = new POSModel(inputStream);
            posTagger = new POSTaggerME(posModel);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param word word to check if it is a verb
     * @return true or false
     */
    public static boolean isVerb(String word) {
        String[] tokens = {word};
        String[] tags = posTagger.tag(tokens);
        return tags[0].startsWith("VB"); // VB* tags represent verbs in OpenNLP
    }

    /**
     *
     * @param query verb
     * @return list of base form, past simple, present participle
     */
    public List<String> getResult(String query) {

        if (isVerb(query)) {

            String baseForm = getBaseForm(query);
            String past = getPastTense(baseForm);
            String present = getPresentParticiple(baseForm);
            List<String> result = new ArrayList<>();
            result.add(baseForm);
            result.add(past);
            result.add(present);
            return result;
        }
        else {
            return null;
        }
    }


}
