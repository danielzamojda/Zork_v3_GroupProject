class TakeCommand extends Command {

    private String itemName;
    private String overburdened;

    TakeCommand(String itemName) {
        this.itemName = itemName;
    }

    public String execute() {

        int MAXWEIGHT = GameState.maxWeight;
        if (GameState.instance().getAdventurersCurrentRoom().isDark() &&
            !GameState.instance().hasLight())
            return "The room is dark and you are unable to see anything to take.";
        if (itemName == null || itemName.trim().length() == 0) {
            return "Take what?\n";
        }
        overburdened = "Sorry, you are overburdened with weight";
        try {
            Room currentRoom = GameState.instance().getAdventurersCurrentRoom();
            Item theItem = currentRoom.getItemNamed(itemName);
            int predictedWeight = theItem.getWeight() + GameState.instance().getWeight();
            if ((GameState.instance().checkWeight()) && (predictedWeight<MAXWEIGHT))
            {

                GameState.instance().addToInventory(theItem);
                currentRoom.remove(theItem);
                theItem.updateInventory();
                if(theItem.isLightSource()){
                    GameState.instance().setLightSource(true);
                }
                return itemName + " taken.\n";
            }
            else {
                return overburdened;
            }

        } catch (Item.NoItemException e) {
            // Check and see if we have this already. If no exception is
            // thrown from the line below, then we do.
            try {
                GameState.instance().getItemFromInventoryNamed(itemName);
                return "You already have the " + itemName + ".\n";
            } catch (Item.NoItemException e2) {
                return "There's no " + itemName + " here.\n";
            }
        }

    }

}
