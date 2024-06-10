package server;

import java.io.*;
import java.net.*;

public class TextEditorServer {
    private static final int PORT = 7777; // Порт для сервера
    private static final String DIRECTORY_PATH = "C:\\Users\\nkouk\\OneDrive\\Рабочий стол\\servers folder"; // Путь к директории с файлами

    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        Socket clientSocket = null;

        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Сервер запущен и ждет подключения...");

            while (true) {
                clientSocket = serverSocket.accept();
                System.out.println("Клиент подключен: " + clientSocket.getInetAddress());

                // Создаем новый поток для обработки клиента
                new ClientHandler(clientSocket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (serverSocket != null) {
                    serverSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static class ClientHandler extends Thread {
        private Socket clientSocket;
        private BufferedReader in;
        private PrintWriter out;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new PrintWriter(clientSocket.getOutputStream(), true);

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    System.out.println("Получено сообщение: " + inputLine);

                    if (inputLine.equals("GET_FILES")) {
                        sendFileList();
                    } else if (inputLine.startsWith("GET_FILE_CONTENT")) {
                        String fileName = inputLine.substring("GET_FILE_CONTENT ".length());
                        sendFileContent(fileName);
                    } else if (inputLine.startsWith("SAVE_FILE")) {
                        String fileName = inputLine.substring("SAVE_FILE ".length());
                        String fileType = in.readLine(); // Получаем информацию о формате файла
                        boolean isRTF = fileType.equals("RTF");
                        handleSaveFileRequest(fileName, isRTF);
                    } else {
                        // Остальные условия обработки запросов

                        // Проверяем запрос на отключение
                        if (inputLine.equals("DISCONNECT")) {
                            // Если клиент запросил отключение, закрываем соединение
                            System.out.println("Client disconnected.");
                            break;
                        } else {
                            out.println("Эхо: " + inputLine); // Отправка ответа клиенту
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                    if (out != null) {
                        out.close();
                    }
                    if (clientSocket != null) {
                        clientSocket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


        private void sendFileList() {
            File directory = new File(DIRECTORY_PATH);
            File[] files = directory.listFiles((dir, name) -> {
                File file = new File(dir, name);
                return isTextFile(file);
            });

            if (files != null) {
                out.println(files.length);
                for (File file : files) {
                    out.println(file.getName());
                }
            } else {
                out.println(0);
            }
        }

        private boolean isTextFile(File file) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                while (br.readLine() != null) {
                    // Если можем прочитать хотя бы одну строку, считаем файл текстовым
                }
                return true;
            } catch (IOException e) {
                return false; // Если не удалось прочитать файл, он не текстовый
            }
        }

        private void sendFileContent(String fileName) {
            File file = new File(DIRECTORY_PATH, fileName);
            if (file.exists()) {
                try (BufferedReader fileReader = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = fileReader.readLine()) != null) {
                        out.println(line);
                    }
                    out.println("EOF"); // Маркер конца файла
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                out.println("Файл не найден");
            }
        }
        private void saveFileContent(String fileName, boolean isRTF) {
            File file = new File(DIRECTORY_PATH, fileName + (isRTF ? ".rtf" : ".txt"));
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                String line;
                while ((line = in.readLine()) != null && !line.equals("EOF")) {
                    writer.write(line);
                    writer.newLine();
                }
                System.out.println("Файл " + fileName + " успешно сохранен на сервере в формате " + (isRTF ? "RTF" : "TXT"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }



        // Добавляем обработчик запроса сохранения файла
        private void handleSaveFileRequest(String fileName, boolean isRTF) {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                saveFileContent(fileName, isRTF);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
