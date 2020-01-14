import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.*;

public class Reduce {

    public static void main(String[] args) throws Exception {
        //InetAddress IPAddress = InetAddress.getByName("42.42.42.42");
        DatagramSocket serverSocket = new DatagramSocket(9884);
        byte[] recBuf = new byte[1024];
        byte[] sendBuf = new byte[1024];
        
        int i = 0;
        int exit = 0;
        ArrayList<String> references = new ArrayList();

        System.out.println("Recebendo referencias...");
        while (exit!=3) {
            DatagramPacket recPacket = new DatagramPacket(recBuf, recBuf.length);
            serverSocket.receive(recPacket);
            InetAddress IPAddress = recPacket.getAddress();
            int port = recPacket.getPort();

            
            String informacao = new String(recPacket.getData(),
                                           recPacket.getOffset(),
                                           recPacket.getLength());
            
            int equal = 0;
            
            if (informacao.equals("exit"))
                exit += 1;
            
            if(exit != 3 && !(informacao.equals("exit")))
                references.add(informacao);

            //System.out.println(informacao);

            sendBuf = "Mensagem recebida.".getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendBuf, sendBuf.length, IPAddress, port);
            serverSocket.send(sendPacket);
        }
        serverSocket.close();
        
        System.out.println(references.size() + " referencias recebidas.");
        
        // conta quantas vezes cada URL referenciada apareceu
        HashMap<String, Integer> indice = new HashMap<>();
        for (i = 0; i < references.size(); i++) {
            String item = references.get(i);
            if (indice.containsKey(item))
                indice.put(item, indice.get(item) + 1);
            else
                indice.put(item, 1);
        }
        
        // mostra isso de um jeito bonitinho
        ArrayList<String> indiceInvertido = new ArrayList();
        for (Map.Entry <String, Integer> element: indice.entrySet()) {
            if(element.getValue()<10)
                indiceInvertido.add("0" + element.getValue() + " - " + element.getKey());
            else
                indiceInvertido.add(element.getValue() + " - " + element.getKey());
        }

        // ordena do mais referenciado ao menos
        Collections.sort(indiceInvertido, Collections.reverseOrder());

        for(i=0; i<indiceInvertido.size();i++){
            System.out.println(indiceInvertido.get(i));
        }
        
        System.out.println("Enviando indice invertido ao cliente...");
        InetAddress IPAddress = InetAddress.getByName("172.31.95.167");
        DatagramSocket clientSocket = new DatagramSocket();
        
        byte[] sendData = new byte[1024];
        
        System.out.println("Enviando lista de referencias para o Reduce...");
        for(i = 0; i < indiceInvertido.size(); i++){
        
            sendData = indiceInvertido.get(i).getBytes();

            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9874);
            clientSocket.send(sendPacket);

            byte[] recBuffer = new byte[1024];
            DatagramPacket recPacket = new DatagramPacket(recBuffer, recBuffer.length);
            clientSocket.receive(recPacket);

            String informacao = new String(recPacket.getData());
            System.out.println(informacao);
            //sleep(100);
        }
        sendData = "exit".getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9874);
        clientSocket.send(sendPacket);
        clientSocket.close();
    }
}
