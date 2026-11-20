package br.ufc.qx;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RelatorioTest {

  private final List<Pagamento> pagamentos = List.of(
          new Pagamento("Aluguel", 1500.0),
          new Pagamento("Energia", 230.5));

  private String linhas(String... linhas) {
    return String.join("\n", linhas) + "\n";
  }

  @Test
  void relatorioComFormatadorCsvGeraLinhaDeColunasEUmaLinhaPorPagamento() {
    Relatorio relatorio = new Relatorio(pagamentos);

    assertEquals(linhas(
            "destino,valor",
            "Aluguel,1500.0",
            "Energia,230.5"), relatorio.gerarRelatorio(new FormatadorCSV()));
  }

  @Test
  void relatorioComFormatadorHtmlGeraTabela() {
    Relatorio relatorio = new Relatorio(pagamentos);

    assertEquals(linhas(
            "<table>",
            "  <tr><th>Destino</th><th>Valor</th></tr>",
            "  <tr><td>Aluguel</td><td>1500.0</td></tr>",
            "  <tr><td>Energia</td><td>230.5</td></tr>",
            "</table>"), relatorio.gerarRelatorio(new FormatadorHTML()));
  }

  @Test
  void novoFormatoPodeSerCriadoComLambdaSemMudarRelatorio() {
    Relatorio relatorio = new Relatorio(pagamentos);
    Formatador apenasDestinos = ps -> {
      StringBuilder saida = new StringBuilder();
      for (Pagamento p : ps) {
        saida.append(p.getDestino()).append("\n");
      }
      return saida.toString();
    };

    assertEquals(linhas("Aluguel", "Energia"), relatorio.gerarRelatorio(apenasDestinos));
  }

  @Test
  void relatorioNaoPodeSerGeradoSemFormatador() {
    Relatorio relatorio = new Relatorio(pagamentos);

    assertThrows(NullPointerException.class, () -> relatorio.gerarRelatorio(null));
  }

  @Test
  void relatorioSemPagamentosGeraApenasALinhaDeColunas() {
    Relatorio relatorio = new Relatorio(List.of());

    assertEquals(linhas("destino,valor"), relatorio.gerarRelatorio(new FormatadorCSV()));
  }

  @Test
  void relatorioNaoSofreAlteracoesNaListaOriginal() {
    List<Pagamento> original = new ArrayList<>(pagamentos);
    Relatorio relatorio = new Relatorio(original);

    original.add(new Pagamento("Internet", 99.9));

    assertFalse(relatorio.gerarRelatorio(new FormatadorCSV()).contains("Internet"));
  }
}
