import static java.lang.Thread.sleep;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.net.*;
import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Map2 {

    public static void main(String[] args) throws Exception {
         //InetAddress IPAddress = InetAddress.getByName("42.42.42.42");
        DatagramSocket serverSocket = new DatagramSocket(9881);
        byte[] recBuf = new byte[1024];
        byte[] sendBuf = new byte[1024];
        
        int i = 0;
        int exit = 0;
        ArrayList<String> links = new ArrayList();

        System.out.println("Recebendo links do coordenador...");
        while (exit==0) {
            DatagramPacket recPacket = new DatagramPacket(recBuf, recBuf.length);
            serverSocket.receive(recPacket);
            InetAddress IPAddress = recPacket.getAddress();
            int port = recPacket.getPort();

            
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
        System.out.println("Links recebidos.");


        ArrayList<String> pageLines = new ArrayList();
        
        // Armazena cada linha dos sites em uma lista
        System.out.println("Iniciando armazenamento de páginas...");
        for(i=0; i<links.size();i++){
            URL link = new URL(links.get(i));
            BufferedReader in = new BufferedReader(
            new InputStreamReader(link.openStream()));
    
            String inputLine;
            while ((inputLine = in.readLine()) != null){
                //System.out.println(inputLine);
                pageLines.add(inputLine);
            }
            in.close();
            
            System.out.println("Pagina " + links.get(i) + " armazenada.");
            //System.out.println(links.get(i));
            //sleep(500);
        }
        
        ArrayList<String> references = new ArrayList();
        
        // Encontra os links nas linhas dos sites usando Regex
        String mydata;
        System.out.println("Extraindo links...");
        for(i=0; i<pageLines.size();i++){
            mydata = pageLines.get(i);
            Pattern pattern = Pattern.compile("(\"((http|https|ftp)://(.*?))(\"))");
            Matcher matcher = pattern.matcher(mydata);
            if (matcher.find())
            {
                references.add(matcher.group(2));
                //System.out.println(matcher.group(2) + "\n");
            }
        }
        System.out.println(references.size() + " links extraídos.");
        // Imprime as referencias encontradas nos sites
        /*for(i=0; i<references.size();i++){
            System.out.println(references.get(i));
        }*/
        
        InetAddress IPAddress = InetAddress.getByName("172.31.95.167");
        DatagramSocket clientSocket = new DatagramSocket();
        
        byte[] sendData = new byte[1024];
        
        System.out.println("Enviando lista de referencias para o Reduce...");
        for(i = 0; i < references.size(); i++){
        
            sendData = references.get(i).getBytes();

            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9884);
            clientSocket.send(sendPacket);

            byte[] recBuffer = new byte[1024];
            DatagramPacket recPacket = new DatagramPacket(recBuffer, recBuffer.length);
            clientSocket.receive(recPacket);

            String informacao = new String(recPacket.getData());
            System.out.println(informacao);
            //sleep(100);
        }
        sendData = "exit".getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9884);
        clientSocket.send(sendPacket);
        clientSocket.close();
    }
}
