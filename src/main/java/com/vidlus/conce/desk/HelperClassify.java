package com.vidlus.conce.desk;

import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import com.vidlus.conce.CKUtils;
import com.vidlus.conce.Setup;
import com.vidlus.conce.Steps;
import com.vidlus.conce.WorkRef;

import br.com.pointel.jarch.desk.DButton;
import br.com.pointel.jarch.desk.DCheckEdit;
import br.com.pointel.jarch.desk.DColPane;
import br.com.pointel.jarch.desk.DComboEdit;
import br.com.pointel.jarch.desk.DEdit;
import br.com.pointel.jarch.desk.DFrame;
import br.com.pointel.jarch.desk.DIntegerField;
import br.com.pointel.jarch.desk.DPane;
import br.com.pointel.jarch.desk.DPopup;
import br.com.pointel.jarch.desk.DRowPane;
import br.com.pointel.jarch.desk.DScroll;
import br.com.pointel.jarch.desk.DSplitter;
import br.com.pointel.jarch.desk.DText;
import br.com.pointel.jarch.mage.WizGUI;
import br.com.pointel.jarch.mage.WizRand;
import br.com.pointel.jarch.mage.WizUtilDate;

public class HelperClassify extends DFrame {

    private final DButton buttonClear = new DButton("Clear")
            .onAction(this::buttonClearActionPerformed);
    private final DButton buttonAskOrderify = new DButton("Ask Orders")
            .onAction(this::buttonAskOrderifyActionPerformed);
    private final DButton buttonAskClassify = new DButton("Ask Classes")
            .onAction(this::buttonAskClassifyActionPerformed);
    private final DButton buttonPaste = new DButton("Ʇ")
            .onAction(this::buttonPasteActionPerformed);
    private final DPopup popupParse = new DPopup()
            .item("Orderify", this::buttonParseOrderifyActionPerformed)
            .item("Classify", this::buttonParseClassifyActionPerformed);
    private final DButton buttonParse = new DButton("Parse")
            .popup(popupParse);
    private final DPopup popupBring = new DPopup()
            .item("Orderify", this::buttonBringOrderifyActionPerformed)
            .item("Classify", this::buttonBringClassifyActionPerformed);
    private final DButton buttonBring = new DButton("Bring")
            .popup(popupBring);
    
    private final DPane paneAskActs = new DRowPane().insets(2)
        .growNone().put(buttonClear)
        .growHorizontal().put(buttonAskOrderify)
        .growHorizontal().put(buttonAskClassify)
        .growNone().put(buttonPaste)
        .growNone().put(buttonParse)
        .growNone().put(buttonBring);

    private final TextEditor textAsk = new TextEditor();
     
    private final DPane paneAsk = new DColPane().insets(2)
            .growHorizontal().put(paneAskActs)
            .growBoth().put(textAsk);

    private final DButton buttonOrdIn = new DButton("OrdIn")
            .onAction(this::buttonOrdInActionPerformed);
    private final DButton buttonOrdBy = new DButton("OrdBy")
            .onAction(this::buttonOrdByActionPerformed);
    private final DButton buttonSet = new DButton("Set")
            .onAction(this::buttonSetActionPerformed);
    private final DComboEdit<String> comboGroup = new DComboEdit<String>()
            .onAction(this::comboGroupActionPerformed);
    private final DEdit<Boolean> checkAutoSave = new DCheckEdit()
            .name("AutoSave");
    private final DButton buttonSave = new DButton("Save")
            .onAction(this::buttonSaveActionPerformed);
    private final DButton buttonWrite = new DButton("Write")
            .onAction(this::buttonWriteActionPerformed);
    private final DPane paneGroupActs = new DRowPane().insets(2)
            .growNone().put(buttonOrdIn)
            .growNone().put(buttonOrdBy)
            .growNone().put(buttonSet)
            .growHorizontal().put(comboGroup)
            .growNone().put(checkAutoSave)
            .growNone().put(buttonSave)
            .growNone().put(buttonWrite);

    private final DEdit<Integer> fieldClassOrder = new DIntegerField()
            .cols(4).horizontalAlignmentCenter()
            .onFocusLost(this::callSaveOnFocusLost);
    private final TextEditor fieldClassTitle = new TextEditor()
            .onFocusLost(this::callSaveOnFocusLost);
    private final DSplitter splitterClass = new DSplitter()
            .horizontal().left(fieldClassOrder).right(fieldClassTitle)
            .divider(0.2f)
            .name("splitterClass");

    private final DText textTitration = new DText().editable(false);
    private final DScroll scrollTitration = new DScroll(textTitration);
    private final DText textTopics = new DText().editable(false);
    private final DScroll scrollTopics = new DScroll(textTopics);
    private final DSplitter splitterGroup = new DSplitter()
            .vertical().top(scrollTitration).bottom(scrollTopics)
            .divider(0.5f)
            .name("splitterGroup");

    private final DSplitter splitterClassGroup = new DSplitter()
            .vertical().top(splitterClass).bottom(splitterGroup)
            .divider(0.3f)
            .name("splitterClassGroup");

    private final DPane paneGroup = new DColPane().insets(2)
            .growHorizontal().put(paneGroupActs)
            .growBoth().put(splitterClassGroup);

    private final DSplitter splitterBody = new DSplitter()
            .horizontal().left(paneAsk).right(paneGroup)
            .divider(0.5f)
            .name("splitterBody")
            .borderEmpty(7);


    private final WorkRef workRef;

    
    public HelperClassify(WorkRef workRef) {
        super("Helper Classify");
        this.workRef = workRef;
        body(splitterBody);
        comboGroup.clear();
        for (int i = 0; i < workRef.ref.groups.size(); i++) {
            comboGroup.add("Group " + String.format("%02d", i + 1));
        }
        onFirstActivated(e -> buttonBringClassifyActionPerformed(null));
    }

    private void buttonClearActionPerformed(ActionEvent e) {
        for (var group : workRef.ref.groups) {
            group.clearClassified();
        }
        comboGroupActionPerformed(e);
    }

    private volatile AskOrderifyThread askOrderifyThread = null;

    private void buttonAskOrderifyActionPerformed(ActionEvent e) {
        if (askOrderifyThread != null) {
            askOrderifyThread.stop = true;
            askOrderifyThread = null;
            buttonAskOrderify.setText("Ask Orders");
        } else {
            askOrderifyThread = new AskOrderifyThread();
            askOrderifyThread.start();
            buttonAskOrderify.setText("Asking...");
        }
    }

    private volatile AskClassifyThread askClassifyThread = null;

    private void buttonAskClassifyActionPerformed(ActionEvent e) {
        if (askClassifyThread != null) {
            askClassifyThread.stop = true;
            askClassifyThread = null;
            buttonAskClassify.setText("Ask");
        } else {
            askClassifyThread = new AskClassifyThread();
            askClassifyThread.start();
            buttonAskClassify.setText("Asking...");
        }
    }

    private void buttonPasteActionPerformed(ActionEvent e) {
        textAsk.edit().clear();
        textAsk.edit().paste();
    }

    private void buttonParseOrderifyActionPerformed(ActionEvent e) {
        try {
            var source = textAsk.edit().getValue().trim();
            if (source.isBlank()) {
                return;
            }
            for (var replace : Setup.getReplacesList(ReplaceAutoOn.OnOrderify)) {
                source = replace.apply(source);
            }
            textAsk.edit().setValue(source);
            var index = 0;
            var matcher = Pattern.compile("\\[(\\d+)\\]").matcher(source);
            while (matcher.find()) {
                if (index >= workRef.ref.groups.size()) {
                    break;
                }
                workRef.ref.groups.get(index).order = matcher.group(1);
                index++;
            }
            comboGroupActionPerformed(e);
        } catch (Exception ex) {
            WizGUI.showError(ex);
        }
    }

    private void buttonParseClassifyActionPerformed(ActionEvent e) {
        try {
            var source = textAsk.edit().getValue().trim();
            if (source.isBlank()) {
                return;
            }
            for (var replace : Setup.getReplacesList(ReplaceAutoOn.OnClassify)) {
                source = replace.apply(source);
            }
            textAsk.edit().setValue(source);
            var index = 0;
            var orders = List.copyOf(getOrders());
            var start = source.indexOf("[[");
            while (start > -1) {
                if (index >= orders.size()) {
                    break;
                }
                var end = source.indexOf("]]", start);
                if (end > -1) {
                    var classification = source.substring(start + 2, end).trim();
                    if (!classification.startsWith("-")) {
                        classification = "-" + classification;
                    }
                    classification = CKUtils.cleanFileName(classification);
                    var order = orders.get(index).toString();
                    for (var group : workRef.ref.groups) {
                        if (order.equals(group.order)) {
                            group.classification = classification;
                        }
                    }
                    start = source.indexOf("[[", end);
                    index++;
                } else {
                    break;
                }
            }
            comboGroupActionPerformed(e);
        } catch (Exception ex) {
            WizGUI.showError(ex);
        }
    }

    private void buttonBringOrderifyActionPerformed(ActionEvent e) {
        var builder = new StringBuilder();
        for (var group : workRef.ref.groups) {
            var order = group.order;
            if (order == null) {
                order = "";
            }
            builder.append("[");
            builder.append(order);
            builder.append("]\n\n");
        }
        textAsk.setValue(builder.toString());
    }

    private void buttonBringClassifyActionPerformed(ActionEvent e) {
        var map = new LinkedHashMap<Integer, String>();
        for (var group : workRef.ref.groups) {
            if (group.order == null || group.order.isBlank()) {
                continue;
            }
            if (group.classification == null || group.classification.isBlank()) {
                continue;
            }
            try {
                var order = Integer.parseInt(group.order);
                map.put(order, group.classification);
            } catch (Exception ex) {}
        }
        var builder = new StringBuilder();
        for (var order : map.entrySet()) {
            builder.append("[[");
            builder.append(order.getValue());
            builder.append("]]\n\n");
        }
        textAsk.setValue(builder.toString());
    }

    private void buttonOrdInActionPerformed(ActionEvent e) {
        var ordersIn = WizGUI.showInput("Group Orders In:", "4");
        if (ordersIn == null || ordersIn.isBlank()) {
            return;
        }
        createOrdersIn(Integer.parseInt(ordersIn));
        comboGroupActionPerformed(e);
    }

    private void buttonOrdByActionPerformed(ActionEvent e) {
        var ordersBy = WizGUI.showInput("Group Orders By:", "3");
        if (ordersBy == null || ordersBy.isBlank()) {
            return;
        }
        createOrdersBy(Integer.parseInt(ordersBy));
        comboGroupActionPerformed(e);
    }

    private void buttonSetActionPerformed(ActionEvent e) {
        var index = comboGroup.selectedIndex();
        if (index > -1) {
            fieldClassTitle.setValue(textAsk.edit().selectedText().trim());
            saveIfAutoSave(e != null ? e.getSource() : null);
        }
    }

    private void comboGroupActionPerformed(ActionEvent e) {
        var index = comboGroup.selectedIndex();
        if (index == -1 || index >= workRef.ref.groups.size()) {
            fieldClassTitle.setValue("");
            textTitration.setValue("");
            textTopics.setValue("");
            return;
        }
        var group = workRef.ref.groups.get(index);
        Integer orderInt = null;
        try {
            orderInt = Integer.parseInt(group.order.trim());
        } catch (Exception ex) {}
        fieldClassOrder.setValue(orderInt);
        fieldClassTitle.setValue(group.classification);
        textTitration.setValue(group.titration);
        textTopics.setValue(group.topics);
    }

    private void buttonSaveActionPerformed(ActionEvent e) {
        var index = comboGroup.selectedIndex();
        if (index == -1) {
            return;
        }
        var group = workRef.ref.groups.get(index);
        var orderStr = "";
        try {
            orderStr = fieldClassOrder.getValue().toString();
        } catch (Exception ex) {}
        group.order = orderStr;
        group.classification = fieldClassTitle.getValue().trim();
    }

    private void buttonWriteActionPerformed(ActionEvent e) {
        try {
            workRef.ref.props.classifiedAt = WizUtilDate.formatDateMach(new Date());
            for (var group : workRef.ref.groups) {
                group.hierarchy = getHierarchy(group.classification);
                group.writeClassification(workRef.baseFolder);
            }
            workRef.write();
            WizGUI.close(this);
        } catch (Exception ex) {
            WizGUI.showError(ex);
        }
    }

    private void callSaveOnFocusLost(FocusEvent e) {
        saveIfAutoSave(e != null ? e.getSource() : null);
    }

    private void saveIfAutoSave(Object source) {
        if (Boolean.TRUE.equals(checkAutoSave.value())) {
            buttonSaveActionPerformed(new ActionEvent(source, ActionEvent.ACTION_PERFORMED, "AutoSave"));
        }
    }

    private String getHierarchy(String ofClassification) {
        var builder = new StringBuilder("[[index]]");
        if (ofClassification == null || ofClassification.isBlank()) {
            return builder.toString();
        }
        var parts = ofClassification.split("\\-");
        var currentPath = new StringBuilder();
        for (var p : parts) {
            var part = p.trim();
            if (part.isBlank()) {
                continue;
            }
            var formattedPart = "- " + part;
            if (!currentPath.isEmpty()) {
                currentPath.append("/");
            }
            currentPath.append(formattedPart);
            builder.append(" [[").append(currentPath)
                   .append("/").append(formattedPart)
                   .append("]]");
        }
        return builder.toString();
    }

    private String getOrderifyInsertion() {
        var result = new StringBuilder();
        var first = true;
        for (var group : workRef.ref.groups) {
            if (first) {
                first = false;
            } else {
                result.append("\n\n---\n\n");
            }
            result.append(group.titration.trim());
            result.append("\n\n");
            result.append(group.topics.trim());
        }
        return result.toString();
    }

    private String[] getClassifyInsertions() throws Exception {
        return new String[] {
            getClassifyInsertionClasses(),
            getClassifyInsertionTopics(),
        };
    }

    private String getClassifyInsertionClasses() throws Exception {
        var classes = CKUtils.putBrackets(CKUtils.getAllClassifications(workRef.baseFolder));
        while (classes.size() < 64) {
            classes.add(classifyExamples[WizRand.getInt(classifyExamples.length)]);
        }
        return String.join("\n", classes);
    }

    private String getClassifyInsertionTopics() {
        var result = new StringBuilder();
        var first = true;
        var orders = getOrders();
        for (var order : orders) {
            if (first) {
                first = false;
            } else {
                result.append("\n\n---\n\n");
            }
            result.append(getClassifyInsertion(order));
        }
        return result.toString();
    }

    private String getClassifyInsertion(Integer of) {
        var result = new StringBuilder();
        for (var group : workRef.ref.groups) {
            if (group.order == null || group.order.isBlank()) {
                continue;
            }
            if (!group.order.equals(of.toString())) {
                continue;
            }
            result.append(group.titration.trim());
            result.append("\n\n");
            result.append(group.topics.trim());
            result.append("\n\n");
        }
        return result.toString();
    }

    private Set<Integer> getOrders() {
        var result = new LinkedHashSet<Integer>();
        for (var group : workRef.ref.groups) {
            if (group.order == null || group.order.isBlank()) {
                continue;
            }
            try {
                group.order = group.order.trim();
                var order = Integer.parseInt(group.order);
                result.add(order);
            } catch (Exception ex) {}
        }
        if (!result.isEmpty()) {
            return result;
        } else {
            return createOrdersBy(3);
        }
    }

    private Set<Integer> createOrdersIn(int ordIn) {
        var result = new LinkedHashSet<Integer>();
        if (ordIn < 1) {
            ordIn = 1;
        }
        var total = workRef.ref.groups.size();
        var perGroup = total / ordIn;
        var extra = total % ordIn;
        var order = 1;
        var currentCount = 0;
        var currentLimit = perGroup + (extra > 0 ? 1 : 0);
        if (extra > 0) {
            extra--;
        }
        for (var group : workRef.ref.groups) {
            group.order = String.valueOf(order);
            result.add(order);
            currentCount++;
            if (currentCount >= currentLimit && order < ordIn) {
                order++;
                currentCount = 0;
                currentLimit = perGroup + (extra > 0 ? 1 : 0);
                if (extra > 0) {
                    extra--;
                }
            }
        }
        return result;
    }

    private Set<Integer> createOrdersBy(int ordBy) {
        var result = new LinkedHashSet<Integer>();
        var iOrdBy = ordBy;
        if (iOrdBy < 1) {
            iOrdBy = 1;
        }
        var size = (int) Math.ceil(workRef.ref.groups.size() / (double) iOrdBy);
        if (workRef.ref.groups.size() % iOrdBy == 1) {
            size--;
        }
        if (size < 1) {
            size = 1;
        }
        Integer order = 1;
        int count = 0;
        for (var group : workRef.ref.groups) {
            group.order = order.toString();
            if (!result.contains(order)) {
                result.add(order);
            }
            count++;
            if (count % iOrdBy == 0 && order < size) {
                order++;
            }
        }
        return result;
    }

    private class AskOrderifyThread extends Thread {

        public volatile boolean stop = false;

        public AskOrderifyThread() {
            super("Asking Orderify");
        }

        @Override
        public void run() {
            try {
                var result = workRef.talkWithBase(Steps.Orderify.getCommand(getOrderifyInsertion()));
                if (stop) {
                    return;
                }
                SwingUtilities.invokeLater(() -> {
                    textAsk.setValue(result);
                    textAsk.edit().selectionStart(0);
                    textAsk.edit().selectionEnd(0);
                });
            } catch (Exception ex) {
                WizGUI.showError(ex);
            } finally {
                if (askOrderifyThread == this) {
                    askOrderifyThread = null;
                    SwingUtilities.invokeLater(() -> buttonAskOrderify.setText("Ask Orders"));
                }
            }
        }
    }

    private class AskClassifyThread extends Thread {

        public volatile boolean stop = false;

        public AskClassifyThread() {
            super("Asking Classify");
        }

        @Override
        public void run() {
            try {
                var result = workRef.talkWithBase(Steps.Classify.getCommand(getClassifyInsertions()));
                if (stop) {
                    return;
                }
                SwingUtilities.invokeLater(() -> {
                    textAsk.setValue(result);
                    textAsk.edit().selectionStart(0);
                    textAsk.edit().selectionEnd(0);
                });
            } catch (Exception ex) {
                WizGUI.showError(ex);
            } finally {
                if (askClassifyThread == this) {
                    askClassifyThread = null;
                    SwingUtilities.invokeLater(() -> buttonAskClassify.setText("Ask"));
                }
            }
        }
    }

    private static final String[] classifyExamples = new String[] {
        // =====================================================================
        // 000 - OBRAS GERAIS, METODOLOGIA E PESQUISA ACADÊMICA
        // =====================================================================
        "[[- Acadêmico - Ciência da Informação - Metodologia Científica - Tipos de Pesquisa (Qualitativa e Quantitativa)]]",
        "[[- Acadêmico - Ciência da Informação - Metodologia Científica - Normas ABNT para Trabalhos Acadêmicos]]",
        "[[- Acadêmico - Ciência da Informação - Metodologia Científica - Estrutura do Artigo Científico]]",
        "[[- Acadêmico - Ciência da Informação - Epistemologia da Informação - Sociedade da Informação e do Conhecimento]]",
        "[[- Profissional - Ciência da Informação - Propriedade Intelectual - Patentes e Direitos Autorais]]",

        // =====================================================================
        // 004 / 005 - CIÊNCIA DA COMPUTAÇÃO: FUNDAMENTOS E TEORIA
        // =====================================================================
        "[[- Acadêmico - Ciência da Computação - Teoria da Computação - Máquinas de Turing e Autômatos]]",
        "[[- Acadêmico - Ciência da Computação - Lógica Computacional - Álgebra Booleana e Portas Lógicas]]",
        "[[- Acadêmico - Ciência da Computação - Matemática Discreta - Teoria dos Grafos]]",
        "[[- Acadêmico - Ciência da Computação - Paradigmas de Linguagens - Programação Funcional (Cálculo Lambda)]]",
        "[[- Acadêmico - Ciência da Computação - Compiladores - Análise Léxica, Sintática e Semântica]]",

        // =====================================================================
        // CIÊNCIA DA COMPUTAÇÃO: ALGORITMOS E ESTRUTURAS DE DADOS
        // =====================================================================
        "[[- Profissional - Tecnologia da Informação - Algoritmos e Estruturas de Dados - Notação Big-O (Complexidade de Tempo e Espaço)]]",
        "[[- Profissional - Tecnologia da Informação - Algoritmos e Estruturas de Dados - Estruturas Lineares (Listas, Pilhas e Filas)]]",
        "[[- Profissional - Tecnologia da Informação - Algoritmos e Estruturas de Dados - Árvores de Busca Binária (BST) e Árvores AVL]]",
        "[[- Profissional - Tecnologia da Informação - Algoritmos e Estruturas de Dados - Tabelas de Espalhamento (Hash Tables) e Tratamento de Colisões]]",
        "[[- Profissional - Tecnologia da Informação - Algoritmos e Estruturas de Dados - Algoritmos de Ordenação (Merge Sort, Quick Sort, Heap Sort)]]",
        "[[- Profissional - Tecnologia da Informação - Algoritmos e Estruturas de Dados - Algoritmos de Caminho Mínimo (Dijkstra, Bellman-Ford)]]",

        // =====================================================================
        // CIÊNCIA DA COMPUTAÇÃO: ARQUITETURA E SISTEMAS OPERACIONAIS
        // =====================================================================
        "[[- Profissional - Tecnologia da Informação - Arquitetura de Computadores - Arquitetura de Von Neumann e Harvard]]",
        "[[- Profissional - Tecnologia da Informação - Arquitetura de Computadores - Hierarquia de Memória (Registradores, Cache L1/L2/L3, RAM)]]",
        "[[- Profissional - Tecnologia da Informação - Arquitetura de Computadores - Pipeline de Instruções e Processamento Paralelo]]",
        "[[- Profissional - Tecnologia da Informação - Sistemas Operacionais - Escalonamento de Processos (FCFS, SJF, Round Robin)]]",
        "[[- Profissional - Tecnologia da Informação - Sistemas Operacionais - Gerenciamento de Memória e Paginação Virtual]]",
        "[[- Profissional - Tecnologia da Informação - Sistemas Operacionais - Concorrência, Semáforos e Prevenção de Deadlocks]]",
        "[[- Profissional - Tecnologia da Informação - Sistemas Operacionais - Sistemas de Arquivos (NTFS, ext4, FAT32)]]",

        // =====================================================================
        // CIÊNCIA DA COMPUTAÇÃO: REDES DE COMPUTADORES
        // =====================================================================
        "[[- Acadêmico - Tecnologia da Informação - Redes de Computadores - Modelo de Referência OSI (As 7 Camadas)]]",
        "[[- Profissional - Tecnologia da Informação - Redes de Computadores - Arquitetura TCP/IP e Encapsulamento de Dados]]",
        "[[- Profissional - Tecnologia da Informação - Redes de Computadores - Protocolos de Aplicação (HTTP/HTTPS, FTP, SMTP, DNS)]]",
        "[[- Profissional - Tecnologia da Informação - Redes de Computadores - Endereçamento IPv4 (Sub-redes e CIDR) e IPv6]]",
        "[[- Profissional - Tecnologia da Informação - Redes de Computadores - Protocolos de Transporte (Diferenças entre TCP e UDP)]]",
        "[[- Profissional - Tecnologia da Informação - Redes de Computadores - Roteamento Dinâmico (OSPF, BGP)]]",
        "[[- Profissional - Tecnologia da Informação - Redes de Computadores - Redes Sem Fio e Protocolos IEEE 802.11 (Wi-Fi)]]",

        // =====================================================================
        // CIÊNCIA DA COMPUTAÇÃO: ENGENHARIA DE SOFTWARE E DEVOPS
        // =====================================================================
        "[[- Profissional - Tecnologia da Informação - Ciclo de Vida do Software - Modelos em Cascata, Espiral e Iterativo]]",
        "[[- Profissional - Tecnologia da Informação - Metodologias Ágeis - Framework Scrum (Papéis, Artefatos e Cerimônias)]]",
        "[[- Profissional - Tecnologia da Informação - Metodologias Ágeis - Sistema Kanban e Limite de WIP]]",
        "[[- Profissional - Tecnologia da Informação - Engenharia de Requisitos - Elicitação e Requisitos Funcionais vs Não Funcionais]]",
        "[[- Profissional - Tecnologia da Informação - Modelagem de Sistemas - Diagramas UML (Casos de Uso, Classes, Sequência)]]",
        "[[- Profissional - Tecnologia da Informação - Padrões de Projeto (Design Patterns) - Criacionais (Singleton, Factory)]]",
        "[[- Profissional - Tecnologia da Informação - Padrões de Projeto (Design Patterns) - Estruturais e Comportamentais (Observer, Strategy)]]",
        "[[- Profissional - Tecnologia da Informação - Arquitetura de Software - Arquitetura de Microsserviços vs Monolítica]]",
        "[[- Profissional - Tecnologia da Informação - Arquitetura de Software - Padrão MVC (Model-View-Controller) e API RESTful]]",
        "[[- Profissional - Tecnologia da Informação - Testes de Software - Testes Unitários, de Integração e TDD (Test-Driven Development)]]",
        "[[- Profissional - Tecnologia da Informação - DevOps - Integração e Entrega Contínuas (CI/CD)]]",
        "[[- Profissional - Tecnologia da Informação - DevOps - Contêineres e Orquestração (Docker e Kubernetes)]]",

        // =====================================================================
        // CIÊNCIA DA COMPUTAÇÃO: BANCO DE DADOS E BIG DATA
        // =====================================================================
        "[[- Acadêmico - Tecnologia da Informação - Banco de Dados - Álgebra Relacional e Cálculo Relacional]]",
        "[[- Profissional - Tecnologia da Informação - Banco de Dados - Modelagem Entidade-Relacionamento (MER/DER)]]",
        "[[- Profissional - Tecnologia da Informação - Banco de Dados - Normalização de Dados (1FN a 5FN e BCNF)]]",
        "[[- Profissional - Tecnologia da Informação - Banco de Dados - Propriedades ACID em Transações]]",
        "[[- Profissional - Tecnologia da Informação - Banco de Dados - Comandos SQL: DDL, DML, DCL e DQL]]",
        "[[- Profissional - Tecnologia da Informação - Banco de Dados - Otimização de Consultas (Query Tuning) e Índices B-Tree]]",
        "[[- Profissional - Tecnologia da Informação - Banco de Dados - Bancos NoSQL (Chave-Valor, Grafos, Colunares e Documentos)]]",
        "[[- Profissional - Tecnologia da Informação - Ciência de Dados - Data Warehouse, Modelagem Multidimensional (Star Schema) e OLAP]]",
        "[[- Profissional - Tecnologia da Informação - Ciência de Dados - Processos ETL (Extract, Transform, Load) e Data Lakes]]",
        "[[- Profissional - Tecnologia da Informação - Ciência de Dados - Ecossistema Hadoop e Processamento Distribuído (Apache Spark)]]",

        // =====================================================================
        // CIÊNCIA DA COMPUTAÇÃO: INTELIGÊNCIA ARTIFICIAL E MACHINE LEARNING
        // =====================================================================
        "[[- Profissional - Tecnologia da Informação - Inteligência Artificial - Algoritmos de Busca (A*, Minimax)]]",
        "[[- Profissional - Tecnologia da Informação - Machine Learning - Aprendizado Supervisionado (Regressão Linear e Logística, Árvores de Decisão)]]",
        "[[- Profissional - Tecnologia da Informação - Machine Learning - Aprendizado Não Supervisionado (Clusterização K-Means, PCA)]]",
        "[[- Profissional - Tecnologia da Informação - Machine Learning - Aprendizado por Reforço (Q-Learning e Processos de Decisão de Markov)]]",
        "[[- Profissional - Tecnologia da Informação - Deep Learning - Redes Neurais Convolucionais (CNNs) para Visão Computacional]]",
        "[[- Profissional - Tecnologia da Informação - Deep Learning - Redes Neurais Recorrentes (RNNs) e Transformadores (LLMs, GPT)]]",
        "[[- Profissional - Tecnologia da Informação - Inteligência Artificial - Processamento de Linguagem Natural (PLN) e Análise de Sentimentos]]",

        // =====================================================================
        // CIÊNCIA DA COMPUTAÇÃO: SEGURANÇA DA INFORMAÇÃO E GOVERNANÇA DE TI
        // =====================================================================
        "[[- Profissional - Tecnologia da Informação - Segurança da Informação - Princípios Básicos (Confidencialidade, Integridade e Disponibilidade - CID)]]",
        "[[- Profissional - Tecnologia da Informação - Segurança da Informação - Criptografia Simétrica (AES) e Assimétrica (RSA)]]",
        "[[- Profissional - Tecnologia da Informação - Segurança da Informação - Assinaturas Digitais e Infraestrutura de Chaves Públicas (ICP/PKI)]]",
        "[[- Profissional - Tecnologia da Informação - Segurança da Informação - Malwares (Ransomware, Trojan, Rootkit, Worms)]]",
        "[[- Profissional - Tecnologia da Informação - Segurança da Informação - Ataques de Rede (DDoS, Man-in-the-Middle, Spoofing)]]",
        "[[- Profissional - Tecnologia da Informação - Segurança da Informação - Testes de Invasão (Penetration Testing) e Ethical Hacking]]",
        "[[- Profissional - Tecnologia da Informação - Segurança da Informação - Controle de Acesso (RBAC, DAC, MAC) e Autenticação Multifator (MFA)]]",
        "[[- Profissional - Tecnologia da Informação - Governança de TI - Framework ITIL v4 (Práticas de Gerenciamento de Serviços)]]",
        "[[- Profissional - Tecnologia da Informação - Governança de TI - Framework COBIT 2019 (Alinhamento de TI ao Negócio)]]",

        // =====================================================================
        // 020 - BIBLIOTECONOMIA E CIÊNCIA DA INFORMAÇÃO
        // =====================================================================
        "[[- Acadêmico - Biblioteconomia - Fundamentos - As Cinco Leis de Ranganathan]]",
        "[[- Profissional - Biblioteconomia - Representação Descritiva - Regras de Catalogação (AACR2 e RDA)]]",
        "[[- Profissional - Biblioteconomia - Representação Temática - Classificação Decimal de Dewey (CDD)]]",
        "[[- Profissional - Biblioteconomia - Representação Temática - Classificação Decimal Universal (CDU)]]",
        "[[- Profissional - Biblioteconomia - Indexação e Resumo - Linguagens Documentárias e Tesauros]]",
        "[[- Profissional - Biblioteconomia - Formatos de Intercâmbio - Formato MARC 21 (Campos e Subcampos)]]",
        "[[- Profissional - Biblioteconomia - Serviço de Referência - Disseminação Seletiva da Informação (DSI) e Comutação Bibliográfica]]",
        "[[- Profissional - Biblioteconomia - Gestão de Unidades de Informação - Desenvolvimento e Avaliação de Coleções]]",
        "[[- Profissional - Biblioteconomia - Bibliometria - Leis Bibliométricas (Lotka, Bradford, Zipf)]]",

        // =====================================================================
        // 060 - ARQUIVOLOGIA, MUSEOLOGIA E ORGANIZAÇÕES
        // =====================================================================
        "[[- Acadêmico - Arquivologia - Teoria Arquivística - Princípio da Proveniência e da Ordem Original]]",
        "[[- Profissional - Arquivologia - Ciclo Vital dos Documentos - Teoria das Três Idades (Corrente, Intermediário, Permanente)]]",
        "[[- Profissional - Arquivologia - Gestão de Documentos - Classificação e Tabelas de Temporalidade Documental (TTD)]]",
        "[[- Profissional - Arquivologia - Descrição Arquivística - Norma Brasileira de Descrição Arquivística (NOBRADE) e ISAD(G)]]",
        "[[- Profissional - Arquivologia - Preservação Documental - Conservação Preventiva e Restauração de Suportes Físicos]]",
        "[[- Profissional - Arquivologia - Arquivos Digitais - Gestão Eletrônica de Documentos (GED) e Modelo OAIS]]",
        "[[- Profissional - Museologia - Gestão Museológica - Plano Museológico e Documentação de Acervos]]",
        "[[- Profissional - Museologia - Expografia - Conservação Preventiva em Exposições e Iluminação Museal]]",

        // =====================================================================
        // 070 - COMUNICAÇÃO SOCIAL, JORNALISMO E EDITORAÇÃO
        // =====================================================================
        "[[- Acadêmico - Comunicação Social - Teorias da Comunicação - Teoria Hipodérmica e Agenda-Setting]]",
        "[[- Acadêmico - Jornalismo - Ética Jornalística - Código de Ética e Deontologia Profissional]]",
        "[[- Profissional - Jornalismo - Redação Jornalística - Estrutura do Lead e Pirâmide Invertida]]",
        "[[- Profissional - Jornalismo - Gêneros Jornalísticos - Reportagem, Entrevista e Opinião]]",
        "[[- Profissional - Jornalismo - Webjornalismo - Arquitetura da Informação e SEO para Notícias]]",
        "[[- Profissional - Jornalismo - Assessoria de Imprensa - Media Training e Produção de Press Releases]]",
        "[[- Profissional - Editoração - Produção Editorial - Projeto Gráfico, Diagramação e Revisão de Provas]]",

        // =====================================================================
        // 100 - FILOSOFIA: METAFÍSICA, ONTOLOGIA E EPISTEMOLOGIA (110 e 120)
        // =====================================================================
        "[[- Acadêmico - Filosofia - Ontologia - O Ser e o Nada (Teorias do Ser)]]",
        "[[- Acadêmico - Filosofia - Ontologia - Substância, Essência e Acidente]]",
        "[[- Acadêmico - Filosofia - Metafísica - Cosmologia Filosófica e a Origem do Universo]]",
        "[[- Acadêmico - Filosofia - Metafísica - O Problema do Livre-Arbítrio e Determinismo]]",
        "[[- Acadêmico - Filosofia - Epistemologia - Teoria do Conhecimento (Sujeito e Objeto)]]",
        "[[- Acadêmico - Filosofia - Epistemologia - Empirismo (A Experiência Sensível)]]",
        "[[- Acadêmico - Filosofia - Epistemologia - Racionalismo (A Razão como Fonte de Conhecimento)]]",
        "[[- Acadêmico - Filosofia - Epistemologia - Criticismo e a Síntese Kantiana]]",
        "[[- Acadêmico - Filosofia - Epistemologia - Construtivismo e Paradigmas Científicos (Thomas Kuhn)]]",

        // =====================================================================
        // 140/180/190 - FILOSOFIA: HISTÓRIA E ESCOLAS FILOSÓFICAS
        // =====================================================================
        "[[- Acadêmico - Filosofia - Filosofia Antiga - Pré-Socráticos e a Busca pela Arché]]",
        "[[- Acadêmico - Filosofia - Filosofia Antiga - Sócrates: Ironia e Maiêutica]]",
        "[[- Acadêmico - Filosofia - Filosofia Antiga - Platão: Teoria das Ideias e o Mito da Caverna]]",
        "[[- Acadêmico - Filosofia - Filosofia Antiga - Aristóteles: Hilemorfismo (Matéria e Forma)]]",
        "[[- Acadêmico - Filosofia - Filosofia Antiga - Helenismo: Estoicismo, Epicurismo e Ceticismo]]",
        "[[- Acadêmico - Filosofia - Filosofia Medieval - Patrística: Santo Agostinho e a Teoria da Iluminação]]",
        "[[- Acadêmico - Filosofia - Filosofia Medieval - Escolástica: São Tomás de Aquino e as Cinco Vias]]",
        "[[- Acadêmico - Filosofia - Filosofia Moderna - Descartes: Dúvida Metódica e o Cogito]]",
        "[[- Acadêmico - Filosofia - Filosofia Moderna - John Locke: A Mente como Tábula Rasa]]",
        "[[- Acadêmico - Filosofia - Filosofia Moderna - David Hume: O Problema da Causalidade]]",
        "[[- Acadêmico - Filosofia - Filosofia Moderna - Hegel: Dialética (Tese, Antítese, Síntese)]]",
        "[[- Acadêmico - Filosofia - Filosofia Contemporânea - Nietzsche: Niilismo, Super-homem e Vontade de Potência]]",
        "[[- Acadêmico - Filosofia - Filosofia Contemporânea - Fenomenologia: Edmund Husserl e a Intencionalidade da Consciência]]",
        "[[- Acadêmico - Filosofia - Filosofia Contemporânea - Existencialismo: Sartre, Heidegger e a Condição Humana]]",
        "[[- Acadêmico - Filosofia - Filosofia Contemporânea - Escola de Frankfurt: Teoria Crítica e Indústria Cultural (Adorno e Horkheimer)]]",
        "[[- Acadêmico - Filosofia - Filosofia Contemporânea - Pós-Estruturalismo: Foucault (Microfísica do Poder) e Deleuze]]",

        // =====================================================================
        // 160 - LÓGICA FILOSÓFICA E FORMAL
        // =====================================================================
        "[[- Acadêmico - Filosofia - Lógica - Lógica Aristotélica: O Silogismo Categórico]]",
        "[[- Acadêmico - Filosofia - Lógica - Princípios Lógicos: Identidade, Não Contradição e Terceiro Excluído]]",
        "[[- Acadêmico - Filosofia - Lógica - Lógica Proposicional: Conectivos e Tabelas Verdade]]",
        "[[- Acadêmico - Filosofia - Lógica - Lógica de Predicados: Quantificadores Universais e Existenciais]]",
        "[[- Acadêmico - Filosofia - Lógica - Argumentação: Indução, Dedução e Abdução]]",
        "[[- Acadêmico - Filosofia - Lógica - Falácias Não Formais: Ad Hominem, Espantalho e Apelo à Ignorância]]",
        "[[- Profissional - Filosofia - Lógica Aplicada - Pensamento Crítico na Resolução de Problemas Complexos]]",

        // =====================================================================
        // 170 - ÉTICA (MORAL)
        // =====================================================================
        "[[- Acadêmico - Filosofia - Ética Teórica - Ética das Virtudes (Aristóteles)]]",
        "[[- Acadêmico - Filosofia - Ética Teórica - Deontologia: O Imperativo Categórico (Kant)]]",
        "[[- Acadêmico - Filosofia - Ética Teórica - Utilitarismo: O Princípio da Maior Felicidade (Bentham e Stuart Mill)]]",
        "[[- Acadêmico - Filosofia - Ética Teórica - Relativismo Moral vs Absolutismo Moral]]",
        "[[- Profissional - Filosofia - Ética Aplicada - Bioética: Eutanásia, Aborto e Clonagem Genética]]",
        "[[- Profissional - Filosofia - Ética Aplicada - Ética Profissional e Deontologia Corporativa]]",
        "[[- Profissional - Filosofia - Ética Aplicada - Ética na Inteligência Artificial e Algoritmos]]",

        // =====================================================================
        // 150 - PSICOLOGIA: TEORIAS DE BASE E SISTEMAS
        // =====================================================================
        "[[- Acadêmico - Psicologia - Psicanálise - Tópicas de Freud: Consciente, Pré-Consciente e Inconsciente]]",
        "[[- Acadêmico - Psicologia - Psicanálise - Estrutura Dinâmica: Id, Ego e Superego]]",
        "[[- Acadêmico - Psicologia - Psicanálise - Mecanismos de Defesa (Recalque, Projeção, Sublimação)]]",
        "[[- Acadêmico - Psicologia - Psicologia Analítica (Jung) - Inconsciente Coletivo e Arquétipos]]",
        "[[- Acadêmico - Psicologia - Behaviorismo - Condicionamento Clássico (Pavlov e Watson)]]",
        "[[- Acadêmico - Psicologia - Behaviorismo - Condicionamento Operante e Reforço (Skinner)]]",
        "[[- Acadêmico - Psicologia - Psicologia Humanista - Pirâmide das Necessidades (Maslow)]]",
        "[[- Acadêmico - Psicologia - Psicologia Humanista - Abordagem Centrada na Pessoa (Carl Rogers)]]",
        "[[- Acadêmico - Psicologia - Psicologia da Gestalt - Leis da Percepção Visual e Fechamento]]",

        // =====================================================================
        // 150 - PSICOLOGIA: DESENVOLVIMENTO, COGNITIVA E SOCIAL
        // =====================================================================
        "[[- Acadêmico - Psicologia - Psicologia do Desenvolvimento - Epistemologia Genética e Fases (Piaget)]]",
        "[[- Acadêmico - Psicologia - Psicologia do Desenvolvimento - Teoria Histórico-Cultural e ZDP (Vygotsky)]]",
        "[[- Acadêmico - Psicologia - Psicologia do Desenvolvimento - Desenvolvimento Psicossocial ao Longo da Vida (Erikson)]]",
        "[[- Acadêmico - Psicologia - Psicologia do Desenvolvimento - Psicanálise Infantil e o Objeto Transicional (Winnicott)]]",
        "[[- Acadêmico - Psicologia - Psicologia Cognitiva - Processos Mnemônicos (Memória de Trabalho, Curto e Longo Prazo)]]",
        "[[- Acadêmico - Psicologia - Psicologia Cognitiva - Atenção Sustentada, Alternada e Seletiva]]",
        "[[- Acadêmico - Psicologia - Psicologia Cognitiva - Funções Executivas e Córtex Pré-Frontal]]",
        "[[- Acadêmico - Psicologia - Psicologia Social - Formação de Atitudes, Preconceito e Estereótipos]]",
        "[[- Acadêmico - Psicologia - Psicologia Social - Dinâmica de Grupo e Conformidade (Experimento de Asch)]]",

        // =====================================================================
        // 158/159 - PSICOLOGIA: APLICAÇÕES CLÍNICAS E MERCADOLÓGICAS (FUNCIONAL)
        // =====================================================================
        "[[- Profissional - Psicologia - Psicologia Clínica - Terapia Cognitivo-Comportamental (TCC): Crenças Centrais e Distorções Cognitivas]]",
        "[[- Profissional - Psicologia - Psicologia Clínica - Entrevista Psicológica e Anamnese]]",
        "[[- Profissional - Psicologia - Psicopatologia - Transtornos do Neurodesenvolvimento (TEA, TDAH)]]",
        "[[- Profissional - Psicologia - Psicopatologia - Transtornos de Humor (Depressão Maior e Transtorno Bipolar)]]",
        "[[- Profissional - Psicologia - Psicopatologia - Transtornos de Personalidade (Borderline, Narcisista, Antissocial)]]",
        "[[- Profissional - Psicologia - Psicometria - Testes Projetivos (Rorschach, TAT, HTP)]]",
        "[[- Profissional - Psicologia - Psicometria - Testes de Inteligência e Cognitivos (WISC, WAIS)]]",
        "[[- Profissional - Psicologia - Psicometria - Testes de Personalidade (Palográfico, Quati, Big Five)]]",
        "[[- Profissional - Psicologia - Psicologia Organizacional - Recrutamento, Seleção e Entrevistas por Competência]]",
        "[[- Profissional - Psicologia - Psicologia Organizacional - Avaliação de Desempenho e Clima Organizacional]]",
        "[[- Profissional - Psicologia - Psicologia Organizacional - Saúde do Trabalhador e Síndrome de Burnout]]",
        "[[- Profissional - Psicologia - Psicologia Jurídica - Perícia Psicológica e Avaliação de Imputabilidade Penal]]",
        "[[- Profissional - Psicologia - Psicologia do Esporte - Gestão de Ansiedade e Alta Performance em Atletas]]",
        "[[- Profissional - Psicologia - Psicologia Hospitalar - Cuidados Paliativos e Processos de Luto (Kübler-Ross)]]",

        // =====================================================================
        // 200 / 210 - FILOSOFIA DA RELIGIÃO E TEOLOGIA NATURAL
        // =====================================================================
        "[[- Acadêmico - Religião - Fenomenologia da Religião - O Sagrado e o Profano (Mircea Eliade)]]",
        "[[- Acadêmico - Religião - Sociologia da Religião - Secularização e Laicidade do Estado]]",
        "[[- Acadêmico - Religião - Filosofia da Religião - Provas da Existência de Deus (Argumentos Ontológico e Cosmológico)]]",
        "[[- Acadêmico - Religião - Teologia Natural - Teodiceia e o Problema do Mal]]",
        "[[- Acadêmico - Religião - Teologia Natural - Criacionismo, Design Inteligente e Evolução Teísta]]",

        // =====================================================================
        // 220 - BÍBLIA E ESTUDOS ESCRITURÍSTICOS
        // =====================================================================
        "[[- Acadêmico - Religião - Estudos Bíblicos - Exegese, Hermenêutica e Contexto Histórico-Cultural]]",
        "[[- Acadêmico - Religião - Antigo Testamento - Pentateuco (Gênesis, Êxodo, Levítico, Números, Deuteronômio)]]",
        "[[- Acadêmico - Religião - Antigo Testamento - Livros Históricos (Josué, Juízes, Reis, Crônicas)]]",
        "[[- Acadêmico - Religião - Antigo Testamento - Livros Proféticos (Profetas Maiores e Menores)]]",
        "[[- Acadêmico - Religião - Antigo Testamento - Livros Sapienciais e Poéticos (Salmos, Provérbios, Jó)]]",
        "[[- Acadêmico - Religião - Novo Testamento - Evangelhos Sinóticos (Mateus, Marcos e Lucas) e Problema Sinótico]]",
        "[[- Acadêmico - Religião - Novo Testamento - Evangelho de João e Teologia Joanina]]",
        "[[- Acadêmico - Religião - Novo Testamento - Epístolas Paulinas e Justificação pela Fé]]",
        "[[- Acadêmico - Religião - Novo Testamento - Epístolas Gerais e Apocalipse de João]]",
        "[[- Acadêmico - Religião - Escritos Apócrifos - Evangelhos Gnósticos e Manuscritos do Mar Morto (Qumran)]]",

        // =====================================================================
        // 230 / 240 - TEOLOGIA CRISTÃ (DOGMÁTICA E MORAL)
        // =====================================================================
        "[[- Acadêmico - Teologia - Teologia Sistemática - Doutrina da Trindade (Pai, Filho e Espírito Santo)]]",
        "[[- Acadêmico - Teologia - Cristologia - União Hipostática (Naturezas Divina e Humana de Cristo)]]",
        "[[- Acadêmico - Teologia - Soteriologia - Doutrinas da Salvação, Graça e Expiação]]",
        "[[- Acadêmico - Teologia - Pneumatologia - Pessoa e Obra do Espírito Santo (Dons Espirituais)]]",
        "[[- Acadêmico - Teologia - Eclesiologia - Marcas da Igreja e Modelos de Governo (Episcopal, Presbiteriano, Congregacional)]]",
        "[[- Acadêmico - Teologia - Escatologia - Visões do Milênio (Amilenismo, Pré-Milenismo, Pós-Milenismo) e Juízo Final]]",
        "[[- Acadêmico - Teologia - Mariologia - Dogmas Marianos (Imaculada Conceição, Assunção, Theotokos)]]",
        "[[- Acadêmico - Teologia - Teologia Moral - Pecado Original, Hamartiologia e Virtudes Cardeais]]",
        "[[- Acadêmico - Teologia - Espiritualidade Cristã - Misticismo, Ascetismo e Disciplinas Espirituais]]",

        // =====================================================================
        // 250 / 260 - TEOLOGIA PRÁTICA E IGREJA LOCAL (APLICAÇÃO FUNCIONAL)
        // =====================================================================
        "[[- Profissional - Teologia - Homilética - Oratória Sagrada e Estruturação de Sermões Expositivos]]",
        "[[- Profissional - Teologia - Aconselhamento Pastoral - Psicologia Pastoral e Cuidado de Almas]]",
        "[[- Profissional - Teologia - Capelania - Capelania Hospitalar, Prisional e Escolar]]",
        "[[- Profissional - Teologia - Gestão Eclesiástica - Administração Financeira e Jurídica de Igrejas e ONGs]]",
        "[[- Profissional - Teologia - Liturgia - Estruturação de Cultos, Cerimônias e Missas]]",
        "[[- Profissional - Teologia - Missiologia - Estratégias de Plantação de Igrejas e Antropologia Missionária]]",
        "[[- Profissional - Teologia - Pedagogia Cristã - Escola Dominical, Catequese e Discipulado]]",
        "[[- Acadêmico - Teologia - Sacramentos e Ordenanças - Batismo e Eucaristia/Ceia do Senhor]]",

        // =====================================================================
        // 270 / 280 - HISTÓRIA DA IGREJA E DENOMINAÇÕES CRISTÃS
        // =====================================================================
        "[[- Acadêmico - História - História da Igreja - Igreja Primitiva, Perseguição e Apologistas (Séculos I ao IV)]]",
        "[[- Acadêmico - História - História da Igreja - Concílios Ecumênicos da Antiguidade (Niceia, Éfeso, Calcedônia)]]",
        "[[- Acadêmico - História - História da Igreja - Cisma do Oriente (1054) e o Surgimento da Igreja Ortodoxa]]",
        "[[- Acadêmico - História - História da Igreja - Inquisição, Cruzadas e Papado na Idade Média]]",
        "[[- Acadêmico - História - História da Igreja - Reforma Protestante (Lutero, Calvino, Zwinglio, Knox)]]",
        "[[- Acadêmico - História - História da Igreja - Contrarreforma Católica, Companhia de Jesus e Concílio de Trento]]",
        "[[- Acadêmico - Teologia - Denominações Cristãs - Catolicismo Romano (Magistério e Tradição)]]",
        "[[- Acadêmico - Teologia - Denominações Cristãs - Anglicanismo e a Igreja da Inglaterra]]",
        "[[- Acadêmico - Teologia - Denominações Cristãs - Protestantismo Histórico (Luteranos, Presbiterianos, Metodistas, Batistas)]]",
        "[[- Acadêmico - Teologia - Denominações Cristãs - Pentecostalismo, Neopentecostalismo e Movimento Carismático]]",

        // =====================================================================
        // 290 - OUTRAS RELIGIÕES, MITOLOGIA E TRADIÇÕES
        // =====================================================================
        "[[- Acadêmico - Mitologia - Mitologia Greco-Romana - O Panteão Olímpico e os Mitos Heroicos]]",
        "[[- Acadêmico - Mitologia - Mitologia Nórdica - Deuses Aesir, Vanir e a Escatologia do Ragnarök]]",
        "[[- Acadêmico - Religião - Judaísmo - Textos Sagrados (Torá, Tanakh e Talmude)]]",
        "[[- Acadêmico - Religião - Judaísmo - Correntes Contemporâneas (Ortodoxo, Conservador e Reformista)]]",
        "[[- Acadêmico - Religião - Islamismo - Os Cinco Pilares do Islã e a Leitura do Alcorão]]",
        "[[- Acadêmico - Religião - Islamismo - Divisão Histórica e Teológica (Sunitas e Xiitas)]]",
        "[[- Acadêmico - Religião - Hinduísmo - Textos Védicos, Upanishads e o Bhagavad Gita]]",
        "[[- Acadêmico - Religião - Hinduísmo - Sistema de Castas, Karma e o Ciclo de Samsara]]",
        "[[- Acadêmico - Religião - Budismo - As Quatro Nobres Verdades e o Nobre Caminho Óctuplo]]",
        "[[- Acadêmico - Religião - Budismo - Vertentes Filosóficas (Theravada, Mahayana e Vajrayana/Tibetano)]]",
        "[[- Acadêmico - Religião - Religiões Orientais - Taoísmo, Yin-Yang e o Tao Te Ching]]",
        "[[- Acadêmico - Religião - Religiões Orientais - Confucionismo e Xintoísmo (Religião Tradicional Japonesa)]]",
        "[[- Acadêmico - Religião - Religiões Afro-Diaspóricas - Candomblé, Orixás e o Culto de Nação]]",
        "[[- Acadêmico - Religião - Religiões Afro-Diaspóricas - Umbanda (Surgimento, Entidades e Sincretismo)]]",
        "[[- Acadêmico - Religião - Religiões Indígenas e Tribais - Animismo, Xamanismo e Totemismo]]",
        "[[- Acadêmico - Religião - Novos Movimentos Religiosos - Espiritismo (Codificação de Allan Kardec)]]",
        "[[- Acadêmico - Religião - Novos Movimentos Religiosos - Fé Bahá'í, Cientologia e Movimentos de Nova Era]]",
        "[[- Acadêmico - Religião - Novos Movimentos Religiosos - Esoterismo, Ocultismo e Sociedades Secretas]]",

        // =====================================================================
        // 300 / 310 - SOCIOLOGIA, ANTROPOLOGIA E DEMOGRAFIA
        // =====================================================================
        "[[- Acadêmico - Sociologia - Teoria Clássica - Fatos Sociais e Suicídio (Émile Durkheim)]]",
        "[[- Acadêmico - Sociologia - Teoria Clássica - Ação Social e Burocracia (Max Weber)]]",
        "[[- Acadêmico - Sociologia - Teoria Clássica - Materialismo Histórico e Luta de Classes (Karl Marx)]]",
        "[[- Acadêmico - Sociologia - Sociologia Brasileira - Casa-Grande & Senzala (Gilberto Freyre)]]",
        "[[- Acadêmico - Sociologia - Sociologia Brasileira - O Povo Brasileiro e a Miscigenação (Darcy Ribeiro)]]",
        "[[- Acadêmico - Sociologia - Sociologia Urbana - Especulação Imobiliária, Favelização e Gentrificação]]",
        "[[- Acadêmico - Sociologia - Movimentos Sociais - Feminismo, Movimento Negro e Lutas Sindicais]]",
        "[[- Acadêmico - Antropologia - Antropologia Cultural - Relativismo Cultural e Etnocentrismo]]",
        "[[- Acadêmico - Antropologia - Antropologia Estrutural - Parentesco e Tabu do Incesto (Lévi-Strauss)]]",
        "[[- Profissional - Demografia - Estatística Social - Pirâmides Etárias, Taxa de Fecundidade e Mortalidade]]",
        "[[- Profissional - Demografia - Censos e Pesquisas - Metodologia do IBGE e PNAD Contínua]]",

        // =====================================================================
        // 320 - CIÊNCIA POLÍTICA E RELAÇÕES INTERNACIONAIS
        // =====================================================================
        "[[- Acadêmico - Ciência Política - Teoria do Estado - Formas de Governo (Monarquia, República)]]",
        "[[- Acadêmico - Ciência Política - Teoria do Estado - Sistemas de Governo (Presidencialismo, Parlamentarismo)]]",
        "[[- Acadêmico - Ciência Política - Teoria Política Moderna - O Príncipe e a Realpolitik (Maquiavel)]]",
        "[[- Profissional - Ciência Política - Sistemas Eleitorais - Voto Majoritário, Proporcional e Quociente Eleitoral]]",
        "[[- Profissional - Ciência Política - Partidos Políticos - Pluripartidarismo, Fundo Partidário e Cláusula de Barreira]]",
        "[[- Profissional - Relações Internacionais - Geopolítica - Hegemonia Americana e Ascensão do BRICS]]",
        "[[- Profissional - Relações Internacionais - Organizações Internacionais - Conselho de Segurança da ONU e Poder de Veto]]",
        "[[- Profissional - Relações Internacionais - Comércio Exterior - Barreiras Tarifárias, OMC e Acordos Bilaterais]]",

        // =====================================================================
        // 330 - ECONOMIA (TEORIA E APLICAÇÃO)
        // =====================================================================
        "[[- Acadêmico - Economia - História do Pensamento Econômico - Fisiocracia e Mercantilismo]]",
        "[[- Acadêmico - Economia - História do Pensamento Econômico - Mão Invisível e Liberalismo Clássico (Adam Smith)]]",
        "[[- Acadêmico - Economia - História do Pensamento Econômico - Teoria Geral do Emprego e Estado de Bem-Estar (Keynes)]]",
        "[[- Profissional - Economia - Microeconomia - Lei da Oferta e da Demanda e Ponto de Equilíbrio]]",
        "[[- Profissional - Economia - Microeconomia - Elasticidade-Preço da Demanda e Bens Substitutos/Complementares]]",
        "[[- Profissional - Economia - Microeconomia - Estruturas de Mercado (Monopólio, Oligopólio, Concorrência Perfeita)]]",
        "[[- Profissional - Economia - Macroeconomia - Contabilidade Social e Cálculo do PIB (Óticas da Produção, Renda e Despesa)]]",
        "[[- Profissional - Economia - Macroeconomia - Instrumentos de Política Monetária (Taxa Selic, Redesconto, Compulsório)]]",
        "[[- Profissional - Economia - Macroeconomia - Política Fiscal e Curva de Laffer (Carga Tributária vs Arrecadação)]]",
        "[[- Profissional - Economia - Economia do Trabalho - Taxa de Desemprego (Aberto e Oculto) e Curva de Phillips]]",
        "[[- Profissional - Economia - Sistema Financeiro Nacional - Papel do Banco Central (BACEN) e da CVM]]",

        // =====================================================================
        // 340 - DIREITO (CONSTITUCIONAL, ADMINISTRATIVO, CIVIL, PENAL, ETC.)
        // =====================================================================
        "[[- Acadêmico - Direito - Teoria Geral do Direito - Fontes do Direito (Lei, Jurisprudência, Costumes, Doutrina)]]",
        "[[- Acadêmico - Direito - Teoria Geral do Direito - Positivismo Jurídico (Hans Kelsen e a Norma Fundamental)]]",
        "[[- Profissional - Direito - Direito Constitucional - Direitos e Deveres Individuais e Coletivos (Art. 5º da CF)]]",
        "[[- Profissional - Direito - Direito Constitucional - Remédios Constitucionais (Habeas Corpus, Mandado de Segurança, Mandado de Injunção)]]",
        "[[- Profissional - Direito - Direito Constitucional - Controle de Constitucionalidade (ADI, ADC, ADPF e Súmula Vinculante)]]",
        "[[- Profissional - Direito - Direito Administrativo - Organização Administrativa (Administração Direta e Indireta)]]",
        "[[- Profissional - Direito - Direito Administrativo - Atos Administrativos (Requisitos: Competência, Finalidade, Forma, Motivo, Objeto)]]",
        "[[- Profissional - Direito - Direito Administrativo - Licitações e Contratos Administrativos (Nova Lei 14.133/2021)]]",
        "[[- Profissional - Direito - Direito Administrativo - Agentes Públicos e Regime Jurídico Único (Lei 8.112/90)]]",
        "[[- Profissional - Direito - Direito Administrativo - Improbidade Administrativa (Lei 8.429/92)]]",
        "[[- Profissional - Direito - Direito Civil - Lei de Introdução às Normas do Direito Brasileiro (LINDB)]]",
        "[[- Profissional - Direito - Direito Civil - Capacidade Civil, Personalidade e Emancipação]]",
        "[[- Profissional - Direito - Direito Civil - Negócio Jurídico (Defeitos: Erro, Dolo, Coação, Fraude contra Credores)]]",
        "[[- Profissional - Direito - Direito Civil - Direito das Obrigações (Dar, Fazer e Não Fazer)]]",
        "[[- Profissional - Direito - Direito Civil - Responsabilidade Civil (Dano Moral, Material e Nexo de Causalidade)]]",
        "[[- Profissional - Direito - Direito Civil - Direito de Família (Regimes de Bens e Pensão Alimentícia)]]",
        "[[- Profissional - Direito - Direito Penal - Teoria do Delito (Fato Típico, Ilicitude e Culpabilidade)]]",
        "[[- Profissional - Direito - Direito Penal - Crimes contra a Vida (Homicídio, Infanticídio, Aborto)]]",
        "[[- Profissional - Direito - Direito Penal - Crimes contra o Patrimônio (Furto, Roubo, Estelionato, Extorsão)]]",
        "[[- Profissional - Direito - Direito Penal - Crimes contra a Administração Pública (Corrupção Passiva, Peculato, Concussão)]]",
        "[[- Profissional - Direito - Direito Processual Penal - Inquérito Policial e Ação Penal (Pública e Privada)]]",
        "[[- Profissional - Direito - Direito Processual Penal - Prisões Cautelares (Flagrante, Preventiva e Temporária)]]",
        "[[- Profissional - Direito - Direito do Trabalho - Relação de Emprego (Subordinação, Habitualidade, Onerosidade, Pessoalidade)]]",
        "[[- Profissional - Direito - Direito do Trabalho - Remuneração e Salário (Adicionais de Insalubridade e Periculosidade)]]",
        "[[- Profissional - Direito - Direito do Trabalho - Rescisão do Contrato de Trabalho e Aviso Prévio]]",
        "[[- Profissional - Direito - Direito Tributário - Princípios Tributários (Legalidade, Anterioridade, Noventena)]]",
        "[[- Profissional - Direito - Direito Tributário - Suspensão, Extinção e Exclusão do Crédito Tributário]]",
        "[[- Profissional - Direito - Direitos Difusos e Coletivos - Direito do Consumidor (CDC) e Responsabilidade do Fornecedor]]",
        "[[- Profissional - Direito - Direitos Difusos e Coletivos - Estatuto da Criança e do Adolescente (ECA) e Medidas Socioeducativas]]",

        // =====================================================================
        // 350 - ADMINISTRAÇÃO PÚBLICA, GESTÃO E ADMINISTRAÇÃO GERAL
        // =====================================================================
        "[[- Acadêmico - Administração - Teorias da Administração - Administração Científica (Taylor) e Clássica (Fayol)]]",
        "[[- Acadêmico - Administração - Teorias da Administração - Teoria das Relações Humanas (Experiência de Hawthorne)]]",
        "[[- Profissional - Administração - Planejamento Estratégico - Análise de Cenários (Matriz SWOT) e Balanced Scorecard (BSC)]]",
        "[[- Profissional - Administração - Gestão de Projetos - Guia PMBOK (Escopo, Tempo, Custos e Qualidade)]]",
        "[[- Profissional - Administração - Gestão de Processos - Mapeamento de Processos (BPMN) e Ciclo PDCA]]",
        "[[- Profissional - Administração - Gestão de Pessoas - Avaliação de Desempenho (360 Graus e 9-Box)]]",
        "[[- Profissional - Administração - Gestão de Pessoas - Políticas de Remuneração e Benefícios Flexíveis]]",
        "[[- Profissional - Administração - Logística e Supply Chain - Gestão de Estoques (Curva ABC e Just-in-Time)]]",
        "[[- Profissional - Administração Pública - Modelos de Gestão Pública - Administração Patrimonialista, Burocrática e Gerencial]]",
        "[[- Profissional - Administração Pública - Políticas Públicas - Ciclo das Políticas Públicas (Agenda, Formulação, Implementação, Avaliação)]]",

        // =====================================================================
        // CONTABILIDADE (Geralmente incluída nas Sociais Aplicadas)
        // =====================================================================
        "[[- Profissional - Contabilidade - Contabilidade Geral - Princípios Contábeis (Entidade, Competência, Continuidade)]]",
        "[[- Profissional - Contabilidade - Contabilidade Geral - Plano de Contas e Escrituração (Método das Partidas Dobradas)]]",
        "[[- Profissional - Contabilidade - Demonstrações Contábeis - Balanço Patrimonial (Ativo, Passivo e Patrimônio Líquido)]]",
        "[[- Profissional - Contabilidade - Demonstrações Contábeis - Demonstração do Resultado do Exercício (DRE) e EBITDA]]",
        "[[- Profissional - Contabilidade - Contabilidade Pública - Orçamento Público (PPA, LDO, LOA e Lei de Responsabilidade Fiscal)]]",
        "[[- Profissional - Contabilidade - Contabilidade Pública - Estágios da Receita e da Despesa Pública (Empenho, Liquidação, Pagamento)]]",
        "[[- Profissional - Contabilidade - Auditoria e Perícia - Papéis de Trabalho, Parecer de Auditoria e Risco de Imagem]]",

        // =====================================================================
        // 360 - SERVIÇO SOCIAL, PATOLOGIA SOCIAL E CRIMINOLOGIA
        // =====================================================================
        "[[- Acadêmico - Assistência Social - Fundamentos Históricos - Origem do Serviço Social no Brasil (Ação Católica e Estado Varguista)]]",
        "[[- Profissional - Assistência Social - Políticas Sociais - Sistema Único de Assistência Social (SUAS) e CRAS/CREAS]]",
        "[[- Profissional - Assistência Social - Seguridade Social - Previdência Social, Assistência Social e Saúde (Art. 194 da CF)]]",
        "[[- Profissional - Criminologia - Vitimologia - Vitimização Primária, Secundária e Terciária]]",
        "[[- Profissional - Criminologia - Teorias Sociológicas da Criminalidade - Teoria da Anomia, Escola de Chicago e Labelling Approach]]",

        // =====================================================================
        // 370 - EDUCAÇÃO E PEDAGOGIA
        // =====================================================================
        "[[- Acadêmico - Educação - História da Educação - Educação Jesuítica e Reformas Pombalinas no Brasil]]",
        "[[- Acadêmico - Educação - Tendências Pedagógicas - Pedagogia Liberal (Tradicional, Renovada, Tecnicista)]]",
        "[[- Acadêmico - Educação - Tendências Pedagógicas - Pedagogia Progressista (Libertadora de Paulo Freire, Crítico-Social dos Conteúdos)]]",
        "[[- Acadêmico - Educação - Psicologia da Educação - Construtivismo (Piaget), Sociointeracionismo (Vygotsky) e Aprendizagem Significativa (Ausubel)]]",
        "[[- Profissional - Educação - Legislação Educacional - Lei de Diretrizes e Bases da Educação Nacional (LDB - Lei 9.394/96)]]",
        "[[- Profissional - Educação - Legislação Educacional - Base Nacional Comum Curricular (BNCC) e Novo Ensino Médio]]",
        "[[- Profissional - Educação - Didática e Metodologia - Planejamento de Ensino (Plano de Aula, de Unidade e de Curso)]]",
        "[[- Profissional - Educação - Didática e Metodologia - Instrumentos de Avaliação (Diagnóstica, Formativa e Somativa)]]",
        "[[- Profissional - Educação - Gestão Escolar - Projeto Político-Pedagógico (PPP) e Gestão Democrática]]",
        "[[- Profissional - Educação - Educação Especial e Inclusiva - Atendimento Educacional Especializado (AEE) e Tecnologias Assistivas]]",

        // =====================================================================
        // 400 / 410 - LINGUÍSTICA GERAL, TEORIA DA LINGUAGEM E FILOLOGIA
        // =====================================================================
        "[[- Acadêmico - Linguística - Fundamentos da Linguística - Estruturalismo (Signo, Significante e Significado de Saussure)]]",
        "[[- Acadêmico - Linguística - Fundamentos da Linguística - Gramática Gerativa (A Estrutura Profunda de Chomsky)]]",
        "[[- Acadêmico - Linguística - Fonética e Fonologia - Aparelho Fonador e Ponto de Articulação das Consoantes]]",
        "[[- Acadêmico - Linguística - Fonética e Fonologia - Fonemas, Alones e Arquifonemas]]",
        "[[- Acadêmico - Linguística - Morfologia Linguística - Morfemas Lexicais e Gramaticais (Raiz, Afixos, Desinências)]]",
        "[[- Acadêmico - Linguística - Sintaxe Teórica - Árvores Sintáticas e Teoria X-barra]]",
        "[[- Acadêmico - Linguística - Semântica - Relações Semânticas (Sinonímia, Antonímia, Polissemia, Homonímia)]]",
        "[[- Acadêmico - Linguística - Pragmática - Teoria dos Atos de Fala (Locucionário, Ilocucionário, Perlocucionário)]]",
        "[[- Acadêmico - Linguística - Pragmática - Máximas Conversacionais de Grice (Quantidade, Qualidade, Relação, Modo)]]",
        "[[- Acadêmico - Linguística - Sociolinguística - Variação Linguística (Diatópica, Diastrática, Diafásica, Diacrônica)]]",
        "[[- Acadêmico - Linguística - Sociolinguística - Preconceito Linguístico e Diglossia]]",
        "[[- Acadêmico - Linguística - Psicolinguística - Aquisição da Linguagem e Bilinguismo Cognitivo]]",
        "[[- Acadêmico - Filologia - História da Língua - Etimologia e Evolução do Latim Vulgar para as Línguas Românicas]]",

        // =====================================================================
        // LINGUÍSTICA APLICADA E TRADUÇÃO (APLICAÇÃO FUNCIONAL)
        // =====================================================================
        "[[- Profissional - Linguística - Análise do Discurso - Análise Crítica do Discurso (ACD) e Ideologia Midiática]]",
        "[[- Profissional - Linguística - Análise do Discurso - Formações Discursivas e Interdiscursividade (Pêcheux)]]",
        "[[- Profissional - Linguística - Tradução e Interpretação - Técnicas de Tradução (Empréstimo, Calque, Transposição, Modulação)]]",
        "[[- Profissional - Linguística - Tradução e Interpretação - Uso de CAT Tools (Trados, MemoQ) e Tradução Automática]]",
        "[[- Profissional - Linguística - Ensino de Línguas - Abordagem Comunicativa e Ensino Baseado em Tarefas (TBLT)]]",

        // =====================================================================
        // 469 - LÍNGUA PORTUGUESA: FONÉTICA, ORTOGRAFIA E MORFOLOGIA
        // =====================================================================
        "[[- Acadêmico - Português - Fonologia e Ortografia - Encontros Vocálicos (Ditongo, Tritongo, Hiato) e Consonantais]]",
        "[[- Acadêmico - Português - Fonologia e Ortografia - Dígrafos Vocaicos e Consonantais]]",
        "[[- Acadêmico - Português - Fonologia e Ortografia - Divisão Silábica e Translineação]]",
        "[[- Acadêmico - Português - Fonologia e Ortografia - Acentuação Gráfica (Regras das Oxítonas, Paroxítonas e Proparoxítonas)]]",
        "[[- Acadêmico - Português - Fonologia e Ortografia - Novo Acordo Ortográfico (Fim do Trema, Novas Regras de Hífen)]]",
        "[[- Acadêmico - Português - Fonologia e Ortografia - Uso dos Porquês, Mal/Mau, Onde/Aonde, Mas/Mais]]",
        "[[- Acadêmico - Português - Morfologia - Formação de Palavras (Derivação Prefixal, Sufixal, Parassintética, Imprópria)]]",
        "[[- Acadêmico - Português - Morfologia - Classes: Substantivos (Classificação, Gênero, Número e Grau)]]",
        "[[- Acadêmico - Português - Morfologia - Classes: Adjetivos (Locuções Adjetivas e Superlativos)]]",
        "[[- Acadêmico - Português - Morfologia - Classes: Pronomes (Pessoais, Possessivos, Demonstrativos, Indefinidos)]]",
        "[[- Acadêmico - Português - Morfologia - Classes: Pronomes Relativos (Que, Quem, Qual, Onde, Cujo)]]",
        "[[- Acadêmico - Português - Morfologia - Classes: Verbos (Tempos e Modos do Indicativo, Subjuntivo e Imperativo)]]",
        "[[- Acadêmico - Português - Morfologia - Classes: Verbos (Vozes Verbais: Ativa, Passiva Analítica/Sintética, Reflexiva)]]",
        "[[- Acadêmico - Português - Morfologia - Classes: Verbos (Verbos Irregulares, Defectivos e Anômalos)]]",
        "[[- Acadêmico - Português - Morfologia - Classes: Conjunções (Coordenativas e Subordinativas)]]",
        "[[- Acadêmico - Português - Morfologia - Classes: Preposições, Advérbios e Interjeições]]",

        // =====================================================================
        // 469 - LÍNGUA PORTUGUESA: SINTAXE E ESTILÍSTICA
        // =====================================================================
        "[[- Acadêmico - Português - Sintaxe do Período Simples - Tipos de Sujeito (Simples, Composto, Oculto, Indeterminado, Inexistente)]]",
        "[[- Acadêmico - Português - Sintaxe do Período Simples - Predicação Verbal (VI, VTD, VTI, VTDI, Verbo de Ligação)]]",
        "[[- Acadêmico - Português - Sintaxe do Período Simples - Complementos Verbais (Objeto Direto e Indireto) e Predicativos]]",
        "[[- Acadêmico - Português - Sintaxe do Período Simples - Adjunto Adnominal vs. Complemento Nominal]]",
        "[[- Acadêmico - Português - Sintaxe do Período Simples - Adjunto Adverbial, Aposto e Vocativo]]",
        "[[- Acadêmico - Português - Sintaxe do Período Composto - Orações Coordenadas (Sindéticas e Assindéticas)]]",
        "[[- Acadêmico - Português - Sintaxe do Período Composto - Orações Subordinadas Substantivas]]",
        "[[- Acadêmico - Português - Sintaxe do Período Composto - Orações Subordinadas Adjetivas (Restritivas e Explicativas)]]",
        "[[- Acadêmico - Português - Sintaxe do Período Composto - Orações Subordinadas Adverbiais (Causais, Condicionais, Concessivas, etc.)]]",
        "[[- Acadêmico - Português - Sintaxe de Concordância - Concordância Verbal (Regra Geral e Casos Especiais com 'Haver' e 'Fazer')]]",
        "[[- Acadêmico - Português - Sintaxe de Concordância - Concordância Nominal (Casos com 'Anexo', 'Meio', 'Bastante', 'Proibido')]]",
        "[[- Acadêmico - Português - Sintaxe de Regência - Regência Verbal (Verbos Assistir, Visar, Aspirar, Preferir)]]",
        "[[- Acadêmico - Português - Sintaxe de Regência - Regência Nominal]]",
        "[[- Acadêmico - Português - Sintaxe - Crase (Regra Geral, Casos Proibidos, Obrigatórios e Facultativos)]]",
        "[[- Acadêmico - Português - Sintaxe de Colocação - Colocação Pronominal (Próclise, Mesóclise e Ênclise)]]",
        "[[- Acadêmico - Português - Pontuação - Uso da Vírgula (Deslocamento de Adjuntos, Enumerações, Vocativos)]]",
        "[[- Acadêmico - Português - Pontuação - Uso do Ponto e Vírgula, Dois-Pontos e Aspas]]",

        // =====================================================================
        // INTERPRETAÇÃO, REDAÇÃO E COMUNICAÇÃO APLICADA
        // =====================================================================
        "[[- Acadêmico - Português - Interpretação de Texto - Tipologia Textual (Narrações, Descrições, Dissertações, Injunções)]]",
        "[[- Acadêmico - Português - Interpretação de Texto - Gêneros Textuais (Notícia, Crônica, Artigo de Opinião, Editorial)]]",
        "[[- Acadêmico - Português - Interpretação de Texto - Coesão Referencial (Anáfora e Catáfora)]]",
        "[[- Acadêmico - Português - Interpretação de Texto - Coesão Sequencial (Uso de Conectivos e Articuladores do Discurso)]]",
        "[[- Acadêmico - Português - Interpretação de Texto - Coerência Textual (Princípio da Não Contradição)]]",
        "[[- Acadêmico - Português - Interpretação de Texto - Funções da Linguagem (Referencial, Emotiva, Conativa, Metalinguística, Fática, Poética)]]",
        "[[- Acadêmico - Português - Interpretação de Texto - Figuras de Linguagem (Metáfora, Metonímia, Paradoxo, Ironia, Eufemismo)]]",
        "[[- Acadêmico - Português - Interpretação de Texto - Níveis de Leitura (Compreensão Literal, Inferencial e Crítica)]]",
        "[[- Profissional - Português - Redação Oficial - Manual de Redação da Presidência da República (Pronomes de Tratamento)]]",
        "[[- Profissional - Português - Redação Oficial - Estrutura do Padrão Ofício (Aviso, Memorando e Ofício)]]",
        "[[- Profissional - Português - Redação Oficial - Atributos da Redação Oficial (Clareza, Concisão, Impessoalidade)]]",
        "[[- Profissional - Português - Comunicação Empresarial - Elaboração de E-mails Corporativos, Relatórios e Atas]]",

        // =====================================================================
        // LÍNGUAS DE SINAIS E INCLUSÃO (LIBRAS)
        // =====================================================================
        "[[- Profissional - LIBRAS - Estrutura da Língua - Parâmetros da LIBRAS (Configuração de Mão, Ponto de Articulação, Movimento)]]",
        "[[- Profissional - LIBRAS - Estrutura da Língua - Orientação/Direcionalidade e Expressões Não Manuais (Facial/Corporal)]]",
        "[[- Profissional - LIBRAS - Gramática - Estrutura Sintática (Tópico-Comentário e Ordem SOV/SVO)]]",
        "[[- Profissional - LIBRAS - Vocabulário - Alfabeto Manual (Datilologia) e Numerais em LIBRAS]]",
        "[[- Profissional - LIBRAS - Legislação e Inclusão - Lei de LIBRAS (Lei nº 10.436/2002) e o Papel do Intérprete (TILS)]]",

        // =====================================================================
        // 420 - LÍNGUA INGLESA E IDIOMAS ESTRANGEIROS
        // =====================================================================
        "[[- Acadêmico - Inglês - Gramática - Simple Present e Present Continuous (Verbos Auxiliares Do/Does, Am/Is/Are)]]",
        "[[- Acadêmico - Inglês - Gramática - Simple Past e Past Continuous (Verbos Regulares e Irregulares)]]",
        "[[- Acadêmico - Inglês - Gramática - Present Perfect e Present Perfect Continuous (Uso de Since, For, Yet, Already)]]",
        "[[- Acadêmico - Inglês - Gramática - Future Tenses (Will, Going to, Future Continuous)]]",
        "[[- Acadêmico - Inglês - Gramática - Modal Verbs (Can, Could, May, Might, Must, Should)]]",
        "[[- Acadêmico - Inglês - Gramática - Conditionals (Zero, First, Second, Third and Mixed Conditionals)]]",
        "[[- Acadêmico - Inglês - Gramática - Passive Voice (Voz Passiva em Diferentes Tempos Verbais)]]",
        "[[- Acadêmico - Inglês - Gramática - Reported Speech (Discurso Indireto e Mudança de Tempos Verbais)]]",
        "[[- Acadêmico - Inglês - Gramática - Nouns and Pronouns (Countable/Uncountable, Quantifiers: Much/Many, Some/Any)]]",
        "[[- Acadêmico - Inglês - Gramática - Adjectives and Adverbs (Comparatives and Superlatives)]]",
        "[[- Acadêmico - Inglês - Vocabulário - Phrasal Verbs, Idioms e Collocations Comuns]]",
        "[[- Acadêmico - Inglês - Vocabulário - Falsos Cognatos (False Friends: Pretend, Actually, Fabric)]]",
        "[[- Profissional - Inglês - Inglês Instrumental - Técnicas de Leitura Rápida (Skimming e Scanning)]]",
        "[[- Profissional - Inglês - Inglês Instrumental - Inferência Lexical e Marcadores Discursivos (Linkers)]]",
        "[[- Profissional - Inglês - Business English - Terminologia Corporativa, E-mails Formais e Apresentações Profissionais]]",
        "[[- Profissional - Inglês - Certificações - Preparação Estratégica para TOEFL, IELTS e Cambridge Exams]]",

        // =====================================================================
        // 460 - LÍNGUA ESPANHOLA E OUTRAS LÍNGUAS CLÁSSICAS
        // =====================================================================
        "[[- Acadêmico - Espanhol - Gramática e Ortografia - Regras de Acentuação (Agudas, Llanas, Esdrújulas)]]",
        "[[- Acadêmico - Espanhol - Gramática e Ortografia - Artigos (El, La, Los, Las, Lo Neutro) e Contrações (Al, Del)]]",
        "[[- Acadêmico - Espanhol - Gramática e Ortografia - Verbos Regulares e Irregulares no Presente de Indicativo (Diptongación)]]",
        "[[- Acadêmico - Espanhol - Gramática e Ortografia - Pretérito Indefinido vs. Pretérito Perfecto Compuesto]]",
        "[[- Acadêmico - Espanhol - Vocabulário - Heterossemânticos (Falsos Amigos: Embarazada, Exquisito, Pelado)]]",
        "[[- Acadêmico - Espanhol - Vocabulário - Heterogenéricos (El Viaje, La Leche, La Sangre)]]",
        "[[- Acadêmico - Espanhol - Vocabulário - Heterotônicos (Magia, Cerebro, Policía)]]",
        "[[- Profissional - Espanhol - Espanhol Instrumental - Compreensão de Textos Acadêmicos e Jornalísticos (DELE/SIELE)]]",
        "[[- Acadêmico - Línguas Clássicas - Latim - Casos Gramaticais (Nominativo, Genitivo, Dativo, Acusativo, Ablativo, Vocativo)]]",
        "[[- Acadêmico - Línguas Clássicas - Latim - As Cinco Declinações Nominativas e Radicais Etimológicos]]",
        "[[- Acadêmico - Línguas Clássicas - Grego Antigo - O Alfabeto Grego e a Formação de Prefixos e Sufixos Científicos]]",

        // =====================================================================
        // 510 - MATEMÁTICA: ARITMÉTICA, ÁLGEBRA E GEOMETRIA
        // =====================================================================
        "[[- Acadêmico - Matemática - Aritmética - Operações Básicas, Frações e Números Decimais]]",
        "[[- Acadêmico - Matemática - Aritmética - Mínimo Múltiplo Comum (MMC) e Máximo Divisor Comum (MDC)]]",
        "[[- Acadêmico - Matemática - Aritmética - Razão, Proporção e Regra de Três (Simples e Composta)]]",
        "[[- Acadêmico - Matemática - Teoria dos Números - Números Primos, Fatoração e Crivo de Eratóstenes]]",
        "[[- Acadêmico - Matemática - Álgebra Elementar - Produtos Notáveis e Fatoração Algébrica]]",
        "[[- Acadêmico - Matemática - Álgebra Elementar - Equações e Inequações do 1º e 2º Grau (Fórmula de Bhaskara)]]",
        "[[- Acadêmico - Matemática - Funções - Domínio, Imagem e Estudo do Sinal (Função Afim e Quadrática)]]",
        "[[- Acadêmico - Matemática - Funções - Funções Exponenciais, Logarítmicas e Propriedades dos Logaritmos]]",
        "[[- Acadêmico - Matemática - Álgebra Linear - Matrizes (Determinantes, Teorema de Laplace e Regra de Sarrus)]]",
        "[[- Acadêmico - Matemática - Álgebra Linear - Sistemas Lineares (Regra de Cramer e Escalonamento)]]",
        "[[- Acadêmico - Matemática - Números Complexos - Forma Algébrica, Forma Trigonométrica e Fórmula de Moivre]]",
        "[[- Acadêmico - Matemática - Geometria Plana - Teorema de Pitágoras, Semelhança de Triângulos e Relações Métricas]]",
        "[[- Acadêmico - Matemática - Geometria Plana - Polígonos Regulares, Ângulos Internos e Cálculo de Áreas]]",
        "[[- Acadêmico - Matemática - Geometria Plana - Circunferência e Círculo (Comprimento, Área e Setor Circular)]]",
        "[[- Acadêmico - Matemática - Geometria Espacial - Poliedros de Platão e Relação de Euler]]",
        "[[- Acadêmico - Matemática - Geometria Espacial - Prismas, Pirâmides e Cilindros (Área Superficial e Volume)]]",
        "[[- Acadêmico - Matemática - Geometria Espacial - Cones e Esferas (Troncos e Volumes)]]",
        "[[- Acadêmico - Matemática - Geometria Analítica - Estudo do Ponto e da Reta (Coeficiente Angular e Linear)]]",
        "[[- Acadêmico - Matemática - Geometria Analítica - Cônicas (Circunferência, Elipse, Parábola e Hipérbole)]]",
        "[[- Acadêmico - Matemática - Trigonometria - Ciclo Trigonométrico (Seno, Cosseno e Tangente)]]",
        "[[- Acadêmico - Matemática - Trigonometria - Lei dos Senos, Lei dos Cossenos e Relação Fundamental]]",

        // =====================================================================
        // 510 - MATEMÁTICA: CÁLCULO, PROBABILIDADE E ESTATÍSTICA APLICADA
        // =====================================================================
        "[[- Acadêmico - Matemática - Análise Combinatória - Princípio Fundamental da Contagem (Fatorial)]]",
        "[[- Acadêmico - Matemática - Análise Combinatória - Permutações, Arranjos e Combinações Simples/Com Repetição]]",
        "[[- Acadêmico - Matemática - Probabilidade - Espaço Amostral, Eventos e Probabilidade Condicional]]",
        "[[- Acadêmico - Matemática - Cálculo Diferencial e Integral - Limites e Continuidade de Funções]]",
        "[[- Acadêmico - Matemática - Cálculo Diferencial e Integral - Derivadas (Regra da Cadeia e Regra do Produto/Quociente)]]",
        "[[- Acadêmico - Matemática - Cálculo Diferencial e Integral - Integrais Indefinidas e Definidas (Teorema Fundamental do Cálculo)]]",
        "[[- Acadêmico - Matemática - Cálculo Diferencial e Integral - Equações Diferenciais Ordinárias (EDOs)]]",
        "[[- Profissional - Matemática - Estatística Descritiva - Medidas de Tendência Central (Média, Moda e Mediana)]]",
        "[[- Profissional - Matemática - Estatística Descritiva - Medidas de Dispersão (Variância, Desvio Padrão e Coeficiente de Variação)]]",
        "[[- Profissional - Matemática - Estatística Inferencial - Distribuições de Probabilidade (Normal/Gaussiana, Binomial e Poisson)]]",
        "[[- Profissional - Matemática - Estatística Inferencial - Testes de Hipótese (Testes T de Student, Qui-Quadrado e ANOVA)]]",
        "[[- Profissional - Matemática - Matemática Financeira - Juros Simples, Compostos e Taxas Equivalentes]]",
        "[[- Profissional - Matemática - Matemática Financeira - Sistemas de Amortização (SAC, Tabela Price e SAM)]]",

        // =====================================================================
        // 520 - ASTRONOMIA E ASTROFÍSICA
        // =====================================================================
        "[[- Acadêmico - Astronomia - Mecânica Celeste - Leis de Kepler (Órbitas, Áreas e Períodos)]]",
        "[[- Acadêmico - Astronomia - Sistema Solar - Planetas Rochosos, Gasosos e Corpos Menores (Asteroides e Cometas)]]",
        "[[- Acadêmico - Astronomia - Astrofísica Estelar - Ciclo de Vida das Estrelas (Anãs Brancas, Supernovas e Buracos Negros)]]",
        "[[- Acadêmico - Astronomia - Cosmologia - Teoria do Big Bang, Radiação Cósmica de Fundo e Expansão do Universo]]",

        // =====================================================================
        // 530 - FÍSICA: MECÂNICA, TERMODINÂMICA E ELETROMAGNETISMO
        // =====================================================================
        "[[- Acadêmico - Física - Cinemática - Movimento Retilíneo Uniforme (MRU) e Uniformemente Variado (MRUV)]]",
        "[[- Acadêmico - Física - Cinemática - Lançamento Oblíquo, Horizontal e Queda Livre]]",
        "[[- Acadêmico - Física - Cinemática - Movimento Circular Uniforme (MCU) e Aceleração Centrípeta]]",
        "[[- Acadêmico - Física - Dinâmica - As Três Leis de Newton e Força de Atrito (Estático e Cinético)]]",
        "[[- Acadêmico - Física - Dinâmica - Força Elástica (Lei de Hooke) e Plano Inclinado]]",
        "[[- Acadêmico - Física - Trabalho e Energia - Energia Cinética, Potencial (Gravitacional e Elástica) e Teorema do Trabalho]]",
        "[[- Acadêmico - Física - Impulso e Quantidade de Movimento - Conservação da Quantidade de Movimento e Colisões]]",
        "[[- Acadêmico - Física - Gravitação Universal - Força Gravitacional de Newton e Campo Gravitacional]]",
        "[[- Acadêmico - Física - Estática e Hidrostática - Torque (Momento de uma Força) e Centro de Massa]]",
        "[[- Acadêmico - Física - Estática e Hidrostática - Pressão Atmosférica, Teorema de Stevin, Pascal e Empuxo (Arquimedes)]]",
        "[[- Acadêmico - Física - Termologia - Escalas Termométricas (Celsius, Kelvin, Fahrenheit) e Dilatação Térmica]]",
        "[[- Acadêmico - Física - Calorimetria - Calor Sensível, Calor Latente e Trocas de Calor]]",
        "[[- Acadêmico - Física - Termodinâmica - Estudo dos Gases Ideais (Equação de Clapeyron e Transformações Gasosas)]]",
        "[[- Acadêmico - Física - Termodinâmica - Primeira Lei (Conservação de Energia) e Segunda Lei (Entropia e Máquinas Térmicas)]]",
        "[[- Acadêmico - Física - Óptica Geométrica - Reflexão da Luz, Espelhos Planos e Esféricos (Côncavos e Convexos)]]",
        "[[- Acadêmico - Física - Óptica Geométrica - Refração da Luz (Lei de Snell-Descartes), Lentes e Instrumentos Ópticos]]",
        "[[- Acadêmico - Física - Ondulatória - Natureza e Propagação das Ondas (Frequência, Comprimento, Velocidade)]]",
        "[[- Acadêmico - Física - Ondulatória - Acústica (Qualidades Fisiológicas do Som e Efeito Doppler)]]",
        "[[- Acadêmico - Física - Eletrostática - Carga Elétrica, Processos de Eletrização e Lei de Coulomb]]",
        "[[- Acadêmico - Física - Eletrostática - Campo Elétrico, Potencial Elétrico e Superfícies Equipotenciais]]",
        "[[- Acadêmico - Física - Eletrodinâmica - Corrente Elétrica, Tensão, Resistência e as Leis de Ohm]]",
        "[[- Acadêmico - Física - Eletrodinâmica - Associação de Resistores (Série, Paralelo e Mista) e Potência Elétrica]]",
        "[[- Acadêmico - Física - Eletrodinâmica - Geradores, Receptores, Capacitores e Leis de Kirchhoff]]",
        "[[- Acadêmico - Física - Eletromagnetismo - Campo Magnético gerado por Corrente (Fio, Espira e Solenoide)]]",
        "[[- Acadêmico - Física - Eletromagnetismo - Força Magnética (Regra da Mão Direita) e Indução Eletromagnética (Lei de Faraday-Lenz)]]",
        "[[- Acadêmico - Física - Física Moderna - Relatividade Restrita (Dilatação do Tempo e Contração do Espaço)]]",
        "[[- Acadêmico - Física - Física Moderna - Mecânica Quântica (Dualidade Onda-Partícula e Efeito Fotoelétrico)]]",
        "[[- Acadêmico - Física - Física Moderna - Física Nuclear (Radioatividade, Fissão e Fusão Nuclear)]]",

        // =====================================================================
        // 540 - QUÍMICA: GERAL, FÍSICO-QUÍMICA E ORGÂNICA
        // =====================================================================
        "[[- Acadêmico - Química - Estrutura Atômica - Modelos Atômicos (Dalton, Thomson, Rutherford, Bohr e Sommerfeld)]]",
        "[[- Acadêmico - Química - Estrutura Atômica - Partículas Subatômicas (Prótons, Nêutrons, Elétrons, Isótopos)]]",
        "[[- Acadêmico - Química - Tabela Periódica - Classificação dos Elementos e Propriedades Periódicas (Eletronegatividade e Raio Atômico)]]",
        "[[- Acadêmico - Química - Ligações Químicas - Regra do Octeto, Ligações Iônicas, Covalentes e Metálicas]]",
        "[[- Acadêmico - Química - Geometria Molecular - Teoria da Repulsão (VSEPR), Polaridade e Forças Intermoleculares]]",
        "[[- Acadêmico - Química - Funções Inorgânicas - Ácidos, Bases, Sais e Óxidos (Nomenclatura e Classificação)]]",
        "[[- Acadêmico - Química - Estequiometria - Leis Ponderais (Lavoisier e Proust) e Cálculo Estequiométrico (Mol, Massa Molar)]]",
        "[[- Acadêmico - Química - Soluções - Tipos de Soluções, Concentração Comum, Molaridade e Título]]",
        "[[- Acadêmico - Química - Propriedades Coligativas - Ebulioscopia, Crioscopia, Tonoscopia e Pressão Osmótica]]",
        "[[- Acadêmico - Química - Termoquímica - Reações Exotérmicas, Endotérmicas, Entalpia e Lei de Hess]]",
        "[[- Acadêmico - Química - Cinética Química - Velocidade das Reações, Energia de Ativação e Catalisadores]]",
        "[[- Acadêmico - Química - Equilíbrio Químico - Constante de Equilíbrio (Kc e Kp) e Princípio de Le Chatelier]]",
        "[[- Acadêmico - Química - Equilíbrio Químico - Equilíbrio Iônico, Cálculo de pH e pOH, e Soluções Tampão]]",
        "[[- Acadêmico - Química - Eletroquímica - Pilhas (Células Galvânicas, Ânodo, Cátodo e DDP)]]",
        "[[- Acadêmico - Química - Eletroquímica - Eletrólise Ígnea e Aquosa (Leis de Faraday)]]",
        "[[- Acadêmico - Química - Química Orgânica - Postulados de Kekulé, Hibridização do Carbono e Cadeias Carbônicas]]",
        "[[- Acadêmico - Química - Química Orgânica - Funções Hidrocarbonetos (Alcanos, Alcenos, Alcinos, Aromáticos)]]",
        "[[- Acadêmico - Química - Química Orgânica - Funções Oxigenadas (Álcoois, Fenóis, Éteres, Aldeídos, Cetonas, Ácidos Carboxílicos e Ésteres)]]",
        "[[- Acadêmico - Química - Química Orgânica - Funções Nitrogenadas (Aminas, Amidas, Nitrilas e Nitrocompostos)]]",
        "[[- Acadêmico - Química - Química Orgânica - Isomeria Plana (Cadeia, Posição, Função, Compensação e Tautomeria)]]",
        "[[- Acadêmico - Química - Química Orgânica - Isomeria Espacial (Geométrica Cis-Trans/E-Z e Isomeria Óptica/Quiralidade)]]",
        "[[- Acadêmico - Química - Química Orgânica - Reações Orgânicas (Adição, Substituição, Eliminação e Oxidação)]]",
        "[[- Profissional - Química - Química Analítica - Métodos Volumétricos (Titulação Ácido-Base e Redox)]]",
        "[[- Profissional - Química - Química Analítica - Métodos Instrumentais (Cromatografia e Espectrofotometria)]]",

        // =====================================================================
        // 550 / 560 - CIÊNCIAS DA TERRA, GEOCIÊNCIAS E PALEONTOLOGIA
        // =====================================================================
        "[[- Acadêmico - Geociências - Geologia - Estrutura Interna da Terra (Crosta, Manto, Núcleo)]]",
        "[[- Acadêmico - Geociências - Geologia - Tectônica de Placas, Deriva Continental e Sismos]]",
        "[[- Acadêmico - Geociências - Mineralogia e Petrologia - Rochas Ígneas, Metamórficas, Sedimentares e o Ciclo das Rochas]]",
        "[[- Profissional - Geociências - Meteorologia e Climatologia - Circulação Atmosférica, Massas de Ar e Previsão do Tempo]]",
        "[[- Acadêmico - Geociências - Oceanografia - Relevo Submarino, Correntes Marítimas e Marés]]",
        "[[- Acadêmico - Geociências - Paleontologia - Fósseis, Tafonomia e a Escala de Tempo Geológico (Éons, Eras, Períodos)]]",

        // =====================================================================
        // 570 / 580 / 590 - CIÊNCIAS BIOLÓGICAS (BIOLOGIA, BOTÂNICA E ZOOLOGIA)
        // =====================================================================
        "[[- Acadêmico - Biologia - Bioquímica - Água, Sais Minerais, Carboidratos e Lipídios]]",
        "[[- Acadêmico - Biologia - Bioquímica - Proteínas (Estrutura, Função e Enzimas)]]",
        "[[- Acadêmico - Biologia - Bioquímica - Ácidos Nucleicos (DNA, RNA e Replicação)]]",
        "[[- Acadêmico - Biologia - Citologia - Membrana Plasmática (Estrutura e Transportes: Osmose, Difusão, Bomba de Sódio/Potássio)]]",
        "[[- Acadêmico - Biologia - Citologia - Organelas Citoplasmáticas (Mitocôndrias, Ribossomos, Lisossomos, Complexo de Golgi)]]",
        "[[- Acadêmico - Biologia - Citologia - Bioenergética (Respiração Celular, Fermentação e Fotossíntese)]]",
        "[[- Acadêmico - Biologia - Citologia - Divisão Celular (Ciclo Celular, Mitose e Meiose)]]",
        "[[- Acadêmico - Biologia - Histologia Animal - Tecidos Epitelial, Conjuntivo, Muscular e Nervoso]]",
        "[[- Acadêmico - Biologia - Genética - Primeira e Segunda Lei de Mendel (Monoibridismo e Diibridismo)]]",
        "[[- Acadêmico - Biologia - Genética - Alelos Múltiplos (Sistema ABO e Fator Rh) e Herança Ligada ao Sexo]]",
        "[[- Acadêmico - Biologia - Genética - Linkage (Ligação Gênica) e Mutações Cromossômicas (Aneuploidias)]]",
        "[[- Acadêmico - Biologia - Evolução - Evidências da Evolução (Órgãos Homólogos/Análogos e Órgãos Vestigiais)]]",
        "[[- Acadêmico - Biologia - Evolução - Teorias Evolutivas (Lamarckismo, Darwinismo e Teoria Sintética/Neodarwinismo)]]",
        "[[- Acadêmico - Biologia - Evolução - Genética de Populações (Teorema de Hardy-Weinberg) e Especiação]]",
        "[[- Acadêmico - Biologia - Ecologia - Conceitos Básicos (Habitat, Nicho Ecológico, População e Comunidade)]]",
        "[[- Acadêmico - Biologia - Ecologia - Cadeias e Teias Alimentares, e Níveis Tróficos]]",
        "[[- Acadêmico - Biologia - Ecologia - Dinâmica de Populações e Relações Ecológicas (Harmônicas e Desarmônicas)]]",
        "[[- Acadêmico - Biologia - Ecologia - Ciclos Biogeoquímicos (Ciclo da Água, Carbono, Nitrogênio e Fósforo)]]",
        "[[- Acadêmico - Biologia - Ecologia - Sucessão Ecológica e Biomas Terrestres/Brasileiros]]",
        "[[- Profissional - Biologia - Biotecnologia - Tecnologia do DNA Recombinante, Clonagem e Transgênicos]]",
        "[[- Profissional - Biologia - Virologia e Microbiologia - Estrutura Viral, Bacterioses e Resistência a Antibióticos]]",
        "[[- Acadêmico - Botânica - Taxonomia Vegetal - Briófitas, Pteridófitas, Gimnospermas e Angiospermas]]",
        "[[- Acadêmico - Botânica - Morfologia Vegetal - Raiz, Caule, Folha, Flor, Fruto e Semente]]",
        "[[- Acadêmico - Botânica - Fisiologia Vegetal - Condução de Seiva (Xilema e Floema) e Transpiração]]",
        "[[- Acadêmico - Botânica - Fisiologia Vegetal - Hormônios Vegetais (Auxinas, Giberelinas, Etileno) e Fototropismo]]",
        "[[- Acadêmico - Zoologia - Invertebrados - Poríferos, Cnidários, Platelmintos e Nematelmintos]]",
        "[[- Acadêmico - Zoologia - Invertebrados - Moluscos, Anelídeos, Artrópodes (Insetos, Aracnídeos, Crustáceos) e Equinodermos]]",
        "[[- Acadêmico - Zoologia - Cordados e Vertebrados - Peixes (Condrictes e Osteíctes) e Anfíbios]]",
        "[[- Acadêmico - Zoologia - Cordados e Vertebrados - Répteis, Aves e Mamíferos (Anatomia Comparada)]]",

        // =====================================================================
        // 610 - MEDICINA, ENFERMAGEM E SAÚDE PÚBLICA
        // =====================================================================
        "[[- Acadêmico - Medicina - Anatomia Humana - Sistema Nervoso Central e Periférico]]",
        "[[- Acadêmico - Medicina - Anatomia Humana - Sistema Cardiovascular e Circulação Sistêmica/Pulmonar]]",
        "[[- Acadêmico - Medicina - Fisiologia Humana - Sistema Endócrino e Eixo Hipotálamo-Hipófise]]",
        "[[- Acadêmico - Medicina - Fisiologia Humana - Fisiologia Renal e Néfrons]]",
        "[[- Profissional - Medicina - Saúde Pública - Epidemiologia, Endemias e Notificação Compulsória]]",
        "[[- Profissional - Medicina - Saúde Pública - Sistema Único de Saúde (SUS) e Princípios Doutrinários (Lei 8.080/90)]]",
        "[[- Profissional - Medicina - Clínica Médica - Semiologia, Anamnese e Exame Físico]]",
        "[[- Profissional - Medicina - Clínica Médica - Interpretação de Exames Laboratoriais (Hemograma, Lipidograma)]]",
        "[[- Profissional - Medicina - Pediatria - Puericultura e Marcos do Desenvolvimento Infantil]]",
        "[[- Profissional - Medicina - Ginecologia e Obstetrícia - Ciclo Menstrual e Assistência ao Pré-Natal]]",
        "[[- Profissional - Medicina - Cirurgia Geral - Assepsia, Antissepsia e Tempos Cirúrgicos (Diérese, Hemostasia, Síntese)]]",
        "[[- Profissional - Enfermagem - Fundamentos de Enfermagem - Sistematização da Assistência de Enfermagem (SAE)]]",
        "[[- Profissional - Enfermagem - Urgência e Emergência - Protocolo de Manchester e Suporte Básico de Vida (BLS)]]",
        "[[- Profissional - Enfermagem - Centro Cirúrgico - Instrumentação Cirúrgica e Cuidados Pós-Anestésicos (RPA)]]",
        "[[- Profissional - Farmácia - Farmacologia Básica - Farmacocinética (Absorção, Distribuição, Metabolismo, Excreção)]]",
        "[[- Profissional - Farmácia - Farmacologia Clínica - Antibioticoterapia e Mecanismos de Resistência Bacteriana]]",
        "[[- Profissional - Farmácia - Farmacotécnica - Formas Farmacêuticas Líquidas e Sólidas (Xaropes, Cápsulas, Pomadas)]]",
        "[[- Profissional - Odontologia - Cariologia - Formação do Biofilme Dental e Prevenção com Flúor]]",
        "[[- Profissional - Odontologia - Periodontia - Doença Periodontal e Raspagem Supra/Subgengival]]",
        "[[- Profissional - Odontologia - Endodontia - Acesso, Instrumentação e Obturação do Canal Radicular]]",
        "[[- Profissional - Nutrição - Nutrição Clínica - Avaliação Antropométrica e Cálculo de IMC]]",
        "[[- Profissional - Nutrição - Dietoterapia - Terapia Nutricional Enteral e Parenteral em Pacientes Críticos]]",
        "[[- Profissional - Fisioterapia - Ortopedia e Traumatologia - Reabilitação de Fraturas e Lesões Ligamentares]]",
        "[[- Profissional - Fisioterapia - Fisioterapia Respiratória - Ventilação Mecânica Não Invasiva (VNI)]]",

        // =====================================================================
        // 620 - ENGENHARIA CIVIL, MECÂNICA, ELÉTRICA E AFINS
        // =====================================================================
        "[[- Acadêmico - Engenharia - Ciência dos Materiais - Estruturas Cristalinas e Defeitos em Metais]]",
        "[[- Profissional - Engenharia Civil - Resistência dos Materiais - Tensão, Deformação e Lei de Hooke Aplicada]]",
        "[[- Profissional - Engenharia Civil - Estruturas de Concreto - Dimensionamento de Vigas e Pilares (NBR 6118)]]",
        "[[- Profissional - Engenharia Civil - Estruturas Metálicas - Ligações Parafusadas e Soldadas (NBR 8800)]]",
        "[[- Profissional - Engenharia Civil - Geotecnia - Mecânica dos Solos, Sondagem SPT e Muros de Arrimo]]",
        "[[- Profissional - Engenharia Civil - Instalações Prediais - Instalações Hidrossanitárias (Água Fria, Quente e Esgoto)]]",
        "[[- Profissional - Engenharia Mecânica - Termodinâmica Aplicada - Ciclos de Potência (Otto, Diesel, Brayton)]]",
        "[[- Profissional - Engenharia Mecânica - Mecânica dos Fluidos - Equação de Bernoulli e Perda de Carga em Tubulações]]",
        "[[- Profissional - Engenharia Mecânica - Elementos de Máquinas - Dimensionamento de Eixos, Engrenagens e Rolamentos]]",
        "[[- Profissional - Engenharia Mecânica - Refrigeração e Ar Condicionado - Ciclo de Compressão a Vapor e Carta Psicrométrica]]",
        "[[- Profissional - Engenharia Elétrica - Eletrotécnica - Circuitos de Corrente Alternada e Sistemas Trifásicos]]",
        "[[- Profissional - Engenharia Elétrica - Eletrotécnica - Triângulo de Potências e Correção do Fator de Potência]]",
        "[[- Profissional - Engenharia Elétrica - Máquinas Elétricas - Motores de Indução Trifásicos e Transformadores]]",
        "[[- Profissional - Engenharia Elétrica - Instalações Elétricas - Projeto Luminotécnico e Quadro de Distribuição (NBR 5410)]]",
        "[[- Profissional - Engenharia Eletrônica - Eletrônica Analógica - Diodos, Amplificadores Operacionais e Transistores (BJT/MOSFET)]]",
        "[[- Profissional - Engenharia Eletrônica - Eletrônica Digital - Portas Lógicas, Flip-Flops e Microcontroladores (Arduino/PIC)]]",
        "[[- Profissional - Engenharia de Telecomunicações - Redes de Transmissão - Modulação AM/FM, Multiplexação e Fibras Ópticas]]",

        // =====================================================================
        // 630 - AGRICULTURA, ZOOTECNIA E MEDICINA VETERINÁRIA
        // =====================================================================
        "[[- Profissional - Agronomia - Ciência do Solo - Pedologia, Perfil do Solo e Classificação Brasileira de Solos]]",
        "[[- Profissional - Agronomia - Fertilidade do Solo - Correção de Acidez (Calagem) e Adubação NPK]]",
        "[[- Profissional - Agronomia - Fitotecnia - Manejo Integrado de Pragas (MIP) e Uso de Defensivos Agrícolas]]",
        "[[- Profissional - Agronomia - Engenharia Agrícola - Sistemas de Irrigação (Gotejamento, Aspersão, Pivô Central)]]",
        "[[- Profissional - Medicina Veterinária - Clínica de Pequenos Animais - Doenças Infecciosas (Cinomose, Parvovirose, Leishmaniose)]]",
        "[[- Profissional - Medicina Veterinária - Clínica de Grandes Animais - Cólica Equina e Mastite Bovina]]",
        "[[- Profissional - Zootecnia - Nutrição Animal - Formulação de Rações para Monogástricos e Ruminantes]]",
        "[[- Profissional - Zootecnia - Melhoramento Genético Animal - Inseminação Artificial (IA) e Transferência de Embriões (TE)]]",
        "[[- Profissional - Zootecnia - Avicultura e Suinocultura - Manejo de Instalações, Ambiência e Bem-Estar Animal]]",

        // =====================================================================
        // 640 - ECONOMIA DOMÉSTICA, GASTRONOMIA E HOTELARIA
        // =====================================================================
        "[[- Profissional - Gastronomia - Técnicas Culinárias - Cortes Clássicos de Vegetais (Julienne, Brunoise, Mirepoix)]]",
        "[[- Profissional - Gastronomia - Técnicas Culinárias - Bases Culinárias (Fundos de Cozimento, Roux e Molhos Mãe)]]",
        "[[- Profissional - Gastronomia - Segurança Alimentar - Boas Práticas de Manipulação e Controle de Temperatura]]",
        "[[- Profissional - Hotelaria - Gestão Hoteleira - Front Office (Recepção), Governança e Revenue Management]]",
        "[[- Profissional - Moda e Vestuário - Design de Moda - Modelagem Plana Industrial e Moulage (Draping)]]",

        // =====================================================================
        // 650 - ADMINISTRAÇÃO CORPORATIVA E NEGÓCIOS (APLICAÇÃO DE MERCADO)
        // =====================================================================
        "[[- Profissional - Gestão de Negócios - Marketing Estratégico - Composto de Marketing (Os 4 Ps) e Segmentação de Mercado]]",
        "[[- Profissional - Gestão de Negócios - Marketing Digital - Inbound Marketing, Funil de Vendas e Copywriting]]",
        "[[- Profissional - Gestão de Negócios - Recursos Humanos - Recrutamento, Seleção e Onboarding de Talentos]]",
        "[[- Profissional - Gestão de Negócios - Recursos Humanos - Avaliação de Desempenho (360 Graus) e Plano de Cargos e Salários]]",
        "[[- Profissional - Gestão de Negócios - Gestão de Projetos - Metodologias Ágeis (Scrum: Sprints, Product Backlog, Daily)]]",
        "[[- Profissional - Gestão de Negócios - Gestão de Projetos - Metodologias Preditivas (Guia PMBOK e Gráfico de Gantt)]]",
        "[[- Profissional - Gestão de Negócios - Logística Empresarial - Gestão de Estoques, Curva ABC e Cross-Docking]]",
        "[[- Profissional - Contabilidade Gerencial - Análise de Custos - Custeio Baseado em Atividades (ABC) e Ponto de Equilíbrio]]",
        "[[- Profissional - Relações Públicas - Comunicação Corporativa - Gestão de Crise de Imagem e Assessoria de Imprensa]]",

        // =====================================================================
        // 660 A 690 - INDÚSTRIA, ENGENHARIA QUÍMICA E CONSTRUÇÃO
        // =====================================================================
        "[[- Profissional - Engenharia Química - Operações Unitárias - Destilação Fracionada, Extração Líquido-Líquido e Filtração]]",
        "[[- Profissional - Engenharia Química - Cinética de Reatores - Reatores Batelada, CSTR e PFR]]",
        "[[- Profissional - Ciência dos Alimentos - Tecnologia de Alimentos - Processos Térmicos (Pasteurização, UHT, Liofilização)]]",
        "[[- Profissional - Engenharia de Produção - Gestão da Qualidade - Ferramentas Lean Six Sigma (DMAIC e Diagrama de Ishikawa)]]",
        "[[- Profissional - Engenharia de Produção - Controle de Produção - Planejamento e Controle da Produção (PCP), MRP e Kanban]]",
        "[[- Profissional - Manufatura - Usinagem e Fabricação - Processos de Torneamento, Fresamento e Soldagem (TIG/MIG)]]",
        "[[- Profissional - Construção Civil - Orçamentação de Obras - Composição de Custos Unitários, BDI e Tabela SINAPI]]",
        "[[- Profissional - Construção Civil - Planejamento de Obras - Cronograma Físico-Financeiro e Curva S]]",

        // =====================================================================
        // 700 - TEORIA DA ARTE, ESTÉTICA E HISTÓRIA DA ARTE
        // =====================================================================
        "[[- Acadêmico - Artes Visuais - Teoria da Arte - Estética Filosófica e o Conceito do Belo]]",
        "[[- Acadêmico - Artes Visuais - Teoria da Arte - Semiótica da Imagem e Análise Visual]]",
        "[[- Acadêmico - Artes Visuais - História da Arte - Arte Pré-Histórica (Pinturas Rupestres)]]",
        "[[- Acadêmico - Artes Visuais - História da Arte - Antiguidade Clássica (Arte Grega e Romana)]]",
        "[[- Acadêmico - Artes Visuais - História da Arte - Idade Média (Arte Românica, Gótica e Bizantina)]]",
        "[[- Acadêmico - Artes Visuais - História da Arte - Renascimento Cultural (Perspectiva e Esfumato)]]",
        "[[- Acadêmico - Artes Visuais - História da Arte - Barroco e Rococó (Tenebrismo e Exagero Dramático)]]",
        "[[- Acadêmico - Artes Visuais - História da Arte - Século XIX (Neoclassicismo, Romantismo e Realismo)]]",
        "[[- Acadêmico - Artes Visuais - História da Arte - Vanguardas Europeias (Cubismo, Expressionismo, Surrealismo)]]",
        "[[- Acadêmico - Artes Visuais - História da Arte - Arte Moderna Brasileira (Semana de Arte de 22)]]",
        "[[- Acadêmico - Artes Visuais - História da Arte - Arte Contemporânea (Pop Art, Minimalismo, Arte Conceitual)]]",

        // =====================================================================
        // 710 / 720 - URBANISMO E ARQUITETURA
        // =====================================================================
        "[[- Profissional - Arquitetura - Planejamento Urbano - Plano Diretor, Zoneamento e Uso do Solo]]",
        "[[- Profissional - Arquitetura - Planejamento Urbano - Mobilidade Urbana e Cidades Inteligentes (Smart Cities)]]",
        "[[- Profissional - Arquitetura - Paisagismo - Projeto Paisagístico, Espécies Nativas e Arborização Urbana]]",
        "[[- Acadêmico - Arquitetura - História da Arquitetura - Ordens Clássicas (Dórica, Jônica, Coríntia)]]",
        "[[- Acadêmico - Arquitetura - História da Arquitetura - Arquitetura Moderna (Le Corbusier e Oscar Niemeyer)]]",
        "[[- Profissional - Arquitetura - Projeto Arquitetônico - Representação Gráfica (Planta Baixa, Cortes, Fachadas)]]",
        "[[- Profissional - Arquitetura - Projeto Arquitetônico - Metodologia BIM (Building Information Modeling)]]",
        "[[- Profissional - Arquitetura - Conforto Ambiental - Conforto Térmico (Ventilação Cruzada, Brise-Soleil, Inércia Térmica)]]",
        "[[- Profissional - Arquitetura - Conforto Ambiental - Conforto Acústico e Luminotécnico (Iluminação Natural e Zenital)]]",
        "[[- Profissional - Arquitetura - Legislação e Normas - Acessibilidade a Edificações (Norma NBR 9050)]]",
        "[[- Profissional - Arquitetura - Legislação e Normas - Código de Obras e Posturas Municipais]]",

        // =====================================================================
        // 730 / 740 / 750 / 760 - ARTES PLÁSTICAS, DESENHO E DESIGN (UX/UI)
        // =====================================================================
        "[[- Profissional - Artes Plásticas - Escultura - Técnicas de Modelagem, Entalhe e Fundição (Bronze/Gesso)]]",
        "[[- Profissional - Artes Plásticas - Pintura - Técnicas (Óleo sobre Tela, Aquarela, Acrílica, Fresco)]]",
        "[[- Profissional - Artes Plásticas - Gravura - Xilogravura, Litogravura e Serigrafia]]",
        "[[- Profissional - Design - Teoria das Cores - Círculo Cromático, RGB/CMYK e Psicologia das Cores]]",
        "[[- Profissional - Design - Design Gráfico - Tipografia (Serif, Sans-Serif, Kerning, Tracking)]]",
        "[[- Profissional - Design - Design Gráfico - Identidade Visual, Branding e Design de Embalagens]]",
        "[[- Profissional - Design - Desenho Técnico - Perspectiva Isométrica, Cavaleira e Ponto de Fuga]]",
        "[[- Profissional - Design - Desenho Industrial - Ergonomia e Design de Produto]]",
        "[[- Profissional - Design - UX Design - Pesquisa de Usuário (User Research) e Criação de Personas]]",
        "[[- Profissional - Design - UX Design - Arquitetura da Informação e Jornada do Usuário]]",
        "[[- Profissional - Design - UI Design - Wireframing, Prototipagem e Design Systems]]",
        "[[- Profissional - Design - UI Design - Heurísticas de Usabilidade (Jakob Nielsen) e Acessibilidade Digital]]",

        // =====================================================================
        // 770 - FOTOGRAFIA E CINEMA (AUDIOVISUAL)
        // =====================================================================
        "[[- Profissional - Audiovisual - Fotografia - Triângulo de Exposição (ISO, Diafragma/Abertura e Obturador/Velocidade)]]",
        "[[- Profissional - Audiovisual - Fotografia - Composição Fotográfica (Regra dos Terços, Linhas Guia e Profundidade de Campo)]]",
        "[[- Profissional - Audiovisual - Fotografia - Iluminação (Luz Dura/Suave, Key Light, Fill Light, Backlight)]]",
        "[[- Acadêmico - Audiovisual - História do Cinema - Cinema Mudo, Expressionismo Alemão e Neorrealismo Italiano]]",
        "[[- Acadêmico - Audiovisual - Teoria do Cinema - Linguagem Cinematográfica e Montagem Soviética (Eisenstein)]]",
        "[[- Profissional - Audiovisual - Produção Cinematográfica - Roteiro (A Jornada do Herói e Estrutura de Três Atos)]]",
        "[[- Profissional - Audiovisual - Produção Cinematográfica - Direção de Arte e Decupagem de Cenas]]",
        "[[- Profissional - Audiovisual - Produção Cinematográfica - Edição e Pós-Produção (Corte Seco, Fade, Correção de Cor/Color Grading)]]",

        // =====================================================================
        // 780 - MÚSICA E PRODUÇÃO MUSICAL
        // =====================================================================
        "[[- Acadêmico - Música - História da Música - Períodos Barroco, Clássico e Romântico (Bach, Mozart, Beethoven)]]",
        "[[- Acadêmico - Música - Teoria Musical - Pauta, Claves (Sol, Fá, Dó) e Figuras Rítmicas]]",
        "[[- Acadêmico - Música - Teoria Musical - Escalas Maiores, Menores e Modos Gregos]]",
        "[[- Acadêmico - Música - Harmonia e Contraponto - Formação de Acordes, Tríades, Tétrades e Campo Harmônico]]",
        "[[- Profissional - Música - Prática Instrumental - Técnicas de Execução (Corda, Sopro, Percussão, Teclas)]]",
        "[[- Profissional - Música - Canto e Técnica Vocal - Apoio Diafragmático, Registros Vocais e Afinação]]",
        "[[- Profissional - Música - Produção Musical - Gravação em Home Studio e Uso de DAWs (Pro Tools, Logic, Ableton)]]",
        "[[- Profissional - Música - Produção Musical - Engenharia de Áudio (Mixagem, Masterização, Compressores e EQs)]]",

        // =====================================================================
        // 790 - ARTES CÊNICAS, ESPORTES, RECREAÇÃO E EDUCAÇÃO FÍSICA
        // =====================================================================
        "[[- Acadêmico - Artes Cênicas - História do Teatro - Tragédia/Comédia Grega e Teatro Elisabetano (Shakespeare)]]",
        "[[- Profissional - Artes Cênicas - Atuação - O Método (Stanislavski) e Expressão Corporal/Vocal]]",
        "[[- Profissional - Esportes - Educação Física Escolar - Jogos Cooperativos, Lúdico e Psicomotricidade Infantil]]",
        "[[- Profissional - Esportes - Fisiologia do Exercício - Vias Energéticas (Anaeróbia Alática/Lática, Aeróbia) e VO2 Máx]]",
        "[[- Profissional - Esportes - Cinesiologia e Biomecânica - Análise do Movimento Humano, Alavancas e Contração Muscular]]",
        "[[- Profissional - Esportes - Treinamento Desportivo - Princípios do Treinamento (Sobrecarga, Especificidade, Reversibilidade)]]",
        "[[- Profissional - Esportes - Treinamento Desportivo - Periodização do Treinamento (Microciclo, Mesociclo, Macrociclo)]]",
        "[[- Profissional - Esportes - Regras Desportivas - Esportes Coletivos (Futebol, Vôlei, Basquete, Handebol)]]",
        "[[- Profissional - Esportes - Regras Desportivas - Esportes Individuais (Atletismo, Natação, Ginástica Olímpica)]]",

        // =====================================================================
        // 800 / 801 - TEORIA LITERÁRIA, POÉTICA E NARRATOLOGIA
        // =====================================================================
        "[[- Acadêmico - Literatura - Teoria Literária - Gêneros Literários (Épico, Lírico e Dramático)]]",
        "[[- Acadêmico - Literatura - Teoria Literária - Narratologia (Foco Narrativo, Tempo Psicológico/Cronológico, Espaço)]]",
        "[[- Acadêmico - Literatura - Teoria Literária - Tipos de Personagem (Plana, Redonda, Protagonista, Antagonista)]]",
        "[[- Acadêmico - Literatura - Teoria Literária - Intertextualidade (Paródia, Paráfrase, Epígrafe, Citação)]]",
        "[[- Acadêmico - Literatura - Teoria Literária - Escolas da Crítica (Formalismo Russo, Estruturalismo, Estética da Recepção)]]",
        "[[- Acadêmico - Literatura - Poética - Versificação (Métrica, Escansão, Sílabas Poéticas)]]",
        "[[- Acadêmico - Literatura - Poética - Rimas (Ricas, Pobres, Raras, Emparelhadas, Cruzadas, Interpoladas)]]",
        "[[- Acadêmico - Literatura - Poética - Formas Fixas (Soneto, Haicai, Balada, Ode)]]",

        // =====================================================================
        // 808 - RETÓRICA, ESCRITA CRIATIVA E REDAÇÃO (FUNCIONAL)
        // =====================================================================
        "[[- Profissional - Retórica - Oratória - Técnicas de Persuasão (Ethos, Pathos, Logos)]]",
        "[[- Profissional - Retórica - Oratória - Expressão Corporal, Controle de Voz e Dicção em Apresentações]]",
        "[[- Profissional - Escrita Criativa - Produção Literária - Estrutura Dramática (Pirâmide de Freytag e Curva de Tensão)]]",
        "[[- Profissional - Escrita Criativa - Produção Literária - A Jornada do Herói (Monomito de Joseph Campbell)]]",
        "[[- Profissional - Escrita Criativa - Storytelling - Construção de Arcos de Personagem e Conflitos (Interno, Relacional, Extra-pessoal)]]",
        "[[- Profissional - Escrita Criativa - Copywriting - Gatilhos Mentais e Escrita Persuasiva para Vendas]]",
        "[[- Profissional - Escrita Criativa - Roteirização - Formatação de Roteiro Audiovisual (Padrão Master Scenes)]]",

        // =====================================================================
        // 869.3 - LITERATURA BRASILEIRA: ERA COLONIAL AO SÉCULO XIX
        // =====================================================================
        "[[- Acadêmico - Literatura - Literatura Brasileira - Quinhentismo (Literatura de Informação e Carta de Caminha)]]",
        "[[- Acadêmico - Literatura - Literatura Brasileira - Quinhentismo (Literatura de Catequese e Padre José de Anchieta)]]",
        "[[- Acadêmico - Literatura - Literatura Brasileira - Barroco (Poesia de Gregório de Matos - 'O Boca do Inferno')]]",
        "[[- Acadêmico - Literatura - Literatura Brasileira - Barroco (Prosa e Sermões do Padre Antônio Vieira)]]",
        "[[- Acadêmico - Literatura - Literatura Brasileira - Arcadismo (Bucolismo, Nativismo e Tomás Antônio Gonzaga)]]",
        "[[- Acadêmico - Literatura - Literatura Brasileira - Arcadismo (Poesia Épica: O Uraguai e Caramuru)]]",
        "[[- Acadêmico - Literatura - Literatura Brasileira - Romantismo (1ª Geração: Nacionalismo, Indianismo e Gonçalves Dias)]]",
        "[[- Acadêmico - Literatura - Literatura Brasileira - Romantismo (2ª Geração: Mal do Século, Ultrarromantismo e Álvares de Azevedo)]]",
        "[[- Acadêmico - Literatura - Literatura Brasileira - Romantismo (3ª Geração: Condoreirismo, Abolicionismo e Castro Alves)]]",
        "[[- Acadêmico - Literatura - Literatura Brasileira - Romantismo em Prosa (Romances Urbanos, Indianistas e Regionais de José de Alencar)]]",
        "[[- Acadêmico - Literatura - Literatura Brasileira - Realismo (Análise Psicológica e Ironia em Machado de Assis)]]",
        "[[- Acadêmico - Literatura - Literatura Brasileira - Realismo (Obras-primas Machadianas: Memórias Póstumas, Dom Casmurro, Quincas Borba)]]",
        "[[- Acadêmico - Literatura - Literatura Brasileira - Naturalismo (Determinismo Social, Zoomorfização e Aluísio Azevedo - 'O Cortiço')]]",
        "[[- Acadêmico - Literatura - Literatura Brasileira - Parnasianismo (Arte pela Arte, Preciosismo Vocabular e Olavo Bilac)]]",
        "[[- Acadêmico - Literatura - Literatura Brasileira - Simbolismo (Sinestesia, Misticismo, Musicalidade e Cruz e Sousa)]]",

        // =====================================================================
        // 869.3 - LITERATURA BRASILEIRA: SÉCULO XX E CONTEMPORÂNEA
        // =====================================================================
        "[[- Acadêmico - Literatura - Literatura Brasileira - Pré-Modernismo (Sertões de Euclides da Cunha e o Brasil Profundo)]]",
        "[[- Acadêmico - Literatura - Literatura Brasileira - Pré-Modernismo (Crítica Social de Lima Barreto e Monteiro Lobato)]]",
        "[[- Acadêmico - Literatura - Literatura Brasileira - Pré-Modernismo (Poesia Sincrética de Augusto dos Anjos)]]",
        "[[- Acadêmico - Literatura - Literatura Brasileira - Modernismo (Semana de 22, Ruptura Estética e Manifestos Antropofágico/Pau-Brasil)]]",
        "[[- Acadêmico - Literatura - Literatura Brasileira - Modernismo 1ª Geração (Mário de Andrade e Oswald de Andrade)]]",
        "[[- Acadêmico - Literatura - Literatura Brasileira - Modernismo 2ª Geração Poesia (Carlos Drummond de Andrade, Vinicius de Moraes, Cecília Meireles)]]",
        "[[- Acadêmico - Literatura - Literatura Brasileira - Modernismo 2ª Geração Prosa (Romance de 30: Graciliano Ramos, Jorge Amado, Rachel de Queiroz)]]",
        "[[- Acadêmico - Literatura - Literatura Brasileira - Modernismo 3ª Geração (Geração de 45 e Prosa Intimista de Clarice Lispector)]]",
        "[[- Acadêmico - Literatura - Literatura Brasileira - Modernismo 3ª Geração (Regionalismo Universal e Neologismos de João Guimarães Rosa)]]",
        "[[- Acadêmico - Literatura - Literatura Brasileira - Modernismo 3ª Geração (Poesia Engajada de João Cabral de Melo Neto)]]",
        "[[- Acadêmico - Literatura - Literatura Brasileira - Concretismo (Poesia Visual e Vanguardas Paulistas - Década de 50)]]",
        "[[- Acadêmico - Literatura - Literatura Brasileira - Literatura Contemporânea (Conto Urbano, Brutalismo: Rubem Fonseca, Dalton Trevisan)]]",
        "[[- Acadêmico - Literatura - Literatura Brasileira - Literatura Contemporânea (Poesia Marginal - Geração Mimeógrafo)]]",

        // =====================================================================
        // 869 - LITERATURA PORTUGUESA
        // =====================================================================
        "[[- Acadêmico - Literatura - Literatura Portuguesa - Trovadorismo (Cantigas de Amigo, Amor, Escárnio e Maldizer)]]",
        "[[- Acadêmico - Literatura - Literatura Portuguesa - Humanismo (Teatro Vicentino: Auto da Barca do Inferno)]]",
        "[[- Acadêmico - Literatura - Literatura Portuguesa - Classicismo (Epopeia 'Os Lusíadas' de Luís Vaz de Camões)]]",
        "[[- Acadêmico - Literatura - Literatura Portuguesa - Barroco (Conceptismo e Cultismo na Península Ibérica)]]",
        "[[- Acadêmico - Literatura - Literatura Portuguesa - Arcadismo (Bocage e a Nova Arcádia)]]",
        "[[- Acadêmico - Literatura - Literatura Portuguesa - Romantismo (Almeida Garrett e Camilo Castelo Branco)]]",
        "[[- Acadêmico - Literatura - Literatura Portuguesa - Realismo/Naturalismo (Geração de 70 e a Crítica de Eça de Queirós)]]",
        "[[- Acadêmico - Literatura - Literatura Portuguesa - Modernismo (Geração de Orpheu, Fernando Pessoa e seus Heterônimos)]]",
        "[[- Acadêmico - Literatura - Literatura Portuguesa - Literatura Contemporânea (Neorrealismo e a Prosa de José Saramago)]]",

        // =====================================================================
        // 810 A 890 - LITERATURA MUNDIAL (UNIVERSAL) E CLÁSSICA
        // =====================================================================
        "[[- Acadêmico - Literatura - Literatura Clássica - Epopeias Gregas (A Ilíada e A Odisseia de Homero)]]",
        "[[- Acadêmico - Literatura - Literatura Clássica - Teatro Grego (Édipo Rei de Sófocles)]]",
        "[[- Acadêmico - Literatura - Literatura Clássica - Literatura Latina (A Eneida de Virgílio)]]",
        "[[- Acadêmico - Literatura - Literatura Italiana - Transição para o Renascimento (A Divina Comédia de Dante Alighieri)]]",
        "[[- Acadêmico - Literatura - Literatura Inglesa - Era Elisabetana (Tragédias e Comédias de William Shakespeare)]]",
        "[[- Acadêmico - Literatura - Literatura Inglesa - Era Vitoriana (Romance Social de Charles Dickens e as Irmãs Brontë)]]",
        "[[- Acadêmico - Literatura - Literatura Inglesa - Modernismo Britânico (Fluxo de Consciência de Virginia Woolf e James Joyce)]]",
        "[[- Acadêmico - Literatura - Literatura Norte-Americana - Romantismo Obscuro (Edgar Allan Poe e o Conto de Terror)]]",
        "[[- Acadêmico - Literatura - Literatura Norte-Americana - Geração Perdida (F. Scott Fitzgerald e Ernest Hemingway)]]",
        "[[- Acadêmico - Literatura - Literatura Francesa - Romantismo Francês (Victor Hugo - Os Miseráveis)]]",
        "[[- Acadêmico - Literatura - Literatura Francesa - Realismo e Naturalismo Francês (Flaubert, Balzac, Zola)]]",
        "[[- Acadêmico - Literatura - Literatura Francesa - Modernismo Francês (Em Busca do Tempo Perdido de Marcel Proust)]]",
        "[[- Acadêmico - Literatura - Literatura Alemã - Romantismo Alemão (Sturm und Drang e Goethe - Fausto/Werther)]]",
        "[[- Acadêmico - Literatura - Literatura Alemã - Existencialismo e Absurdo (A Metamorfose de Franz Kafka)]]",
        "[[- Acadêmico - Literatura - Literatura Russa - Realismo Psicológico (Dostoiévski - Crime e Castigo, Os Irmãos Karamázov)]]",
        "[[- Acadêmico - Literatura - Literatura Russa - Realismo Histórico (Liev Tolstói - Guerra e Paz, Anna Kariênina)]]",
        "[[- Acadêmico - Literatura - Literatura Hispano-Americana - Boom Latino-Americano e Realismo Mágico (Gabriel García Márquez)]]",
        "[[- Acadêmico - Literatura - Literatura Hispano-Americana - Literatura Fantástica e Labirintos (Jorge Luis Borges)]]",

        // =====================================================================
        // 900 - HISTORIOGRAFIA E CIÊNCIAS AUXILIARES DA HISTÓRIA
        // =====================================================================
        "[[- Acadêmico - História - Historiografia - Escola dos Annales e a Nova História Cultural]]",
        "[[- Acadêmico - História - Historiografia - Materialismo Histórico (Marxismo Aplicado à História)]]",
        "[[- Acadêmico - História - Arqueologia - Métodos de Datação (Carbono-14 e Termoluminescência)]]",
        "[[- Profissional - História - Gestão de Patrimônio - Tombamento, Musealização e Preservação (IPHAN/UNESCO)]]",

        // =====================================================================
        // 910 - GEOGRAFIA FÍSICA E CARTOGRAFIA
        // =====================================================================
        "[[- Acadêmico - Geografia - Cartografia Básica - Projeções Cartográficas (Mercator, Peters, Cilíndricas e Cônicas)]]",
        "[[- Acadêmico - Geografia - Cartografia Básica - Escalas (Gráfica e Numérica), Fusos Horários e Coordenadas]]",
        "[[- Profissional - Geografia - Cartografia Digital - Sensoriamento Remoto, GPS e Sistemas de Informação Geográfica (SIG)]]",
        "[[- Acadêmico - Geografia - Geologia e Geomorfologia - Estrutura Interna da Terra e Tectônica de Placas]]",
        "[[- Acadêmico - Geografia - Geologia e Geomorfologia - Agentes Endógenos (Tectonismo/Vulcanismo) e Exógenos (Intemperismo/Erosão)]]",
        "[[- Acadêmico - Geografia - Geologia e Geomorfologia - Formas de Relevo (Planaltos, Planícies, Depressões e Montanhas)]]",
        "[[- Acadêmico - Geografia - Climatologia - Elementos e Fatores Climáticos (Latitude, Altitude, Maritimidade/Continentalidade)]]",
        "[[- Acadêmico - Geografia - Climatologia - Fenômenos Atmosféricos (El Niño, La Niña e Inversão Térmica)]]",
        "[[- Acadêmico - Geografia - Climatologia - Classificação Climática Global e Tipos de Clima do Brasil (Köppen e Strahler)]]",
        "[[- Acadêmico - Geografia - Hidrografia - Bacias Hidrográficas Brasileiras (Amazônica, Tocantins-Araguaia, São Francisco, Prata)]]",
        "[[- Acadêmico - Geografia - Hidrografia - Águas Subterrâneas (Aquífero Guarani e Sistema Aquífero Grande Amazônia)]]",
        "[[- Acadêmico - Geografia - Biogeografia - Biomas Terrestres Mundiais (Tundra, Taiga, Florestas Temperadas/Tropicais, Desertos)]]",
        "[[- Acadêmico - Geografia - Biogeografia - Domínios Morfoclimáticos Brasileiros (Aziz Ab'Sáber: Amazônico, Cerrado, Mares de Morros, Caatinga, Araucárias, Pradarias)]]",

        // =====================================================================
        // GEOGRAFIA HUMANA, ECONÔMICA, GEOPOLÍTICA E TURISMO
        // =====================================================================
        "[[- Acadêmico - Geografia - Geografia da População - Transição Demográfica, Pirâmides Etárias e Envelhecimento Populacional]]",
        "[[- Acadêmico - Geografia - Geografia da População - Teorias Demográficas (Malthusiana, Neomalthusiana e Reformista/Marxista)]]",
        "[[- Acadêmico - Geografia - Geografia da População - Movimentos Migratórios (Migração Pendular, Transumância, Êxodo Rural, Refugiados)]]",
        "[[- Acadêmico - Geografia - Geografia Urbana - Processos de Urbanização, Metropolização e Conurbação]]",
        "[[- Acadêmico - Geografia - Geografia Urbana - Problemas Urbanos (Segregação Socioespacial, Favelização, Ilhas de Calor e Gentrificação)]]",
        "[[- Acadêmico - Geografia - Geografia Agrária - Estrutura Fundiária Brasileira, Latifúndios e Reforma Agrária]]",
        "[[- Acadêmico - Geografia - Geografia Agrária - Revolução Verde, Agronegócio e Agricultura Familiar]]",
        "[[- Profissional - Geopolítica - Ordem Mundial - Nova Ordem Mundial, Multipolaridade e Guerra Comercial (EUA vs. China)]]",
        "[[- Profissional - Geopolítica - Blocos Econômicos - Tipos de Integração (Área de Livre Comércio, União Aduaneira, Mercado Comum, União Monetária)]]",
        "[[- Profissional - Geopolítica - Blocos Econômicos - União Europeia, NAFTA/USMCA, Mercosul, ASEAN e BRICS]]",
        "[[- Profissional - Geopolítica - Conflitos Contemporâneos - Questão Palestina (Israel vs. Hamas/OLP), Primavera Árabe e Guerra da Síria]]",
        "[[- Profissional - Geopolítica - Conflitos Contemporâneos - Conflitos no Leste Europeu (Rússia vs. Ucrânia e OTAN)]]",
        "[[- Profissional - Turismo - Gestão Turística - Planejamento Estratégico do Turismo, Ecoturismo e Turismo Sustentável]]",

        // =====================================================================
        // 930 - HISTÓRIA GERAL: ANTIGUIDADE CLÁSSICA E ORIENTAL
        // =====================================================================
        "[[- Acadêmico - História - Pré-História - Paleolítico, Neolítico, Idade dos Metais e Revolução Agrícola]]",
        "[[- Acadêmico - História - Antiguidade Oriental - Mesopotâmia (Sumérios, Acádios, Babilônicos e Código de Hamurábi)]]",
        "[[- Acadêmico - História - Antiguidade Oriental - Egito Antigo (Faraós, Sociedade Teocrática e o Rio Nilo)]]",
        "[[- Acadêmico - História - Antiguidade Oriental - Hebreus, Fenícios (Comércio Marítimo/Alfabeto) e Persas]]",
        "[[- Acadêmico - História - Antiguidade Clássica - Grécia Antiga (Período Homérico, Pólis: Atenas e Esparta)]]",
        "[[- Acadêmico - História - Antiguidade Clássica - Grécia Antiga (Guerras Médicas, Guerra do Peloponeso e Helenismo)]]",
        "[[- Acadêmico - História - Antiguidade Clássica - Roma Antiga (Monarquia, República, Lutas Plebeias e Triunviratos)]]",
        "[[- Acadêmico - História - Antiguidade Clássica - Roma Antiga (Império Romano, Pax Romana, Crise do Século III e Invasões Bárbaras)]]",

        // =====================================================================
        // 940 - HISTÓRIA GERAL: IDADE MÉDIA E IDADE MODERNA
        // =====================================================================
        "[[- Acadêmico - História - Idade Média - Alta Idade Média (Império Bizantino, Expansão Islâmica e Império Carolíngio)]]",
        "[[- Acadêmico - História - Idade Média - Feudalismo (Relações de Suserania e Vassalagem, Servidão, Três Ordens)]]",
        "[[- Acadêmico - História - Idade Média - Baixa Idade Média (As Cruzadas, Renascimento Comercial e Urbano)]]",
        "[[- Acadêmico - História - Idade Média - Baixa Idade Média (Crise do Século XIV: Peste Negra, Guerra dos Cem Anos e Fome)]]",
        "[[- Acadêmico - História - Idade Moderna - Formação dos Estados Nacionais e Absolutismo Monárquico (Maquiavel, Hobbes, Bossuet)]]",
        "[[- Acadêmico - História - Idade Moderna - Expansão Marítima Europeia (Grandes Navegações e Tratado de Tordesilhas)]]",
        "[[- Acadêmico - História - Idade Moderna - Capitalismo Comercial (Mercantilismo: Metalismo, Balança Favorável e Pacto Colonial)]]",
        "[[- Acadêmico - História - Idade Moderna - Renascimento Cultural e Científico (Antropocentrismo, Racionalismo)]]",
        "[[- Acadêmico - História - Idade Moderna - Reformas Religiosas (Luteranismo, Calvinismo, Anglicanismo) e Contrarreforma Católica (Concílio de Trento)]]",

        // =====================================================================
        // HISTÓRIA GERAL: IDADE CONTEMPORÂNEA (SÉCULOS XVIII AO XX)
        // =====================================================================
        "[[- Acadêmico - História - Idade Contemporânea - Iluminismo (Enciclopedismo, Voltaire, Montesquieu, Rousseau e Despotismo Esclarecido)]]",
        "[[- Acadêmico - História - Idade Contemporânea - Revoluções Inglesas (Revolução Puritana e Revolução Gloriosa de 1688)]]",
        "[[- Acadêmico - História - Idade Contemporânea - Revolução Industrial (Fases I, II e III, Movimento Ludista e Cartista)]]",
        "[[- Acadêmico - História - Idade Contemporânea - Independência dos EUA (A Guerra das Treze Colônias e a Constituição de 1787)]]",
        "[[- Acadêmico - História - Idade Contemporânea - Revolução Francesa (Queda da Bastilha, Fase Jacobina/Terror e Diretório)]]",
        "[[- Acadêmico - História - Idade Contemporânea - Era Napoleônica (Consulado, Império, Bloqueio Continental) e Congresso de Viena]]",
        "[[- Acadêmico - História - Idade Contemporânea - Unificações Tardias (Unificação da Itália e da Alemanha - Otto von Bismarck)]]",
        "[[- Acadêmico - História - Idade Contemporânea - Imperialismo e Neocolonialismo (Conferência de Berlim e Partilha da África e Ásia)]]",

        // =====================================================================
        // HISTÓRIA GERAL: O BREVE SÉCULO XX
        // =====================================================================
        "[[- Acadêmico - História - Século XX - Primeira Guerra Mundial (Tríplice Aliança vs. Entente, Guerra de Trincheiras, Tratado de Versalhes)]]",
        "[[- Acadêmico - História - Século XX - Revolução Russa (Mencheviques vs. Bolcheviques, Lênin, Stalinismo e a URSS)]]",
        "[[- Acadêmico - História - Século XX - Período Entre Guerras (Crise de 1929, New Deal e Fascismo na Itália)]]",
        "[[- Acadêmico - História - Século XX - Nazismo (Ascensão de Hitler, Antissemitismo e Espaço Vital)]]",
        "[[- Acadêmico - História - Século XX - Segunda Guerra Mundial (Eixo vs. Aliados, Batalha de Stalingrado, Dia D e Bombas Atômicas)]]",
        "[[- Acadêmico - História - Século XX - Guerra Fria (Doutrina Truman, Plano Marshall, OTAN vs. Pacto de Varsóvia)]]",
        "[[- Acadêmico - História - Século XX - Guerra Fria (Corrida Espacial, Crise dos Mísseis em Cuba e Muro de Berlim)]]",
        "[[- Acadêmico - História - Século XX - Descolonização Afro-Asiática (Conferência de Bandung, Guerra do Vietnã e Apartheid na África do Sul)]]",

        // =====================================================================
        // 970 / 980 - HISTÓRIA DA AMÉRICA E HISTÓRIA DO BRASIL
        // =====================================================================
        "[[- Acadêmico - História - História da América - Povos Pré-Colombianos (Maias, Astecas e Incas)]]",
        "[[- Acadêmico - História - História da América - Colonização Espanhola (Mita, Encomienda, Chapetones e Criollos)]]",
        "[[- Acadêmico - História - História da América - Independência da América Espanhola (Simon Bolívar, San Martín e Caudilhismo)]]",
        "[[- Acadêmico - História - História do Brasil - Período Pré-Colonial (Extração de Pau-Brasil e Escambo)]]",
        "[[- Acadêmico - História - História do Brasil - Brasil Colônia (Capitanias Hereditárias, Governo Geral, Economia Açucareira e Escravidão)]]",
        "[[- Acadêmico - História - História do Brasil - Brasil Colônia (Invasões Holandesas e Insurreição Pernambucana)]]",
        "[[- Acadêmico - História - História do Brasil - Brasil Colônia (Bandeirantes, Ciclo do Ouro e Tratado de Madri)]]",
        "[[- Acadêmico - História - História do Brasil - Brasil Colônia (Revoltas Nativistas: Beckman, Mascates, Emboabas e Vila Rica)]]",
        "[[- Acadêmico - História - História do Brasil - Brasil Colônia (Revoltas Separatistas: Inconfidência Mineira e Conjuração Baiana)]]",
        "[[- Acadêmico - História - História do Brasil - Período Joanino (Abertura dos Portos em 1808 e Elevação a Reino Unido)]]",
        "[[- Acadêmico - História - História do Brasil - Brasil Império (Primeiro Reinado: D. Pedro I, Constituição de 1824 e Poder Moderador)]]",
        "[[- Acadêmico - História - História do Brasil - Brasil Império (Período Regencial: Avanço Liberal, Ato Adicional e Revoltas: Farroupilha, Cabanagem, Sabinada, Balaiada)]]",
        "[[- Acadêmico - História - História do Brasil - Brasil Império (Segundo Reinado: D. Pedro II, Parlamentarismo às Avessas e Economia Cafeeira)]]",
        "[[- Acadêmico - História - História do Brasil - Brasil Império (Segundo Reinado: Leis Abolicionistas e a Guerra do Paraguai)]]",
        "[[- Acadêmico - História - História do Brasil - Brasil República (República da Espada: Deodoro e Floriano, Encilhamento)]]",
        "[[- Acadêmico - História - História do Brasil - Brasil República (República Oligárquica: Política do Café com Leite, Coronelismo, Voto de Cabresto)]]",
        "[[- Acadêmico - História - História do Brasil - Brasil República (Revoltas na Primeira República: Canudos, Contestado, Vacina e Chibata)]]",
        "[[- Acadêmico - História - História do Brasil - Brasil República (A Era Vargas: Governo Provisório, Constitucional e Estado Novo)]]",
        "[[- Acadêmico - História - História do Brasil - Brasil República (República Populista: Dutra, Vargas, JK e Plano de Metas, Jânio Quadros, João Goulart)]]",
        "[[- Acadêmico - História - História do Brasil - Brasil República (Ditadura Militar/Civil: Castelo Branco, Costa e Silva, AI-5, Médici/Milagre Econômico, Geisel e Figueiredo)]]",
        "[[- Acadêmico - História - História do Brasil - Brasil República (Redemocratização: Diretas Já, Constituição Cidadã de 1988, Plano Real e Governos Contemporâneos)]]"
    };

}
