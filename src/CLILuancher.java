import com.peak.cli.ExecuteCommand;
import com.peak.core.spring.SpringContext;

public class CLILuancher {

	public static void main(String[] args) {		
		SpringContext.run();
		String name = "sdk_db_2_peak_data";
		ExecuteCommand command = ExecuteCommand.get(name);
		if (command == null) {
			System.out.println(String.format("%s command not found", name));
			return;
		}
		command.execute(args);
	}
}