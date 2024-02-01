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
	
	
	//Inicio das nossas altera�oes
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
	
	private void startTP2 (PubSubClient user, String userName, int brokerPort, String brokerAdd){
		//String[] resources = {"var X", "var Y", "var Z"};		
		//String[] resources = {"var X"};
		
		int NumClients=1;
		int clockInit;
		int last=0;
		int first=0;
		int MaxLeft=0;
		
		String[] lastLog= {""};
		String prev="NULL";
		
		Random seed = new Random();
		
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

		//Aguarda todos os usuarios terem enviado seus clocks
		do {
			List<Message> logUserClock = user.getLogMessages();
			
			//treatLog(logUserClock);
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			clockCount = logCount(logUserClock);
		}while( clockCount < NumClients );
		
		//Achar posicao

		
		List<Message> logOrd = user.getLogMessages();
		treatLog(logOrd);
		
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
			//fazendo um pub no broker
			
			xAcquire =0;
			yAcquire =0;
			zAcquire =0;
			
			//Espera um pouco para ver se tem clientes para entrar no broker

			
			//Conferir se e minha hora de fazer o Acquire
			do{
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				List<Message> logProx = user.getLogMessages();
				treatLog(logProx);
				
				Message Verifi = null;
				Iterator<Message> itVer = logProx.iterator();
				while(itVer.hasNext()){
					Verifi=itVer.next();
				}
				lastLog=split_string(Verifi.getContent());
			}while(!((lastLog[1].equals(">>NextAcquire>>") && lastLog[0].equals(prev)) ||
					(lastLog[1].equals(">>StartAcquires>>") && "NULL"==prev)));
			//Adiciona a possibilidade do Backup
			//E faz a contagem do Clock 
			
			//calculando as chances de se querer ou nao uma variavel
			if(50<(seed.nextInt(100))){//Pedir VarX
				xAcquire = 1;
				Thread sendOneMsg = new ThreadWrapper(user, userName+":"+"Acquire"+":"+"X", brokerAdd, brokerPort);
				sendOneMsg.start();
				try{
					sendOneMsg.join();			
				}catch (Exception e){
					e.printStackTrace();
				}
			}
			if(50<(seed.nextInt(100))) {//Pedir VarY
				yAcquire = 1;
				Thread sendOneMsg = new ThreadWrapper(user, userName+":"+"Acquire"+":"+"Y", brokerAdd, brokerPort);
				sendOneMsg.start();
				try{
					sendOneMsg.join();			
				}catch (Exception e){
					e.printStackTrace();
				}
			}
			if(50<(seed.nextInt(100))) {//Pedir VarZ
				zAcquire = 1;
				Thread sendOneMsg = new ThreadWrapper(user, userName+":"+"Acquire"+":"+"Z", brokerAdd, brokerPort);
				sendOneMsg.start();
				try{
					sendOneMsg.join();			
				}catch (Exception e){
					e.printStackTrace();
				}
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
			
			treatLog(logUser);
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			//Avisa que terminou os Acquires
			if(last==NumClients) {//Se for o ultimo inicia os releases
				Thread sendOneMsg = new ThreadWrapper(user, userName+":"+">>StartReleases>>"+":"+"Void", brokerAdd, brokerPort);
				sendOneMsg.start();
				try{
					sendOneMsg.join();			
				}catch (Exception e){
					e.printStackTrace();
				}
			}
			else {//Caso contrario passa para os proximos Aquires
				Thread sendOneMsg = new ThreadWrapper(user, userName+":"+">>NextAcquire>>"+":"+"Void", brokerAdd, brokerPort);
				sendOneMsg.start();
				try{
					sendOneMsg.join();			
				}catch (Exception e){
					e.printStackTrace();
				}
			}
			
			//Aguarda o momento de fazer release

			do{
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				List<Message> logProx = user.getLogMessages();
				treatLog(logProx);
				
				Message Verifi = null;
				Iterator<Message> itVer = logProx.iterator();

				while(itVer.hasNext()){
					Verifi=itVer.next();
				}
				
				lastLog=split_string(Verifi.getContent());

			}while(!((lastLog[1].equals(">>StartReleases>>") && "NULL".equals(prev))||
					(lastLog[1].equals(">>NextRelease>>") && lastLog[0].equals(prev))));
			

			
			
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

		
		}
		
						
		user.unsubscribe(brokerAdd, brokerPort);
		
		user.stopPubSubClient();
		
	}
	
	private void treatLog(List<Message> logUser){
		//aqui existe toda a l�gica do protocolo do TP2
		//se permanece neste m�todo at� que o acesso a VAR X ou VAR Y ou VAR Z ocorra
		Iterator<Message> it = logUser.iterator();
		System.out.print("Log User itens: ");
		while(it.hasNext()){
			Message aux = it.next();
			System.out.print(aux.getContent() + aux.getLogId() + " | ");
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
