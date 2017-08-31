import com.peak.cli.ExecuteCommand;
import com.peak.core.spring.SpringContext;

public class CLICopyAndMoveLuancher {

	public static void main(String[] args) {		
		SpringContext.run();
		String name = "move_data_to_Submeter";
		ExecuteCommand command = ExecuteCommand.get(name);
		if (command == null) {
			System.out.println(String.format("%s command not found", name));
			return;
		}
		command.execute(args);
	}

}
