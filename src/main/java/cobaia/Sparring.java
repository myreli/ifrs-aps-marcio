package cobaia;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import org.apache.commons.codec.digest.DigestUtils;

import cobaia.model.Area;

public class Sparring {

  public static void main(String[] args) throws NoSuchAlgorithmException, UnsupportedEncodingException {
    System.out.println(UUID.randomUUID().toString().split("-")[0].length());    
    String md5 = DigestUtils.md5Hex("test");
    System.out.println(md5);
    System.out.println("marcio.torres@riogrande.ifrs.edu.br".matches("[\\w._]+@\\w+(\\.\\w+)+"));
    
    System.out.println("testing dao...");
    
    Area test = new Area("teste");

    System.out.println(test.getId() == null);
    
    test.save();
    
    System.out.println(test.getId() != null);
    
    test.setNome("teste alterado");
    
    test.save();
    
    System.out.println(test.getId() != null);

    test.delete();
    
    System.out.println(test.getId() == null);
    
  }

}
