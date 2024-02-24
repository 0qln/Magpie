package Interface;

public enum Title
{
    GM, IM, FM, WGM, WIM, none;

    public static Title parse(String input) {
        for (Title title : Title.values()) {
            if (title.name().equalsIgnoreCase(input)) {
                return title;
            }
        }
        return none;
    }
}
