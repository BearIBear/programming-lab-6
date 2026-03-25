package managers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Scanner;

import models.Color;
import models.Coordinates;
import models.MusicBand;
import models.MusicGenre;
import models.Person;

/**
 * Менеджер ввода, использующий Scanner для чтения данных при исполнении скриптов
 *
 * @author Михаил
 */
public class InputManager {
    private Scanner scanner;

    public InputManager(Scanner scanner) {
        this.scanner = scanner;
    }

    public String askName() {
        while (true) {
            System.out.print("Введите name группы: ");
            String name = scanner.nextLine();
            if (name == null || name.equals("")) {
                System.out.println("name не может быть null");
                continue;
            }
            if (name.isBlank()) {
                System.out.println("name не может быть пустым");
                continue;
            }
            return name;
        }
    }

    public Long askX() {
        while (true) {
            System.out.print("Введите x группы: ");
            String input = scanner.nextLine();
            if (input == null || input.isBlank()) {
                System.out.println("x не может быть null");
                continue;
            }
            try {
                Long x = Long.parseLong(input);
                if (x > 432) {
                    System.out.println("x не может превышать 432");
                    continue;
                }
                return x;
            } catch (NumberFormatException e) {
                System.out.println("x должно быть типа Long");
            }
        }
    }

    public float askY() {
        while (true) {
            System.out.print("Введите y группы: ");
            String input = scanner.nextLine();
            if (input == null || input.isBlank()) {
                System.out.println("y не может быть null");
                continue;
            }
            try {
                return Float.parseFloat(input);
            } catch (NumberFormatException e) {
                System.out.println("y должно быть типа float");
            }
        }
    }

    public Coordinates askCoordinates() {
        Long x = askX();
        float y = askY();
        return new Coordinates(x, y);
    }

    public long askNumberOfParticipants() {
        while (true) {
            System.out.print("Введите numberOfParticipants группы: ");
            String input = scanner.nextLine();
            try {
                long n = Long.parseLong(input);
                if (n < 1) {
                    System.out.println("numberOfParticipants должно быть больше 0");
                    continue;
                }
                return n;
            } catch (NumberFormatException e) {
                System.out.println("numberOfParticipants должно быть типа long");
            }
        }
    }

    public long askSinglesCount() {
        while (true) {
            System.out.print("Введите singlesCount группы: ");
            String input = scanner.nextLine();
            try {
                long n = Long.parseLong(input);
                if (n < 1) {
                    System.out.println("singlesCount должно быть больше 0");
                    continue;
                }
                return n;
            } catch (NumberFormatException e) {
                System.out.println("singlesCount должно быть типа long");
            }
        }
    }

    public String askDescription() {
        System.out.print("Введите description группы: ");
        String description = scanner.nextLine();
        if (description == null || description.isBlank()) {
            return null;
        }
        return description;
    }

    public MusicGenre askMusicGenre() {
        while (true) {
            System.out.println("Доступные жанры: " + Arrays.toString(MusicGenre.values()));
            System.out.print("Введите MusicGenre: ");
            String input = scanner.nextLine();
            if (input == null || input.isBlank()) {
                return null;
            }
            try {
                return MusicGenre.valueOf(input.toUpperCase().trim());
            } catch (IllegalArgumentException e) {
                System.out.println("Такого жанра нет в списке");
            }
        }
    }

    public String askPersonName() {
        while (true) {
            System.out.print("Введите name фронтмена: ");
            String name = scanner.nextLine();
            if (name == null || name.equals("")) {
                System.out.println("name не может быть null");
                continue;
            }
            if (name.isBlank()) {
                System.out.println("name не может быть пустым");
                continue;
            }
            return name;
        }
    }

    public LocalDate askBirthday() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        while (true) {
            System.out.print("Введите birthday фронтмена (yyyy-MM-dd): ");
            String input = scanner.nextLine();
            if (input == null || input.isBlank()) {
                return null;
            }
            try {
                return LocalDate.parse(input, formatter);
            } catch (DateTimeParseException e) {
                System.out.println("Неверный формат даты. Используйте yyyy-MM-dd");
            }
        }
    }

    public Color askColor() {
        while (true) {
            System.out.println("Доступные цвета: " + Arrays.toString(Color.values()));
            System.out.print("Введите eyeColor фронтмена: ");
            String input = scanner.nextLine();
            if (input == null || input.isBlank()) {
                return null;
            }
            try {
                return Color.valueOf(input.toUpperCase().trim());
            } catch (IllegalArgumentException e) {
                System.out.println("Такого цвета нет в списке");
            }
        }
    }

    public Person askFrontMan() {
        String name = askPersonName();
        LocalDate birthday = askBirthday();
        Color eyeColor = askColor();
        return new Person(name, birthday, eyeColor);
    }

    public MusicBand askMusicBand() {
        String name = askName();
        Coordinates coords = askCoordinates();
        long participants = askNumberOfParticipants();
        long singles = askSinglesCount();
        String description = askDescription();
        MusicGenre genre = askMusicGenre();
        Person frontMan = askFrontMan();
        return new MusicBand(name, coords, participants, singles, description, genre, frontMan);
    }
}
