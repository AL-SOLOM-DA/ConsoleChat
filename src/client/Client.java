package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;

import main.Const;

import static main.Const.ANSI_RED;
import static main.Const.ANSI_RESET;
import static main.Const.EXIT_CODE;

/**
 * Обеспечивает работу программы в режиме клиента
 *
 * @author Влад
 */
public class Client {
	private BufferedReader in;
	private PrintWriter out;
	private Socket socket;

	/**
	 * Запрашивает у пользователя ник и организовывает обмен сообщениями с
	 * сервером
	 */
	public Client() {
		Scanner scan = new Scanner(System.in);
/*
		System.out.println("Введите IP для подключения к серверу.");
		System.out.println("Формат: xxx.xxx.xxx.xxx");
		String ip = scan.nextLine();
*/
        String ip = "127.0.0.1";

        try {
			// Подключаемся в серверу и получаем потоки(in и out) для передачи сообщений
			socket = new Socket(ip, Const.PORT);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);

			System.out.print("Введите свой ник:");
			out.println(scan.nextLine());

			// Запускаем вывод всех входящих сообщений в консоль
			Resender resend = new Resender();
			resend.start();

			// Пока пользователь не введёт "exit" отправляем на сервер всё, что
			// введено из консоли
			String str = "";
			while (!str.equals(EXIT_CODE)) {
				str = scan.nextLine();
				out.println(str);
			}
			resend.setStop();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close();
		}
	}

	/**
	 * Закрывает входной и выходной потоки и сокет
	 */
	private void close() {
		try {
			in.close();
			out.close();
			socket.close();
		} catch (Exception e) {
			System.err.println("Потоки не были закрыты!");
		}
	}

	/**
	 * Класс в отдельной нити пересылает все сообщения от сервера в консоль.
	 * Работает пока не будет вызван метод setStop().
	 *
	 * @author Влад
	 */
	private class Resender extends Thread {

		private boolean stoped;

		/**
		 * Прекращает пересылку сообщений
		 */
		public void setStop() {
			stoped = true;
		}

		/**
		 * Считывает все сообщения от сервера и печатает их в консоль.
		 * Останавливается вызовом метода setStop()
		 *
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {
            try {
                String str = "";
                while (!stoped) {
				    try {
                        str = in.readLine();
                    } catch (SocketException e){
                        System.out.println(ANSI_RED + "Произошло отключение от сервера!" + ANSI_RESET);
                    }
					System.out.println(str);
				}
			} catch (IOException e) {
				System.err.println("Ошибка при получении сообщения.");
				e.printStackTrace();
			}
		}
	}

}