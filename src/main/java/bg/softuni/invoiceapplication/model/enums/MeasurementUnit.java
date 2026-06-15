package bg.softuni.invoiceapplication.model.enums;

import lombok.Getter;

@Getter
public enum MeasurementUnit {
    PIECE("Piece"),
    KILOGRAM("Kilogram"),
    GRAM("Gram"),
    LITER("Liter"),
    METER("Meter"),
    SQUARE_METER("Square meter"),
    CUBIC_METER("Cubic meter"),
    HOUR("Hour"),
    DAY("Day"),
    SERVICE("Service");

    private final String displayName;

    MeasurementUnit(String displayName) {
        this.displayName = displayName;
    }
}
