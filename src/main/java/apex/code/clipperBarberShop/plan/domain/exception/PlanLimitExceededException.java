package apex.code.clipperBarberShop.plan.domain.exception;

public class PlanLimitExceededException extends RuntimeException {
    private final String recurso;
    private final Integer limiteActual;
    private final Integer limitePermitido;

    public PlanLimitExceededException(String recurso, Integer limiteActual, Integer limitePermitido) {
        super(String.format("Límite del plan excedido para %s. Actual: %d, Permitido: %d", 
              recurso, limiteActual, limitePermitido));
        this.recurso = recurso;
        this.limiteActual = limiteActual;
        this.limitePermitido = limitePermitido;
    }

    public String getRecurso() {
        return recurso;
    }

    public Integer getLimiteActual() {
        return limiteActual;
    }

    public Integer getLimitePermitido() {
        return limitePermitido;
    }
}
