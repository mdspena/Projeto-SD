import static java.lang.Thread.sleep;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Client {

    public static void main(String[] args) throws Exception {
        int i;
        ArrayList<String> links = new ArrayList();
        String arquivo = "links.txt";
     
        try {
          FileReader arq = new FileReader(arquivo);
          BufferedReader lerArq = new BufferedReader(arq);
     
          String linha = lerArq.readLine();
          
          while (linha != null) {
            links.add(linha);
            linha = lerArq.readLine();
          }
          arq.close();
        } catch (IOException e) {
            System.err.printf("Erro na abertura do arquivo: %s.\n", e.getMessage());
        }
        
        InetAddress IPAddress = InetAddress.getByName("172.31.95.167");
        DatagramSocket clientSocket = new DatagramSocket(9874);
        
        byte[] sendData = new byte[1024];
        
        System.out.println("Enviando lista L com " + links.size() + " URLs para o coordenador");
        for(i = 0; i < links.size(); i++){
        
            sendData = links.get(i).getBytes();

            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9876);
            clientSocket.send(sendPacket);

            byte[] recBuffer = new byte[1024];
            DatagramPacket recPacket = new DatagramPacket(recBuffer, recBuffer.length);
            clientSocket.receive(recPacket);

            String informacao = new String(recPacket.getData());
            System.out.println(informacao);
            sleep(100);
        }
        sendData = "exit".getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9876);
        clientSocket.send(sendPacket);
        clientSocket.close();
        
        System.out.println("Aguardando resposta da requisicao...");
        
        DatagramSocket serverSocket = new DatagramSocket(9874);
        byte[] recBuf = new byte[1024];
        byte[] sendBuf = new byte[1024];
        
        int exit = 0;
        ArrayList<String> indiceInvertido = new ArrayList();
        
        while (exit==0) {
            DatagramPacket recPacket = new DatagramPacket(recBuf, recBuf.length);
            serverSocket.receive(recPacket);
            IPAddress = recPacket.getAddress();
            int port = recPacket.getPort();

            
            String informacao = new String(recPacket.getData(),
                                           recPacket.getOffset(),
                                           recPacket.getLength());
            
            int equal = 0;
            
            if (informacao.equals("exit"))
                exit = 1;
            
            if(exit == 0)
                indiceInvertido.add(informacao);
            //System.out.println(informacao);

            sendBuf = "Mensagem recebida.".getBytes();
            DatagramPacket returnPacket = new DatagramPacket(sendBuf, sendBuf.length, IPAddress, port);
            serverSocket.send(returnPacket);
        }
        serverSocket.close();
        
        System.out.println("Indice invertido recebido, armazenando na pasta atual...");
        
        File arq = new File("indiceInvertido.txt");
        FileWriter writer = new FileWriter(arq);
        for (i=0; i<indiceInvertido.size();i++) {
            writer.write(indiceInvertido.get(i) + "\n");
        }
        writer.close();
        
        System.out.println("Finalizando.");
    }
}