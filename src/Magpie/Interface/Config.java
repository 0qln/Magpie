package Interface;

import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Logger;
import Misc.LoggerConfigurator;

public final class Config {
    private static Logger logger = LoggerConfigurator.configureLogger(Config.class);
    public static boolean Debug = false;
    
    public static final Interface.UCI.Option<?>[] UCI_Options = {
        new Interface.UCI.Option<Opponent>("UCI_Opponent", OptionType.String, Opponent.getDefault(), Optional.empty(), Optional.empty(), Optional.empty(), 
            rawData -> {
                // TODO: make this safe
                if (rawData.length < 4) return Optional.empty();
                return Optional.of(new Opponent(
                    String.join(" ", Arrays.copyOfRange(rawData, 3, rawData.length)), 
                    Title.parse(rawData[0]), 
                    rawData[1].equals("none") ? Optional.empty() : Optional.of(Integer.parseInt(rawData[0])),
                    EntityType.parse(rawData[2])
                ));
            })
    };
 
    @SuppressWarnings("unchecked")
    public static <T> Optional<Interface.UCI.Option<T>> getOption(String name) {
        logger.info("Name: " + name);
        return Arrays.stream(UCI_Options)
            .filter(opt -> name.equals(opt.getName()))
            .map(opt -> (Interface.UCI.Option<T>) opt)
            .findFirst();
    }
}


