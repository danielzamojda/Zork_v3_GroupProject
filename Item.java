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
    
    //Variable for if it's a lightsource
    private boolean lightSource;

    Item(Scanner s, boolean suppFeature) throws NoItemException,
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

        if(suppFeature) {
            lightSource = Boolean.valueOf(s.nextLine());
        } else {
            lightSource = false;
        }

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
                events.put(verbLine.substring(0,verbLine.indexOf('[')), eventList);
                verbLine = verbLine.substring(0,verbLine.indexOf('[')+1) + verbLine.substring(verbLine.indexOf('['));
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

    /*This method returns a string indicating how much the player has been wounded
  and by what item (the item this method was called on). Using the event hashtable,
  it will collect the String, convert it to an integer and update GameState.instance()
  accordingly.
  @author Jonathan Samuelsen
  @author Daniel Zamojda
  @author Brendon Kertcher
*/
    public String wound(int healthPoints) {
        healthPoints = healthPoints * -1;
        GameState.instance().changeHealth(healthPoints);
        if(healthPoints > 0)
            return "You gained " + healthPoints + " health points!";
        return "You lost " + Math.abs(healthPoints) + " health points!";
    }
    
    /*This method returns the string explaining that the item it was called on has
      vanished and disappeared from the dungeon forever. This method shouldn't need
      a spot in the hashtable, though.
      @author Jonathan Samuelsen
      @author Daniel Zamojda
      @author Brendon Kertcher
    */
    public String disappear(Item item) throws NoItemException
    {
        String output;
        try {
            GameState current = GameState.instance();
            Dungeon currentDungeon = current.getDungeon();
            output = item.getPrimaryName() + " is gone forever!";
            current.removeItem(item.getPrimaryName());
            currentDungeon.removeItem(item.getPrimaryName());
        } catch(NoItemException e) {
            output = "";
        }
         return output;
    }

    
    /*This method returns a string indicating what item has replaced the item the method
      was called on. The hashtable's value of the key will be the name of the
      replacement item. This will make it easy to look up the item, add it to the inventory
      and take the old item out. By replace of course, it's "transforming".
      @author Jonathan Samuelsen
      @author Daniel Zamojda
      @author Brendon Kertcher
    */

    public String transform(String newItemName) throws NoItemException
    {
        GameState current = GameState.instance();
        Dungeon theDungeon = GameState.instance().getDungeon();

        Item heldItem = this;
        Item newItem = theDungeon.getItem(newItemName);

        current.addToInventory(newItem);
        current.removeFromInventory(heldItem);
        theDungeon.removeItem(heldItem.getPrimaryName());

        String output = "This item has now transformed into " + newItemName + "\n";
        System.out.println(output);
        return "";
    }

    public String teleport()
    {
        Room newRoom = GameState.instance().getDungeon().getRandomRoom();
        while(newRoom.isDark() && !GameState.instance().hasLight())
            newRoom = GameState.instance().getDungeon().getRandomRoom();

        GameState.instance().setAdventurersCurrentRoom(newRoom);

        return "You teleported to " + newRoom.getTitle() + "\n" + newRoom.describe();
    }


    public void updateInventory()
    {
        GameState.instance().setWeight(weight);
    }
    public void dropFromInvetory() { GameState.instance().setWeight(weight*(-1));}

    public String[] getEventsForVerb(String verb) { return events.get(verb); }
    public boolean isLightSource(){return lightSource;}
    public void setLightSource(boolean b){
        if (b)
            lightSource = true;
        else
            lightSource = false;
    }

    public int getWeight() { return weight; }
}
