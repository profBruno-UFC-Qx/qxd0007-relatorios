package br.ufc.qx;

public class Pagamento {

  private final String destino;
  private final double valor;

  public Pagamento(String destino, double valor) {
    this.destino = destino;
    this.valor = valor;
  }

  public String getDestino() {
    return destino;
  }

  public double getValor() {
    return valor;
  }

  @Override
  public String toString() {
    return "Pagamento{" +
            "destino='" + destino + '\'' +
            ", valor=" + valor +
            '}';
  }
}
