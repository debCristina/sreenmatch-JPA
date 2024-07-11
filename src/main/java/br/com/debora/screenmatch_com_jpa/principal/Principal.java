package br.com.debora.screenmatch_com_jpa.principal;

import br.com.debora.screenmatch_com_jpa.model.*;
import br.com.debora.screenmatch_com_jpa.repository.SerieRepository;

import br.com.debora.screenmatch_com_jpa.service.ConsumoApi;
import br.com.debora.screenmatch_com_jpa.service.ConverteDados;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.swing.*;
import java.util.*;
import java.util.stream.Collectors;

public class Principal {

    private Scanner scanner = new Scanner(System.in);
    private ConsumoApi consumo = new ConsumoApi();
    private ConverteDados conversor = new ConverteDados();
    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=6c3e49be";

    private List<DadosSerie> dadosSeries = new ArrayList<>();
    private SerieRepository repositorio;

    private List<Serie> series = new ArrayList<>();

    private Optional<Serie> serieBusca;

    public Principal(SerieRepository repositorio) {
        this.repositorio = repositorio;
    }

    public void exibeMenu() {
        var opcao = -1;
        while (opcao != 0) {
            var menu = """
                    1 - Buscar séries
                    2 - Buscar episódios
                    3 - Listar séries buscadas
                    4 - Buscar série por titulo
                    5 - Buscar séries por ator
                    6 - Buscar séries por avaliação
                    7 - Buscar top 5 séries
                    8 - Buscar séries por categoria
                    9 - Filtrar séries
                    10 -  Buscar episódio por trecho
                    11 - Top episódios por séries
                    12 - Buscar episódios a partir de uma data
                    0 - Sair              
                    """;

            System.out.println(menu);
            opcao = scanner.nextInt();
            scanner.nextLine();

            switch (opcao) {
                case 1:
                    buscarSerieWeb();
                    break;
                case 2:
                    buscarEpisodioPorSerie();
                    break;
                case 3:
                    listarSeriesBuscadas();
                    break;
                case 4:
                    buscarSeriePorTitulo();
                    break;
                case 5:
                    buscarSeriePorAtor();
                    break;
                case 6:
                    buscarSeriesPorAvaliacao();
                    break;
                case 7:
                    buscarTop5Series();
                    break;
                case 8:
                    buscarSeriePorCategoria();
                    break;
                case 9:
                    buscarSeriePorTotalTemporada();
                    break;
                case 10:
                    buscarEpisodioPorTrecho();
                    break;
                case 11:
                    topEpisodiosPorSerie();
                    break;
                case 12:
                    buscarEpisodiosData();
                    break;
                case 0:
                    System.out.println("Saindo...");
                    break;
                default:
                    System.out.println("Opção inválida");
            }
        }
    }

    private void buscarEpisodiosData() {
        buscarSeriePorTitulo();
        if (serieBusca.isPresent()) {
            Serie serie = serieBusca.get();
            System.out.println("Digite o ano de lançamento");
            var anoLancamento = scanner.nextInt();
            scanner.nextLine();
            List<Episodio> episodiosAnos = repositorio.episodiosPorSerieEAno(serie, anoLancamento);
            episodiosAnos.forEach(System.out::println);
        }
    }

    private void topEpisodiosPorSerie() {
        buscarSeriePorTitulo();
        if (serieBusca.isPresent()){
            Serie serie = serieBusca.get();
            List<Episodio> topEpisodios = repositorio.topEpisodiosPorSerie(serie);
            topEpisodios.forEach(e->
                    System.out.printf("Série: %s Temporada: %s - Episódio %s - %s Avaliação %s\n",
                            e.getSerie().getTitulo(), e.getTemporada(),
                            e.getNumeroEpisodio(), e.getTitulo(), e.getAvaliacao()));
        }
    }

    private void buscarEpisodioPorTrecho() {
        System.out.println("Digite um trecho do episódio: ");
        var trechoEpisodio = scanner.nextLine();
        List<Episodio> episodiosEncontrados = repositorio.episodiosPorTrecho(trechoEpisodio);
        episodiosEncontrados.forEach(e->
                System.out.printf("Série: %s Temporada: %s - Episódio %s - %s\n",
                        e.getSerie().getTitulo(), e.getTemporada(),
                        e.getNumeroEpisodio(), e.getTitulo()));
    }

    private void buscarSeriePorTotalTemporada() {
        System.out.println("Informe o máximo de temporadas");
        var maxTemporadas = scanner.nextInt();
        scanner.nextLine();
        System.out.println("Informe a avaliação mínima das séries: ");
        var minAvaliacao = scanner.nextDouble();
        List<Serie> seriesTotalTemporadas = repositorio.seriesPorTemporadaEAvaliacao(maxTemporadas, minAvaliacao);
        System.out.println("Resultados para series buscadas: ");
        seriesTotalTemporadas.forEach(System.out::println);
    }

    private void buscarSeriePorCategoria() {
        System.out.println("Digite a categoria para pesquisa: ");
        var nomeCategoria = scanner.nextLine();
        Categoria categoria = Categoria.fromPortugues(nomeCategoria);
        List<Serie> seriesPorCategoria = repositorio.findByGenero(categoria);
        System.out.println("Resultados para categoria: "+nomeCategoria);
        seriesPorCategoria.forEach(System.out::println);
    }

    private void buscarTop5Series() {

        List<Serie> topSerie = repositorio.findTop5ByOrderByAvaliacaoDesc();
        topSerie.forEach(s->
                System.out.println(s.getTitulo() + " avaliação: " +s.getAvaliacao()));
    }

    private void buscarSeriesPorAvaliacao() {
        System.out.println("Informe a avaliação: ");
        var serieAvaliacao = scanner.nextDouble();
        List<Serie> seriesEncontradas = repositorio.findByAvaliacaoGreaterThanEqualOrderByAvaliacao(serieAvaliacao);
        System.out.println("Séries encontradas para avaliação a partir de: " +serieAvaliacao);
        seriesEncontradas.forEach(s->
                System.out.println(s.getTitulo() + " avaliação: " +s.getAvaliacao()));

    }

    private void buscarSeriePorAtor() {
        System.out.println("Qual o noome para busca:");
        var nomeAtor = scanner.nextLine();
        List<Serie> seriesEncontradas = repositorio.findByAtoresContainingIgnoreCase(nomeAtor);
        System.out.println("Séries encontradas para  " +nomeAtor);
        seriesEncontradas.forEach(s->
                System.out.println(s.getTitulo() + " avaliação: " +s.getAvaliacao()));

    }

    private void buscarSeriePorTitulo() {
        System.out.println("Escolha uma série pelo nome: ");
        var nomeSerie = scanner.nextLine();
        serieBusca = repositorio.findByTituloContainingIgnoreCase(nomeSerie);

        if (serieBusca.isPresent()) {
            System.out.println("Dados da serie: " +serieBusca.get());
        }else {
            System.out.println("Série não encontrada");
        }
    }

    private void buscarSerieWeb() {
        DadosSerie dados = getDadosSerie();
        Serie serie = new Serie(dados);
//        dadosSeries.add(dados);
        repositorio.save(serie);
        System.out.println(dados);
    }

    private DadosSerie getDadosSerie() {
        System.out.println("Digite o nome da série para busca");
        var nomeSerie = scanner.nextLine();
        var json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
        return dados;
    }

    private void buscarEpisodioPorSerie(){
        listarSeriesBuscadas();
        System.out.println("Escolha uma série pelo nome: ");
        var nomeSerie = scanner.nextLine();

        Optional<Serie> serie = repositorio.findByTituloContainingIgnoreCase(nomeSerie);

        if (serie.isPresent()){
            var serieEncontrada = serie.get();
            List<DadosTemporada> temporadas = new ArrayList<>();

            for (int i = 1; i <= serieEncontrada.getTotalTemporadas(); i++) {
                var json = consumo.obterDados(ENDERECO + serieEncontrada.getTitulo().replace(" ", "+") + "&season=" + i + API_KEY);
                DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
                temporadas.add(dadosTemporada);
            }
            temporadas.forEach(System.out::println);

            List<Episodio> episodios = temporadas.stream()
                    .flatMap(d -> d.episodios().stream()
                            .map(e -> new Episodio(d.numero(), e)))
                    .collect(Collectors.toList());

            serieEncontrada.setEpisodios(episodios);
            repositorio.save(serieEncontrada);

        } else {
            System.out.println("Serie não encontrada");
        }


    }

    private void listarSeriesBuscadas () {
        series = repositorio.findAll();

        series.stream()
                .sorted(Comparator.comparing(Serie::getGenero))
                .forEach(System.out::println);
    }

}
