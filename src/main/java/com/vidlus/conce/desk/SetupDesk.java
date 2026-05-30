package com.vidlus.conce.desk;

import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import com.vidlus.conce.Setup;
import com.vidlus.conce.SetupGenaiModel;
import com.vidlus.conce.SetupRefBaseKind;
import com.vidlus.conce.SetupSounderKind;
import com.vidlus.conce.SetupTalkerKind;

import br.com.pointel.jarch.desk.DFrame;
import br.com.pointel.jarch.mage.WizString;

public class SetupDesk extends DFrame {

    private final JPanel panelBody = new JPanel();
    private final JScrollPane scrollBody = new JScrollPane(panelBody);

    private final JLabel labelGenaiModel = new JLabel("Genai Model:");
    private final DefaultComboBoxModel<String> modelGenaiModel = new DefaultComboBoxModel<>(WizString.getFromEnum(SetupGenaiModel.class));
    private final JComboBox<String> comboGenaiModel = new JComboBox<>(modelGenaiModel);

    private final JLabel labelTalkerKind = new JLabel("Talker Kind:");
    private final DefaultComboBoxModel<String> modelTalkerKind = new DefaultComboBoxModel<>(WizString.getFromEnum(SetupTalkerKind.class));
    private final JComboBox<String> comboTalkerKind = new JComboBox<>(modelTalkerKind);

    private final JLabel labelRefBaseKind = new JLabel("RefBase Kind:");
    private final DefaultComboBoxModel<String> modelRefBaseKind = new DefaultComboBoxModel<>(WizString.getFromEnum(SetupRefBaseKind.class));
    private final JComboBox<String> comboRefBaseKind = new JComboBox<>(modelRefBaseKind);

    private final JLabel labelSounderKind = new JLabel("Sounder Kind:");
    private final DefaultComboBoxModel<String> modelSounderKind = new DefaultComboBoxModel<>(WizString.getFromEnum(SetupSounderKind.class));
    private final JComboBox<String> comboSounderKind = new JComboBox<>(modelSounderKind);

    private final JLabel labelBalconVoice = new JLabel("Balcon Voice:");
    private final JTextField textBalconVoice = new JTextField();
    
    public SetupDesk() {
        super("Setup");
        initComponents();
    }

    private void initComponents() {
        comboGenaiModel.setName("GenaiModel");
        comboTalkerKind.setName("TalkerKind");
        comboRefBaseKind.setName("RefBaseKind");
        comboSounderKind.setName("SounderKind");
        textBalconVoice.setName("BalconVoice");

        panelBody.setBorder(BorderFactory.createEmptyBorder(7, 7, 7, 7));

        panelBody.setLayout(new GridLayout(0, 2, 2, 2));
        panelBody.add(labelGenaiModel);
        panelBody.add(comboGenaiModel);
        panelBody.add(labelTalkerKind);
        panelBody.add(comboTalkerKind);
        panelBody.add(labelRefBaseKind);
        panelBody.add(comboRefBaseKind);
        panelBody.add(labelSounderKind);
        panelBody.add(comboSounderKind);
        panelBody.add(labelBalconVoice);
        panelBody.add(textBalconVoice);

        setContentPane(scrollBody);
        setLocationRelativeTo(null);
        pack();
    }

}
