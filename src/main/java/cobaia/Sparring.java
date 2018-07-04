package cobaia;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import org.apache.commons.codec.digest.DigestUtils;

import cobaia.model.Area;
import cobaia.model.Usuario;
import cobaia.model.Validator;

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
    
    System.out.println("validator...");
    
    Area area = new Area(null);
    
    System.out.println(Validator.validate(area) == false);
    
    area.setNome("teste");
    
    System.out.println(Validator.validate(area) == true);  
    
    Usuario u = new Usuario();
    
    u.setNome("My");
    u.setEmail("myreli.com");
    u.setSenha("");
    
    System.out.println(Validator.validate(u) == false);
    
    u.setNome("Myreli");
    
    System.out.println(Validator.validate(u) == false);
    
    u.setEmail("myreli@mail.com");

    System.out.println(Validator.validate(u) == false);
    
    u.setSenha(md5);
    
    System.out.println(Validator.validate(u) == true);
    System.out.println("\n\nGAME OVER");

  }

}
