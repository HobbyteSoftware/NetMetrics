package gr.hobbyte.netmetrics;

public class Constants {
    public static final String CONN_TYPE = "ConnectionType";
    public static final String CONN_SIGNAL = "ConnectionSignalStrength";
    public static final String PING_NUM = "NumberOfPings";
    public static final String PING_BYTES = "BytesPerPing";
    public static final String PING_WAIT = "WaitAfterEachPing";
    public static final String TOTAL_BYTES = "BytesTotal";
    public static final String TOTAL_TIME = "TimeTotal";
    public static final String BATTERY_USED = "BatteryAh";
    public static final String IPERF_JITTER = "Jitter";
    public static final String IPERF_MODE = "Mode";
    public static final String IPERF_LOST_PACKETS = "Lost packets";
    public static final String IPERF_BANDWIDTH = "Bandwidth";
    public static final String USAGE_MODE = "Usage Mode";

    private Constants() {
        throw new IllegalAccessError("Utility class");
    }
}