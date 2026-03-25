package models;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;

/**
 * Класс, представляющий музыкальную группу
 *
 * @author Михаил
 */
public class MusicBand implements Comparable<MusicBand> {
    private long id; //Значение поля должно быть больше 0, Значение этого поля должно быть уникальным, Значение этого поля должно генерироваться автоматически
    private String name; //Поле не может быть null, Строка не может быть пустой
    private Coordinates coordinates; //Поле не может быть null
    private java.util.Date creationDate; //Поле не может быть null, Значение этого поля должно генерироваться автоматически
    private long numberOfParticipants; //Значение поля должно быть больше 0
    private long singlesCount; //Значение поля должно быть больше 0
    private String description; //Поле может быть null
    private MusicGenre genre; //Поле может быть null
    private Person frontMan; //Поле не может быть null
    private static long nextId = 1;
    private static ArrayList<Long> vacantIds = new ArrayList<>();

    public MusicBand(String name, Coordinates coordinates, long numberOfParticipants, long singlesCount,
            String description, MusicGenre genre, Person frontMan) {

        if (vacantIds.isEmpty()) {
            this.id = nextId++;
        } else {
            this.id = vacantIds.remove(0);
        }
        
        if (name == null || name.equals("")) {
            throw new RuntimeException("name не может быть null");
        }
        if (name.isBlank()) {
            throw new RuntimeException("name не может быть пустым");
        }
        this.name = name;
        if (coordinates == null) {
            throw new RuntimeException("coordinates не может быть null");
        }
        this.coordinates = coordinates;
        this.creationDate = Date.from(Instant.now());
        if (numberOfParticipants < 1) {
            throw new RuntimeException("numberOfParticipants должно быть больше 0");
        }
        this.numberOfParticipants = numberOfParticipants;
        if (singlesCount < 1) {
            throw new RuntimeException("singlesCount должно быть больше 0");
        }
        this.singlesCount = singlesCount;
        this.description = description;
        this.genre = genre;
        if (frontMan == null) {
            throw new RuntimeException("frontMan не может быть null");
        }
        this.frontMan = frontMan;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public java.util.Date getCreationDate() {
        return creationDate;
    }

    public long getNumberOfParticipants() {
        return numberOfParticipants;
    }

    public long getSinglesCount() {
        return singlesCount;
    }

    public String getDescription() {
        return description;
    }

    public MusicGenre getGenre() {
        return genre;
    }

    public Person getFrontMan() {
        return frontMan;
    }

    @Override
    public String toString() {
        return "MusicBand [id=" + id + ", name=" + name + ", coordinates=" + coordinates + ", creationDate="
                + creationDate + ", numberOfParticipants=" + numberOfParticipants + ", singlesCount=" + singlesCount
                + ", description=" + description + ", genre=" + genre + ", frontMan=" + frontMan + "]";
    }

    @Override
    public int compareTo(MusicBand other) {
        return Long.compare(this.id, other.id);
    }

    public static void setNextId(long nextId) {
        MusicBand.nextId = nextId;
    }

    public static ArrayList<Long> getVacantIds() {
        return vacantIds;
    }

    public static void setVacantIds(ArrayList<Long> vacantIds) {
        MusicBand.vacantIds = vacantIds;
    }

    public static void addVacantId(Long vacantId) {
        MusicBand.vacantIds.add(vacantId);
    }

    public static long getNextId() {
        return nextId;
    }

    public void setId(long id) {
        this.id = id;
    }
}