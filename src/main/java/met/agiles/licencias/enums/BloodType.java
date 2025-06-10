package met.agiles.licencias.enums;

public enum BloodType {
    A_POSITIVE("A+"),
    A_NEGATIVE("A-"),
    B_POSITIVE("B+"),
    B_NEGATIVE("B-"),
    AB_POSITIVE("AB+"),
    AB_NEGATIVE("AB-"),
    O_POSITIVE("O+"),
    O_NEGATIVE("O-");

    private final String shortName;

    BloodType(String shortName) {
        this.shortName = shortName;
    }

    public String getShortName() {
        return shortName;
    }

    public static BloodType fromShortName(String shortName) {
        for (BloodType type : values()) {
            if (type.shortName.equalsIgnoreCase(shortName)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown blood type: " + shortName);
    }
}
