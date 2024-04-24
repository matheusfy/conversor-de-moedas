package br.com;

import br.com.application.Conversor;
import br.com.connection.Connectiondb;

import java.sql.SQLException;
import java.util.Scanner;

public class App {
    public static void main(String[] args) {


//        testando conexão com o banco

        Conversor conversorMoedas = new Conversor();
        conversorMoedas.init();
        userChoice(conversorMoedas);

    }

    public static Double getInput(Scanner scanner){
        scanner.nextLine();
        System.out.println("Insira valor para conversão: ");
        Double value = scanner.nextDouble();
        return value;
    }

    public static void userChoice(Conversor conversorMoedas){
        
        Scanner scanner = new Scanner(System.in);
        var entrada = 1;

        String consoleText = """
                *****************************************************************************************************
                
                1 - Listar moedas aceitas para conversão.
                2 - USD ===> BRL
                3 - BRL ===> USD
                4 - BRL ===> KRW
                5 - KRW ===> BRL
                6 - USD ===> KRW 
                7 -  KRW ===> USD
                8 - Realize conversão outras moedas. 
                9 - Mostre o histórico de conversões.
                10 - Digite 10 ou um numero negativo para encerrar.
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
                    conversorMoedas.convertCurrency("USD", "BRL", getInput(scanner));
                    break;
                case 3:
                    conversorMoedas.convertCurrency("BRL", "USD", getInput(scanner));
                    break;
                case 4:
                    conversorMoedas.convertCurrency("BRL", "KRW", getInput(scanner));
                    break;
                case 5:
                    conversorMoedas.convertCurrency("KRW", "BRL", getInput(scanner));
                    break;
                case 6:
                    conversorMoedas.convertCurrency("USD", "KRW", getInput(scanner));
                    break;
                case 7:
                    conversorMoedas.convertCurrency("KRW", "USD", getInput(scanner));
                    break;

                case 8:
                    scanner.nextLine();

                    System.out.println("Escolha a moeda 1: ");
                    String currency1 = scanner.nextLine();


                    System.out.println("Escolha a moeda 2: ");
                    String currency2 = scanner.nextLine();

                    System.out.println("Insira valor para conversão: ");
                    Double value = scanner.nextDouble();

                    conversorMoedas.convertCurrency(currency1, currency2, value);
                    break;
                case 9:
                    System.out.println("\n" + conversorMoedas.getConversionHistory());
                    break;
                case 10:
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