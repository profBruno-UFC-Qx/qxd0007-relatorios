# Relatório — herança, sobrescrita de métodos e polimorfismo em Java num exemplo mínimo

Este repositório é material de apoio da disciplina de Programação Orientada a Objetos (QXD0007). Ele existe para o aluno ler o código e sair sabendo: a sintaxe de herança em Java (`extends`, `super`, `@Override`), como a sobrescrita de métodos funciona de verdade (inclusive quando é a *superclasse* que chama o método) e por que isso faz do polimorfismo uma vantagem prática, e não só uma palavra da prova.

O domínio é deliberadamente pequeno: um `Relatorio` de `Pagamento`s que pode ser gerado em formato `CSV` ou `HTML`.

---

## 🚀 Conceitos de Herança Explorados

O código não tem estágios a narrar, então os conceitos estão na ordem em que vale a pena estudá-los: da sintaxe mais simples até as decisões de design.

### 1. `extends` e a cadeia de construtores com `super(...)`

`RelatorioCSV` e `RelatorioHTML` são um `Relatorio` (relação *é um*). Nenhuma delas guarda o nome do arquivo ou a lista de pagamentos: entregam esses dados à superclasse e só acrescentam a extensão que as diferencia.

```java
public class RelatorioCSV extends Relatorio {

  public RelatorioCSV(String nomeBase, List<Pagamento> pagamentos) {
    super(nomeBase + ".csv", pagamentos);
  }
```

**Por que essa decisão?**
* O construtor da subclasse recebe só o nome base (`"pagamentos"`), e a extensão vem do *tipo*. A alternativa óbvia, passar `"pagamentos.csv"` de fora, permitiria criar um `RelatorioCSV` chamado `"pagamentos.html"`.
* A subclasse não repete os campos da superclasse: o estado continua em um único lugar.

### 2. `private` nos campos, `protected` nos métodos

A superclasse não entrega seus campos às subclasses. Entrega métodos.

```java
public class Relatorio {

  private final String nome;
  private final List<Pagamento> pagamentos;
  private final String versao = "0.1";
  ...
  protected String getVersao() {
    return "v" + versao;
  }

  protected List<Pagamento> getPagamentos() {
    return pagamentos;
  }
```

**Por que essa decisão?**
* Com `protected String versao`, toda subclasse passaria a depender de como a versão é *representada*. Trocar o tipo do campo quebraria as subclasses. Como o acesso é por `getVersao()`, a superclasse pode mudar a representação sem que ninguém perceba.
* `protected` em métodos define o que é permitido *a quem herda*, sem abrir para o resto do programa.

### 3. `@Override` + `super.metodo()`: estender em vez de substituir

`gerarRelatorio()` das subclasses não reescreve o cabeçalho. Parte do resultado da superclasse e acrescenta o corpo.

```java
  @Override
  public String gerarRelatorio() {
    StringBuilder relatorio = new StringBuilder(super.gerarRelatorio());
    relatorio.append("Gerando arquivo csv\n");
    for (Pagamento p : getPagamentos()) {
      relatorio.append(p.getDestino()).append(",").append(p.getValor()).append("\n");
    }
    return relatorio.toString();
  }
```

**Por que essa decisão?**
* Sem `super.gerarRelatorio()`, o cabeçalho seria copiado em cada formato, e mudá-lo exigiria editar todas as classes.
* `@Override` faz o compilador conferir que existe mesmo um método a sobrescrever. Um erro de digitação em `gerarRelatorio` vira erro de compilação, e não um método novo que ninguém chama.
* Custo real desse desenho: se uma subclasse *esquecer* o `super.`, o cabeçalho some sem nenhum aviso. Esse é um dos motivos pelos quais existem outras formas de organizar esse reuso.

### 4. Sobrescrita seletiva: herdar é o padrão, sobrescrever é a exceção

Só `RelatorioCSV` sobrescreve `getVersao()`. `RelatorioHTML` herda o comportamento da superclasse sem escrever nada.

```java
  @Override
  protected String getVersao() {
    return super.getVersao() + ".1";
  }
```

**Por que essa decisão?**
* `super.getVersao() + ".1"` em vez de escrever `"v0.1.1"`: se a versão da superclasse mudar, o CSV acompanha. Um literal copiado ficaria silenciosamente desatualizado.
* Ter uma subclasse que sobrescreve e outra que não mostra que herança é reuso: cada classe escreve só o que a diferencia.

### 5. Despacho dinâmico: a superclasse chama, a subclasse responde

O ponto mais fácil de passar despercebido. `Relatorio.gerarRelatorio()` chama `getVersao()`:

```java
  public String gerarRelatorio() {
    return "Imprimindo relatório " + nome + "\n"
            + "Versão " + getVersao() + "\n"
            + "-----------------------------\n";
  }
```

Quando o objeto é um `RelatorioCSV`, esse mesmo código, escrito na superclasse, executa o `getVersao()` *da subclasse* e imprime `v0.1.1`. Para um `RelatorioHTML`, imprime `v0.1`. O teste abaixo fixa esse comportamento:

```java
  @Test
  void variavelDoTipoBaseExecutaOMetodoDaSubclasse() {
    Relatorio relatorio = new RelatorioCSV("pagamentos", pagamentos);

    String saida = relatorio.gerarRelatorio();

    assertTrue(saida.contains("Versão v0.1.1"));
    assertTrue(saida.contains("Gerando arquivo csv"));
  }
```

**Por que isso importa?**
* O método executado é decidido pelo tipo do *objeto* em tempo de execução, não pelo tipo da *variável* (`Relatorio`). É isso que sustenta o polimorfismo.
* Quem escreveu `Relatorio` não conhecia `RelatorioCSV` e, mesmo assim, o código dele passa a usar o comportamento novo.

### 6. Polimorfismo: quem usa não precisa saber o formato

Em `Main`, a mesma variável do tipo `Relatorio` recebe formatos diferentes, e a chamada não muda.

```java
    Relatorio relatorio = new RelatorioHTML("pagamentos", pagamentos);
    System.out.print(relatorio.gerarRelatorio());

    relatorio = new RelatorioCSV("pagamentos", pagamentos);
    System.out.print(relatorio.gerarRelatorio());
```

**Por que é uma vantagem?**
* Um novo formato é uma nova classe. `Relatorio` e a chamada a `gerarRelatorio()` continuam como estão, sem `if`/`switch` por tipo espalhados pelo código.

### 7. Herança sem quebrar o encapsulamento

Quem herda tem acesso a `getPagamentos()`, mas não deve poder corromper o estado da superclasse.

```java
  public Relatorio(String nome, List<Pagamento> pagamentos) {
    this.nome = nome;
    this.pagamentos = List.copyOf(pagamentos);
  }
```

**Por que essa decisão?**
* Guardar a referência recebida deixaria o relatório à mercê de quem criou a lista: alterá-la depois mudaria um relatório já construído. `List.copyOf` guarda uma cópia imutável, e `getPagamentos()` a devolve sem que uma subclasse consiga adicionar ou remover itens.
* `Pagamento` também tem campos `final`: um objeto que não muda não precisa de cópia defensiva.
* Os testes `relatorioNaoSofreAlteracoesNaListaOriginal` e `listaExpostaParaSubclassesNaoPodeSerModificada` documentam isso.

### 8. Testabilidade: retornar `String` em vez de imprimir

`gerarRelatorio()` devolve o texto, e quem chama decide o que fazer com ele. Só `Main` usa `System.out`.

```java
    System.out.print(relatorio.gerarRelatorio());
```

**Por que essa decisão?**
* Com `System.out.println` dentro do método, o teste precisaria redirecionar a saída padrão. Devolvendo `String`, o teste vira uma comparação simples, e o mesmo método serviria para gravar em arquivo ou enviar por rede, sem mudar as classes.

### 9. Quando *não* usar herança

Este exemplo usa herança porque o objetivo é ensiná-la, mas vale desconfiar dele. `RelatorioCSV` e `RelatorioHTML` diferem apenas em *como formatam os pagamentos*, e o resto (nome, versão, cabeçalho) é igual. Isso se resolve com **composição**: um único `Relatorio` que recebe um objeto formatador (CSV, HTML, ...) e delega a ele a montagem do corpo. Essa versão não está implementada neste repositório.

**Como decidir?**
* Herança serve quando existe uma relação *é um* estável e o comportamento herdado faz sentido para todas as subclasses.
* Composição serve quando só uma *parte* do comportamento varia. Ela evita o acoplamento entre superclasse e subclasse (como o risco do `super.` esquecido do item 3) e permite trocar o formato em tempo de execução, sem criar uma classe nova para cada combinação.
* Pergunte-se: "o que muda é o objeto inteiro ou só uma parte dele?"

---

## 🛠️ Tecnologias Utilizadas
* **Java:** a linguagem do exemplo. Requer JDK 17 ou superior, por causa do Gradle 9.
* **Gradle (wrapper 9.2.0):** compila o projeto e roda os testes, sem precisar instalar o Gradle.
* **JUnit 5 (Jupiter, BOM 5.10.0):** os testes de `RelatorioTest`, que documentam o comportamento esperado de cada classe.

## ▶️ Como executar

Com um JDK 17+ instalado, na raiz do projeto:

```bash
./gradlew test
```

Para ver a saída do exemplo, compile e rode o `Main`:

```bash
./gradlew classes
java -cp build/classes/java/main br.ufc.qx.main.Main
```

Também dá para abrir o projeto no IntelliJ IDEA e executar `br.ufc.qx.main.Main` direto do editor.

## 📖 Como usar este repositório para estudo

* Rode o `Main` e leia a saída acompanhando o código: observe que o HTML imprime `Versão v0.1` e o CSV `Versão v0.1.1`, sendo que quem imprime a versão é sempre o mesmo método de `Relatorio`.
* Apague o método `getVersao()` de `RelatorioCSV` e rode `./gradlew test`: veja quais testes falham e por quê. Depois troque `@Override` por um erro de digitação no nome do método e veja o compilador reclamar.
* Apague a linha `super.gerarRelatorio()` de `RelatorioHTML` e observe o que sai do relatório e o que os testes acusam. Depois compare com o risco descrito no item 3.
* Crie um `RelatorioMarkdown` que estenda `Relatorio` e note que nem `Relatorio` nem `Main` precisam mudar para o novo formato existir.
* Note que `new Relatorio("x", pagamentos)` compila e gera só um cabeçalho. Pergunte-se se um objeto assim faz sentido e como a linguagem poderia impedi-lo. Essa pergunta é o ponto de partida das classes abstratas.
* Reflita sobre o item 9: o que ganharia e o que perderia esse código se `RelatorioCSV` e `RelatorioHTML` fossem trocados por um formatador injetado?

---
Desenvolvido por [Bruno Mateus](https://github.com/brunomateus) para fins didáticos.
