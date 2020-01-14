import static java.lang.Thread.sleep;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

public class Coordinator {

    public static void main(String[] args) throws Exception {
        //InetAddress IPAddress = InetAddress.getByName("42.42.42.42");
        DatagramSocket serverSocket = new DatagramSocket(9876);
        byte[] recBuf = new byte[1024];
        byte[] sendBuf = new byte[1024];
        
        int i = 0;
        int exit = 0;
        ArrayList<String> links = new ArrayList();

        // recebendo a lista de URLs do cliente
        System.out.println("Recebendo lista L...");
        while (exit==0) {
            DatagramPacket recPacket = new DatagramPacket(recBuf, recBuf.length);
            serverSocket.receive(recPacket);
            InetAddress IPAddress = recPacket.getAddress();
            int port = recPacket.getPort();
            System.out.println("Porta do cliente: " + port);

            
            String informacao = new String(recPacket.getData(),
                                           recPacket.getOffset(),
                                           recPacket.getLength());
            
            int equal = 0;
            
            if (informacao.equals("exit"))
                exit = 1;
            
            for(i=0; (i<links.size()); i++){
                if(informacao.equals(links.get(i))){
                    equal = 1;
                }
            }
            
            if(equal == 0 && exit == 0)
                links.add(informacao);
            equal = 0;
            //System.out.println(informacao);

            sendBuf = "Mensagem recebida.".getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendBuf, sendBuf.length, IPAddress, port);
            serverSocket.send(sendPacket);
        }
        serverSocket.close();
        
        System.out.println("Lista L com " + links.size() + " URLs recebidas.");
        
        for(i=0; i<links.size();i++){
            System.out.println(links.get(i));
            //sleep(500);
        }
        
        // Cria outro socket pra mandar pros maps
        InetAddress IPmap1 = InetAddress.getByName("42.42.42.42");
        InetAddress IPmap2 = InetAddress.getByName("42.42.42.42");
        InetAddress IPmap3 = InetAddress.getByName("42.42.42.42");
        int portMap1 = 9880;
        int portMap2 = 9881;
        int portMap3 = 9882;
        DatagramSocket clientSocket = new DatagramSocket();
        
        byte[] sendData = new byte[1024];
        
        System.out.println("Enviando lista para os mappers...");
        for(i = 0; i < links.size(); i++){
        
            sendData = links.get(i).getBytes();
            DatagramPacket sendPacket;

            if(i%3 == 0)
                sendPacket = new DatagramPacket(sendData, sendData.length, IPmap1, portMap1);
            else if(i%3 == 1)
                sendPacket = new DatagramPacket(sendData, sendData.length, IPmap2, portMap2);
            else
                sendPacket = new DatagramPacket(sendData, sendData.length, IPmap3, portMap3);
                
            clientSocket.send(sendPacket);

            byte[] recBuffer = new byte[1024];
            DatagramPacket recPacket = new DatagramPacket(recBuffer, recBuffer.length);
            clientSocket.receive(recPacket);

            String informacao = new String(recPacket.getData());
            System.out.println(informacao);
            sleep(100);
        }
        sendData = "exit".getBytes();
        DatagramPacket sendPacket1 = new DatagramPacket(sendData, sendData.length, IPmap1, portMap1);
        clientSocket.send(sendPacket1);
        sendData = "exit".getBytes();
        DatagramPacket sendPacket2 = new DatagramPacket(sendData, sendData.length, IPmap2, portMap2);
        clientSocket.send(sendPacket2);
        sendData = "exit".getBytes();
        DatagramPacket sendPacket3 = new DatagramPacket(sendData, sendData.length, IPmap3, portMap3);
        clientSocket.send(sendPacket3);
        clientSocket.close();
    }
}
