package Interface;

public enum EntityType
{
    Computer, Human, Unknown;

    public static EntityType parse(String input) {
        for (EntityType type : EntityType.values()) {
            if (type.name().equalsIgnoreCase(input)) {
                return type;
            }
        }
        return Unknown;
    }
}
