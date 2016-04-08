package version02;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Author                           

 $$$$$$\                                       $$$\           $$\                $$\ 
$$  __$$\                                     $$ $$\          $$ |               $$ |
$$ /  $$ |$$\   $$\  $$$$$$\   $$$$$$\        \$$$\ |         $$ |      $$$$$$\  $$ |
$$$$$$$$ |$$ |  $$ |$$  __$$\ $$  __$$\       $$\$$\$$\       $$ |      \____$$\ $$ |
$$  __$$ |$$ |  $$ |$$ |  \__|$$ /  $$ |      $$ \$$ __|      $$ |      $$$$$$$ |$$ |
$$ |  $$ |$$ |  $$ |$$ |      $$ |  $$ |      $$ |\$$\        $$ |     $$  __$$ |$$ |
$$ |  $$ |\$$$$$$  |$$ |      \$$$$$$  |       $$$$ $$\       $$$$$$$$\\$$$$$$$ |$$ |
\__|  \__| \______/ \__|       \______/        \____\__|      \________|\_______|\__|
                                                                                     
 */

public class MainActivity {
	public static void main(String[] args) throws IOException	{
		File file = new File("inputData.txt");
		if (!file.exists()) 
		{
			System.out.println("Input file not exist");
			System.exit(0);
		}
		@SuppressWarnings("resource")
		BufferedReader br = new BufferedReader(new FileReader(file));
		switch(br.readLine())	{
		case "RR"	:	RoundRobin.rrMain();
		break;
		case "RRP"	:	RoundRobinPriority.rrpMain();
		break;
		}
	}
}
