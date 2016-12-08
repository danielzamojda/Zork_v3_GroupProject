
class DropCommand extends Command {

    private String itemName;

    DropCommand(String itemName) {
        this.itemName = itemName;
    }

    public String execute() {
        if (itemName == null || itemName.trim().length() == 0) {
            return "Drop what?\n";
        }
        try {
            Item theItem = GameState.instance().getItemFromInventoryNamed(
                itemName);
            theItem.dropFromInvetory();
            GameState.instance().removeFromInventory(theItem);
            GameState.instance().getAdventurersCurrentRoom().add(theItem);
            GameState.instance().setLightSource(false);
            for (Item x : GameState.instance().getInventory()){
                if(x.isLightSource())
                    GameState.instance().setLightSource(true);
            }
            return itemName + " dropped.\n";
        } catch (Item.NoItemException e) {
            return "You don't have a " + itemName + ".\n";
        }
    }
}
