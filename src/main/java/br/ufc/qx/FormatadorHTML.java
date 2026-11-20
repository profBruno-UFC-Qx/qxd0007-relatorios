package br.ufc.qx;

import java.util.List;

public class FormatadorHTML implements Formatador {

  @Override
  public String formatar(List<Pagamento> pagamentos) {
    StringBuilder relatorio = new StringBuilder();
    relatorio.append("<table>\n");
    relatorio.append("  <tr><th>Destino</th><th>Valor</th></tr>\n");
    for (Pagamento p : pagamentos) {
      relatorio.append("  <tr><td>").append(p.getDestino())
              .append("</td><td>").append(p.getValor()).append("</td></tr>\n");
    }
    relatorio.append("</table>\n");
    return relatorio.toString();
  }
}
