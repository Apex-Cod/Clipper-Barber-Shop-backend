package apex.code.clipperBarberShop.Entities.enums;

/**
 * Enum para definir el público objetivo de una empresa o servicio
 */
public enum PublicoObjetivo {
    HOMBRES("Hombres"),
    MUJERES("Mujeres"),
    UNISEX("Unisex"),
    NIÑOS("Niños"),
    NIÑAS("Niñas");

    private final String descripcion;

    PublicoObjetivo(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
