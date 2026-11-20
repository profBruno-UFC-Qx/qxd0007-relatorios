# Relatório — da herança à composição, o mesmo polimorfismo em dois desenhos

Este repositório é material de apoio da disciplina de Programação Orientada a Objetos (QXD0007). Ele existe para o aluno ler o código em duas versões e sair sabendo: a sintaxe de herança em Java (`extends`, `super`, `@Override`), como a sobrescrita de métodos funciona de verdade (inclusive quando é a *superclasse* que chama o método), e por que, quando só uma *parte* do comportamento varia, vale trocar a herança por **composição** com uma interface.

O domínio é deliberadamente pequeno: um `Relatorio` de `Pagamento`s que pode ser gerado em formato `CSV` ou `HTML`.

---

## 🏗️ Evolução da Arquitetura

O projeto começa com uma hierarquia de classes (`RelatorioCSV` e `RelatorioHTML` estendem `Relatorio`) e termina com um único `Relatorio` que delega a formatação a um objeto que implementa a interface `Formatador`. O primeiro estágio está no commit `05c1de0`; o segundo chegou pelo branch `utilizando-interfaces` (PR #1).

### 1. Reuso por herança: `extends`, `super` e despacho dinâmico

Cada formato é uma subclasse de `Relatorio` (relação *é um*). A superclasse guarda o estado e monta o cabeçalho; as subclasses acrescentam o corpo.

#### 1.1 `extends` e a cadeia de construtores com `super(...)`

```java
public class RelatorioCSV extends Relatorio {

  public RelatorioCSV(String nomeBase, List<Pagamento> pagamentos) {
    super(nomeBase + ".csv", pagamentos);
  }
```

**Por que essa decisão?**
* O construtor da subclasse recebe só o nome base (`"pagamentos"`), e a extensão vem do *tipo*. A alternativa óbvia, passar `"pagamentos.csv"` de fora, permitiria criar um `RelatorioCSV` chamado `"pagamentos.html"`.
* A subclasse não repete os campos da superclasse: o estado continua em um único lugar.

#### 1.2 `private` nos campos, `protected` nos métodos

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
* Com `protected String versao`, toda subclasse passaria a depender de como a versão é *representada*. Como o acesso é por `getVersao()`, a superclasse pode mudar a representação sem que ninguém perceba.
* `protected` em métodos define o que é permitido *a quem herda*, sem abrir para o resto do programa.

#### 1.3 `@Override` + `super.metodo()`: estender em vez de substituir

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
* Custo real desse desenho: se uma subclasse *esquecer* o `super.`, o cabeçalho some sem nenhum aviso. Guarde esse ponto: ele é um dos motivos do estágio 2.

#### 1.4 Sobrescrita seletiva

Só `RelatorioCSV` sobrescreve `getVersao()`. `RelatorioHTML` herda o comportamento da superclasse sem escrever nada.

```java
  @Override
  protected String getVersao() {
    return super.getVersao() + ".1";
  }
```

**Por que essa decisão?**
* `super.getVersao() + ".1"` em vez de escrever `"v0.1.1"`: se a versão da superclasse mudar, o CSV acompanha. Um literal copiado ficaria silenciosamente desatualizado.

#### 1.5 Despacho dinâmico: a superclasse chama, a subclasse responde

`Relatorio.gerarRelatorio()` chama `getVersao()`:

```java
  public String gerarRelatorio() {
    return "Imprimindo relatório " + nome + "\n"
            + "Versão " + getVersao() + "\n"
            + "-----------------------------\n";
  }
```

Quando o objeto é um `RelatorioCSV`, esse mesmo código, escrito na superclasse, executa o `getVersao()` *da subclasse* e imprime `v0.1.1`. Para um `RelatorioHTML`, imprime `v0.1`. O teste fixa esse comportamento:

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

#### 1.6 Polimorfismo: quem usa não precisa saber o formato

```java
    Relatorio relatorio = new RelatorioHTML("pagamentos", pagamentos);
    System.out.print(relatorio.gerarRelatorio());

    relatorio = new RelatorioCSV("pagamentos", pagamentos);
    System.out.print(relatorio.gerarRelatorio());
```

**Por que é uma vantagem?**
* Um novo formato é uma nova classe. `Relatorio` e a chamada a `gerarRelatorio()` continuam como estão, sem `if`/`switch` por tipo espalhados pelo código.

**O problema que fica para o próximo estágio:** `RelatorioCSV` e `RelatorioHTML` diferem apenas em *como formatam os pagamentos*. Mesmo assim, cada formato precisa ser um `Relatorio` inteiro, herdar o acoplamento com a superclasse (o `super.` que não pode ser esquecido) e ser escolhido na hora de *construir* o objeto.

### 2. Substituindo herança por composição: a interface `Formatador`

O que varia (a formatação) sai da hierarquia e vira um contrato próprio:

```java
public interface Formatador {

  String formatar(List<Pagamento> pagamentos);
}
```

Cada formato agora *implementa* esse contrato, sem estender nada e sem conhecer `Relatorio`:

```java
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
```

E `Relatorio` deixa de ser superclasse: é uma classe só, que *tem* os pagamentos e *recebe* um formatador para delegar o trabalho.

```java
public class Relatorio {

  private final List<Pagamento> pagamentos;

  public Relatorio(List<Pagamento> pagamentos) {
    this.pagamentos = List.copyOf(pagamentos);
  }

  public String gerarRelatorio(Formatador formatador) {
    Objects.requireNonNull(formatador, "Um relatório precisa de um formatador");
    return formatador.formatar(pagamentos);
  }
}
```

**Por que essa decisão?**
* **Composição em vez de herança:** só *uma parte* do comportamento variava. Com herança, cada formato carregava o relatório inteiro; com composição, cada formato carrega só o que o diferencia.
* **Formato escolhido na chamada, não na construção:** o mesmo objeto `Relatorio` gera CSV e HTML. Na versão com herança, trocar de formato exigia criar outro objeto.
  ```java
      Relatorio relatorio = new Relatorio(pagamentos);
      //CSV
      System.out.print(relatorio.gerarRelatorio(new FormatadorCSV()));
      //HTML
      System.out.print(relatorio.gerarRelatorio(new FormatadorHTML()));
  ```
* **Fim do acoplamento com a superclasse:** não há `super.` para esquecer nem `protected` para expor. `getPagamentos()` sumiu: o formatador recebe a lista como parâmetro, e só enquanto formata.
* **`Objects.requireNonNull` em vez de deixar o `NullPointerException` estourar dentro do formatador:** a falha acontece na entrada de `Relatorio`, com uma mensagem que diz o que faltou. O teste `relatorioNaoPodeSerGeradoSemFormatador` documenta isso.
* **O polimorfismo continua, só mudou de lugar:** antes, o despacho dinâmico escolhia entre subclasses de `Relatorio`; agora, `formatador.formatar(...)` escolhe entre implementações de `Formatador`.

#### 2.1 Interface com um único método: lambda vira formatador

Como `Formatador` tem um único método abstrato, um novo formato não precisa nem de classe. O teste cria um na hora:

```java
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
```

**Por que isso importa?**
* Na versão com herança, um novo formato era obrigatoriamente uma nova subclasse. Aqui, `Relatorio` está fechado para modificação e aberto para extensão de um jeito ainda mais leve.

#### 2.2 O que permaneceu: encapsulamento e testabilidade

Duas decisões atravessaram a refatoração sem mudar:

* **`List.copyOf` no construtor.** Guardar a referência recebida deixaria o relatório à mercê de quem criou a lista: alterá-la depois mudaria um relatório já construído. O teste `relatorioNaoSofreAlteracoesNaListaOriginal` existe nas duas versões. `Pagamento` tem campos `final`, então não precisa de cópia defensiva.
* **Retornar `String` em vez de imprimir.** Só `Main` usa `System.out`. Com `System.out.println` dentro do método, o teste precisaria redirecionar a saída padrão; devolvendo `String`, ele vira um `assertEquals`.

**O que se perdeu, e vale notar:** o cabeçalho com nome e versão (`Imprimindo relatório ...`, `Versão v0.1.1`) não existe mais. Ele dependia justamente da herança e da sobrescrita de `getVersao()`. Se ele fosse necessário, onde ele deveria morar agora: em `Relatorio` ou em cada `Formatador`?

---

## 🛠️ Tecnologias Utilizadas
* **Java:** a linguagem do exemplo. Requer JDK 17 ou superior, por causa do Gradle 9.
* **Gradle (wrapper 9.2.0):** compila o projeto e roda os testes, sem precisar instalar o Gradle.
* **JUnit 5 (Jupiter, BOM 5.10.0):** os testes de `RelatorioTest`, que documentam o comportamento esperado de cada versão.

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

Para estudar a versão com herança (estágio 1):

```bash
git checkout 05c1de0
```

Volte para a versão atual com `git checkout main`.

## 📖 Como usar este repositório para estudo

* Faça o checkout de `05c1de0`, rode o `Main` e observe que o HTML imprime `Versão v0.1` e o CSV `Versão v0.1.1`, sendo que quem imprime a versão é sempre o mesmo método de `Relatorio`.
* Ainda no estágio 1, apague a linha `super.gerarRelatorio()` de `RelatorioHTML` e veja o que sai do relatório e o que os testes acusam. Compare com o item 1.3.
* Compare `git diff 05c1de0 main -- src/main` e observe quantas linhas *saíram* de `Relatorio` com a troca para composição.
* No estágio 2, crie um `FormatadorMarkdown` que implemente `Formatador` e note que nem `Relatorio` nem os outros formatadores precisam mudar.
* Escreva o mesmo formato Markdown como lambda num teste. Pergunte-se quando vale uma classe e quando basta uma lambda.
* Tente recolocar o cabeçalho com versão (item 2.2) sem usar herança. Decida se ele é responsabilidade de `Relatorio` ou do `Formatador` e justifique.

---
Desenvolvido por [Bruno Mateus](https://github.com/brunomateus) para fins didáticos.
