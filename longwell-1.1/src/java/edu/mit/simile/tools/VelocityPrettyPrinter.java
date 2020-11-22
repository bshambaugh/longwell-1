package edu.mit.simile.tools;

import java.io.File;
import java.io.FileInputStream;


/**
 * Pretty print the Velocity templates.

 * @author Mark H. Butler
 */
public class VelocityPrettyPrinter {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("java VelocityPrettyPrinter <file>");
            System.out.println("Reformats Velocity template <file> with a consistent layout");
            System.exit(0);
        }

        File input = new File(args[0]);
        StringBuffer template = new StringBuffer();

        try {
            FileInputStream in = new FileInputStream(input);
            int ch;

            while ((ch = in.read()) != -1) {
                template.append((char) ch);
            }

            in.close();
        } catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }

        String[] splitTemplate = template.toString().split("\n");
        int indent = 0;

        for (int i = 0; i < splitTemplate.length; i++) {
            String current = splitTemplate[i].trim();

            if (current.startsWith("#end") || current.startsWith("#else") ||
                    current.startsWith("</html") || current.startsWith("</head") ||
                    current.startsWith("</body") || current.startsWith("</style") ||
                    current.startsWith("</script")) {
                indent--;
            }

            StringBuffer result = new StringBuffer();

            for (int j = 0; j < indent; j++) {
                result.append("  ");
            }

            result.append(current);

            if (current.startsWith("<html") || current.startsWith("<head") ||
                    current.startsWith("<body") || current.startsWith("#if") ||
                    current.startsWith("#macro") || current.startsWith("#else") ||
                    current.startsWith("#foreach") || current.startsWith("<style") ||
                    current.startsWith("<script")) {
                indent++;
            }

            System.out.println(result.toString());
        }
    }
}
