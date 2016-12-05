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
    static final int MAX_SCORE = 25;
    static final int MAX_WEIGHT = 100;

    /**
    * Variable that stores the current weight of the adventurer's inventory
    */
    private int currentWeight;
    
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
    /**
    *Hashtable that will take in a key, the adventurer's health, and return the correct message based on the adventurer's health
    */

    Hashtable<Integer, String> healthStatus = new Hashtable();



    /**
    *Hashtable that will take in a key, the adventurer's score, and return their rank based on their score
    */
    Hashtable<Integer,String> adventurerRank = new Hashtable<>();
    
    /**
    *Needed a variable to store the direction a player must go back to in when entering a dark room.
    */
    private String mustGoBack;
    
    static synchronized GameState instance() {
        if (theInstance == null) {
            theInstance = new GameState();
        }
        return theInstance;
    }

    private GameState() {
        inventory = new ArrayList<Item>();
        health = 25;
        score = 0;


        int a = 0;
        Integer aI = a;
        int b = 5;
        Integer bI = b;
        int c = 10;
        Integer cI = c;
        int d = 15;
        Integer dI = d;
        int e = 20;
        Integer eI = e;
        int f = 25;
        Integer fI = f;

        String aS = "You are a scrub with no points";
        String bS = "You're finally getting the hang of it, but you still need to get good";
        String cS = "Good job there bud on not getting killed";
        String dS = "If you got this far you might actually win";
        String eS = "You are a great and respected Knight";
        String fS = "Holy crap, you won";

        adventurerRank.put(aI, aS);
        adventurerRank.put(bI, bS);
        adventurerRank.put(cI, cS);
        adventurerRank.put(dI, dS);
        adventurerRank.put(eI, eS);
        adventurerRank.put(fI, fS);

        String aH = "You start to feel cold, you're losing conciousness, confused and fearing the end is imminent";
        String bH = "You're whole body is aching, much more of this and you fear you won't last long.";
        String cH = "Meh, it's just a flesh wound";
        String dH = "tis but a scratch";
        String eH = "You are fit as a fiddle... with one broken string";
        String fH = "You feel one hunna";

        healthStatus.put(aI,aH);
        healthStatus.put(bI,bH);
        healthStatus.put(cI,cH);
        healthStatus.put(dI,dH);
        healthStatus.put(eI,eH);
        healthStatus.put(fI,fH);
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

        String temp = s.nextLine();
        if (temp.startsWith(INVENTORY_LEADER)) {
            String inventoryList = temp.substring(INVENTORY_LEADER.length());
            String[] inventoryItems = inventoryList.split(",");
            for (String itemName : inventoryItems) {
                try {
                    addToInventory(dungeon.getItem(itemName));
                } catch (Item.NoItemException e) {
                    throw new IllegalSaveFormatException("No such item '" +
                        itemName + "'");
                }
            }
            health = Integer.parseInt(s.nextLine().substring(HEALTH_LEADER.length()));
        } else {
            health = Integer.parseInt(temp.substring(HEALTH_LEADER.length()));
        }
        score = Integer.parseInt(s.nextLine().substring(SCORE_LEADER.length()));
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

        if(currentHealth <= 0)
        {
            GameState.instance().die();
        }
        Integer actualHealth = currentHealth;
        if(currentHealth%5 != 0)
        {
            actualHealth = actualHealth - (currentHealth%5);
        }
        return healthStatus.get(actualHealth) + "\n";

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
        Integer actualScore = currentScore;

        GameState.instance().checkHealth();

        if(currentScore%5 != 0)
        {
            actualScore = actualScore - (currentScore%5);
        }
        return adventurerRank.get(actualScore) + "\n";

    }

    public void checkHealth()
    {
        if(health <= 0)
        {
            GameState.instance().die();
        }
    }
    public void checkScore()
    {
        if(score == MAX_SCORE || score > MAX_SCORE)
        {
            GameState.instance().win();
        }
    }
    public void removeItem(String itemName) throws Item.NoItemException
    {
        GameState x = GameState.instance();
        inventory.remove(x.getItemFromInventoryNamed(itemName));
    }

    public void win()
    {
        System.out.println("You win, Congratulations!");
        System.exit(0);
    }
    public void die()
    {
        System.out.println("W A S T E D \n");
        System.out.println("You died");
        System.exit(0);
    }

    /**
    * Getter method that will return an int of the adventurer's total inventory weight
    *@return String message regarding adventurer rank
    */
    public int getInventoryWeight() { return this.currentWeight; }
    
    /**
    * Method that will check the current weight of inventory is lower than MAX_WEIGHT
    *@return boolean true if current weight is less than MAX_WEIGHT, false if greater than MAX_WEIGHT
    */
    public boolean checkWeight() { return currentWeight <= MAX_WEIGHT; }

    public void setWeight(int x){
        currentWeight += x;
    }
    public void setScore(int b){
        score += b;
    }
    public String goBack(){
        return mustGoBack;
    }
    public void setGoBack(String dir){
        switch (dir){
            case 's': mustGoBack = "n";
            case 'n': mustGoBack = "s";
            case 'w': mustGoBack = "e";
            case 'e': mustGoBack = "w";
            case 'u': mustGoBack = "d";
            case 'd': mustGoBack = "u";
        }       
    }

}
