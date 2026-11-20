package br.ufc.qx.main;

import br.ufc.qx.*;

import java.util.ArrayList;
import java.util.List;

public class Main {
  public static void main(String[] args) {
    Pagamento p1 = new Pagamento("Sr. X", 10000);
    Pagamento p2 = new Pagamento("Sr. NM", 4000);
    Pagamento p3 = new Pagamento("Sr. T", 40230);
    Pagamento p4 = new Pagamento("Sr. F", 13230);

    List<Pagamento> pagamentos = new ArrayList<>();
    pagamentos.add(p1);
    pagamentos.add(p2);
    pagamentos.add(p3);
    pagamentos.add(p4);

    Relatorio relatorio = new Relatorio(pagamentos);
    //CSV
    System.out.print(relatorio.gerarRelatorio(new FormatadorCSV()));
    //HTML
    System.out.print(relatorio.gerarRelatorio(new FormatadorHTML()));

  }
}
