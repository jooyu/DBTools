import com.peak.cli.ExecuteCommand;
import com.peak.core.spring.SpringContext;

/**
 * 根据游戏id复制数据表
 * @author eaves.zhu
 *
 */
public class CLICopyLuancher {

	public static void main(String[] args) {		
		SpringContext.run();
		String name = "table_copy_by_gameid";
		ExecuteCommand command = ExecuteCommand.get(name);
		if (command == null) {
			System.out.println(String.format("%s command not found", name));
			return;
		}
		command.execute(args);
	}
}