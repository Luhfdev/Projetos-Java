import java.security.SecureRandom;
import java.util.Scanner;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class GeradorDeSenhas {

    private static final String CARACTERES = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%&*()_+=-[],./?><";
    private static final String CAMINHO_ARQUIVO = "senhas.txt";
    private static final String CHAVE_ARQUIVO = "chave.aes";

    // Gera uma senha aleatória
    public static String gerarSenha(int comprimento) {
        SecureRandom geradorDeNumeroAleatorio = new SecureRandom();
        StringBuilder senha = new StringBuilder(comprimento);

        for (int i = 0; i < comprimento; i++) {
            int indice = geradorDeNumeroAleatorio.nextInt(CARACTERES.length());
            senha.append(CARACTERES.charAt(indice));
        }

        return senha.toString();
    }

    // Criptografa uma senha
    public static String criptografar(String texto, SecretKey chave) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, chave);
        byte[] textoCriptografado = cipher.doFinal(texto.getBytes());
        return Base64.getEncoder().encodeToString(textoCriptografado);
    }

    // Descriptografa uma senha
    public static String descriptografar(String textoCriptografado, SecretKey chave) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, chave);
        byte[] textoDescriptografado = cipher.doFinal(Base64.getDecoder().decode(textoCriptografado));
        return new String(textoDescriptografado);
    }

    // Gera ou lê a chave secreta para criptografia
    public static SecretKey obterChaveSecreta() throws Exception {
        File arquivoChave = new File(CHAVE_ARQUIVO);
        if (!arquivoChave.exists()) {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(128); // Tamanho da chave
            SecretKey chave = keyGen.generateKey();
            try (FileOutputStream fos = new FileOutputStream(arquivoChave)) {
                fos.write(chave.getEncoded());
            }
            return chave;
        } else {
            byte[] chaveBytes = new byte[16];
            try (FileInputStream fis = new FileInputStream(arquivoChave)) {
                fis.read(chaveBytes);
            }
            return new SecretKeySpec(chaveBytes, "AES");
        }
    }

    // Salva uma senha criptografada
    public static void salvarSenha(String lugar, String senha) {
        try {
            SecretKey chave = obterChaveSecreta();
            String senhaCriptografada = criptografar(senha, chave);

            File arquivo = new File(CAMINHO_ARQUIVO);
            if (!arquivo.exists()) {
                arquivo.createNewFile();
            }
            try (FileWriter escritor = new FileWriter(arquivo, true)) {
                escritor.write(lugar + ", " + senhaCriptografada + System.lineSeparator());
            }
        } catch (Exception e) {
            System.out.println("Erro ao salvar a senha: " + e.getMessage());
        }
    }

    // Lê as senhas descriptografadas
    public static List<String> lerSenhas() {
        List<String> senhas = new ArrayList<>();
        try {
            SecretKey chave = obterChaveSecreta();
            try (BufferedReader leitor = new BufferedReader(new FileReader(CAMINHO_ARQUIVO))) {
                String linha;
                while ((linha = leitor.readLine()) != null) {
                    String[] partes = linha.split(", ");
                    if (partes.length == 2) {
                        String lugar = partes[0];
                        String senhaCriptografada = partes[1];
                        String senhaDescriptografada = descriptografar(senhaCriptografada, chave);
                        senhas.add(lugar + ": " + senhaDescriptografada);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Erro ao ler as senhas: " + e.getMessage());
        }
        return senhas;
    }
}
