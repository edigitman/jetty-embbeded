package pack;

import pack.model.Crop;
import pack.model.CropCycle;
import pack.model.CropInOrder;
import pack.model.CropType;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * Created by edi on 6/30/2015.
 */
public class CropsServlet extends HttpServlet {

    private int attempts;
    private List<Crop> crops;
    private int size;
    private int cycles;
    private int maxAttempts;


    public void init() {
        try {
            crops = IOUtils.loadAll("c:\\a.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {

        String query = request.getQueryString();
        response.setContentType("application/json");
        if (query == null || query.trim().isEmpty()) {
            response.getWriter().write(getAll());
        } else {
            if (query.contains("crop")) {
                response.getWriter().write(getOne(query.split("=")[1]));
            }
            if (query.contains("doWork")) {
                fixFavorableSimetry();
                List<Crop> errorCrops = validate();
                crops.removeAll(errorCrops);

                String[] params = query.split("&");
                String[] size = params[1].split("=");
                String[] cycles = params[2].split("=");
                int sizeInt = Integer.parseInt(size[1]);
                int cyclesInt = Integer.parseInt(cycles[1]);

                Algorithm a = new Algorithm(sizeInt, crops);
                List<CropCycle> history = new ArrayList<>();

                //primul ciclu de cultivare
                CropCycle cc1 = a.solve(null);
                history.add(cc1);

                for (;history.size() < cyclesInt;) {
                    cc1 = a.solve(history);
                    history.add(cc1);
                }

                response.getWriter().write(getWork(history));
            }
        }
    }

    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws IOException {
        Map params = request.getParameterMap();
        String name = ((String[]) params.get("name"))[0];

        boolean found = false;
        // update
        for (Crop c : crops) {
            if (c.getName().equals(name)) {
                found = true;
                c.setType(CropType.valueOf(((String[]) params.get("type"))[0]));
                c.setFavorable(decodeCrops((String[]) params.get("favs")));
                break;
            }
        }
        //create
        if (!found) {
            Crop crop = new Crop();
            crop.setName(name);
            crop.setType(CropType.valueOf(((String[]) params.get("type"))[0]));
            crop.setFavorable(decodeCrops((String[]) params.get("favs")));
            crops.add(crop);
        }

        Collections.sort(crops, new CropComparator());
        // todo save to file
        response.sendRedirect("/");

    }

    protected void doDelete(HttpServletRequest request,
                            HttpServletResponse response) {
        Map params = request.getParameterMap();
        String name = (String) params.get("name");

        Iterator<Crop> cropIt = crops.iterator();
        while (cropIt.hasNext()) {
            Crop crop = cropIt.next();
            if (crop.getName().equals(name)) {
                cropIt.remove();
                break;
            }
        }
        // todo save to file
    }

    /**
     * Validates that all the crops have type and favorable list
     *
     * @return list of crops that don;t have type
     */
    private List<Crop> validate() {
        List<Crop> result = new ArrayList<>();
        for (Crop crop : crops) {
            if (crop.getType() == null || crop.getFavorable().isEmpty()) {
                result.add(crop);
            } else {
                for (Crop c : crop.getFavorable()) {
                    if (!c.getFavorable().contains(crop)) {
                        result.add(crop);
                        break;
                    }
                }
            }
        }
        return result;
    }

    /**
     * Adds simetry in the crops list
     */
    private void fixFavorableSimetry() {
        for (Crop crop : crops) {
            for (Crop c : crop.getFavorable()) {
                if (!c.getFavorable().contains(crop)) {
                    c.addFavorable(crop);
                }
            }
        }
    }


    private List<Crop> decodeCrops(String[] favs) {
        List<Crop> result = new ArrayList<>();

        for (String name : favs) {
            for (Crop crop : crops) {
                if (name.equals(crop.getName())) {
                    result.add(crop);
                }
            }
        }
        return result;
    }

    private String getWork(List<CropCycle> history){
        StringBuilder sb = new StringBuilder("[");
        for (CropCycle cropCycle : history) {
            sb.append("{");
            sb.append("\"index\": \"").append(cropCycle.getIndex()).append("\", ");
            sb.append("\"crops\": [");
            for(CropInOrder cin : cropCycle.getCropsInOrder()){
                sb.append("{ \"name\": \"").append(cin.getCrop().getName()).append("\", ");
                sb.append("\"type\": \"").append(cin.getCrop().getType()).append("\" }, ");
            }
            sb.deleteCharAt(sb.length() - 2);
            sb.append("]},\n");
        }
        sb.deleteCharAt(sb.length() - 2);
        sb.append("]");

        return sb.toString();
    }

    private String getAll() {
        StringBuilder sb = new StringBuilder("[");
        for (Crop crop : crops) {
            sb.append("{");
            sb.append("\"name\": \"").append(crop.getName()).append("\", ");
            sb.append("\"type\": \"").append(crop.getType()).append("\", ");
            sb.append("\"fav\": ").append(crop.getFavorableNames());
            sb.append("},\n");
        }

        sb.deleteCharAt(sb.length() - 2);
        sb.append("]");

        return sb.toString();
    }

    private String getOne(String name) {
        for (Crop crop : crops) {
            if (crop.getName().equals(name)) {
                StringBuilder sb = new StringBuilder("{");
                sb.append("\"name\": \"").append(crop.getName()).append("\", ");
                sb.append("\"type\": \"").append(crop.getType()).append("\", ");
                sb.append("\"fav\": ").append(crop.getFavorableNames()).append("}");
                return sb.toString();
            }
        }
        return "";
    }

}
