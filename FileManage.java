import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.File;
import java.util.ArrayList;

public class FileManage{
    public static ArrayList<String> load(String filePath){
        BufferedReader input = null;
        ArrayList<String> content = new ArrayList<>();
        try{
            input = new BufferedReader(new FileReader(filePath));
            String line = input.readLine();
            while(line != null){
                content.add(line);
                line = input.readLine();
            }
            input.close();
        }catch(FileNotFoundException e){
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }
        return content;
    }

    public static void save(String filePath, String content){
        PrintWriter output = null;
        try{
            output = new PrintWriter(new File(filePath));
            output.write(content);
            output.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}