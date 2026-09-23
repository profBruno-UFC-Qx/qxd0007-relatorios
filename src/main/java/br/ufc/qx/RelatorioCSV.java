package br.ufc.qx;

import java.util.List;

public class RelatorioCSV extends Relatorio {

  public RelatorioCSV(String nomeBase, List<Pagamento> pagamentos) {
    super(nomeBase + ".csv", pagamentos);
  }

  @Override
  protected String getVersao() {
    return super.getVersao() + ".1";
  }

  @Override
  public String gerarRelatorio() {
    StringBuilder relatorio = new StringBuilder(super.gerarRelatorio());
    relatorio.append("Gerando arquivo csv\n");
    for (Pagamento p : getPagamentos()) {
      relatorio.append(p.getDestino()).append(",").append(p.getValor()).append("\n");
    }
    return relatorio.toString();
  }
}
