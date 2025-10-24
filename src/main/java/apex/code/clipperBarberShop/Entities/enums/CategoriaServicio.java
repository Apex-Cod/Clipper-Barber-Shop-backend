package apex.code.clipperBarberShop.Entities.enums;

/**
 * Enum para categorías de servicios de barbería/peluquería
 */
public enum CategoriaServicio {
    CORTE("Corte de cabello"),
    TINTE("Tinte y coloración"),
    BARBA("Arreglo de barba"),
    TRATAMIENTO_CAPILAR("Tratamiento capilar"),
    PEINADO("Peinado y styling"),
    DEPILACION("Depilación"),
    MANICURA("Manicura"),
    PEDICURA("Pedicura"),
    FACIAL("Tratamiento facial"),
    MASAJE("Masaje"),
    MAQUILLAJE("Maquillaje"),
    CEJAS_PESTAÑAS("Cejas y pestañas"),
    ALISADO("Alisado y permanente"),
    EXTENSION("Extensiones"),
    OTRO("Otro servicio");

    private final String descripcion;

    CategoriaServicio(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
