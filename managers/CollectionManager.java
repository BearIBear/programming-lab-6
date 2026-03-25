package managers;

import models.MusicBand;
import java.util.PriorityQueue;
import java.time.LocalDateTime;

/**
 * Менеджер коллекции для управления элементами типа MusicBand
 *
 * @author Михаил
 */
public class CollectionManager {
    private PriorityQueue<MusicBand> collection;
    private LocalDateTime initTime;

    public CollectionManager() {
        this.collection = new PriorityQueue<>();
        this.initTime = LocalDateTime.now(); 
    }

    public PriorityQueue<MusicBand> getCollection() {
        return collection;
    }

    public LocalDateTime getInitTime() {
        return initTime;
    }    

    public void addElement(MusicBand band) {
        collection.add(band);
    }

    public void clearCollection() {
        collection.clear();
    }

    public boolean removeElement(long id) {
        for (MusicBand musicBand : collection) {
            if (musicBand.getId() == id) {
                collection.remove(musicBand);
                return true;
            }
        }
        return false;
    }
}