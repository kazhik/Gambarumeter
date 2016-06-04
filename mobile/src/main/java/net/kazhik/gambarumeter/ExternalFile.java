package net.kazhik.gambarumeter;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.Xml;

import com.google.android.gms.wearable.DataMap;

import net.kazhik.gambarumeterlib.storage.DataStorage;
import net.kazhik.gambarumeterlib.storage.LocationTable;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by kazhik on 16/02/04.
 */
public class ExternalFile {
    private static final String TAG = "ExternalFile";
    private String dir;
    private SimpleDateFormat timeFormatter =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    public ExternalFile() {
        this.dir = Environment.getExternalStorageDirectory().getPath();

    }
    public void importGpxFile(Context context, String filename) {
        /*
        List<Location> locations = this.readGpxFile(filename);
        this.saveLocations(context, locations);
        */
    }
    public void importTcxFile(Context context, String filename) {

    }

    private List<Location> readGpxFile(String filename) {
        List<Location> locations = new ArrayList<>();
        InputStream fis = null;
        XmlPullParserFactory pullMaker;
        try {
            fis = new BufferedInputStream(new FileInputStream(filename));
            pullMaker = XmlPullParserFactory.newInstance();

            XmlPullParser parser = pullMaker.newPullParser();


            parser.setInput(fis, null);

            List<String> elements = new ArrayList<>();
            Location loc = null;
            String currentTagName;
            SimpleDateFormat timeFormatter = new SimpleDateFormat("y'-'M'-'d'T'H':'m':'s'Z'");

            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        currentTagName = parser.getName();
                        switch (currentTagName) {
                            case "trkpt":
                                // now get the lat and lon
                                String lat = parser.getAttributeValue(null, "lat");
                                String lon = parser.getAttributeValue(null, "lon");

                                loc = new Location(LocationManager.GPS_PROVIDER);
                                loc.setLatitude(Double.parseDouble(lat));
                                loc.setLongitude(Double.parseDouble(lon));

                                elements.clear();
                                elements.add(currentTagName);
                                break;
                            case "ele":
                            case "name":
                            case "time":
                                if (elements.size() == 1 && elements.get(0).equals("trkpt")) {
                                    elements.add(currentTagName);
                                }
                                break;
                            default:
                                break;
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        currentTagName = parser.getName();
                        switch (currentTagName) {
                            case "trkpt":
                                locations.add(loc);
                                elements.clear();
                                break;
                            case "ele":
                            case "name":
                            case "time":
                                if (elements.size() == 2) {
                                    elements.remove(1);
                                }
                                break;
                            default:
                                break;
                        }
                        break;
                    case XmlPullParser.TEXT:
                        if (elements.isEmpty()) {
                            break;
                        }
                        if (elements.size() != 2) {
                            break;
                        }
                        if (loc == null) {
                            break;
                        }
                        switch (elements.get(1)) {
                            case "name":
                                Bundle b = new Bundle();
                                b.putString("name", parser.getText());
                                loc.setExtras(b);
                                break;
                            case "ele":
                                loc.setAltitude(Double.valueOf(parser.getText()));
                                break;
                            case "time":
                                long currentTime = timeFormatter.parse(parser.getText()).getTime();
                                loc.setTime(currentTime);
                                break;
                            default:
                                break;
                        }
                        break;

                }
                eventType = parser.next();
            }

        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            this.close(fis);
        }
//        Toast.makeText(context, "Finished XmlPull parsing", Toast.LENGTH_SHORT).show();

        return locations;
    }
    private void saveLocations(Context context, List<Location> locations) {
        if (locations.isEmpty()) {
            return;
        }
        long startTime = locations.get(0).getTime();
        LocationTable locTable = new LocationTable(context);
        locTable.openWritable();
        for (Location loc: locations) {
            locTable.insert(startTime, loc);
        }
        locTable.openWritable();

        locTable.close();
    }
    private List<Location> readLocations(Context context, long startTime) {
        LocationTable locTable = new LocationTable(context);
        locTable.openReadonly();
        List<Location> locations = locTable.selectAll(startTime);
        locTable.close();
        return locations;
    }
    private String prepareAppDir(String appname) {
        String root = Environment.getExternalStorageDirectory().getPath();
        String dirname = root + "/" + appname;
        boolean result = makeDir(dirname);
        if (!result) {
            dirname = "";
        }
        return dirname;

    }
    private boolean makeDir(String dirname) {
        boolean result;
        File dir = new File(dirname);
        if (dir.isFile()) {
            result = dir.delete();
            if (!result) {
                return false;
            }
        }
        if (!dir.exists()) {
            result = dir.mkdir();
            if (!result) {
                return false;
            }
        }
        return true;
    }
    private String getGpxFilePath(String dirname, long startTime) {
        return this.getFilePath(dirname, startTime, "gpx");
    }
    private String getTcxFilePath(String dirname, long startTime) {
        return this.getFilePath(dirname, startTime, "tcx");
    }
    private String getFilePath(String dirname, long startTime, String extension) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss");
        return String.format("%s/%s.%s",
                dirname,
                formatter.format(new Date(startTime)),
                extension);

    }
    public void writeText(XmlSerializer serializer,
                          String tagName,
                          String text) throws IOException {
        serializer.startTag("", tagName);
        serializer.text(text);
        serializer.endTag("", tagName);

    }
    public void writeExtension(XmlSerializer serializer,
                               String groupTagName,
                               String tagName,
                               String text) throws IOException {
        serializer.startTag("", "Extensions");
        serializer.startTag("", groupTagName);
        serializer.attribute("", "xmlns",
                "http://www.garmin.com/xmlschemas/ActivityExtension/v1");
        this.writeText(serializer, tagName, text);
        serializer.endTag("", groupTagName);
        serializer.endTag("", "Extensions");

    }
    public String exportTcxFile(Context context, long startTime) {
        String appname = context.getString(R.string.app_name);

        String dirname = this.prepareAppDir(appname);
        String tcxFilePath = this.getTcxFilePath(dirname, startTime);

        DataStorage dataStorage = new DataStorage(context);
        DataMap dataMap = dataStorage.load(startTime);
        dataStorage.close();

        XmlSerializer serializer = Xml.newSerializer();
        FileOutputStream tcxFile = null;
        OutputStreamWriter stWriter = null;
        try {
//            StringWriter writer = new StringWriter();
            tcxFile = new FileOutputStream(tcxFilePath);
            stWriter = new OutputStreamWriter(tcxFile);
            serializer.setOutput(stWriter);
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output",
                    true);
            serializer.startDocument("UTF-8", true);
            serializer.startTag("", "TrainingCenterDatabase");
            serializer.attribute("",
                    "xsi:schemaLocation",
                    "http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2 " +
                            "http://www.garmin.com/xmlschemas/TrainingCenterDatabasev2.xsd");
            serializer.startTag("", "Activities");
            serializer.startTag("", "Activity");
            serializer.attribute("", "Sport", "Running");
            serializer.startTag("", "Lap");

            String startTimeStr = this.timeFormatter.format(new Date(startTime));
            serializer.attribute("", "StartTime", startTimeStr);

            this.writeText(serializer, "Id", startTimeStr);

            long totalTimeSeconds =
                    (dataMap.getLong(DataStorage.COL_STOP_TIME) - startTime) / 1000;
            this.writeText(serializer, "TotalTimeSeconds",
                    String.valueOf(totalTimeSeconds));

            this.writeText(serializer, "Calories", "0");
            this.writeText(serializer, "Intensity", "Active");
            this.writeText(serializer, "TriggerMethod", "Manual");

            DataMap workoutDataMap = dataMap.getDataMap(DataStorage.TBL_WORKOUT);
            float distance = workoutDataMap.getFloat(DataStorage.COL_DISTANCE);
            this.writeText(serializer, "DistanceMeters", String.valueOf(distance));

            int heartRate = workoutDataMap.getInt(DataStorage.COL_HEART_RATE);
            this.writeText(serializer, "AverageHeartRateBpm",
                    String.valueOf(heartRate));

            int steps = workoutDataMap.getInt(DataStorage.COL_STEP_COUNT);
            this.writeExtension(serializer,
                    "ActivityLapExtension", "Steps", String.valueOf(steps));

            List<DataMap> locationMapList =
                    dataMap.getDataMapArrayList(DataStorage.TBL_LOCATION);
            serializer.startTag("", "Track");
            for (DataMap locationMap: locationMapList) {
                serializer.startTag("", "Trackpoint");

                long timestamp = locationMap.getLong(DataStorage.COL_TIMESTAMP);
                String timestampStr = this.timeFormatter.format(new Date(timestamp));
                this.writeText(serializer, "Time", timestampStr);
                serializer.startTag("", "Position");
                double latitude = locationMap.getDouble(DataStorage.COL_LATITUDE);
                String latitudeStr = String.valueOf(latitude);
                this.writeText(serializer, "LatitudeDegrees", latitudeStr);
                double longitude = locationMap.getDouble(DataStorage.COL_LONGITUDE);
                String longitudeStr = String.valueOf(longitude);
                this.writeText(serializer, "LongitudeDegrees", longitudeStr);
                serializer.endTag("", "Position");
                double altitude = locationMap.getDouble(DataStorage.COL_ALTITUDE);
                String altitudeStr = String.valueOf(altitude);
                this.writeText(serializer, "AltitudeMeters", altitudeStr);

                serializer.endTag("", "Trackpoint");

            }
            serializer.endTag("", "Track");
            //serializer.text(str);
            serializer.endTag("", "Lap");
            serializer.endTag("", "Activity");
            serializer.endTag("", "Activities");
            /*
            serializer.attribute("", "number", String.valueOf(messages.size()));
            for (Message msg: messages){
                serializer.startTag("", "message");
                serializer.attribute("", "date", msg.getDate());
                serializer.startTag("", "title");
                serializer.text(msg.getTitle());
                serializer.endTag("", "title");
                serializer.startTag("", "url");
                serializer.text(msg.getLink().toExternalForm());
                serializer.endTag("", "url");
                serializer.startTag("", "body");
                serializer.text(msg.getDescription());
                serializer.endTag("", "body");
                serializer.endTag("", "message");
            }
            */
            serializer.endTag("", "TrainingCenterDatabase");
            serializer.endDocument();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            this.close(stWriter);
            this.close(tcxFile);
        }



        return tcxFilePath;
    }
    public String exportGpxFile(Context context, long startTime) {
        List<Location> locations = this.readLocations(context, startTime);

        String appname = context.getString(R.string.app_name);

        String dirname = this.prepareAppDir(appname);
        String gpxFilePath = this.getGpxFilePath(dirname, startTime);
        GpxMetaInfo gpxMetaInfo = new GpxMetaInfo();
        gpxMetaInfo.setCreator(appname);
        this.writeGpxFile(gpxFilePath, gpxMetaInfo, locations);

        return gpxFilePath;
    }
    private void writeGpxFile(String filename, GpxMetaInfo metaInfo,
                              List<Location> locations) {

        FileOutputStream gpxFile = null;
        OutputStreamWriter stWriter = null;
        try {
            gpxFile = new FileOutputStream(filename);
            stWriter = new OutputStreamWriter(gpxFile);
            stWriter
                    .write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                            + "<gpx\n"
                            + " version=\"1.1\"\n"
                            + "creator=\"" + metaInfo.getCreator() + "\"\n"
                            + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
                            + "xmlns=\"http://www.topografix.com/GPX/1/1\"\n"
                            + "xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\">\n");

            stWriter.write("<metadata>\n");
            stWriter.write("<name><![CDATA[" + metaInfo.getName() + "]]></name>\n");
            stWriter.write("<desc><![CDATA[" + metaInfo.getDescription() + "]]></desc>\n");
            stWriter.write("</metadata>\n");

            stWriter.write("<trk>\n" + "<trkseg>\n");

            SimpleDateFormat dtFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

            long currentTime;
            double latitude;
            double longitude;
            String pointName;

            for (Location loc: locations) {
                currentTime = loc.getTime();
                latitude = loc.getLatitude();
                longitude = loc.getLongitude();
                Bundle b = loc.getExtras();
                if (b != null && b.getString("name") != null) {
                    pointName = b.getString("name");
                } else {
                    pointName = "";
                }

                stWriter.write("<trkpt lat=\"" + latitude
                        + "\" lon=\"" + longitude + "\">\n"
                        + "<ele>" + loc.getAltitude() + "</ele>\n"
                        + "<time>" + dtFormat.format(currentTime) + "</time>\n"
                        + "<speed>" + 0 + "</speed>\n"
                        + "<name>" + pointName + "</name>\n"
                        + "<fix>none</fix>\n"
                        + "</trkpt>\n");
            }

            stWriter.write("</trkseg>\n" + "</trk>\n" + "</gpx>\n");
            stWriter.flush();

        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            this.close(stWriter);
            this.close(gpxFile);
        }
    }
    private void close(Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }
    private static class GpxMetaInfo {
        private String creator = "";
        private String name = "";
        private String description = "";

        public String getCreator() {
            return creator;
        }

        public void setCreator(String creator) {
            this.creator = creator;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

    }

}
