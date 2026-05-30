package com.vidlus.conce;

import java.io.File;
import java.nio.file.Files;

public enum Steps { 

    Upload(null, new ActUpload()), 
    Structure("S01 - Structure.txt", new ActStructure()), 
    Identify("S02 - Identify.txt", new ActIdentify()), 
    Organize("S03 - Organize.txt", new ActOrganize()), 
    Orderify("S04 - Orderify.txt", new ActClassify()),
    Classify("S05 - Classify.txt", new ActClassify()),
    Atomize("S06 - Atomize.txt", new ActAtomize()), 
    Questify("S07 - Questify.txt", new ActQuestify()), 
    Explains("S08 - Explains.txt", new ActExplains()),
    Didactic("S09 - Didactic.txt", new ActDidactic()),
    DoneAtNow(null, new ActDoneAtNow()),
    RevisedAtNow(null, new ActRevisedAtNow());

    private final String commandName;
    private final Act stepAct;

    private Steps(String commandName, Act stepAct) {
        this.commandName = commandName;
        this.stepAct = stepAct;
    }

    public String getCommandName() {
        return this.commandName;
    }

    public Act getAct() {
        return this.stepAct;
    }
    
    public File getCommandFile() {
        return new File(STEPFS_FOLDER, this.commandName);
    }

    public String getCommand() throws Exception {
        return Files.readString(getCommandFile().toPath());
    }

    public String getCommand(String withInsertion) throws Exception {
        return getCommand().replace("< INSERT >", withInsertion);
    }

    public String getCommand(String... withInsertions) throws Exception {
        var command = getCommand();
        for (var i = 0; i < withInsertions.length; i++) {
            command = command.replace("< INSERT " + (i + 1) + " >", withInsertions[i]);
        }
        return command;
    }

    public static final File STEPFS_FOLDER = new File("steps");

}
