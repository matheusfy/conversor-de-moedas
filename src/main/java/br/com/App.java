package br.com;

import br.com.application.Conversor;

import java.util.Scanner;

public class App {
    public static void main(String[] args) {


        Conversor conversorMoedas = new Conversor();

        conversorMoedas.init();
        userChoice(conversorMoedas);

    }

    public static void userChoice(Conversor conversorMoedas){
        
        Scanner scanner = new Scanner(System.in);
        var entrada = 1;

        String consoleText = """
                1 - Listar moedas aceitas para conversão.
                2 - Realize uma conversão. 
                3 - Mostre o histórico de conversões.
                4 - Digite 4 ou um numero negativo para encerrar.
                """;

        while(entrada > 0)
        {
            System.out.println(consoleText);
            entrada = scanner.nextInt();
            switch (entrada){
                case 1:
                    conversorMoedas.showCurrencyList();
                    break;
                case 2:
                    scanner.nextLine();

                    System.out.println("Escolha a moeda 1: ");
                    String currency1 = scanner.nextLine();


                    System.out.println("Escolha a moeda 2: ");
                    String currency2 = scanner.nextLine();

                    System.out.println("Insira valor para conversão: ");
                    Double value = scanner.nextDouble();

                    conversorMoedas.convertCurrency(currency1, currency2, value);
                    break;
                case 3:
                    System.out.println("\n" + conversorMoedas.getConversionHistory());
                    break;
                case 4:
                    entrada = -1;
                    scanner.close();
                    break;
                default:
                    System.out.println("Entrada invalida.");
                    break;
            }
        }
    }
}