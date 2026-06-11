
package pruebita3;


public class Main{
    
    private Main(){
       
    }
   
    public static String formatearMoneda(double valor){
        return String.format("Lps. %.2f", valor);
    }
    
    public static boolean validarCodigo(int codigo){
        return codigo > 0;
    }
   
    public static boolean validarMonto(double monto){
        return monto > 0;
    }
}
