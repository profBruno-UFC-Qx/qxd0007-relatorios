package br.ufc.qx;

import java.util.List;

public class RelatorioHTML extends Relatorio {

  public RelatorioHTML(String nomeBase, List<Pagamento> pagamentos) {
    super(nomeBase + ".html", pagamentos);
  }

  @Override
  public String gerarRelatorio() {
    StringBuilder relatorio = new StringBuilder(super.gerarRelatorio());
    relatorio.append("Gerando arquivo HTML\n");
    relatorio.append("<table>\n");
    for (Pagamento p : getPagamentos()) {
      relatorio.append("  <tr><td>").append(p.getDestino())
              .append("</td><td>").append(p.getValor()).append("</td></tr>\n");
    }
    relatorio.append("</table>\n");
    return relatorio.toString();
  }
}
