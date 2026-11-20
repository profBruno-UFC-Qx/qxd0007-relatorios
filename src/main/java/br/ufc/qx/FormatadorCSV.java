package br.ufc.qx;

import java.util.List;

public class FormatadorCSV implements Formatador {

  @Override
  public String formatar(List<Pagamento> pagamentos) {
    StringBuilder relatorio = new StringBuilder();
    relatorio.append("destino,valor\n");
    for (Pagamento p : pagamentos) {
      relatorio.append(p.getDestino()).append(",").append(p.getValor()).append("\n");
    }
    return relatorio.toString();
  }
}
