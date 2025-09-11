package co.com.bancolombia.usecase;

import co.com.bancolombia.model.loanapplication.LoanApplicationReviewItem;
import co.com.bancolombia.model.loanapplication.gateways.LoanApplicationRepository;
import co.com.bancolombia.model.loantype.gateways.LoanTypeRepository;
import co.com.bancolombia.model.userquery.gateways.IUserQueryClient;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class LoanApplicationListUseCase {

    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanTypeRepository loanTypeRepository;
    private final IUserQueryClient iUserQueryClient;

    public Flux<LoanApplicationReviewItem> listLoanApplications(int page, int size, Long loanTypeId, Long status) {
        int p = Math.max(page, 0);
        int s = Math.max(size, 1);
        int offset = p * s;
        return loanApplicationRepository.filterLoanApplications(offset, s, loanTypeId, status)
   .flatMap(app ->
                Mono.zip(
                        loanTypeRepository.findById(app.getLoanTypeId()),
                        iUserQueryClient.getUserByEmail(app.getEmail())
                ).map(tuple -> {
                    var loanType = tuple.getT1();
                    var user     = tuple.getT2();
                    String statusName = statusToLabel(app.getApplicationStatusId());
                    double approvedMonthlyDebtTotal =
                            (app.getApplicationStatusId() != null && app.getApplicationStatusId() == 4)
                                    ? annuityPayment(app.getLoanAmount(), loanType.getInterestRate(), app.getTermMonths())
                                    : 0.0;

                    return new LoanApplicationReviewItem(
                            app.getId(),
                            app.getLoanAmount(),
                            app.getTermMonths(),
                            app.getEmail(),
                            user.name(),
                            loanType.getName(),
                            loanType.getInterestRate(),
                            statusName,
                            user.baseSalary(),
                            approvedMonthlyDebtTotal
                    );
                })
        );
    }

    private static String statusToLabel(Long statusId) {
        if (statusId == null) return "Unknown";
        return switch (statusId.intValue()) {
            case 1 -> "Pending review";
            case 2 -> "Rejected";
            case 3 -> "Manual review";
            case 4 -> "Approved";
            default -> "Unknown";
        };
    }
    /**
     * Calcula la cuota fija (anualidad) de un préstamo con interés compuesto mensual.
     *
     * Variables del modelo financiero:
     * - P (principal): monto del préstamo o capital inicial (amount).
     * - i (tasa mensual): tasa de interés por periodo en forma decimal (p. ej., 0.02 = 2% mensual).
     * - n (meses): número total de cuotas/periodos de pago.
     *
     * Casos especiales:
     * - Si P <= 0 o n <= 0, la cuota es 0.
     * - Si i == 0, la cuota es P / n (sin intereses).
     *
     * Fórmula utilizada (anualidad vencida):
     *   A = P * i * (1 + i)^n / ((1 + i)^n - 1)
     *
     * @param amount        principal P (capital). Si es null se asume 0.0.
     * @param monthlyRate   tasa mensual i en decimal (no en %). Si es null se asume 0.0.
     * @param months        número de cuotas n. Si es null o <= 0 se asume 0.
     * @return              valor de la cuota fija A para cada mes.
     */

    private static double annuityPayment(Double amount, Double monthlyRate, Integer months) {
        double P = amount == null ? 0.0 : amount;
        double i = monthlyRate == null ? 0.0 : monthlyRate;
        int n = (months == null || months <= 0) ? 0 : months;

        if (P <= 0.0 || n <= 0) return 0.0;
        if (i == 0.0) return P / n;

        double factor = Math.pow(1.0 + i, n);
        return P * i * factor / (factor - 1.0);
    }

}

