/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.IOException;
import java.io.PrintWriter;
import static java.lang.Integer.parseInt;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author gnajd
 */
@WebServlet(urlPatterns = {"/IPv4"})
public class IPv4 extends HttpServlet {
    
    int hostsNum;
    String startingIP;
    ArrayList<Subnet> subnet = new ArrayList<>();
    
    public class Subnet{
     
        String subnetAddress;
        String defautGateway;
        String broadcastAddress;
        int cidr;
        
        public Subnet(String subnetAddress, String defautGateway, String broadcastAddress, int cidr){
            
            this.subnetAddress = subnetAddress; // first address in network
            this.defautGateway = defautGateway;  // firest usable address after subnet address
            this.broadcastAddress = broadcastAddress; // last address
            this.cidr = cidr; // CIDR
            
        }
    }
    
    public String addHosts(String IPv4, int numberOfHosts){
        
        String[] string_parts = IPv4.split("\\.");
        int part0 = Integer.parseInt(string_parts[0]);
        int part1 = Integer.parseInt(string_parts[1]);
        int part2 = Integer.parseInt(string_parts[2]);
        int part3 = Integer.parseInt(string_parts[3]);
        
        numberOfHosts--;
        part1 += numberOfHosts/65536; // check XXX.part1.XXX.XXX
        numberOfHosts %= 65536;
        if (part1 > 255) {
            //error part1
        }
        
        part2 += numberOfHosts/256;  // check XXX.XXX.part2.XXX
        numberOfHosts %= 256;
        if (part2 > 255) {
            part2 %= 256;
            part1 = part1 + (part2/255);
            if (part1 > 255){
                //error
            }
        }
        
        part3 += numberOfHosts; //  check XXX.XXX.XXX.part3
        if (part3 > 255){
            part2 = part2 + (part3/255);
            part3 %= 256;
            if (part2 > 255) {
                part1 = part1 + (part2/255);
                part2 %= 256;
                if (part1 > 255){
                    //error
                }
            }
        }
        
        String resultIP = part0 + "." + part1 + "." + part2 + "." + part3;
        return resultIP;
    }
    
    public Subnet createSubnet(String startingIP, int cidr){
        int numberOfHosts = (int)Math.pow(2, (32-cidr));
        String defautGateway = addHosts(startingIP, 2);
        String broadcastAddress = addHosts(startingIP, numberOfHosts);
        
        Subnet newSubnet = new Subnet(startingIP, defautGateway, broadcastAddress, cidr);
        return newSubnet;
    }
    
    public ArrayList<Integer> checkAvailableMasks (String IP){
          String[] string_parts = IP.split("\\.");
          int part1 = Integer.parseInt(string_parts[1]);
          int part2 = Integer.parseInt(string_parts[2]);
          int part3 = Integer.parseInt(string_parts[3]);
          ArrayList<Integer> availablePower = new ArrayList<>();
          
          for (int i=2; i<23; i++){ // check only last three parts
              if (i/8 == 0){ // check part3
                  if (part3 % Math.pow(2,i%8) == 0){
                      availablePower.add(i);
                  }
              }
              else if (i/8 == 1 && part3 == 0){ // check part2
                  if (part2 % Math.pow(2,i%8) == 0){
                      availablePower.add(i);
                  }
              }
              else if (i/8 >= 2 && part2 == 0 && part3 == 0){ // check part1
                  if (part1 % Math.pow(2,i%8) == 0){
                      availablePower.add(i);
                  }
              }
          }
//          System.out.println(availablePower);
          return availablePower;
          
    }
    
    public ArrayList<Subnet> calculateFull(String IPv4, int hostsNum){

        int tempHostsNumber, cidr, i;
        Subnet IPv4Subnet;
        ArrayList<Subnet> subnetsList = new ArrayList<>();
        ArrayList<Integer> availableMasks = new ArrayList<>();
        
        while (hostsNum > 0){
            availableMasks = checkAvailableMasks(IPv4);
            for (i = 0; i < availableMasks.size(); i++) {
                tempHostsNumber = (int)Math.pow(2,availableMasks.get(i));
                if (tempHostsNumber > hostsNum){
                    if ((tempHostsNumber - 3) == hostsNum){ // np. tempHostsNumber = 16, hostsNum = 13
                        cidr = 32-(availableMasks.get(i));
                        IPv4Subnet = createSubnet(IPv4, cidr);
                        subnetsList.add(IPv4Subnet);
                        hostsNum = 0;
                        break;
                    }
                    else if ((tempHostsNumber - 3) < hostsNum){ // np. tempHostsNumber = 16, hostsNum = 15
                        cidr = 32-(availableMasks.get(i));
                        IPv4Subnet = createSubnet(IPv4, cidr);
                        subnetsList.add(IPv4Subnet);
                        IPv4 = addHosts(IPv4Subnet.broadcastAddress,2);
                        hostsNum = hostsNum - (tempHostsNumber - 3);
                        break;
                    }
                    else{
                        tempHostsNumber = (int)Math.pow(2,availableMasks.get(i-1)); // np. tempHostsNumber = 128, hostsNum = 190
                        cidr = 32-(availableMasks.get(i-1));
                        IPv4Subnet = createSubnet(IPv4, cidr);
                        subnetsList.add(IPv4Subnet);
                        IPv4 = addHosts(IPv4Subnet.broadcastAddress,2);
                        hostsNum = (hostsNum - tempHostsNumber) + 3;
                        break;
                    }
                }
                if( i == availableMasks.size()-1){
                        tempHostsNumber = (int)Math.pow(2,availableMasks.get(i)); // np. tempHostsNumber = 128, hostsNum = 190
                        cidr = 32-(availableMasks.get(i));
                        IPv4Subnet = createSubnet(IPv4, cidr);
                        subnetsList.add(IPv4Subnet);
                        IPv4 = addHosts(IPv4Subnet.broadcastAddress,2);
                        hostsNum = (hostsNum - tempHostsNumber) + 3;
                        break;
                }
            }
        }
    return subnetsList;
    }

    public String validateIPv4 (String IP){
          String[] string_parts = IP.split("\\.");
          int part3 = Integer.parseInt(string_parts[3]);
          
          if (part3 > 252){
              IP = addHosts(IP, (255 - part3)+2);
          }
          else{
              if (part3 % 2 != 0) IP = addHosts(IP, 2);
          }
          
          return IP;
    }
    
    public void printSubnets(ArrayList<Subnet> subnets, PrintWriter out){
        
        out.println("<!DOCTYPE html>");
         out.println("<html><head>");
         out.println("<meta http-equiv='Content-Type' content='text/html; charset=UTF-8'>");
         out.println("<link href=\"//maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css\" rel=\"stylesheet\" media=\"screen\">");
         out.println("<script src=\"http://code.jquery.com/jquery-latest.js\"></script>");
         out.println("<script src=\"//maxcdn.bootstrapcdn.com/bootstrap/3.3.6/js/bootstrap.min.js\"></script>");
         out.println("<title>Subnet Calculator</title></head>");
         out.println("<body>");
         out.println("<center><h1>" + getServletInfo() + "</h1>");  // says Hello
         
         out.println("<table class=\"table table-bordered\">");
         out.println("<thead><tr><th>Subnet No.</th><th>IP/CIDR</th><th>Default Gateway</th><th>Broadcast IP</th></tr></thead>");
         out.println("<tbody>");
         
        for ( int iter =0; iter< subnets.size(); iter++ ){
            out.println("<tr>");
            out.println("<td>" + iter + "</td>");
            out.println("<td>" + subnets.get(iter).subnetAddress+"/" + subnets.get(iter).cidr + "</td>");
            out.println("<td>" + subnets.get(iter).defautGateway + "</td>");
            out.println("<td>" + subnets.get(iter).broadcastAddress + "</td>");
            out.println("</tr>");            
        }
         
         out.println("</tbody>");
         out.println("</table>");
         out.println("</body>");
         out.println("</html>");
    }
    


    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        startingIP = validateIPv4(request.getParameter("ip"));
        hostsNum = parseInt(request.getParameter("hosts_number"));

        ArrayList<Subnet> subnets = new ArrayList<>();
        subnets = calculateFull(startingIP, hostsNum);
        printSubnets(subnets, out);
    }
    
    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Subnet Calculator";
    }// </editor-fold>

}
