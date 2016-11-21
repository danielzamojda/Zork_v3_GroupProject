import java.util.Scanner;
import java.util.Hashtable;

public class Item {

    static class NoItemException extends Exception {}

    private String primaryName;
    private int weight;
    private Hashtable<String,String> messages;
    
    /*@param This hashtable represents the events possible for every item. The key
      will be the event that is taking place. The value will be what the method needs
      (as explained below) in order to accomplish the event.
    */
    private Hashtable<String,String[]> events;

    Item(Scanner s) throws NoItemException,
        Dungeon.IllegalDungeonFormatException {

        messages = new Hashtable<String,String>();
        events = new Hashtable<>();

        // Read item name.
        primaryName = s.nextLine();
        if (primaryName.equals(Dungeon.TOP_LEVEL_DELIM)) {
            throw new NoItemException();
        }

        // Read item weight.
        weight = Integer.valueOf(s.nextLine());

        // Read and parse verbs lines, as long as there are more.
        String verbLine = s.nextLine();
        while (!verbLine.equals(Dungeon.SECOND_LEVEL_DELIM)) {
            if (verbLine.equals(Dungeon.TOP_LEVEL_DELIM)) {
                throw new Dungeon.IllegalDungeonFormatException("No '" +
                    Dungeon.SECOND_LEVEL_DELIM + "' after item.");
            }
            if(verbLine.contains("[")) {
                String temp = verbLine.substring(verbLine.indexOf('[')+1,verbLine.indexOf(']'));
                String[] eventList = temp.split(",");
                    events.put(verbLine.substring(0,verbLine.indexOf('[')+1), eventList);
                String[] verbParts = verbLine.split(":");
                verbParts[0] = verbParts[0].substring(0,verbParts[0].indexOf('[')); //IndexOutOfBounds Exception when using '(' rather than '['
                messages.put(verbParts[0],verbParts[1]);
            } else {
                String[] verbParts = verbLine.split(":");
                messages.put(verbParts[0],verbParts[1]);
            }
            verbLine = s.nextLine();
        }
    }

    boolean goesBy(String name) {
        // could have other aliases
        return this.primaryName.equals(name);
    }

    String getPrimaryName() { return primaryName; }

    public String getMessageForVerb(String verb) {
        return messages.get(verb);
    }

    public String toString() {
        return primaryName;
    }
    
    /*This method returns a string indicating the score achieved from retrieving
      the item this method was called on. It will use a separate hashtable indicated
      above in order to determine it's score point total(probably only 1). Then,
      Gamestate.instance() will be updated.
      @author Jonathan Samuelsen
      @author Daniel Zamojda
      @author Brendon Kertcher
    */
    public String score()
    {

        return null;
    }
    
    /*This method returns a string indicating how much the player has been wounded
      and by what item (the item this method was called on). Using the event hashtable,
      it will collect the String, convert it to an integer and update GameState.instance()
      accordingly.
      @author Jonathan Samuelsen
      @author Daniel Zamojda
      @author Brendon Kertcher
    */
    public String wound(int healthPoints) {
        GameState.instance().changeHealth(healthPoints);
        if(healthPoints > 0)
            return "You gained " + healthPoints + " health points!";
        return "You lost " + healthPoints + " health points!";
    }
    
    /*This method returns the string explaining that the item it was called on has
      vanished and disappeared from the dungeon forever. This method shouldn't need
      a spot in the hashtable, though.
      @author Jonathan Samuelsen
      @author Daniel Zamojda
      @author Brendon Kertcher
    */
    public String disappear(){
        Dungeon theDungeon = GameState.instance().getDungeon();
        theDungeon.removeItem(this.getPrimaryName);
        
        String output = "This item has disappeared from the Dugeon forever...";
        System.out.println(output);
        return "";
    }
    
    /*This method returns a string indicating what item has replaced the item the method
      was called on. The hashtable's value of the key will be the name of the
      replacement item. This will make it easy to look up the item, add it to the inventory
      and take the old item out. By replace of course, it's "transforming".
      @author Jonathan Samuelsen
      @author Daniel Zamojda
      @author Brendon Kertcher
    */
    public String transform(String newItemName){ return null; }
    
    /*Finally, the teleport method will return a String letting the adventurer know they
      have teleported. The String value in the hashtable will include the name of the room
      to be teleported to. Then, the Gamestate.instance() will be updated and the player
      can be moved.
      @author Jonathan Samuelsen
      @author Daniel Zamojda
      @author Brendon Kertcher
    */
    public String teleport(String newRoom)
    {
        String output = "";
        GameState current = GameState.instance();
        Dungeon theDungeon = current.getDungeon();
        Room setRoom = theDungeon.getRoom(newRoom);
        current.setAdventurersCurrentRoom(setRoom);

        output = "You have been teleported to a new room! \n ";

        setRoom.describe();
        System.out.println(output);
        return "";
    }

    public String[] getEventsForVerb(String verb) { return events.get(verb); }
}
