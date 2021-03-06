import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Scanner;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;

public class   Dungeon {

    public static class IllegalDungeonFormatException extends Exception {
        public IllegalDungeonFormatException(String e) {
            super(e);
        }
    }

    // Variables relating to both dungeon file and game state storage.
    public static String TOP_LEVEL_DELIM = "===";
    public static String SECOND_LEVEL_DELIM = "---";

    // Variables relating to dungeon file (.bork) storage.
    public static String ROOMS_MARKER = "Rooms:";
    public static String EXITS_MARKER = "Exits:";
    public static String ITEMS_MARKER = "Items:";
    
    // Variables relating to game state (.sav) storage.
    static String FILENAME_LEADER = "Dungeon file: ";
    static String ROOM_STATES_MARKER = "Room states:";

    private String name;
    private Room entry;
    private Hashtable<String,Room> rooms;
    private Hashtable<String,Item> items;
    private Hashtable<String,NPC> NPCs;
    private String filename;

    Dungeon(String name, Room entry) {
        init();
        this.filename = null;    // null indicates not hydrated from file.
        this.name = name;
        this.entry = entry;
        rooms = new Hashtable<String,Room>();
    }

    /**
     * Read from the .bork filename passed, and instantiate a Dungeon object
     * based on it.
     */
    public Dungeon(String filename) throws FileNotFoundException, 
        IllegalDungeonFormatException {

        this(filename, true);
    }

    /**
     * Read from the .bork filename passed, and instantiate a Dungeon object
     * based on it, including (possibly) the items in their original locations.
     */
    public Dungeon(String filename, boolean initState) 
        throws FileNotFoundException, IllegalDungeonFormatException {

        init();
        this.filename = filename;
        boolean suppFeature = false;

        Scanner s = new Scanner(new FileReader(filename));
        name = s.nextLine();
        if(s.nextLine().equals("Final Zork")) {
            suppFeature = true;
        }

        // Throw away delimiter.
        if (!s.nextLine().equals(TOP_LEVEL_DELIM)) {
            throw new IllegalDungeonFormatException("No '" +
                    TOP_LEVEL_DELIM + "' after version indicator.");
        }

        // Throw away Items starter.
        if (!s.nextLine().equals(ITEMS_MARKER)) {
            throw new IllegalDungeonFormatException("No '" +
                    ITEMS_MARKER + "' line where expected.");
        }

        try {
            // Instantiate items.
            while (true) {
                add(new Item(s, suppFeature));
            }
        } catch (Item.NoItemException e) {  /* end of items */ }

        // Throw away Rooms starter.
        if (!s.nextLine().equals(ROOMS_MARKER)) {
            throw new IllegalDungeonFormatException("No '" +
                    ROOMS_MARKER + "' line where expected.");
        }

        try {
            // Instantiate and add first room (the entry).
            entry = new Room(s, this, initState, false);
            add(entry);

            // Instantiate and add other rooms.
            while (true) {
                add(new Room(s, this, initState, suppFeature));
            }
        } catch (Room.NoRoomException e) {  /* end of rooms */ }

        // Throw away Exits starter.
        if (!s.nextLine().equals(EXITS_MARKER)) {
            throw new IllegalDungeonFormatException("No '" +
                    EXITS_MARKER + "' line where expected.");
        }

        try {
            // Instantiate exits.
            while (true) {
                Exit exit = new Exit(s, this, suppFeature);
            }
        } catch (Exit.NoExitException e) {  /* end of exits */ }

        if(suppFeature) {
            s.nextLine();           // Throw away "NPCs:" line
            try {
                while (true) {
                    add(new NPC(s, this, initState));
                }
            } catch (NPC.NoNPCException e) { /* end of NPCs */ }
        }

        s.close();
    }
    
    // Common object initialization tasks, regardless of which constructor
    // is used.
    private void init() {
        rooms = new Hashtable<String,Room>();
        items = new Hashtable<String,Item>();
        NPCs = new Hashtable<>();
    }

    /*
     * Store the current (changeable) state of this dungeon to the writer
     * passed.
     */
    void storeState(PrintWriter w) throws IOException {
        w.println(FILENAME_LEADER + getFilename());
        w.println(ROOM_STATES_MARKER);
        for (Room room : rooms.values()) {
            room.storeState(w);
        }
        w.println(TOP_LEVEL_DELIM);
    }

    /*
     * Restore the (changeable) state of this dungeon to that reflected in the
     * reader passed.
     */
    void restoreState(Scanner s) throws GameState.IllegalSaveFormatException {

        // Note: the filename has already been read at this point.
        
        if (!s.nextLine().equals(ROOM_STATES_MARKER)) {
            throw new GameState.IllegalSaveFormatException("No '" +
                ROOM_STATES_MARKER + "' after dungeon filename in save file.");
        }

        String roomName = s.nextLine();
        while (!roomName.equals(TOP_LEVEL_DELIM)) {
            getRoom(roomName.substring(0,roomName.length()-1)).restoreState(s, this);
            roomName = s.nextLine();
        }
    }

    public Room getEntry() { return entry; }
    public String getName() { return name; }
    public String getFilename() { return filename; }
    public void add(Room room) { rooms.put(room.getTitle(),room); }
    public void add(Item item) { items.put(item.getPrimaryName(),item); }

    public Room getRoom(String roomTitle) {
        return rooms.get(roomTitle);
    }

    /**
     * Get the Item object whose primary name is passed. This has nothing to
     * do with where the Adventurer might be, or what's in his/her inventory,
     * etc.
     */
    public Item getItem(String primaryItemName) throws Item.NoItemException {
        
        if (items.get(primaryItemName) == null) {
            throw new Item.NoItemException();
        }
        return items.get(primaryItemName);
    }


    public void removeItem(String itemName) throws Item.NoItemException{
        if(items.remove(itemName) == null){
            throw new Item.NoItemException();
        }
        items.remove(itemName);

    }

    public Room getRandomRoom() {
        ArrayList<Room> roomList = new ArrayList<>();
        for(Room r : rooms.values()) {
            roomList.add(r);
        }
        int x = (int) (Math.random() * roomList.size());
        return roomList.get(x);

        // TAKE THIS OUT
    }

    public Hashtable<String,Item> getItems() { return items; }

    public void add(NPC npc) { NPCs.put(npc.getName(), npc); }

    public Hashtable<String,NPC> getNPCs() {
        return NPCs;
    }
    public NPC getNPC(String name)
    {
        return NPCs.get(name);
    }
}
