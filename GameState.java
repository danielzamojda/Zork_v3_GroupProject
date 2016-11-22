import java.util.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;


public class GameState {

    public static class IllegalSaveFormatException extends Exception {
        public IllegalSaveFormatException(String e) {
            super(e);
        }
    }

    static String DEFAULT_SAVE_FILE = "bork_save";
    static String SAVE_FILE_EXTENSION = ".sav";
    static String SAVE_FILE_VERSION = "Bork v3.0 save data";

    static String ADVENTURER_MARKER = "Adventurer:";
    static String CURRENT_ROOM_LEADER = "Current room: ";
    static String INVENTORY_LEADER = "Inventory: ";
    static String SCORE_LEADER = "Score: ";
    static String HEALTH_LEADER = "Health: ";

    private static GameState theInstance;
    private Dungeon dungeon;
    private ArrayList<Item> inventory;
    private Room adventurersCurrentRoom;

    /** 
    * Final int variable that stores the maximum weight that an adventururer can carry
    */
    static final int MAX_WEIGHT = 20;

    static final int MAX_HEALTH = 25;

    /**
    * Variable that stores the current weight of the adventurer's inventory
    */
    private int currrentWeight;
    
    /** 
    * A private variable that will store the score of the adventurer, this will then be used
    * as a key to retrieve the rank from the Rank hashtable
    */
    private int score;
    
    /**
    * A private variable that will store the score of the adventurer, this will then be used
    * as a key to retrieve the health from the healthMessage hashtable
    */
    private int health;
    final int max_health = 25;
    /**
    *Hashtable that will take in a key, the adventurer's health, and return the correct message based on the adventurer's health
    */

    Hashtable<Integer, String> healthStatus = new Hashtable();



    /**
    *Hashtable that will take in a key, the adventurer's score, and return their rank based on their score
    */
    Hashtable<Integer,String> adventurerRank = new Hashtable<>();
    
    
    static synchronized GameState instance() {
        if (theInstance == null) {
            theInstance = new GameState();
        }
        return theInstance;
    }

    private GameState() {
        inventory = new ArrayList<Item>();
    }

    void restore(String filename) throws FileNotFoundException,
        IllegalSaveFormatException, Dungeon.IllegalDungeonFormatException {

        Scanner s = new Scanner(new FileReader(filename));

        if (!s.nextLine().equals(SAVE_FILE_VERSION)) {
            throw new IllegalSaveFormatException("Save file not compatible.");
        }

        String dungeonFileLine = s.nextLine();

        if (!dungeonFileLine.startsWith(Dungeon.FILENAME_LEADER)) {
            throw new IllegalSaveFormatException("No '" +
                Dungeon.FILENAME_LEADER + 
                "' after version indicator.");
        }

        dungeon = new Dungeon(dungeonFileLine.substring(
            Dungeon.FILENAME_LEADER.length()), false);
        dungeon.restoreState(s);

        s.nextLine();  // Throw away "Adventurer:".
        String currentRoomLine = s.nextLine();
        adventurersCurrentRoom = dungeon.getRoom(
            currentRoomLine.substring(CURRENT_ROOM_LEADER.length()));
        if (s.hasNext()) {
            String inventoryList = s.nextLine().substring(
                INVENTORY_LEADER.length());
            String[] inventoryItems = inventoryList.split(",");
            for (String itemName : inventoryItems) {
                try {
                    addToInventory(dungeon.getItem(itemName));
                } catch (Item.NoItemException e) {
                    throw new IllegalSaveFormatException("No such item '" +
                        itemName + "'");
                }
            }
        }
    }

    void store() throws IOException {
        store(DEFAULT_SAVE_FILE);
    }

    void store(String saveName) throws IOException {
        String filename = saveName + SAVE_FILE_EXTENSION;
        PrintWriter w = new PrintWriter(new FileWriter(filename));
        w.println(SAVE_FILE_VERSION);
        dungeon.storeState(w);
        w.println(ADVENTURER_MARKER);
        w.println(CURRENT_ROOM_LEADER + adventurersCurrentRoom.getTitle());
        if (inventory.size() > 0) {
            w.print(INVENTORY_LEADER);
            for (int i=0; i<inventory.size()-1; i++) {
                w.print(inventory.get(i).getPrimaryName() + ",");
            }
            w.println(inventory.get(inventory.size()-1).getPrimaryName());
        }
        w.println(HEALTH_LEADER + health);
        w.println(SCORE_LEADER + score);
        w.close();
    }

    void initialize(Dungeon dungeon) {
        this.dungeon = dungeon;
        adventurersCurrentRoom = dungeon.getEntry();

        int a = 5;
        Integer aI = a;
        int b = 10;
        Integer bI = b;
        int c = 15;
        Integer cI = c;
        int d = 20;
        Integer dI = d;
        int e = 25;
        Integer eI = e;

        String aS = "You are a scrub with no points";
        String bS = "You're finally getting the hang of it, but you still need to get good";
        String cS = "Good job there bud on not getting killed";
        String dS = "If you got this far you might actually win";
        String eS = "You are a great and respected Knight";

        adventurerRank.put(aI, aS);
        adventurerRank.put(bI, bS);
        adventurerRank.put(cI, cS);
        adventurerRank.put(dI, dS);
        adventurerRank.put(eI, eS);

        String aH = "You start to feel cold, you're losing conciousness, confused and fearing the end is imminent";
        String bH = "You're whole body is aching, much more of this and you fear you won't last long.";
        String cH = "Meh, it's just a flesh wound";
        String dH = "tis but a scratch";
        String eH = "You feel one hunna";

        healthStatus.put(aI,aH);
        healthStatus.put(bI,bH);
        healthStatus.put(cI,cH);
        healthStatus.put(dI,dH);
        healthStatus.put(eI,eH);


    }

    ArrayList<String> getInventoryNames() {
        ArrayList<String> names = new ArrayList<String>();
        for (Item item : inventory) {
            names.add(item.getPrimaryName());
        }
        return names;
    }

    void addToInventory(Item item) /* throws TooHeavyException */ {
        inventory.add(item);
    }

    void removeFromInventory(Item item) {
        inventory.remove(item);
    }

    Item getItemInVicinityNamed(String name) throws Item.NoItemException {

        // First, check inventory.
        for (Item item : inventory) {
            if (item.goesBy(name)) {
                return item;
            }
        }

        // Next, check room contents.
        for (Item item : adventurersCurrentRoom.getContents()) {
            if (item.goesBy(name)) {
                return item;
            }
        }

        throw new Item.NoItemException();
    }

    Item getItemFromInventoryNamed(String name) throws Item.NoItemException {

        for (Item item : inventory) {
            if (item.goesBy(name)) {
                return item;
            }
        }
        throw new Item.NoItemException();
    }

    Room getAdventurersCurrentRoom() {
        return adventurersCurrentRoom;
    }

    void setAdventurersCurrentRoom(Room room) {
        adventurersCurrentRoom = room;
    }

    Dungeon getDungeon() {
        return dungeon;
    }
    
    /**
    *Getter method that will return the current health that is an int
    *@return int current health
    */
    public int getHealth()
    {
        return this.health;
    }

    public void changeHealth(int healthPoints) { health += healthPoints; }

    

    /**
    * Getter method that will return a string from the healthStatus hashtable based on the adventurer's health
    *@return String message regarding adventurer health
    */
    public String getHealthMessage(int currentHealth)
    {

        int actualHealth = currentHealth;

        if(currentHealth%5 != 0)
        {
            actualHealth = actualHealth - (currentHealth%5);
        }
        return healthStatus.get(actualHealth);

    }


    /**
    *Getter method that will return the current score that is an int
    *@return int current score
    */
    public int getScore()
    {
        return this.score;
    }
    
    /**
    * Getter method that will return a string from the adventurerRank hashtable based on the adventurer's score
    *@return String message regarding adventurer rank
    */

    public String getRank(int currentScore)
    {
        int actualScore = currentScore;

        if(currentScore%5 != 0)
        {
            actualScore = actualScore - (currentScore%5);
        }
        return adventurerRank.get(actualScore);

    }

    public void removeItem(String itemName) throws Item.NoItemException
    {
        if(inventory.remove(itemName) == false){
            throw new Item.NoItemException();
        }
        inventory.remove(itemName);
    }
    /**
    * Getter method that will return an int of the adventurer's total inventory weight
    *@return String message regarding adventurer rank
    */
    public int getInventoryWeight() { return this.currrentWeight; }
    
    /**
    * Method that will check the current weight of inventory is lower than MAX_WEIGHT
    *@return boolean true if current weight is less than MAX_WEIGHT, false if greater than MAX_WEIGHT
    */
    public boolean checkWeight() { return currrentWeight <= MAX_WEIGHT; }
   
    public void setScore(int b){
        score += b;
    } 
}
