package appl;

import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import core.Message;

public class SingleUser {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new SingleUser();
	}
	
	public SingleUser(){
		Scanner reader = new Scanner(System.in);  // Reading from System.in
		System.out.print("Enter the Broker port number:\n ");
		int brokerPort = reader.nextInt();
		//int brokerPort = 8080;
		
		System.out.print("Enter the Broker address: \n");
		//String brokerAdd = reader.next();
		String brokerAdd ="localhost";
		System.out.print("Enter the User name: \n");
		String userName = reader.next();
		//String userName = "Marcos";
		
		System.out.print("Enter the User port number: \n");
		int userPort = reader.nextInt();
		//int userPort = 8081;
				
		System.out.print("Enter the User address: \n");
		//String userAdd = reader.next();
		String userAdd = "localhost";
				
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		PubSubClient user = new PubSubClient(userAdd, userPort);
		
		user.subscribe(brokerAdd, brokerPort);
		
		startTP2(user, userName, brokerPort, brokerAdd);
	}
	
	
	//Inicio das nossas alteraï¿½oes
	public int logCount(List<Message> log) {
		Iterator<Message> it = log.iterator();
		int i=0;
		Message aux;
		while(it.hasNext()){
			aux=it.next();
			i=i+1;
		}
		return i;
	}
	
	public String[] split_string(String userName){
		String[] splitted = userName.split(":");
		return splitted;
	}
	public void sendClock(PubSubClient user,String userName,int clockInit,int NumClients,int brokerPort, String brokerAdd){
		Random seed = new Random();
		System.out.println("\nNÃºmero de Clientes: " + NumClients);
		System.out.println("\nEnviando numero aleatorio...");

		//Envia um numero aleatorio simulando um relogio
		Thread sendClock = new ThreadWrapper(user, userName+":"+"Clock"+":"+clockInit, brokerAdd, brokerPort);
		sendClock.start();
		try{
			sendClock.join();			
		}catch (Exception e){
			e.printStackTrace();
		}

		
		int clockCount;
		System.out.println("Aguardando todos os usuarios enviarem seus clocks...");

		//Aguarda todos os usuarios terem enviado seus clocks
		do {
			List<Message> logUserClock = user.getLogMessages();
			
			Iterator<Message> it = logUserClock.iterator();
			String[] Mensage;
			//treatLog(logUserClock);
			while(it.hasNext()){
				Message aux = it.next();
				Mensage = split_string(aux.getContent());
				System.out.print(Mensage[0]+":" + Mensage[2] + "\n");
			}
			System.out.println();
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			clockCount = logCount(logUserClock);
		}while( clockCount < NumClients );
	}
	
	private void startTP2 (PubSubClient user, String userName, int brokerPort, String brokerAdd){
		//String[] resources = {"var X", "var Y", "var Z"};		
		//String[] resources = {"var X"};
		Scanner reader = new Scanner(System.in);
		int NumClients=1;
		int clockInit;
		int last=0;
		int first=0;
		int MaxLeft=0;
		int brokerOff=0;
		
		String[] lastLog= {""};
		String prev="NULL";
		Random seed = new Random();
		clockInit = seed.nextInt(100);
		/*Random seed = new Random();
		System.out.println("\nNÃºmero de Clientes: " + NumClients);
		System.out.println("\nEnviando numero aleatorio...");

		//Envia um numero aleatorio simulando um relogio
		clockInit = seed.nextInt(100);
		Thread sendClock = new ThreadWrapper(user, userName+":"+"Clock"+":"+clockInit, brokerAdd, brokerPort);
		sendClock.start();
		try{
			sendClock.join();			
		}catch (Exception e){
			e.printStackTrace();
		}

		
		int clockCount;
		System.out.println("Aguardando todos os usuarios enviarem seus clocks...");

		//Aguarda todos os usuarios terem enviado seus clocks
		do {
			List<Message> logUserClock = user.getLogMessages();
			
			Iterator<Message> it = logUserClock.iterator();
			String[] Mensage;
			//treatLog(logUserClock);
			while(it.hasNext()){
				Message aux = it.next();
				Mensage = split_string(aux.getContent());
				System.out.print(Mensage[0]+":" + Mensage[2] + "\n");
			}
			System.out.println();
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			clockCount = logCount(logUserClock);
		}while( clockCount < NumClients );
		System.out.println("Clocks enviados\n ");
		*/
		sendClock(user,userName,clockInit,NumClients,brokerPort,brokerAdd);
		//Achar posicao

		
		List<Message> logOrd = user.getLogMessages();
		//treatLog(logOrd);
		
		String[] Mensage;
		Message aux=null;
		Iterator<Message> it = logOrd.iterator();
		

		//Aqui e onde e organizada as prioridades dos usuarios
		while(it.hasNext()){
			aux=it.next();
			Mensage = split_string(aux.getContent());
			if(!Mensage[1].equals("Clock")) {
				break;
			}
			if(Integer.parseInt(Mensage[2]) < clockInit && Integer.parseInt(Mensage[2])> MaxLeft) {
				prev=Mensage[0];
				MaxLeft=Integer.parseInt(Mensage[2]);
			}

			if(Integer.parseInt(Mensage[2]) <= clockInit)
				last++;
			if(Integer.parseInt(Mensage[2]) >= clockInit)
				first++;
		}
		
		//Inicio dos procedimentos de chamada

		
		int xAcquire =0;
		int yAcquire =0;
		int zAcquire =0;
		
		if(first == NumClients){
			Thread sendOneMsg = new ThreadWrapper(user, userName+":"+">>StartAcquires>>"+":"+"Null", brokerAdd, brokerPort);
			sendOneMsg.start();
			try{
				sendOneMsg.join();			
			}catch (Exception e){
				e.printStackTrace();
			}
		}
		
		
		for(int i =0; i<100; i++){
			
			System.out.println("O broker caiu?");
			brokerOff=reader.nextInt();
			
			if(brokerOff==1) {
				user.unsubscribe(brokerAdd, brokerPort);
				brokerPort=8081;
				user.subscribe(brokerAdd, brokerPort);
				//espera para enviar a mensagem de Backup
				do{
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					List<Message> logProx = user.getLogMessages();					
					Message Verifi = null;
					Iterator<Message> itVer = logProx.iterator();
					while(itVer.hasNext()){
						Verifi=itVer.next();
					}
					lastLog=split_string(Verifi.getContent());
				}while(!(lastLog[1].equals(">>StartAcquires>>")));
				//Envia mensagem de Baclup depois de uma chamada de StartAcquires
				
				Thread sendOneMsg = new ThreadWrapper(user, NumClients+":"+">>NewBackup>>"+":"+"?", brokerAdd, brokerPort);
				sendOneMsg.start();
				try{
					sendOneMsg.join();			
				}catch (Exception e){
					e.printStackTrace();
				}
				//Espera a informação de quantos clientes tem no Broker que não caiu
				do{
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					List<Message> logProx = user.getLogMessages();					
					Message Verifi = null;
					Iterator<Message> itVer = logProx.iterator();
					while(itVer.hasNext()){
						Verifi=itVer.next();
					}
					lastLog=split_string(Verifi.getContent());
				}while(!(lastLog[1].equals(">>NumClients>>")));
				NumClients += Integer.parseInt(lastLog[0]);
			}
			
			
			//fazendo um pub no broker
			System.out.println("Ã�nicio dos Acquires");
			
			xAcquire =0;
			yAcquire =0;
			zAcquire =0;
			
			//O primeiro usuario aguarda para poder conferir se existe uma mensagem de Backup
			if("NULL"==prev){
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			
			//Conferir se e minha hora de fazer o Acquire
			do{
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				List<Message> logProx = user.getLogMessages();
				//treatLog(logProx);
				
				Message Verifi = null;
				Iterator<Message> itVer = logProx.iterator();
				while(itVer.hasNext()){
					Verifi=itVer.next();
				}
				lastLog=split_string(Verifi.getContent());
			}while(!((lastLog[1].equals(">>NextAcquire>>") && lastLog[0].equals(prev)) ||
					(lastLog[1].equals(">>StartAcquires>>") && "NULL"==prev)) || 
					(lastLog[1].equals(">>NewBackup>>")) ||
					brokerOff==1);
			
			
			//Verifica se existiu um chamado de BackUp
			if(lastLog[1].equals(">>NewBackup>>")) {
				if(brokerOff==0 && (lastLog[1].equals(">>NewBackup>>"))) {
					NumClients += Integer.parseInt(lastLog[0]);
				}
				if("NULL"==prev && brokerOff==0) {
					Thread sendOneMsg = new ThreadWrapper(user, NumClients+":"+">>NumClients>>"+":"+"?", brokerAdd, brokerPort);
					sendOneMsg.start();
					try{
						sendOneMsg.join();			
					}catch (Exception e){
						e.printStackTrace();
					}
				}
					
				sendClock(user,userName,clockInit,NumClients,brokerPort,brokerAdd);
				//Organiza as prioridades
				logOrd = user.getLogMessages();
				while(it.hasNext()) {
					aux=it.next();
					Mensage = split_string(aux.getContent());
					if(!Mensage[1].equals(">>NewBackup>>")) {
						break;
					}
				}
				while(it.hasNext()){
					aux=it.next();
					Mensage = split_string(aux.getContent());
					if(!Mensage[1].equals("Clock")) {
						break;
					}
					if(Integer.parseInt(Mensage[2]) < clockInit && Integer.parseInt(Mensage[2])> MaxLeft) {
						prev=Mensage[0];
						MaxLeft=Integer.parseInt(Mensage[2]);
					}

					if(Integer.parseInt(Mensage[2]) <= clockInit)
						last++;
					if(Integer.parseInt(Mensage[2]) >= clockInit)
						first++;
				}
			}
			
			//calculando as chances de se querer ou nao uma variavel
			if(50<(seed.nextInt(100))){//Pedir VarX
				xAcquire = 1;
				Thread sendOneMsg = new ThreadWrapper(user, userName+":"+"Acquire"+":"+"X", brokerAdd, brokerPort);
				sendOneMsg.start();
				try{
					sendOneMsg.join();			
				}catch (Exception e){
					System.out.println("Falhou no X");
					e.printStackTrace();
				}
				List<Message> logUser = user.getLogMessages();
				
				treatLog(logUser);
			}
			if(50<(seed.nextInt(100))) {//Pedir VarY
				yAcquire = 1;
				Thread sendOneMsg = new ThreadWrapper(user, userName+":"+"Acquire"+":"+"Y", brokerAdd, brokerPort);
				sendOneMsg.start();
				try{
					sendOneMsg.join();			
				}catch (Exception e){
					System.out.println("Falhou no Y");
					e.printStackTrace();
				}
				List<Message> logUser = user.getLogMessages();
				
				treatLog(logUser);
			}
			if(50<(seed.nextInt(100))) {//Pedir VarZ
				zAcquire = 1;
				Thread sendOneMsg = new ThreadWrapper(user, userName+":"+"Acquire"+":"+"Z", brokerAdd, brokerPort);
				sendOneMsg.start();
				try{
					sendOneMsg.join();			
				}catch (Exception e){
					System.out.println("Falhou no Z");
					e.printStackTrace();
				}
				List<Message> logUser = user.getLogMessages();
				
				treatLog(logUser);
			}
			
			
			//String oneResource = resources[seed.nextInt(resources.length)];
			//Thread sendOneMsg = new ThreadWrapper(user, userName+":"+"Teste"+i+":"+oneResource, brokerAdd, brokerPort);
			
			
			/*try{
				sendOneMsg.join();			
			}catch (Exception e){
				e.printStackTrace();
			}*/
			//fazendo a obtencao dos notifies do broker
			List<Message> logUser = user.getLogMessages();
			
			//treatLog(logUser);
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//List<Message> logUser;
			
			//Avisa que terminou os Acquires
			if(last==NumClients) {//Se for o ultimo inicia os releases
				Thread sendOneMsg = new ThreadWrapper(user, userName+":"+">>StartReleases>>"+":"+"Void", brokerAdd, brokerPort);
				sendOneMsg.start();
				try{
					sendOneMsg.join();			
				}catch (Exception e){
					e.printStackTrace();
				}
				logUser = user.getLogMessages();
				
				treatLog(logUser);
			}
			else {//Caso contrario passa para os proximos Aquires
				Thread sendOneMsg = new ThreadWrapper(user, userName+":"+">>NextAcquire>>"+":"+"Void", brokerAdd, brokerPort);
				sendOneMsg.start();
				try{
					sendOneMsg.join();			
				}catch (Exception e){
					e.printStackTrace();
				}
				logUser = user.getLogMessages();
				
				treatLog(logUser);
			}
			System.out.println("Fim dos Acquires. \n");

			//Aguarda o momento de fazer release

			do{
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				List<Message> logProx = user.getLogMessages();
				//treatLog(logProx);
				
				Message Verifi = null;
				Iterator<Message> itVer = logProx.iterator();

				while(itVer.hasNext()){
					Verifi=itVer.next();
				}
				
				lastLog=split_string(Verifi.getContent());

			}while(!((lastLog[1].equals(">>StartReleases>>") && "NULL".equals(prev))||
					(lastLog[1].equals(">>NextRelease>>") && lastLog[0].equals(prev))));
			

			
			System.out.println("Ã�nicio dos releases");

			//Fazendo o release das variaveis
			if(xAcquire==1){
				Thread sendOneMsg = new ThreadWrapper(user, userName+":"+"Release"+":"+"X", brokerAdd, brokerPort);
				sendOneMsg.start();
				try{
					sendOneMsg.join();			
				}catch (Exception e){
					e.printStackTrace();
				}
			}
			if(yAcquire==1){
				Thread sendOneMsg = new ThreadWrapper(user, userName+":"+"Release"+":"+"Y", brokerAdd, brokerPort);
				sendOneMsg.start();
				try{
					sendOneMsg.join();			
				}catch (Exception e){
					e.printStackTrace();
				}
			}
			
			if(zAcquire==1){
				Thread sendOneMsg = new ThreadWrapper(user, userName+":"+"Release"+":"+"Z", brokerAdd, brokerPort);
				sendOneMsg.start();
				try{
					sendOneMsg.join();			
				}catch (Exception e){
					e.printStackTrace();
				}
			}
			
			if(last==NumClients) {//Caso este cleinte seja o ultimo
				Thread sendOneMsg = new ThreadWrapper(user, userName+":"+">>StartAcquires>>"+":"+"X", brokerAdd, brokerPort);
				sendOneMsg.start();
				try{
					sendOneMsg.join();			
				}catch (Exception e){
					e.printStackTrace();
				}
			}
			
			else {//Caso contrario 
				Thread sendOneMsg = new ThreadWrapper(user, userName+":"+">>NextRelease>>"+":"+"X", brokerAdd, brokerPort);
				sendOneMsg.start();
				try{
					sendOneMsg.join();			
				}catch (Exception e){
					e.printStackTrace();
				}
			}
			List<Message> logProx1 = user.getLogMessages();
			treatLog(logProx1);
			System.out.println("FIM dos releases\n");

		
		}
		
						
		user.unsubscribe(brokerAdd, brokerPort);
		
		user.stopPubSubClient();
		
	}
	
	private void treatLog(List<Message> logUser){
		//aqui existe toda a lï¿½gica do protocolo do TP2
		//se permanece neste mï¿½todo atï¿½ que o acesso a VAR X ou VAR Y ou VAR Z ocorra
		Iterator<Message> it = logUser.iterator();
		System.out.print("Log User itens: ");
		while(it.hasNext()){
			Message aux = it.next();
			System.out.print(aux.getContent() + aux.getLogId() + " | \n");
		}
		System.out.println();
	}
	
	
	class ThreadWrapper extends Thread{
		PubSubClient c;
		String msg;
		String host;
		int port;
		
		public ThreadWrapper(PubSubClient c, String msg, String host, int port){
			this.c = c;
			this.msg = msg;
			this.host = host;
			this.port = port;
		}
		public void run(){
			c.publish(msg, host, port);
		}
	}

}
