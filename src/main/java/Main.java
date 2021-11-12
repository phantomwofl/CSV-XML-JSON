import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws ParserConfigurationException, TransformerException, IOException, SAXException {

        String[] columnMapping = {"id", "firstName", "lastName", "country", "age"};
        String fileName = "data.csv";
        List<Employee> list = parseCSV(columnMapping, fileName);


        String[] employeeOne = "1,John,Smith,USA,25".split(",");
        String[] employeeTwo = "2,Ivan,Petrov,RU,23".split(",");

        try (CSVWriter writer = new CSVWriter(new FileWriter("data.csv"))) {
            writer.writeNext(employeeOne);
            writer.writeNext(employeeTwo);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String json = listToJson(list);
        writeString(json, "data.json");


        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.newDocument();

        Element staff = document.createElement("staff");
        document.appendChild(staff);

        addEmployee(document, "1", "John", "Smith", "USA", "25");
        addEmployee(document, "2", "Ivan", "Petrov", "RU", "23");

        DOMSource domSource = new DOMSource(document);
        StreamResult streamResult = new StreamResult(new File("data.xml"));

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.transform(domSource, streamResult);


        List<Employee> listXML = parseXML("data.xml");
        writeString(listToJson(listXML), "data2.json");

        String json3 = readString("data.json");
        List<Employee> list3 = jsonToList(json3);
        for (Employee i : list3) {
            System.out.println(i.toString());
        }
    }

    public static List parseCSV(String[] columnMapping, String fileName) {
        List<Employee> list = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(fileName))) {
            ColumnPositionMappingStrategy<Employee> strategy = new ColumnPositionMappingStrategy<>();
            strategy.setType(Employee.class);
            strategy.setColumnMapping(columnMapping);

            CsvToBean<Employee> csv = new CsvToBeanBuilder<Employee>(reader)
                    .withMappingStrategy(strategy)
                    .build();
            list = csv.parse();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    private static String listToJson(List<Employee> list) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        Type listType = new TypeToken<List<Employee>>() {
        }.getType();
        String json = gson.toJson(list, listType);
        return json;
    }

    private static void writeString(String json, String name) {
        try (FileWriter writer = new FileWriter(name)) {
            writer.write(json);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static ArrayList<Employee> parseXML(String name) throws ParserConfigurationException, IOException, SAXException {

        ArrayList<Employee> list = new ArrayList<>();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new File(name));
        Node root = doc.getDocumentElement();
        NodeList nodeList = root.getChildNodes();

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);

            if (Node.ELEMENT_NODE == node.getNodeType()) {
                Element employee = (Element) node;
                NamedNodeMap map = employee.getAttributes();

                Node idAtt = null;
                Node firstNameAtt = null;
                Node lastNameAtt = null;
                Node countryAtt = null;
                Node ageAtt = null;

                for (int a = 0; a < map.getLength(); a++) {

                    idAtt = map.getNamedItem("id");
                    firstNameAtt = map.getNamedItem("firstname");
                    lastNameAtt = map.getNamedItem("lastname");
                    countryAtt = map.getNamedItem("country");
                    ageAtt = map.getNamedItem("age");

                }
                Employee employee1 = new Employee(Integer.parseInt(idAtt.getNodeValue()), firstNameAtt.getNodeValue(), lastNameAtt.getNodeValue(), countryAtt.getNodeValue(), Integer.parseInt(ageAtt.getNodeValue()));
                list.add(employee1);
            }
        }
        return list;
    }

    private static void addEmployee(Document doc, String id, String firstname, String lastname, String country, String age) {
        Element employee = doc.createElement("employee");
        employee.setAttribute("id", id);
        employee.setAttribute("firstname", firstname);
        employee.setAttribute("lastname", lastname);
        employee.setAttribute("country", country);
        employee.setAttribute("age", age);
        doc.getDocumentElement().appendChild(employee);
    }

    private static String readString(String name) {
        String s = null;

        try (BufferedReader br = new BufferedReader(new FileReader(name))) {
            s = br.readLine();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        return s;
    }

    private static ArrayList<Employee> jsonToList (String json) {
        ArrayList<Employee> list = new ArrayList<>();
        JSONParser parser = new JSONParser();
        try {
            JSONArray employee = (JSONArray) parser.parse(json);
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            for (Object emp: employee) {
                list.add(gson.fromJson(emp.toString(), Employee.class));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return list;
    }
}
