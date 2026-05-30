package com.vidlus.conce.desk;

import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.apache.commons.io.FilenameUtils;

import com.vidlus.conce.Ref;
import com.vidlus.conce.RefDatex;
import com.vidlus.conce.Setup;
import com.vidlus.conce.Steps;
import com.vidlus.conce.WorkRef;

import br.com.pointel.jarch.desk.DButton;
import br.com.pointel.jarch.desk.DColPane;
import br.com.pointel.jarch.desk.DFrame;
import br.com.pointel.jarch.desk.DPane;
import br.com.pointel.jarch.desk.DRowPane;
import br.com.pointel.jarch.desk.DSplitter;
import br.com.pointel.jarch.mage.WizBytes;
import br.com.pointel.jarch.mage.WizFile;
import br.com.pointel.jarch.mage.WizGUI;
import br.com.pointel.jarch.mage.WizUtilDate;

public class Desk extends DFrame {

    private final JButton buttonSetup = new JButton("@");
    private final JButton buttonBaseSelect = new JButton("♦");
    private final JButton buttonBaseOpen = new JButton("*");
     private final JComboBox<String> comboBase = new JComboBox<>();
    private final JButton buttonBaseAdd = new JButton("+");
    private final JButton buttonBaseDel = new JButton("-");
    private final DRowPane rowBase = new DRowPane().insets(2)
            .growNone().put(buttonSetup)
            .growNone().put(buttonBaseSelect)
            .growNone().put(buttonBaseOpen)
            .growHorizontal().put(comboBase)
            .growNone().put(buttonBaseAdd)
            .growNone().put(buttonBaseDel);
    
    private final JButton buttonSelectRef = new JButton("&");
    private final JButton buttonLastSelected = new JButton("%");
    private final JButton buttonSelectedOpen = new JButton("*");
    private final JTextField fieldSelectedRefWithExtension = new JTextField();
    private final DefaultComboBoxModel<String> modelActChoose = new DefaultComboBoxModel<>();
    private final JComboBox<String> comboActChoose = new JComboBox<>(modelActChoose);
    private final JButton buttonActExecute = new JButton(">");
    private final JButton buttonStepOpen = new JButton("*");
    private final DRowPane rowActs = new DRowPane().insets(2)
            .growNone().put(buttonSelectRef)
            .growNone().put(buttonLastSelected)
            .growNone().put(buttonSelectedOpen)
            .growHorizontal().put(fieldSelectedRefWithExtension)
            .growHorizontal().put(comboActChoose)
            .growNone().put(buttonActExecute)
            .growNone().put(buttonStepOpen);

    private final DButton buttonMemoaWrite = new DButton("Write")
            .onAction(this::buttonMemoaWriteActionPerformed);
    private final TextEditor textMemoaEditor = new TextEditor()
            .addButton(buttonMemoaWrite);

    private final JTextArea textView = new JTextArea();
    private final JScrollPane scrollView = new JScrollPane(textView);

    private final DSplitter splitterBody = new DSplitter()
            .vertical().top(textMemoaEditor).bottom(scrollView)
            .divider(0.3f)
            .name("splitterClassGroup");

    private final DPane paneBody = new DColPane()
            .growHorizontal().put(rowBase)
            .growHorizontal().put(rowActs)
            .growBoth().put(splitterBody)
            .borderEmpty(7);

    public Desk() {
        super();
        initComponents();
    }
    
    private void initComponents() {
        body(paneBody);
        exitOnClose();

        var comboExit = new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                Setup.setBase(getSelectedBase());
            }
        };
        onFirstActivated(e -> {
            loadBases();
            comboBase.addFocusListener(comboExit);
            comboBase.getEditor().getEditorComponent().addFocusListener(comboExit);
        });
        onClosing(e -> saveBases());

        buttonSetup.setToolTipText("Setup");
        buttonSetup.addActionListener(this::buttonSetupActionPerformed);
        buttonBaseSelect.setToolTipText("Select Base Folder");
        buttonBaseSelect.addActionListener(this::buttonBaseSelectActionPerformed);
        buttonBaseOpen.setToolTipText("Open Base Folder");
        buttonBaseOpen.addActionListener(this::buttonBaseOpenActionPerformed);
        comboBase.setEditable(true);
        comboBase.addActionListener(e -> Setup.setBase(getSelectedBase()));
        buttonBaseAdd.setToolTipText("Add Base");
        buttonBaseAdd.addActionListener(this::comboBaseAddActionPerformed);
        buttonBaseDel.setToolTipText("Del Base");
        buttonBaseDel.addActionListener(this::comboBaseDelActionPerformed);

        buttonSelectRef.setToolTipText("Select Reference to Act");
        buttonSelectRef.addActionListener(this::buttonSelectRefActionPerformed);
        buttonLastSelected.setToolTipText("Select Reference from Last Selected");
        buttonLastSelected.addActionListener(this::buttonLastSelectedActionPerformed);
        buttonSelectedOpen.setToolTipText("Open Selected Reference");
        buttonSelectedOpen.addActionListener(this::buttonSelectedOpenActionPerformed);
        fieldSelectedRefWithExtension.setEditable(false);
        comboActChoose.setEditable(false);
        for (var step : Steps.values()) {
            modelActChoose.addElement(step.name());
        }
        buttonActExecute.setToolTipText("Execute Act on Reference");
        buttonActExecute.addActionListener(this::buttonActExecuteActionPerformed);
        buttonStepOpen.setToolTipText("Open the Step Command");
        buttonStepOpen.addActionListener(this::buttonStepOpenActionPerformed);

        textView.setEditable(false);
    }

    private void buttonSetupActionPerformed(ActionEvent evt) {
        new SetupDesk().setVisible(true);
    }

    private void buttonBaseSelectActionPerformed(ActionEvent evt) {
        var selected = new File(getSelectedBase());
        selected = WizFile.openDir(selected);
        if (selected != null) {
            setSelectedBase(selected.getAbsolutePath());
        }
    }

    private void buttonBaseOpenActionPerformed(ActionEvent evt) {
        try {
            var selected = new File(getSelectedBase());
            WizGUI.open(selected);
        } catch (Exception e) {
            WizGUI.showError(e);
        }
    }

    private void comboBaseAddActionPerformed(ActionEvent evt) {
        comboBase.addItem(getSelectedBase());
    }
    
    private void comboBaseDelActionPerformed(ActionEvent evt) {
        comboBase.removeItem(getSelectedBase());
    }

    private void buttonSelectRefActionPerformed(ActionEvent evt) {
        try {
            if (lastSelectedFile == null) {
                lastSelectedFile = getBaseFolder();
            }
            var selectedFile = WizFile.openFile(lastSelectedFile);
            if (selectedFile != null) {
                selectRef(selectedFile);
            }
        } catch (Exception ex) {
            WizGUI.showError(ex);
        }
    }

    private void buttonLastSelectedActionPerformed(ActionEvent evt) {
        new LastSelectedDesk(this).setVisible(true);
    }

    private void buttonSelectedOpenActionPerformed(ActionEvent evt) {
        try {
            if (workRef == null) {
                return;
            }
            WizGUI.exploreAndSelect(workRef.sourceFile);
        } catch (Exception e) {
            WizGUI.showError(e);
        }
    }

    private void buttonActExecuteActionPerformed(ActionEvent evt) {
        try {
            var step = getSelectedStep();
            if (step == null) {
                throw new Exception("Action not selected.");
            }
            if (step == Steps.Structure) {
                step.getAct().execute(new WorkRef(getBaseFolder(), null, null, null, null, null));
                return; 
            }
            if (workRef == null) {
                throw new Exception("Reference not selected.");
            }
            step.getAct().execute(workRef);
        } catch (Exception e) {
            WizGUI.showError(e);
        }
    }

    private void buttonStepOpenActionPerformed(ActionEvent evt) {
        try {
            var selectedStep = getSelectedStep();
            if (selectedStep == null) {
                return;
            }
            if (selectedStep.getCommandName() == null) {
                return;
            }
            WizGUI.open(selectedStep.getCommandFile());
        } catch (Exception e) {
            WizGUI.showError(e);
        }
    }

    private void buttonMemoaWriteActionPerformed(ActionEvent evt) {
        if (workRef == null) {
            return;
        }
        try {
            workRef.ref.memoa.text = textMemoaEditor.getValue().trim();
            workRef.ref.props.memoedAt = WizUtilDate.formatDateMach(new Date());
            workRef.write();
        } catch (Exception ex) {
            WizGUI.showError(ex);
        }    
    }

    private transient File lastSelectedFile = null;
    private transient WorkRef workRef = null;

    public void selectRef(File selectFile) throws Exception {
        var hashMD5 = "& " + WizBytes.getMD5(selectFile);
        var refFile = getBaseRefFile(hashMD5 + ".md");
        var extension = FilenameUtils.getExtension(selectFile.getName());
        var refWithExtension = hashMD5 + "." + extension;
        var sourceFile = getBaseRefFile(refWithExtension);
        if (!sourceFile.exists()) {
            if (WizGUI.showConfirm("Selected reference not found in the base. Do you wanna to move it inside?")) {
                Files.move(selectFile.toPath(), sourceFile.toPath());
                WizGUI.showInfo("Selected reference moved to the base.");
            } else if (WizGUI.showConfirm("Selected reference not found in the base. Do you wanna to copy it to the base?")) {
                Files.copy(selectFile.toPath(), sourceFile.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
                WizGUI.showInfo("Selected reference copied to the base.");
            } else {
                throw new Exception("You must have the selected reference in the base.");
            }
        }
        Ref ref;
        if (!refFile.exists()) {
            ref = new Ref();
            ref.props.hashMD5 = hashMD5;
            ref.props.createdAt = WizUtilDate.formatDateMach(new Date());
            var source = "Fonte: [[" + refWithExtension + "]]";
            if (!ref.memoa.contains(source)) {
                ref.memoa.append(source);
            }
        } else {
            ref = RefDatex.read(refFile);
        }
        var named = "Nomeado: " + selectFile.getName();
        if (!ref.memoa.contains(named)) {
            ref.memoa.appendOnTop(named);
        }
        RefDatex.write(ref, refFile);
        fieldSelectedRefWithExtension.setText(refWithExtension);
        SwingUtilities.updateComponentTreeUI(this);
        Setup.putSelectedRef(refWithExtension);
        lastSelectedFile = selectFile;
        workRef = new WorkRef(getBaseFolder(), ref, refFile, sourceFile, refWithExtension, this::updateStatus);
        updateStatus();
    }

    public void selectRef(String refWithExtension) throws Exception {
        var refFile = getBaseRefFile(FilenameUtils.getBaseName(refWithExtension) + ".md");
        var sourceFile = getBaseRefFile(refWithExtension);
        if (!sourceFile.exists()) {
            throw new Exception("Selected reference not found in the base.");
        }
        Ref ref;
        if (!refFile.exists()) {
            var hashMD5 = "& " + WizBytes.getMD5(sourceFile);
            ref = new Ref();
            ref.props.hashMD5 = hashMD5;
            ref.props.createdAt = WizUtilDate.formatDateMach(new Date());
            RefDatex.write(ref, refFile);
        } else {
            ref = RefDatex.read(refFile);
        }
        fieldSelectedRefWithExtension.setText(refWithExtension);
        SwingUtilities.updateComponentTreeUI(this);
        Setup.putSelectedRef(refWithExtension);
        lastSelectedFile = sourceFile;
        workRef = new WorkRef(getBaseFolder(), ref, refFile, sourceFile, refWithExtension, this::updateStatus);
        updateStatus();
    }

    public void updateStatus() {
        textMemoaEditor.setValue(workRef.ref.memoa.text);
        var start = textView.getSelectionStart();
        var end = textView.getSelectionEnd();
        textView.setText(RefDatex.getRefSource(workRef.ref, false));
        textView.setSelectionStart(start);
        textView.setSelectionEnd(end);
    }

    public File getBaseRefFile(String refWithExtension) {
        var baseRefsFolder = getBaseRefsFolder();
        if (!baseRefsFolder.exists()) {
            baseRefsFolder.mkdirs();
        }
        var baseRefFolder = new File(baseRefsFolder, refWithExtension.substring(0, 4));
        if (!baseRefFolder.exists()) {
            baseRefFolder.mkdirs();
        }
        return new File(baseRefFolder, refWithExtension);
    }

    public File getBaseFolder() {
        return new File(getSelectedBase());
    }

    public File getBaseRefsFolder() {
        return new File(getBaseFolder(), "+ Refs");
    }

    public String getSelectedBase() {
        var selected = comboBase.getSelectedItem();
        return selected == null ? "" : selected.toString();
    }

    public void setSelectedBase(String base) {
        comboBase.setSelectedItem(base);
    }

    public Steps getSelectedStep() {
        if (comboActChoose.getSelectedItem() == null) {
            return null;
        }
        return Steps.valueOf(comboActChoose.getSelectedItem().toString());
    }

    private void loadBases() {
        try {
            var file = new File("bases.ini");
            if (!file.exists()) {
                return;
            }
            var lines = Files.readAllLines(file.toPath());
            for (var line : lines) {
                if (!line.isBlank()) {
                    comboBase.addItem(line.trim());
                }
            }
            comboBase.setSelectedIndex(0);
        } catch (Exception e) {
            WizGUI.showError(e);
        }
    }

    private void saveBases() {
        try {
            var bases = new ArrayList<String>();
            var selected = getSelectedBase();
            if (selected != null && !selected.isBlank()) {
                bases.add(selected);
            }
            for (int i = 0; i < comboBase.getItemCount(); i++) {
                var item = comboBase.getItemAt(i);
                if (item != null && !item.isBlank() && !item.equals(selected)) {
                    bases.add(item);
                }
            }
            Files.write(new File("bases.ini").toPath(), bases);
        } catch (Exception e) {
            WizGUI.showError(e);
        }
    }

}
