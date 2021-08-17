import com.install4j.api.launcher.Variables;

import java.io.IOException;
import java.util.Map;

public class HelloCli {

    private static String getGreetingText() {

        String greetingName = "world";
        try {
            Map variables = Variables.loadFromPreferenceStore(true);
            if (variables != null) {
                greetingName = (String)variables.get("greeting");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return "Hello " + greetingName + "!";
    }

    public static void main(String[] args) {
        System.out.println(getGreetingText());
        String additionalMessage = System.getProperty("additional.message");
        if (additionalMessage != null) {
            // Print the VM property that is contained in the hello.vmoptions file
            System.out.println(additionalMessage);
        }
    }

}
