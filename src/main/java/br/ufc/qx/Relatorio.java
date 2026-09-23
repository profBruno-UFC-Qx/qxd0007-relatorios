package br.ufc.qx;

import java.util.List;

public class Relatorio {

  private final String nome;
  private final List<Pagamento> pagamentos;
  private final String versao = "0.1";

  public Relatorio(String nome, List<Pagamento> pagamentos) {
    this.nome = nome;
    this.pagamentos = List.copyOf(pagamentos);
  }

  protected String getVersao() {
    return "v" + versao;
  }

  protected List<Pagamento> getPagamentos() {
    return pagamentos;
  }

  public String gerarRelatorio() {
    return "Imprimindo relatório " + nome + "\n"
            + "Versão " + getVersao() + "\n"
            + "-----------------------------\n";
  }
}
