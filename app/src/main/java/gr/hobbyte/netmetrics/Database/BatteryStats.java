package gr.hobbyte.netmetrics.database;

/**
 * This class holds battery info about each operation
 */
public class BatteryStats {

    //Battery used data.
    private String batteryUsed;

    public BatteryStats(){
        //Empty Constructor
    }

    public String getBatteryUsed() {
        return batteryUsed;
    }

    public void setBatteryUsed(String batteryUsed) {
        this.batteryUsed = batteryUsed;
    }
}
