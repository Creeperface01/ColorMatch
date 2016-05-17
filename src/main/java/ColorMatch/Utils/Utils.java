package main.java.ColorMatch.Utils;

public class Utils {

    public static String getLastColor(String msg) {
        int i = 0;
        char lastColor = "f".charAt(0);

        for (char chr : msg.toCharArray()) {
            String str = Character.toString(chr);

            if (str.toLowerCase().equals("§")) {
                if ("0123456789AaBbCcDdEeFf".indexOf(msg.charAt(i + 1)) != -1) {
                    lastColor = msg.charAt(i + 1);
                }
            }
        }

        return String.valueOf(lastColor);
    }
}
