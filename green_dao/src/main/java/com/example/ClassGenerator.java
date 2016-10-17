package com.example;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

public class ClassGenerator {
    private static final String PROJECT_DIR = System.getProperty("user.dir");
    private static final int DB_VERSION = 1;

    public static void main(String[] args) {
        Schema schema = new Schema( DB_VERSION, "com.example.aaron.greendao.db");
        schema.enableKeepSectionsByDefault();

        addTables(schema);

        try {
            new DaoGenerator().generateAll(schema, PROJECT_DIR + "\\app\\src\\main\\java");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void addTables(final Schema schema) {
        Entity phoneStatus = addPhoneStatusEntity(schema);
    }

    private static Entity addPhoneStatusEntity(final Schema schema) {
        Entity phoneStat = schema.addEntity("PhoneStatus");
        phoneStat.addIdProperty().primaryKey().autoincrement();
        phoneStat.addBooleanProperty("isPowerOn");
        phoneStat.addDateProperty("date");
        phoneStat.addDoubleProperty("latitud");
        phoneStat.addDoubleProperty("longitud");
        phoneStat.addStringProperty("locationProvider");
        phoneStat.addStringProperty("address");
        phoneStat.addBooleanProperty("usbCharge");
        phoneStat.addBooleanProperty("acCharge");
        phoneStat.addIntProperty("batteryLevel");
        phoneStat.addIntProperty("bateryLevelScale");
        phoneStat.addFloatProperty("batteryPtc");
        return phoneStat;
    }


}
