package pack;

import pack.model.Crop;
import pack.model.CropType;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by edi on 6/22/2015.
 */
public class IOUtils {

    public static List<Crop> loadAll(String agreate) throws IOException {
        List<Crop> result = new ArrayList<>();
        readFile(agreate, result, true);

        Collections.sort(result, new CropComparator());

        return result;
    }

    private static void readFile(String file, List<Crop> list, boolean agreate) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(file), StandardCharsets.UTF_8);
        for (String line : lines) {
            String[] words = line.split(" ");
            Crop cultura = addToList(words[0], list);

            CropType type = CropType.valueOf(words[1]);
            cultura.setType(type);

            for (int i = 2; i < words.length; i++) {
                Crop ctl = addToList(words[i], list);
                cultura.addFavorable(ctl);
            }
        }
    }

    public static void writeToFile(String fileName, List<Crop> crops) throws FileNotFoundException, UnsupportedEncodingException {
        PrintWriter writer = new PrintWriter(fileName, "UTF-8");
        for (Crop crop : crops) {
            if (crop.getType() != null) {
                writer.print(crop.getName() + " " + crop.getType().name() + " ");
                for (Crop c : crop.getFavorable()) {
                    writer.print(c.getName() + " ");
                }
                writer.print("\n");
            }
        }
        writer.close();
    }

    private static Crop addToList(String cultura, List<Crop> list) {
        Crop cult = new Crop(cultura);
        if (!list.contains(cult)) {
            list.add(cult);
            return cult;
        } else {
            return list.get(list.indexOf(cult));
        }
    }

}
