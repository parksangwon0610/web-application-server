package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {

    // 1 test .
            // header 가져오기.

            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));

            String line = br.readLine();
            if(line == null){
                return;
            }
            String url = HttpRequestUtils.getUrl(line); //  첫라인

    // 2 get test.

//            if(url.startsWith("/create")){
//                int index = url.indexOf("?");
//                String queryString = url.substring(index+1);
//                Map<String, String> params = HttpRequestUtils.parseQueryString(queryString);
//                User user = new User(params.get("userId"), params.get("password"), params.get("name"), params.get("email"));
//                log.debug("User : {}", user);
//                url = "/index.html";
//            }

    // 3. post test
            Map<String, String> headers = new HashMap<>();
            while(!"".equals(line)){
                log.debug("header : {}", line);
                line = br.readLine();
                String[] headerTokens = line.split(": ");
                if(headerTokens.length == 2) {
                    headers.put(headerTokens[0], headerTokens[1]);
                }
            }

            log.debug("Content-length : {}", headers.get("Content-Length"));

            if(url.equals("/user/create")){
                log.debug("실행");
                String body = IOUtils.readData(br, Integer.parseInt(headers.get("Content-Length")));
                log.debug("Request Body : {}" , body);
                Map<String, String> params = HttpRequestUtils.parseQueryString(body);
                User user = new User(params.get("userId"), params.get("password"),params.get("name"), params.get("email"));
                log.debug("User : {}", user);

                url = "/index.html";
            }

            DataOutputStream dos = new DataOutputStream(out);
            byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
            response200Header(dos, body.length);
            responseBody(dos, body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
