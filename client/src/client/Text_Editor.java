package client;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.rtf.RTFEditorKit;

public class Text_Editor extends JFrame {
    private JTabbedPane tabbedPane1;
    private JTextPane TextBlock;
    private JCheckBox boldCheckBox;
    private JCheckBox cursiveCheckBox;
    private JTextField searchField;
    private JButton findButton;
    private JComboBox fontBox;
    private JPanel ClientPanel;
    private JButton colorChooseButton;
    private JComboBox sizeFont;
    private JButton resetstyle;
    private JFrame message;
    private JFrame secondFrame = new JFrame("Color Choose");
    private Color selectedColor; // переменная для хранения выбранного цвета
    private boolean isNewFile = true; // флаг для нового файла
    private boolean hasStyles = false; // флаг для проверки наличия стилей
    private String currentFilePath = ""; // переменная для хранения пути к текущему файлу
    private Socket socket; // сокет для подключения к серверу
    private PrintWriter out; // для отправки данных на сервер
    private BufferedReader in; // для получения данных с сервера

    public Text_Editor() {
        super("Системное меню");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        // Создание строки главного меню
        JMenuBar menuBar = new JMenuBar();
        // Создание выпадающего меню
        JMenu file = new JMenu("Файл");
        // Пункт меню "Открыть" с изображением
        JMenuItem open_loc = new JMenuItem("Открыть локально");
        JMenuItem save_loc = new JMenuItem("Сохранить локально");
        JMenuItem open_serv = new JMenuItem("Открыть с сервера");
        JMenuItem save_serv = new JMenuItem("Сохранить на сервер");
        // Добавим в меню пункта open
        file.add(open_loc);
        file.add(open_serv);
        file.add(save_loc);
        file.add(save_serv);
        // Добавление разделителя
        file.addSeparator();
        menuBar.add(file);
        // Подключаем меню к интерфейсу приложения
        setJMenuBar(menuBar);
        add(ClientPanel);
        setTitle("Java");
        setSize(700, 800);

        // Устанавливаем шрифт и размер по умолчанию
        String defaultFont = "Times New Roman";
        int defaultFontSize = 14;
        TextBlock.setFont(new Font(defaultFont, Font.PLAIN, defaultFontSize));
        fontBox.setSelectedItem(defaultFont);
        sizeFont.setSelectedItem(String.valueOf(defaultFontSize));

        // Создание содержимого для secondFrame
        JColorChooser colorChooser = new JColorChooser();
        JButton okColorButton = new JButton("Ok");

        // Используем BorderLayout для secondFrame
        secondFrame.setLayout(new BorderLayout());
        secondFrame.add(colorChooser, BorderLayout.CENTER);
        secondFrame.add(okColorButton, BorderLayout.SOUTH);
        secondFrame.setSize(500, 400);

        // Добавление прослушек для пунктов меню
        open_loc.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser j = new JFileChooser("c:");
                FileNameExtensionFilter filter = new FileNameExtensionFilter("Text and RTF files", "txt", "rtf");
                j.setFileFilter(filter);

                int r = j.showOpenDialog(null);

                if (r == JFileChooser.APPROVE_OPTION) {
                    File fi = new File(j.getSelectedFile().getAbsolutePath());
                    currentFilePath = fi.getAbsolutePath();
                    try {
                        String filePath = fi.getAbsolutePath();
                        if (filePath.endsWith(".rtf")) {
                            readRTFFile(fi);
                        } else {
                            readTextFile(fi);
                        }
                    } catch (Exception evt) {
                        JOptionPane.showMessageDialog(null, evt.getMessage());
                    }
                }
            }

            private void readTextFile(File fi) throws IOException {
                String s1 = "", sl = "";
                FileReader fr = new FileReader(fi);
                BufferedReader br = new BufferedReader(fr);
                sl = br.readLine();
                while ((s1 = br.readLine()) != null) {
                    sl = sl + "\n" + s1;
                }
                TextBlock.setText(sl);
                br.close();
                fr.close();
                hasStyles = false; // Сброс флага стилей при открытии текстового файла
            }

            private void readRTFFile(File fi) throws IOException, BadLocationException {
                RTFEditorKit rtfKit = new RTFEditorKit();
                Document doc = new DefaultStyledDocument();
                FileInputStream fis = new FileInputStream(fi);
                rtfKit.read(fis, doc, 0);
                TextBlock.setDocument(doc);
                fis.close();
                hasStyles = true; // Установка флага стилей при открытии RTF файла
            }
        });

        save_loc.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser j = new JFileChooser("с:");
                int r = j.showSaveDialog(null);

                if (r == JFileChooser.APPROVE_OPTION) {
                    File fi = new File(j.getSelectedFile().getAbsolutePath());
                    try {
                        if (hasStyles || isNewFile) {
                            if (!fi.getAbsolutePath().toLowerCase().endsWith(".rtf")) {
                                fi = new File(fi.getAbsolutePath() + ".rtf");
                            }
                            saveAsRTF(fi);
                        } else {
                            if (!fi.getAbsolutePath().toLowerCase().endsWith(".txt")) {
                                fi = new File(fi.getAbsolutePath() + ".txt");
                            }
                            saveAsText(fi);
                        }
                        isNewFile = false;
                        currentFilePath = fi.getAbsolutePath();
                    } catch (Exception evt) {
                        JOptionPane.showMessageDialog(message, evt.getMessage());
                    }
                }
            }

            private void saveAsText(File fi) throws IOException {
                FileWriter wr = new FileWriter(fi, false);
                BufferedWriter w = new BufferedWriter(wr);
                w.write(TextBlock.getText());
                w.flush();
                w.close();
            }

            private void saveAsRTF(File fi) throws IOException {
                RTFEditorKit rtfKit = new RTFEditorKit();
                Document doc = TextBlock.getDocument();
                FileOutputStream fos = new FileOutputStream(fi);
                try {
                    rtfKit.write(fos, doc, 0, doc.getLength());
                } catch (BadLocationException ex) {
                    ex.printStackTrace();
                }
                fos.close();
            }
        });

        open_serv.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Логика открытия файла с сервера
                requestFileListFromServer();
            }
        });

        save_serv.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                StyledDocument doc = (StyledDocument) TextBlock.getDocument();
                boolean hasStyles = containsStyles(doc);

                String fileName = JOptionPane.showInputDialog("Введите имя файла:");
                if (fileName != null && !fileName.isEmpty()) {
                    if (hasStyles) {
                        System.out.println("Document contains styles.");
                        String rtfContent = getRTFContent(doc);
                        System.out.println("RTF Content: " + rtfContent);
                        saveFileToServer(fileName, rtfContent, true);
                    } else {
                        System.out.println("Document does not contain styles.");
                        String content = TextBlock.getText();
                        if (!content.isEmpty()) {
                            saveFileToServer(fileName, content, false);
                        } else {
                            JOptionPane.showMessageDialog(message, "Нельзя сохранить пустой файл.");
                        }
                    }
                }
            }

            private void saveFileToServer(String fileName, String content, boolean hasStyles) {
                if (out != null) {
                    out.println("SAVE_FILE " + fileName);
                    out.println(hasStyles ? "RTF" : "TXT"); // Отправляем информацию о формате файла
                    out.println(content);
                    out.println("EOF"); // Маркер конца файла
                    JOptionPane.showMessageDialog(message, "Файл успешно сохранен на сервере.");
                }
            }

            private String getRTFContent(StyledDocument doc) {
                RTFEditorKit rtfKit = new RTFEditorKit();
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                try {
                    rtfKit.write(out, doc, 0, doc.getLength());
                } catch (IOException | BadLocationException e) {
                    e.printStackTrace();
                }

                String rtfContent = "";
                try {
                    rtfContent = out.toString("UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                if (rtfContent.isEmpty()) {
                    System.out.println("RTF content is empty. Document length: " + doc.getLength());
                }
                return rtfContent;
            }


            private boolean containsStyles(StyledDocument doc) {
                boolean hasStyles = false;
                try {
                    for (int i = 0; i < doc.getLength(); i++) {
                        Element element = doc.getCharacterElement(i);
                        AttributeSet as = element.getAttributes();

                        // Check for common styling attributes
                        if (StyleConstants.isBold(as) ||
                                StyleConstants.isItalic(as) ||
                                StyleConstants.isUnderline(as) ||
                                StyleConstants.getFontSize(as) != StyleConstants.getFontSize(SimpleAttributeSet.EMPTY) ||
                                StyleConstants.getFontFamily(as) != null ||
                                StyleConstants.getForeground(as) != Color.BLACK) {
                            hasStyles = true;
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return hasStyles;
            }
        });








        findButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String searchText = searchField.getText();
                if (!searchText.isEmpty()) {
                    highlightText(TextBlock, searchText);
                }
            }

            private void highlightText(JTextPane textPane, String searchText) {
                Highlighter highlighter = textPane.getHighlighter();
                Highlighter.HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);
                highlighter.removeAllHighlights();

                String text = textPane.getText();
                int index = text.indexOf(searchText);
                while (index >= 0) {
                    try {
                        int end = index + searchText.length();
                        highlighter.addHighlight(index, end, painter);
                        index = text.indexOf(searchText, end);
                    } catch (BadLocationException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        boldCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                StyledDocument doc = TextBlock.getStyledDocument();
                SimpleAttributeSet attr = new SimpleAttributeSet();
                StyleConstants.setBold(attr, e.getStateChange() == ItemEvent.SELECTED);
                TextBlock.setCharacterAttributes(attr, false);
            }
        });

        cursiveCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                StyledDocument doc = TextBlock.getStyledDocument();
                SimpleAttributeSet attr = new SimpleAttributeSet();
                StyleConstants.setItalic(attr, e.getStateChange() == ItemEvent.SELECTED);
                TextBlock.setCharacterAttributes(attr, false);
            }
        });

        fontBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    String selectedFont = (String) fontBox.getSelectedItem();
                    if (selectedFont != null) {
                        applyFont(selectedFont);
                    }
                }
            }

            private void applyFont(String fontName) {
                StyledDocument doc = TextBlock.getStyledDocument();
                SimpleAttributeSet attr = new SimpleAttributeSet();
                StyleConstants.setFontFamily(attr, fontName);
                TextBlock.setCharacterAttributes(attr, false);
                hasStyles = true; // Установка флага стилей при изменении шрифта текста
            }
        });

        colorChooseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                secondFrame.setVisible(true);
            }
        });

        okColorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedColor = colorChooser.getColor();
                secondFrame.setVisible(false);
                applySelectedColor();
            }
        });

        sizeFont.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    String selectedSize = (String) sizeFont.getSelectedItem();
                    if (selectedSize != null) {
                        applyFontSize(Integer.parseInt(selectedSize));
                    }
                }
            }

            private void applyFontSize(int fontSize) {
                StyledDocument doc = TextBlock.getStyledDocument();
                SimpleAttributeSet attr = new SimpleAttributeSet();
                StyleConstants.setFontSize(attr, fontSize);
                TextBlock.setCharacterAttributes(attr, false);
                hasStyles = true; // Установка флага стилей при изменении размера текста
            }
        });

        // Подключение к серверу при запуске клиента
        try {
            socket = new Socket("localhost", 7777);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("Подключено к серверу");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    // Отправить команду серверу об отключении
                    out.println("DISCONNECT");
                    // Закрыть потоки ввода-вывода и сокет
                    if (in != null) {
                        in.close();
                    }
                    if (out != null) {
                        out.close();
                    }
                    if (socket != null) {
                        socket.close();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        resetstyle.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetTextStyles();
            }

            private void resetTextStyles() {
                // Получаем текущий документ
                StyledDocument doc = TextBlock.getStyledDocument();

                // Создаем простой атрибутный набор, который не содержит никаких стилей
                SimpleAttributeSet attr = new SimpleAttributeSet();

                // Устанавливаем атрибуты для всего текста документа
                doc.setCharacterAttributes(0, doc.getLength(), attr, true);

                // Восстанавливаем шрифт и размер по умолчанию
                String defaultFont = "Times New Roman";
                int defaultFontSize = 14;
                TextBlock.setFont(new Font(defaultFont, Font.PLAIN, defaultFontSize));
                TextBlock.setForeground(Color.BLACK);

                // Обновляем состояние панели управления стилями
                boldCheckBox.setSelected(false);
                cursiveCheckBox.setSelected(false);
                fontBox.setSelectedItem(defaultFont);
                sizeFont.setSelectedItem(String.valueOf(defaultFontSize));
                selectedColor = Color.BLACK;

                // Обновляем флаг стилей
                hasStyles = false;
            }
        });
        TextBlock.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if ((e.getKeyCode() == KeyEvent.VK_R) && ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0)) {
                    showReplaceDialog();
                }
            }
        });

    }
    // Метод для обновления стиля текста
    private void updateTextStyle() {
        // Получение текущего документа
        StyledDocument doc = TextBlock.getStyledDocument();

        // Получение текущих атрибутов текста
        SimpleAttributeSet attr = new SimpleAttributeSet();

        // Применение стилей
        if (boldCheckBox.isSelected()) {
            StyleConstants.setBold(attr, true);
        }
        if (cursiveCheckBox.isSelected()) {
            StyleConstants.setItalic(attr, true);
        }

        // Применение шрифта и размера
        String selectedFont = (String) fontBox.getSelectedItem();
        int selectedSize = Integer.parseInt((String) sizeFont.getSelectedItem());
        StyleConstants.setFontFamily(attr, selectedFont);
        StyleConstants.setFontSize(attr, selectedSize);

        // Применение цвета текста
        StyleConstants.setForeground(attr, selectedColor);

        // Применение атрибутов к выделенному тексту
        doc.setCharacterAttributes(TextBlock.getSelectionStart(), TextBlock.getSelectionEnd() - TextBlock.getSelectionStart(), attr, false);

        hasStyles = true; // Установка флага стилей
    }

    private void showReplaceDialog() {
        // Создание диалогового окна
        JDialog replaceDialog = new JDialog(this, "Замена слов", true);
        replaceDialog.setSize(400, 200);
        replaceDialog.setLayout(new BorderLayout());

        // Создание панели для ввода слов
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(2, 2));

        JLabel findLabel = new JLabel("Найти:");
        JTextField findField = new JTextField();
        JLabel replaceLabel = new JLabel("Заменить на:");
        JTextField replaceField = new JTextField();

        inputPanel.add(findLabel);
        inputPanel.add(findField);
        inputPanel.add(replaceLabel);
        inputPanel.add(replaceField);

        replaceDialog.add(inputPanel, BorderLayout.CENTER);

        // Создание панели с кнопками
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());

        JButton replaceButton = new JButton("Заменить");
        JButton cancelButton = new JButton("Отмена");

        buttonPanel.add(replaceButton);
        buttonPanel.add(cancelButton);

        replaceDialog.add(buttonPanel, BorderLayout.SOUTH);

        // Добавление обработчика событий для кнопки "Заменить"
        replaceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String findText = findField.getText();
                String replaceText = replaceField.getText();
                if (!findText.isEmpty()) {
                    replaceTextWithStyles(findText, replaceText);
                    replaceDialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(replaceDialog, "Введите текст для поиска.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Добавление обработчика событий для кнопки "Отмена"
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                replaceDialog.dispose();
            }
        });

        replaceDialog.setLocationRelativeTo(this);
        replaceDialog.setVisible(true);
    }

    private void replaceTextWithStyles(String findText, String replaceText) {
        try {
            StyledDocument doc = TextBlock.getStyledDocument();
            String content = doc.getText(0, doc.getLength());
            int findLength = findText.length();

            int index = content.indexOf(findText);
            while (index >= 0) {
                int end = index + findLength;
                AttributeSet attr = doc.getCharacterElement(index).getAttributes();
                doc.remove(index, findLength);
                doc.insertString(index, replaceText, attr);

                content = doc.getText(0, doc.getLength()); // обновить содержимое
                index = content.indexOf(findText, index + replaceText.length());
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void applySelectedColor() {
        if (selectedColor != null && TextBlock.getSelectedText() != null) {
            StyledDocument doc = TextBlock.getStyledDocument();
            SimpleAttributeSet attr = new SimpleAttributeSet();
            StyleConstants.setForeground(attr, selectedColor);
            doc.setCharacterAttributes(TextBlock.getSelectionStart(), TextBlock.getSelectionEnd() - TextBlock.getSelectionStart(), attr, false);
        }
    }


    private void requestFileListFromServer() {
        if (out != null) {
            out.println("GET_FILES");
            try {
                int fileCount = Integer.parseInt(in.readLine());
                List<String> fileNames = new ArrayList<>();
                for (int i = 0; i < fileCount; i++) {
                    fileNames.add(in.readLine());
                }
                showFileListDialog(fileNames);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void showFileListDialog(List<String> fileNames) {
        String selectedFile = (String) JOptionPane.showInputDialog(
                this,
                "Выберите файл для открытия",
                "Открыть файл с сервера",
                JOptionPane.PLAIN_MESSAGE,
                null,
                fileNames.toArray(),
                fileNames.get(0)
        );

        if (selectedFile != null) {
            requestFileContentFromServer(selectedFile);
        }
    }

    private void requestFileContentFromServer(String fileName) {
        if (out != null) {
            out.println("GET_FILE_CONTENT " + fileName);
            try {
                StringBuilder fileContent = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null && !line.equals("EOF")) {
                    fileContent.append(line).append("\n");
                }
                if (fileName.endsWith(".rtf")) {
                    readRTFContent(fileContent.toString());
                } else {
                    TextBlock.setText(fileContent.toString());
                }
            } catch (IOException | BadLocationException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void readRTFContent(String content) throws IOException, BadLocationException {
        RTFEditorKit rtfKit = new RTFEditorKit();
        Document doc = new DefaultStyledDocument();
        InputStream is = new ByteArrayInputStream(content.getBytes());
        rtfKit.read(is, doc, 0);
        TextBlock.setDocument(doc);
        hasStyles = true; // Установка флага стилей при открытии RTF содержимого
    }

}
