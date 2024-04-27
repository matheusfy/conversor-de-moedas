package br.com;

import br.com.domain.exception.InvalidConversionValue;
import br.com.domain.exception.InvalidCurrencyException;
import br.com.usecase.ConversorUseCase;

import java.util.Scanner;

public class App {
    public static void main(String[] args) {

        ConversorUseCase conversor = new ConversorUseCase();
        conversor.init();
        userChoice(conversor);

    }


    public static void userChoice(ConversorUseCase conversor){
        
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
            try{
                switch (entrada){
                    case 1:
                        conversor.showCurrencyList();
                        break;
                    case 2:
                            conversor.convertCurrency("USD", "BRL", getInput(scanner));
                        break;
                    case 3:
                        conversor.convertCurrency("BRL", "USD", getInput(scanner));
                        break;
                    case 4:
                        conversor.convertCurrency("BRL", "KRW", getInput(scanner));
                        break;
                    case 5:
                        conversor.convertCurrency("KRW", "BRL", getInput(scanner));
                        break;
                    case 6:
                        conversor.convertCurrency("USD", "KRW", getInput(scanner));
                        break;
                    case 7:
                        conversor.convertCurrency("KRW", "USD", getInput(scanner));
                        break;

                    case 8:
                        scanner.nextLine();

                        System.out.println("Escolha a moeda 1: ");
                        String currency1 = scanner.nextLine();


                        System.out.println("Escolha a moeda 2: ");
                        String currency2 = scanner.nextLine();

                        System.out.println("Insira valor para conversão: ");
                        Double value = scanner.nextDouble();

                        conversor.convertCurrency(currency1, currency2, value);
                        break;
                    case 9:
                        System.out.println("\n" + conversor.getConversionHistory());
                        break;
                    case 10:
                        entrada = -1;
                        scanner.close();
                        break;
                    default:
                        System.out.println("Entrada invalida.");
                        break;
                }
            } catch (InvalidConversionValue | InvalidCurrencyException error) {
                System.out.println(error.getMessage());
            }
        }
    }

    public static Double getInput(Scanner scanner){
        scanner.nextLine();
        System.out.println("Insira valor para conversão: ");
        Double value = scanner.nextDouble();
        return value;
    }
}