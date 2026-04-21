package server.managers;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import models.MusicBand;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.PriorityQueue;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Менеджер для сохранения и загрузки коллекции из файла JSON с использованием Gson
 *
 * @author Михаил
 */
public class FileManager {
    private String fileName;
    private Gson gson;
    private static final Logger log = LogManager.getLogger(FileManager.class);

    public FileManager(String fileName) {
        this.fileName = fileName;
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .serializeSpecialFloatingPointValues()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .create();
    }

    public void load(CollectionManager collectionManager) {
        if (fileName == null || fileName.isEmpty()) {
            log.error("File name is blank");
            return;
        }
        File file = new File(fileName);
        if (!file.exists()) {
            log.error("File " + fileName + " doesn't exist");
            return;
        }
        if (!file.canRead()) {
            log.error("No permissions to read the file " + fileName);
            return;
        }

        // Инициализируем сканер так, чтобы его не надо было закрывать вручную
        try (Scanner fileScanner = new Scanner(file)) {
            StringBuilder jsonString = new StringBuilder();
            while (fileScanner.hasNextLine()) {
                jsonString.append(fileScanner.nextLine());
            }
            if (jsonString.length() == 0) {
                log.warn("File is empty, no collections will be loaded");
                CollectionManager.setNextId(1);
                return;
            }

            // Загружаем JSON собственно говоря 
            Type collectionType = new TypeToken<PriorityQueue<MusicBand>>() {}.getType();
            PriorityQueue<MusicBand> loadedCollection = gson.fromJson(jsonString.toString(), collectionType);

            // По одному элементу добавляем группы в рабочую коллекцию
            // и попутно ищем максимальный ID среди них для генератора уникальных ID
            long max_id = 0; 
            if (loadedCollection != null) {
                for (MusicBand band : loadedCollection) {
                    collectionManager.addElement(band);
                    max_id = Math.max(band.getId(), max_id);
                }
                log.info("File loaded, total bands: " + loadedCollection.size());
            }
            CollectionManager.setNextId(max_id + 1);

            // Ищем пропущенные ID
            if (max_id > loadedCollection.size()) {
                for (long i = 1; i < max_id; i++) {
                    Long searchedId = i; // Иначе выдаёт ошибку, что "i должен быть final"
                    if (loadedCollection.stream().anyMatch(band -> Long.compare(band.getId(), searchedId) == 0)) {
                        continue;
                    } else {
                        CollectionManager.addVacantId(searchedId);
                    }
                }
            }
        
        } catch (FileNotFoundException e) {
            System.out.println("Файл не найден: " + e.getMessage());
        } catch (JsonSyntaxException e) {
            System.out.println("Неправильный JSON");
        }
    }

    public void save(CollectionManager collectionManager) {
        if (fileName == null || fileName.isEmpty()) {
            System.out.println("Имя файла не указано, сохранения не будет");
            return;
        }

        File file = new File(fileName);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            if (!file.canWrite()) {
                System.out.println("Ошибка: Нет прав на запись в файл " + fileName);
                return;
            }

            try (FileOutputStream fos = new FileOutputStream(file)) {
                String json = gson.toJson(collectionManager.getCollection());
                fos.write(json.getBytes());
                System.out.println("Коллекция успешно сохранена в файл: " + fileName);
            }

        } catch (IOException e) {
            System.out.println("Ошибка при сохранении файла: " + e.getMessage());
        }
    }

    private static final class LocalDateAdapter extends TypeAdapter<LocalDate> {
        @Override
        public void write(final JsonWriter jsonWriter, final LocalDate localDate) throws IOException {
            jsonWriter.value(localDate.toString());
        }

        @Override
        public LocalDate read(final JsonReader jsonReader) throws IOException {
            return LocalDate.parse(jsonReader.nextString());
        }
    }
}