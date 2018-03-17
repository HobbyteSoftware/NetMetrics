package gr.hobbyte.netmetrics.database;


public class NetworkStats {

    private String connType;
    private String time;
    private String mbsused;
    private String signalStrength;
    private String timeBetweenPings;
    private String usageType;
    private String mode;
    private String jitter;
    private String lostPackets;
    private String bandwidth;

    public NetworkStats() {
        //Empty Constuctor
    }

    public String getMbsused() {
        return mbsused;
    }

    public void setMbsused(String mbsused) {
        this.mbsused = mbsused;
    }

    public String getConnType() {
        return connType;
    }

    public void setConnType(String connType) {
        this.connType = connType;
    }

    public String getSignalStrength() {
        return signalStrength;
    }

    public void setSignalStrength(String signalStrength) {
        this.signalStrength = signalStrength;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTimeBetweenPings() {
        return timeBetweenPings;
    }

    public void setTimeBetweenPings(String timeBetweenPings) {
        this.timeBetweenPings = timeBetweenPings;
    }

    public String getUsageType() {
        return usageType;
    }

    public void setUsageType(String usageType) {
        this.usageType = usageType;
    }

    public String getBandwidth() {
        return bandwidth;
    }

    public void setBandwidth(String bandwidth) {
        this.bandwidth = bandwidth;
    }

    public String getLostPackets() {
        return lostPackets;
    }

    public void setLostPackets(String lostPackets) {
        this.lostPackets = lostPackets;
    }

    public String getJitter() {
        return jitter;
    }

    public void setJitter(String jitter) {
        this.jitter = jitter;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }
}
