package dev.deepdaddyttv.deepnullreforged.hubnull;

public enum HubNullStationNullType {
    NONE,
    DEEP,
    DAMP,
    DUMP,
    DEN,
    HEX;

    public static HubNullStationNullType byId(int id) {
        HubNullStationNullType[] values = values();
        if (id < 0 || id >= values.length) {
            return NONE;
        }
        return values[id];
    }
}
