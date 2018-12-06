package com;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.logging.Logger;

public class weather {

    private static double longestDayTimeTemp;
    private static long longestDayTime;
    private static long shortestDayTime;
    private static double shortestDayTimeTemp;

    private static String cityOfShortestDay = "";
    private static String cityOfLongestDay = "";
    private static int storeTempOfShortestDaycounter=0;

    private static Logger log = Logger.getLogger("here-weather");


    public weather() {
        longestDayTimeTemp = 0;
        longestDayTime = 0;

        shortestDayTime = 0;
        shortestDayTimeTemp = 0;
    }

    public static boolean find(File f, String searchString) {
        boolean result = false;
        Scanner in = null;
        try {
            in = new Scanner(new FileReader(f));
            while(in.hasNextLine() && !result) {
                if (in.nextLine().contains(searchString))
                    return true;
            }
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        finally {
            try { in.close() ; } catch(Exception e) { /* ignore */ }
        }
        return false;
    }

    public static JSONObject getJsonObjFromFile(String file, String searchString) throws IOException, ParseException {
        JSONParser parser = new JSONParser();
        JSONArray jsonarray = (JSONArray) parser.parse(new FileReader(file));

        for (Object row : jsonarray) {
            JSONObject rowJson = (JSONObject) row;
            if (rowJson.get("name").toString().contains(searchString)) {
                return rowJson;
            }
        }
        return null;
    }

        public static JSONObject parseJsoninString(String inline) throws ParseException {
            JSONParser parser = new JSONParser();
            // parse json data into json object
            JSONObject jobj = (JSONObject)parser.parse(inline);
            return jobj;
        }

    public static String getHttpGetResponse(String urlString) throws IOException {
        String inline = "";

        URL url = new URL(urlString );
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        // open connection stream
        con.connect();
        // get the response code
        int responseCode = con.getResponseCode();
        if (responseCode != 200) {
            return null;
        }
        else
        {
            //Scanner functionality will read the JSON data from the stream
            Scanner sc = new Scanner(url.openStream());
            while(sc.hasNext())
            {
                inline+=sc.nextLine();
            }
            log.info("JSON Response:\n " +inline);
            //Close the stream when reading the data has been finished
            sc.close();
        }
        return inline;
    }


    public static String getURLResponseById(String id) throws IOException {
        String appid = "2129b0d4a034345575dc2dca4e05a2e6";
        String urlString = "https://samples.openweathermap.org/data/2.5/weather?id=" + id +"&appid="+appid;
        return urlString;
    }

    public static String addId2URL(String id) throws IOException {
        String appid = "2129b0d4a034345575dc2dca4e05a2e6";
        String urlString = "https://samples.openweathermap.org/data/2.5/group?id=" + id +"&appid="+appid;
        return urlString;
    }

    public static void main(String[] args) throws IOException, ParseException {
        JSONObject jsonObject = null;
        String[] cities = {"Tel Aviv","Singapore","Auckland","Ushuaia","Miami","London","Berlin","Reykjavik","CapeTown","Kathmandu"};



        for (String thecity : cities) {
            // import json file in current directory into an object. json is in the fortmat of id, city
            jsonObject = getJsonObjFromFile(System.getProperty("user.dir")+"/city.list.json",thecity);
            if (jsonObject !=null){
                System.out.println("name = " + thecity+ ", city id = " + jsonObject.get("id").toString());
                if (jsonObject.get("id") != null) {
                    String urlString = getURLResponseById(jsonObject.get("id").toString());
                    String inline = getHttpGetResponse(urlString);

                    // parse the response
                    JSONObject jobj = parseJsoninString(inline);
                    // get sys.sunset
                    JSONObject tmp = (JSONObject) jobj.get("sys");
                    long sunset = (long) tmp.get("sunset");
                    long sunrise = (long) tmp.get("sunrise");
                    long daytime = sunset-sunrise;

                    tmp = (JSONObject) jobj.get("main");
                    double temperture = (double) tmp.get("temp");

                    storeTempOfLongestDay(daytime, temperture, thecity);
                    storeTempOfShortestDay(daytime, temperture, thecity);


                }
            }
            else {
                System.out.println("couldn't read json object from city.list");
            }
        }


        System.out.println("city of shortest daytime = "+cityOfShortestDay+
                ", shortest day time = " + shortestDayTime+
                ", temperture of shortest daytime = " + shortestDayTimeTemp);

        System.out.println("city of longest daytime = "+cityOfLongestDay+
                ", longest day time = " + longestDayTime+
                ", temperture of longest daytime = " + longestDayTimeTemp);

    }

    private static void storeTempOfLongestDay(long daytime, double temperture, String city) {

        if (longestDayTime < daytime ){
            longestDayTime = daytime;
            longestDayTimeTemp = temperture;
            cityOfLongestDay = city;
        }

    }

    private static void storeTempOfShortestDay(long daytime, double temperture, String city) {
        // only at the first time
        if (storeTempOfShortestDaycounter == 0) {
            shortestDayTime = longestDayTime;
        }
        else {
            if (daytime < shortestDayTime) {
                shortestDayTime = daytime;
            }
        }
        cityOfShortestDay = city;
        shortestDayTimeTemp = temperture;
        storeTempOfShortestDaycounter++;
    }

    private static double getShortestDayTime(){
        return shortestDayTimeTemp;
    }
    private static double getLongestDayTimeTemp(){
        return longestDayTimeTemp;
    }

    // main with group of id's
//    String appid = "2129b0d4a034345575dc2dca4e05a2e6";
//    String urlString = "https://samples.openweathermap.org/data/2.5/group?id=";
//    // get all id's
//        for (String city : cities) {
//        jsonObject = getJsonObjFromFile(System.getProperty("user.dir")+"/city.list.json",city);
//        if (jsonObject ==null) {
//            log.info("couldn't retrieve "+city+" from city.list");
//            continue;
//        }
//
//        System.out.println("name = " + city + ", city id = " + jsonObject.get("id").toString());
//        if (jsonObject.get("id") == null) {
//            log.info("couldn't retrieve id from city.list");
//            continue;
//        }
//        urlString+=jsonObject.get("id")+",";
//
//    }
//    urlString += "&units=metric&appid=59fb36f901b9798c53b88d1d8bd0a3cd";
//    String inline = getHttpGetResponse(urlString);
//
//    // parse the response
//    JSONObject jobj = parseJsoninString(inline);
}
