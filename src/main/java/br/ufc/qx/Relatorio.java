package br.ufc.qx;

import java.util.List;
import java.util.Objects;

public class Relatorio {

  private final List<Pagamento> pagamentos;

  public Relatorio(List<Pagamento> pagamentos) {
    this.pagamentos = List.copyOf(pagamentos);
  }

  public String gerarRelatorio(Formatador formatador) {
    Objects.requireNonNull(formatador, "Um relatório precisa de um formatador");
    return formatador.formatar(pagamentos);
  }
}
