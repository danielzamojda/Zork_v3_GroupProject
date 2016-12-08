import java.util.ArrayList;

/**
 * Created by cranium on 12/7/16.
 */
public class WeightCommand extends Command {

    WeightCommand() {
    }

    public String execute() {
        String value = "You are currently carrying " ;
        GameState current = GameState.instance();
        value += current.getInventoryWeight() + " lbs";
        return value;
    }
}

