package com.vidlus.conce;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

import com.vidlus.conce.desk.Replace;
import com.vidlus.conce.desk.ReplaceAutoOn;

import br.com.pointel.jarch.mage.WizObject;
import br.com.pointel.jarch.mage.WizProps;

public class Setup {

    public static void setBase(String base) {
        WizProps.setFolder(new File(base));
    }

    private static final String KEY_LAST_SELECTED_REF01 = "FRAME_CHARVS_KNOW_LAST_SELECTED_REF01";
    private static final String KEY_LAST_SELECTED_REF02 = "FRAME_CHARVS_KNOW_LAST_SELECTED_REF02";
    private static final String KEY_LAST_SELECTED_REF03 = "FRAME_CHARVS_KNOW_LAST_SELECTED_REF03";
    private static final String KEY_LAST_SELECTED_REF04 = "FRAME_CHARVS_KNOW_LAST_SELECTED_REF04";
    private static final String KEY_LAST_SELECTED_REF05 = "FRAME_CHARVS_KNOW_LAST_SELECTED_REF05";
    private static final String KEY_LAST_SELECTED_REF06 = "FRAME_CHARVS_KNOW_LAST_SELECTED_REF06";
    private static final String KEY_LAST_SELECTED_REF07 = "FRAME_CHARVS_KNOW_LAST_SELECTED_REF07";
    private static final String KEY_LAST_SELECTED_REF08 = "FRAME_CHARVS_KNOW_LAST_SELECTED_REF08";
    private static final String KEY_LAST_SELECTED_REF09 = "FRAME_CHARVS_KNOW_LAST_SELECTED_REF09";
    private static final String KEY_LAST_SELECTED_REF10 = "FRAME_CHARVS_KNOW_LAST_SELECTED_REF10";
    private static final String KEY_LAST_SELECTED_REF11 = "FRAME_CHARVS_KNOW_LAST_SELECTED_REF11";
    private static final String KEY_LAST_SELECTED_REF12 = "FRAME_CHARVS_KNOW_LAST_SELECTED_REF12";
    private static final String[] KEYS_LAST_SELECTED_REFS = new String[] {
            KEY_LAST_SELECTED_REF01,
            KEY_LAST_SELECTED_REF02,
            KEY_LAST_SELECTED_REF03,
            KEY_LAST_SELECTED_REF04,
            KEY_LAST_SELECTED_REF05,
            KEY_LAST_SELECTED_REF06,
            KEY_LAST_SELECTED_REF07,
            KEY_LAST_SELECTED_REF08,
            KEY_LAST_SELECTED_REF09,
            KEY_LAST_SELECTED_REF10,
            KEY_LAST_SELECTED_REF11,
            KEY_LAST_SELECTED_REF12
    };

    public static String[] getLastSelectedRefs() {
        var result = new String[KEYS_LAST_SELECTED_REFS.length];
        for (int i = 0; i < KEYS_LAST_SELECTED_REFS.length; i++) {
            result[i] = WizProps.get(KEYS_LAST_SELECTED_REFS[i], "");
        }
        return result;
    }

    public static void putSelectedRef(String refWithExtension) {
        int foundIndex = -1;
        for (int i = 0; i < KEYS_LAST_SELECTED_REFS.length; i++) {
            if (WizProps.get(KEYS_LAST_SELECTED_REFS[i], "").equals(refWithExtension)) {
                foundIndex = i;
                break;
            }
        }
        if (foundIndex == 0) {
            return;
        }
        int limit = (foundIndex == -1) ? KEYS_LAST_SELECTED_REFS.length - 1 : foundIndex;
        for (int i = limit; i > 0; i--) {
            var previous = WizProps.get(KEYS_LAST_SELECTED_REFS[i - 1], "");
            WizProps.set(KEYS_LAST_SELECTED_REFS[i], previous);
        }
        WizProps.set(KEYS_LAST_SELECTED_REFS[0], refWithExtension);
    }

    public static void clearLastSelectedRefs() {
        for (int i = 0; i < KEYS_LAST_SELECTED_REFS.length; i++) {
            WizProps.set(KEYS_LAST_SELECTED_REFS[i], "");
        }
    }

    public static final String KEY_GENAI_MODEL = "FRAME_SETUP_COMP_GENAI_MODEL";
    
    public static SetupGenaiModel getGenaiModel() {
        return SetupGenaiModel.values()[WizProps.get(KEY_GENAI_MODEL, 0)];
    }

    public static void setGenaiModel(SetupGenaiModel genaiModel) {
        WizProps.set(KEY_GENAI_MODEL, genaiModel.ordinal());
    }

    public static final String KEY_TALKER_KIND = "FRAME_SETUP_COMP_TALKER_KIND";
    
    public static SetupTalkerKind getTalkerKind() {
        return SetupTalkerKind.values()[WizProps.get(KEY_TALKER_KIND, 0)];
    }

    public static void setTalkerKind(SetupTalkerKind talkerKind) {
        WizProps.set(KEY_TALKER_KIND, talkerKind.ordinal());
    }

    public static final String KEY_REF_BASE_KIND = "FRAME_SETUP_COMP_REF_BASE_KIND";
    
    public static SetupRefBaseKind getRefBaseKind() {
        return SetupRefBaseKind.values()[WizProps.get(KEY_REF_BASE_KIND, 0)];
    }

    public static void setRefBaseKind(SetupRefBaseKind refBaseKind) {
        WizProps.set(KEY_REF_BASE_KIND, refBaseKind.ordinal());
    }

    public static final String KEY_OPENAI_MODEL = "FRAME_SETUP_COMP_OPENAI_MODEL";
    
    public static SetupOpenaiModel getOpenaiModel() {
        return SetupOpenaiModel.values()[WizProps.get(KEY_OPENAI_MODEL, 0)];
    }

    public static void setOpenaiModel(SetupOpenaiModel openaiModel) {
        WizProps.set(KEY_OPENAI_MODEL, openaiModel.ordinal());
    }

    public static final String KEY_SOUNDER_KIND = "FRAME_SETUP_COMP_SOUNDER_KIND";
    
    public static SetupSounderKind getSounderKind() {
        return SetupSounderKind.values()[WizProps.get(KEY_SOUNDER_KIND, 0)];
    }

    public static void setSounderKind(SetupSounderKind soundKind) {
        WizProps.set(KEY_SOUNDER_KIND, soundKind.ordinal());
    }

    public static final String KEY_BALCON_VOICE = "FRAME_SETUP_COMP_BALCON_VOICE";
    
    public static String getBalconVoice() {
        return WizProps.get(KEY_BALCON_VOICE, "Microsoft Maria Desktop");
    }

    public static void setBalconVoice(String balconVoice) {
        WizProps.set(KEY_BALCON_VOICE, balconVoice);
    }

    public static File getReplacesListFile() {
        return new File("replaces.ser");
    }

    public static void writeReplacesList(ArrayList<Replace> replaces) throws Exception {
        WizObject.write(getReplacesListFile(), replaces);
    }

    public static ArrayList<Replace> readReplacesList() throws Exception {
        var file = getReplacesListFile();
        if (!file.exists()) {
            return new ArrayList<>();
        }
        return (ArrayList<Replace>) WizObject.read(file);
    }

    public static ArrayList<Replace> getReplacesList(ReplaceAutoOn forStep) throws Exception {
        var result = new ArrayList<Replace>();
        for (var replace : readReplacesList()) {
            if (Objects.equals(replace.autoOn, ReplaceAutoOn.OnAllSteps) 
                    || Objects.equals(replace.autoOn, forStep)) {
                result.add(replace);
            }
        }
        return result;



    }
    
}
