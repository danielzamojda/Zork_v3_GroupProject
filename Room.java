import com.sun.org.apache.xpath.internal.operations.Bool;

import java.util.ArrayList;
import java.util.Scanner;
import java.io.IOException;
import java.io.PrintWriter;

public class Room {

    class NoRoomException extends Exception {}

    static String CONTENTS_STARTER = "Contents: ";

    private String title;
    private String desc;
    private boolean beenHere;
    private ArrayList<Item> contents;
    private ArrayList<Exit> exits;

    /**
    *An arraylist of the NPCs that are in the room
    *@author Daniel Zamojda
    */
    private ArrayList<NPC> npcs;
    
    /**
    *A boolean that is toggled on if the room needs a light source to get its contents, 
    *or off if the adventurer is carrying a light source
    *@author Daniel Zamojda
    */
    private boolean isDark;
    
    Room(String title) {
        init();
        this.title = title;
    }

    Room(Scanner s, Dungeon d) throws NoRoomException,
        Dungeon.IllegalDungeonFormatException {

        this(s, d, true, false);
    }

    Room(Scanner s, Dungeon d, boolean initState, boolean suppFeature) throws NoRoomException,
            Dungeon.IllegalDungeonFormatException {

        init();
        title = s.nextLine();
        desc = "";
        if (title.equals(Dungeon.TOP_LEVEL_DELIM)) {
            throw new NoRoomException();
        }

        if(suppFeature) {
            isDark = Boolean.valueOf(s.nextLine());
        }

        String lineOfDesc = s.nextLine();
        while (!lineOfDesc.equals(Dungeon.SECOND_LEVEL_DELIM) &&
                !lineOfDesc.equals(Dungeon.TOP_LEVEL_DELIM)) {

            if (lineOfDesc.startsWith(CONTENTS_STARTER)) {
                String itemsList = lineOfDesc.substring(CONTENTS_STARTER.length());
                String[] itemNames = itemsList.split(",");
                for (String itemName : itemNames) {
                    try {
                        if (initState) {
                            add(d.getItem(itemName));
                        }
                    } catch (Item.NoItemException e) {
                        throw new Dungeon.IllegalDungeonFormatException(
                                "No such item '" + itemName + "'");
                    }
                }
            } else {
                desc += lineOfDesc + "\n";
            }
            lineOfDesc = s.nextLine();
        }

        // throw away delimiter
        if (!lineOfDesc.equals(Dungeon.SECOND_LEVEL_DELIM)) {
            throw new Dungeon.IllegalDungeonFormatException("No '" +
                    Dungeon.SECOND_LEVEL_DELIM + "' after room.");
        }
    }


    // Common object initialization tasks.
    private void init() {
        contents = new ArrayList<Item>();
        exits = new ArrayList<Exit>();
        beenHere = false;
        npcs = new ArrayList<>();
    }

    String getTitle() { return title; }

    void setDesc(String desc) { this.desc = desc; }

    /*
     * Store the current (changeable) state of this room to the writer
     * passed.
     */
    void storeState(PrintWriter w) throws IOException {
        w.println(title + ":");
        w.println(isDark);
        w.println("beenHere=" + beenHere);
        if (contents.size() > 0) {
            w.print(CONTENTS_STARTER);
            for (int i=0; i<contents.size()-1; i++) {
                w.print(contents.get(i).getPrimaryName() + ",");
            }
            w.println(contents.get(contents.size()-1).getPrimaryName());
        }

        w.println(Dungeon.SECOND_LEVEL_DELIM);
    }

    void restoreState(Scanner s, Dungeon d) throws 
        GameState.IllegalSaveFormatException {
        isDark = Boolean.valueOf(s.nextLine());
        String line = s.nextLine();
        if (!line.startsWith("beenHere")) {
            throw new GameState.IllegalSaveFormatException("No beenHere.");
        }
        beenHere = Boolean.valueOf(line.substring(line.indexOf("=")+1));
        line = s.nextLine();
        if (line.startsWith(CONTENTS_STARTER)) {
            String itemsList = line.substring(CONTENTS_STARTER.length());
            String[] itemNames = itemsList.split(",");
            for (String itemName : itemNames) {
                try {
                    add(d.getItem(itemName));
                } catch (Item.NoItemException e) {
                    throw new GameState.IllegalSaveFormatException(
                        "No such item '" + itemName + "'");
                }
            }
            s.nextLine();  // Consume "---".
        }
    }

    public String describe()
    {
        String description;
        if (isDark && !GameState.instance().hasLight()) {
            description = "It's too dark to see anything in the room. You'll need a light source.";
            return description;

        } else {
            if (beenHere) {
                description = title;
            } else {
                description = title + "\n" + desc;
            }
            for (Item item : contents) {
                description += "\nThere is a " + item.getPrimaryName() + " here.";
            }
            for(NPC npc : npcs) {
                description += "\n" + npc.describe();
            }
            if (contents.size() > 0) {
                description += "\n";
            }
            /*   *****VERY ANNOYING WHEN WE CAN'T SEE WHERE YOU ARE GOING******
            if (!beenHere) {
                for (Exit exit : exits) {
                    description += "\n" + exit.describe();
                }
            }
            */
            beenHere = true;
            return description;
        }
    }
    
    public Room leaveBy(String dir) {
        for (Exit exit : exits) {
            if (exit.getDir().equals(dir)) {
                return exit.getDest();
            }
        }
        return null;
    }
    
    public Exit getExit(String dir) {
        for(Exit exit : exits) {
            if (exit.getDir().equals(dir)) {
                return exit;
            }
        }
        return null;
    }

    void addExit(Exit exit) {
        exits.add(exit);
    }

    void add(Item item) {
        contents.add(item);
    }

    void remove(Item item) {
        contents.remove(item);
    }

    Item getItemNamed(String name) throws Item.NoItemException {
        for (Item item : contents) {
            if (item.goesBy(name)) {
                return item;
            }
        }
        throw new Item.NoItemException();
    }

    ArrayList<Item> getContents() {
        return contents;
    }

    /**
    *A method that sets the boolean variable isDark to either true or false,
    *if true the room requires a lightsource to view its contents,
    *if false the room may be viewed normally
    *@author Daniel Zamojda
    *@return void
    */
    public void setIsDark(boolean dark) {
        if(dark)
            isDark = true;
        else
            isDark = false;
    }
    
    public boolean isDark(){
        return isDark;
    }
    
    /**
    *Method that will return the NPC's that are in the room from the NPC arraylist
    *@return NPC's in the room
    *@author Daniel Zamojda
    */
    public ArrayList<NPC> getNPCs() { return npcs; }
    
    /**
    *Method that will add the NPC's to the NPC arraylist
    *@param  //NPC will be added to the room
    *@return void
    *@author Daniel Zamojda
    */
    public void addNPC(NPC npc) { npcs.add(npc); }
    
    public boolean containsNPC() { return !npcs.isEmpty(); }
}
