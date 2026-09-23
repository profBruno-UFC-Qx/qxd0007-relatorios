package br.ufc.qx;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RelatorioTest {

  private final List<Pagamento> pagamentos = List.of(
          new Pagamento("Aluguel", 1500.0),
          new Pagamento("Energia", 230.5));

  private String linhas(String... linhas) {
    return String.join("\n", linhas) + "\n";
  }

  @Test
  void relatorioBaseRetornaCabecalhoComVersaoBase() {
    Relatorio relatorio = new Relatorio("base.txt", pagamentos);

    assertEquals(linhas(
            "Imprimindo relatório base.txt",
            "Versão v0.1",
            "-----------------------------"), relatorio.gerarRelatorio());
  }

  @Test
  void relatorioCsvReaproveitaCabecalhoEUsaVersaoSobrescrita() {
    Relatorio relatorio = new RelatorioCSV("pagamentos", pagamentos);

    assertEquals(linhas(
            "Imprimindo relatório pagamentos.csv",
            "Versão v0.1.1",
            "-----------------------------",
            "Gerando arquivo csv",
            "Aluguel,1500.0",
            "Energia,230.5"), relatorio.gerarRelatorio());
  }

  @Test
  void relatorioHtmlReaproveitaCabecalhoEMantemVersaoDaBase() {
    Relatorio relatorio = new RelatorioHTML("pagamentos", pagamentos);

    assertEquals(linhas(
            "Imprimindo relatório pagamentos.html",
            "Versão v0.1",
            "-----------------------------",
            "Gerando arquivo HTML",
            "<table>",
            "  <tr><td>Aluguel</td><td>1500.0</td></tr>",
            "  <tr><td>Energia</td><td>230.5</td></tr>",
            "</table>"), relatorio.gerarRelatorio());
  }

  @Test
  void variavelDoTipoBaseExecutaOMetodoDaSubclasse() {
    Relatorio relatorio = new RelatorioCSV("pagamentos", pagamentos);

    String saida = relatorio.gerarRelatorio();

    assertTrue(saida.contains("Versão v0.1.1"));
    assertTrue(saida.contains("Gerando arquivo csv"));
  }

  @Test
  void relatorioSemPagamentosGeraApenasCabecalhoECorpoVazio() {
    Relatorio relatorio = new RelatorioCSV("vazio", List.of());

    assertEquals(linhas(
            "Imprimindo relatório vazio.csv",
            "Versão v0.1.1",
            "-----------------------------",
            "Gerando arquivo csv"), relatorio.gerarRelatorio());
  }

  @Test
  void relatorioNaoSofreAlteracoesNaListaOriginal() {
    List<Pagamento> original = new ArrayList<>(pagamentos);
    Relatorio relatorio = new RelatorioCSV("pagamentos", original);

    original.add(new Pagamento("Internet", 99.9));

    assertFalse(relatorio.gerarRelatorio().contains("Internet"));
  }

  @Test
  void listaExpostaParaSubclassesNaoPodeSerModificada() {
    Relatorio relatorio = new Relatorio("base.txt", pagamentos);

    assertThrows(UnsupportedOperationException.class,
            () -> relatorio.getPagamentos().add(new Pagamento("Internet", 99.9)));
  }
}
